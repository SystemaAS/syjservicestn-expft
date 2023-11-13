package no.systema.jservices.tvinn.digitoll.v2.controller.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.systema.jservices.tvinn.digitoll.v2.util.PrettyLoggerOutputer;
import no.systema.jservices.tvinn.expressfortolling.api.ApiServices;
import no.systema.jservices.tvinn.expressfortolling.api.ApiServicesAir;
import no.systema.jservices.tvinn.expressfortolling2.dto.ApiMrnDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.GenericDtoResponse;
import no.systema.jservices.tvinn.expressfortolling2.enums.EnumControllerMrnType;


/**
 * This service handles the MRN - GET subsequent to a htto POST to the API
 * The MRN get is unpredictable regarding the time-out after the POST.
 * Usually it should be present after 5-10 seconds but 20% of the attempts could be not earlier than 30-40 seconds
 * Therefore this class: in order to loop until we get it !
 * 
 * @author oscardelatorre
 * Oct 2023
 * 
 */
@Service
public class PoolExecutorControllerService {
	private static Logger logger = LoggerFactory.getLogger(PoolExecutorControllerService.class.getName());
	
	@Value("${expft.getmrn.timeout.milliseconds}")
    private Integer GET_MRN_DELAY_MILLISECONDS;
	
	@Value("${expft.getmrn.timeout.numberof.iterations}")
	private Integer GET_MRN_DELAY_NUMBER_OF_ITERATIONS;
	
	@Autowired
	private ApiServices apiServices; 
	
	@Autowired
	private ApiServicesAir apiServicesAir; 
	
	/**
	 * The mrn could delay from 5-45 seconds. 
	 * Ideally, the previous sleep that is triggered before this method call, should have been sufficient for getting the mrn. This happens in a 70% of all cases.
	 * Unfortunately this is not a predictable case. Therefore, the method has been enhanced with a while-loop-until-mrn-appears-mechanism.
	 * This happens ONLY in a POST. Which is the nature of the mrn.
	 * 
	 * @param dtoResponse
	 * @param requestIdCurrent
	 * @param requestIdFirst
	 * @param tollTokenMap
	 * @param isApiAir
	 * @param controller
	 * @return
	 */
	public String getMrnPOSTDigitollV2FromApi( GenericDtoResponse dtoResponse, String requestIdCurrent, String requestIdFirst, Map tollTokenMap, boolean isApiAir, String controller, StringBuilder httpErrorCode) {
		String mrn = "";
		String controllerNameForDebug = getControllNameForDebug(controller);
		
				
		//int UPPER_LIMIT_ITERATIONS = 6; 
		
		//just in case in order to go out of the loop if the system is down
		ScheduledExecutorService pool = Executors.newSingleThreadScheduledExecutor();
		

		int counter = 1;
		try {
			
			//As long as we do not get the mrn...
			while (StringUtils.isEmpty(mrn)) {
				
				//SLEEP and loop again
				logger.warn(PrettyLoggerOutputer.FRAME);
				logger.warn("START of delay: "+ new Date() + " counter-loop:" + counter);
				logger.warn("CONTROLLER:" + controllerNameForDebug + " << " + requestIdCurrent + " >>");
				ScheduledFuture<String> schedule = pool.schedule(()-> "own sleep", GET_MRN_DELAY_MILLISECONDS, TimeUnit.MILLISECONDS);
				logger.warn(schedule.get());
				logger.warn("END of delay: "+ new Date());
				logger.warn(PrettyLoggerOutputer.FRAME);
				
				//in case the mrn never appeared...
				if(counter > GET_MRN_DELAY_NUMBER_OF_ITERATIONS) {
					break;
				}
	
				//=======================
				//START get mrn
				//=======================
				try{
					
					String json = "";
					
					if(controller != null) {
						if (controller.equals(EnumControllerMrnType.TRANSPORT.toString())) {
							json = getValidationStatusTransportDigitollV2(requestIdCurrent, tollTokenMap, isApiAir);
							
						}else if (controller.equals(EnumControllerMrnType.MASTER.toString())) {
							json = getValidationStatusMasterConsignmentDigitollV2(requestIdCurrent, tollTokenMap, isApiAir);
							
						}else if (controller.equals(EnumControllerMrnType.HOUSE.toString())) {
							json = getValidationStatusHouseConsignmentDigitollV2(requestIdCurrent, tollTokenMap, isApiAir);
							
						}
					}
					if(StringUtils.isNotEmpty(json)) {
						ApiMrnDto obj = new ObjectMapper().readValue(json, ApiMrnDto.class);
						logger.warn("JSON = " + json);
						logger.warn("status:" + obj.getStatus());
						logger.warn("MRN = " + obj.getMrn());
						dtoResponse.setStatusApi(obj.getStatus());
						dtoResponse.setTimestamp(obj.getNotificationDate());
						
						if(StringUtils.isNotEmpty(obj.getMrn())) {
							mrn = obj.getMrn();
							
						}else {
							//check for error type
							dtoResponse.setErrMsg(json);
							//this error signals that the current lrn is not good. Lets hope that the first one saved can help ...
							if("FAILURE".equals(obj.getStatus())) {
								
								if(this.isAlreadyInUseFailure(json)) {
									//Special case
									logger.warn("FAILURE (already in use) ... changine uuid to uuid_own");
									//then will continue the loop with the first lrn saved ...
									if(StringUtils.isNotEmpty(requestIdFirst)) {
										logger.warn("Changing uuid:" + requestIdCurrent + "to first valid uuid_own:" + requestIdFirst);
										requestIdCurrent = requestIdFirst;
										
									}else {
										break;
									}
									
								}else if(this.isSomeKnownCustomsRelatedFailure(json)) {
									break;	
						
								}
								
							}else {
								//continue...
							}
		
						}
						//=======================
						//END get mrn
						//=======================
					}
					
				}catch(Exception e) {
					logger.error(e.getMessage());
					if(StringUtils.isNotEmpty(e.getMessage())) {
						if(e.getMessage().contains("404 Not Found")){
							//This error is specific for the fact that the MRN has not yet been released at the back-end but it will be at some point
							//Therefore the reason to implement this whole PoolExcutor-mechanism and the signal to keep going with the loop until we get it or try again in another SEND
							if(StringUtils.isEmpty(httpErrorCode.toString()) ){
								httpErrorCode.append("404");
							}
						}
					}
					
					//e.printStackTrace();
					//Get out stackTrace to the response (errMsg)
					StringWriter sw = new StringWriter();
					e.printStackTrace(new PrintWriter(sw));
					dtoResponse.setErrMsg(sw.toString());
					
				}finally {
					//loop again
					counter++;
					
				}
				
			}
			//stop the scheduler
			pool.shutdown();
			
		}catch (Exception e) {
			//stop the scheduler
			pool.shutdown();
			logger.error(e.getMessage());
			e.printStackTrace();
			
		}
		
		//clear the errMsg that appeared until the mrn was available
		if(StringUtils.isNotEmpty(mrn) && StringUtils.isNotEmpty(dtoResponse.getErrMsg())) {
			dtoResponse.setErrMsg("");
		}
		
		return mrn;
	}
	/**
	 * 
	 * @param json
	 * @return
	 */
	private boolean isAlreadyInUseFailure(String json) {
		boolean retval = false;
		//One or more transport documents are already submitted
		//json.contains("is already in use")) {
		if(json!=null) {
			if (json.contains("is already in use")) {
				retval = true;
				
			}
				
		}
		
		return retval;
		
	}
	
	private boolean isSomeKnownCustomsRelatedFailure(String json) {
		boolean retval = false;
		//One or more transport documents are already submitted
		//json.contains("is already in use")) {
		if(json!=null) {
			if (json.contains("Customs declaration is not valid")) {
				retval = true;
				
			}
			//PUT more here ...	
		}
		
		return retval;
		
	}
	

	/**
	 * 
	 * @param lrn
	 * @param tollTokenMap
	 * @param isApiAir
	 * @return
	 * @throws Exception
	 */
	private String getValidationStatusTransportDigitollV2(String lrn, Map tollTokenMap, boolean isApiAir) throws Exception {
		String json = "";
		
		
		if(isApiAir) {
			json = apiServicesAir.getValidationStatusTransportDigitollV2(lrn, tollTokenMap );
		}else {
			json = apiServices.getValidationStatusTransportDigitollV2(lrn, tollTokenMap );
		}
		
		return json;
	}
	
	private String getValidationStatusMasterConsignmentDigitollV2(String lrn, Map tollTokenMap, boolean isApiAir) throws Exception {
		String json = "";
		
		if(isApiAir) {
			json = apiServicesAir.getValidationStatusMasterConsignmentDigitollV2(lrn, tollTokenMap );
		}else {
			json = apiServices.getValidationStatusMasterConsignmentDigitollV2(lrn, tollTokenMap );
		}
		
		return json;
	}
	private String getValidationStatusHouseConsignmentDigitollV2(String lrn, Map tollTokenMap, boolean isApiAir) throws Exception {
		String json = "";
		
		if(isApiAir) {
			json = apiServicesAir.getValidationStatusHouseConsignmentDigitollV2(lrn, tollTokenMap );
		}else {
			json = apiServices.getValidationStatusHouseConsignmentDigitollV2(lrn, tollTokenMap );
		}
		
		return json;
	}
	
	private String getControllNameForDebug(String controllerValue) {
		String result = "";
		if(controllerValue.equals(EnumControllerMrnType.TRANSPORT.toString())){
			result = EnumControllerMrnType.TRANSPORT.name();
		}else if(controllerValue.equals(EnumControllerMrnType.MASTER.toString())){
			result = EnumControllerMrnType.MASTER.name();
		}else if(controllerValue.equals(EnumControllerMrnType.HOUSE.toString())){
			result = EnumControllerMrnType.HOUSE.name();
		}
		return result;
	}
	
	public String getMrnPOSTDigitollV2FromApiTest( GenericDtoResponse dtoResponse, String lrn, Map tollTokenMap, boolean isApiAir, String controller) {
		String mrn = "";
		
		int UPPER_LIMIT_ITERATIONS = 6; 
		
		//just in case in order to go out of the loop if the system is down
		ScheduledExecutorService pool = Executors.newSingleThreadScheduledExecutor();
		

		int counter = 1;
		//As long as we do not get the mrn...
		while (StringUtils.isEmpty(mrn)) {
			
			//in case the mrn never appeared...
			if(counter > UPPER_LIMIT_ITERATIONS) {
				break;
			}
		
			//=======================
			//START get mrn
			//=======================
			try{
				logger.warn(PrettyLoggerOutputer.FRAME);
				logger.warn("START of delay: "+ new Date());
				ScheduledFuture<String> schedule = pool.schedule(()-> "own sleep", 5, TimeUnit.SECONDS);
				logger.warn(schedule.get());
				logger.warn("END of delay: "+ new Date());
				logger.warn(PrettyLoggerOutputer.FRAME);
				
				if(counter==3) {
					mrn="xxx";
					
				}
				/*
				if(StringUtils.isNotEmpty(obj.getMrn())) {
					mrn = obj.getMrn();
				}else {
					dtoResponse.setErrMsg(json);
				}
				//=======================
				//END get mrn
				//=======================
				
				logger.info("################################ BEFORE SLEEP");
				//SLEEP if is empty
				if(StringUtils.isEmpty(mrn)) {
					logger.info("MRN empty ...");
					//try again after sleep until we get the mrn 
					//TimeUnit.MILLISECONDS.sleep(WAIT_SLEEP_MILLISECONDS); //Thread.sleep not working
					
					//not working with Async from the Controller UI but the code works if it is not Async
					ScheduledFuture<String> schedule = pool.schedule(()-> "own sleep", WAIT_SLEEP_MILLISECONDS, TimeUnit.MILLISECONDS);
					logger.warn(schedule.get());
					System.out.println(schedule.get());
					
				}
				*/
			}catch(Exception e) {
				e.toString();
				
			}finally {
				
				counter++;
			}
			
		}
		//stop the scheduler
		pool.shutdown();
		
		
		return mrn;
	}
	
	
}
