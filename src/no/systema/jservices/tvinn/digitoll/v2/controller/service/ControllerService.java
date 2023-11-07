package no.systema.jservices.tvinn.digitoll.v2.controller.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.systema.jservices.tvinn.expressfortolling.api.ApiServices;
import no.systema.jservices.tvinn.expressfortolling.api.ApiServicesAir;
import no.systema.jservices.tvinn.expressfortolling2.dto.ApiMrnDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.GenericDtoResponse;
import no.systema.jservices.tvinn.expressfortolling2.enums.EnumControllerMrnType;

@Service
public class ControllerService {
	private static Logger logger = LoggerFactory.getLogger(ControllerService.class.getName());
	
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
	 * @param lrn
	 * @param tollTokenMap
	 * @param isApiAir
	 * @param controller
	 * @return
	 */
	public String getMrnPOSTDigitollV2FromApi( GenericDtoResponse dtoResponse, String lrn, Map tollTokenMap, boolean isApiAir, String controller) {
		String mrn = "";
		//at the most: 1-minute (6*10000 milli-seconds)
		int UPPER_LIMIT_ITERATIONS = 6; 
		int WAIT_SLEEP_SECONDS = 8;
		//just in case in order to go out of the loop if the system is down
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
				String json = "";
				if(controller != null) {
					if (controller.equals(EnumControllerMrnType.TRANSPORT.toString())) {
						json = this.getValidationStatusTransportDigitollV2(lrn, tollTokenMap, isApiAir);
						
					}else if (controller.equals(EnumControllerMrnType.MASTER.toString())) {
						json = this.getValidationStatusMasterConsignmentDigitollV2(lrn, tollTokenMap, isApiAir);
						
					}else if (controller.equals(EnumControllerMrnType.HOUSE.toString())) {
						json = this.getValidationStatusHouseConsignmentDigitollV2(lrn, tollTokenMap, isApiAir);
						
					}
				}
				
				
				ApiMrnDto obj = new ObjectMapper().readValue(json, ApiMrnDto.class);
				logger.warn("JSON = " + json);
				logger.warn("status:" + obj.getStatus());
				logger.warn("MRN = " + obj.getMrn());
				dtoResponse.setStatusApi(obj.getStatus());
				dtoResponse.setTimestamp(obj.getNotificationDate());
				
				if(StringUtils.isNotEmpty(obj.getMrn())) {
					mrn = obj.getMrn();
				}else {
					dtoResponse.setErrMsg(json);
				}
				//=======================
				//END get mrn
				//=======================
				
				logger.info("################################ BEFORE SLEEP");
				//SLEEP
				if(StringUtils.isEmpty(mrn)) {
					logger.info("MRN empty ...");
					//try again after sleep until we get the mrn 
					TimeUnit.SECONDS.sleep(WAIT_SLEEP_SECONDS); //Thread.sleep not working
					
					/* OBS ! Try this if the above shows flaws in PROD
					ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
					executorService.schedule(Classname::someTask, delayInSeconds, TimeUnit.SECONDS);
					*/
				}
				
			}catch(Exception e) {
				//to avoid output several times while mrn = empty
				if(StringUtils.isNotEmpty(mrn)) {
					logger.error(e.getMessage());
					//e.printStackTrace();
					//Get out stackTrace to the response (errMsg)
					StringWriter sw = new StringWriter();
					e.printStackTrace(new PrintWriter(sw));
					dtoResponse.setErrMsg(sw.toString());
					
				}else {
					logger.error(e.getMessage());
				}
				
			}finally {
				
				counter++;
			}
			
		}
		//clear the errMsg that appeared until the mrn was available
		if(StringUtils.isNotEmpty(mrn) && StringUtils.isNotEmpty(dtoResponse.getErrMsg())) {
			dtoResponse.setErrMsg("");
		}
		
		return mrn;
	}
	
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
	
}
