package no.systema.jservices.tvinn.digitoll.v2.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import no.systema.jservices.common.dao.services.BridfDaoService;
import no.systema.jservices.tvinn.expressfortolling.api.ApiServices;
import no.systema.jservices.tvinn.expressfortolling.api.ApiServicesAir;
import no.systema.jservices.tvinn.expressfortolling.api.ApiServicesPresentationICS2;
import no.systema.jservices.tvinn.expressfortolling.api.ApiServicesRail;
import no.systema.jservices.tvinn.digitoll.entry.road.EntryMovRoadDto;
import no.systema.jservices.tvinn.digitoll.v2.controller.service.PoolExecutorControllerService;
import no.systema.jservices.tvinn.digitoll.v2.dao.Ics2Ens;
import no.systema.jservices.tvinn.digitoll.v2.dao.Transport;
import no.systema.jservices.tvinn.digitoll.v2.dto.ApiRequestIdDto;
import no.systema.jservices.tvinn.digitoll.v2.dto.SadmotfDto;
import no.systema.jservices.tvinn.digitoll.v2.dto.routing.EntryRoutingDto;
import no.systema.jservices.tvinn.digitoll.v2.enums.EnumSadmotfStatus2;
import no.systema.jservices.tvinn.expressfortolling2.dto.ApiMrnDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.ApiMrnStatusRecordDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.ApiMrnStatusWithDescendantsRecordDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.GenericDtoContainer;
import no.systema.jservices.tvinn.expressfortolling2.dto.GenericDtoResponse;
import no.systema.jservices.tvinn.expressfortolling2.dto.GenericDtoResponseLight;
import no.systema.jservices.tvinn.expressfortolling2.dto.GenericWithDescendantsDtoContainer;
import no.systema.jservices.tvinn.expressfortolling2.enums.EnumControllerMrnType;
import no.systema.jservices.tvinn.digitoll.v2.services.AsynchTransportService;
import no.systema.jservices.tvinn.digitoll.v2.services.MapperTransport;
import no.systema.jservices.tvinn.digitoll.v2.services.SadmomfService;
import no.systema.jservices.tvinn.digitoll.v2.services.SadmotfService;
import no.systema.jservices.tvinn.digitoll.v2.util.ApiRecognizer;
import no.systema.jservices.tvinn.digitoll.v2.util.PrettyLoggerOutputer;
import no.systema.jservices.tvinn.digitoll.v2.util.SadDigitollConstants;
import no.systema.jservices.tvinn.digitoll.v2.util.SadmologLogger;
import no.systema.jservices.tvinn.expressfortolling2.util.GenericJsonStringPrinter;
import no.systema.jservices.tvinn.expressfortolling2.util.ServerRoot;
/**
 * Main entrance for accessing Digitoll Version 2 API.
 * 
 * ===================================================================
 * Query API for the Presentation-UCase on ICS2
 * ===================================================================
 * 
 * @author oscardelatorre
 * @date Maj 2024
 * 
 *
 */



@RestController
public class DigitollV2PresentationICS2Controller {
	private static Logger logger = LoggerFactory.getLogger(DigitollV2PresentationICS2Controller.class.getName());
	// pretty print
	private static ObjectMapper prettyErrorObjectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	private JsonParser prettyJsonParser = new JsonParser();
	private Gson prettyGsonObject = new GsonBuilder().setPrettyPrinting().create();
	
	
	@Value("${expft.getmrn.timeout.milliseconds}")
    private Integer GET_MRN_DELAY_MILLISECONDS;
	
	
	@Autowired
	private BridfDaoService bridfDaoService;	
	
	@Autowired
	private SadmotfService sadmotfService;
	@Autowired
	private SadmomfService sadmomfService;
	
	@Autowired
	private ApiServices apiServices; 
	
	@Autowired
	private ApiServicesAir apiServicesAir; 
	@Autowired
	private ApiServicesRail apiServicesRail;
	
	@Autowired
	private ApiServicesPresentationICS2 apiServicesPresentationICS;
	
	
	@Autowired
	private PoolExecutorControllerService poolExecutorControllerService;
	
	@Autowired
	private SadmologLogger sadmologLogger;	
	
	@Autowired
	private AsynchTransportService asynchTransportService;	
	
	
	/**
	 * Gets the response on request (empty = OK, error = NOK)
	 * 
	 * 
	 * @param session
	 * @param user
	 * @param etlnrt
	 * @throws Exception
	 * 
	 * http://localhost:8080/syjservicestn-expft/digitollv2/postEntrySummaryDeclaration.do?user=NN
	 * 
	 * test - OK
	 * 
	 */
	@RequestMapping(value="/digitollv2/postEntrySummaryDeclaration.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponseLight postEntrySummaryDeclarationDigitollV2(HttpServletRequest request , @RequestParam(value = "user", required = true) String user, 
																				@RequestParam(value = "ensMrn", required = true) String ensMrn,
																				@RequestParam(value = "ensRequestId", required = false) String ensRequestId) throws Exception {
		
		logger.warn(PrettyLoggerOutputer.FRAME + PrettyLoggerOutputer.FRAME);
		logger.warn("START of CALL<controller>: "+ new Date());
		logger.warn(PrettyLoggerOutputer.FRAME + PrettyLoggerOutputer.FRAME);
		
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericDtoResponseLight dtoResponse = new GenericDtoResponseLight();
		dtoResponse.setUser(user);
		//dtoResponse.setEtlnrt(etlnrt);
		//dtoResponse.setEllnrt(Integer.valueOf(etlnrt));//for log purposes only
		//dtoResponse.setTdn("0"); //dummy (needed for db-log on table SADEXLOG)
		dtoResponse.setMrn(ensMrn);
		dtoResponse.setRequestMethodApi("POST");
		
		StringBuilder errMsg = new StringBuilder("ERROR ");
		String methodName = new Object() {}
	      .getClass()
	      .getEnclosingMethod()
	      .getName();
		
		logger.warn("Inside " + methodName + "- ENS-MRNnr: " + ensMrn );
		
		try {
			if(checkUser(user)) {
				logger.warn("user OK:" + user);
				
				
				//API - PROD
				Map tollTokenMap = new HashMap(); //will be populated within the put-method
				//API
				Ics2Ens ensDto = new Ics2Ens();
				ensDto.setMrn(ensMrn);
				ensDto.setRequestId(UUID.randomUUID().toString());
				//TEST error since this requestId already exists: ensDto.setRequestId("8935befa-8cea-43fa-b876-c8408f48b4aa");
				//save requestId in return ---- TODO: sve it in Db
				
				//this in case we want to use the last requestId to test the API with error
				if(StringUtils.isNotEmpty(ensRequestId)) {
					ensDto.setRequestId(ensRequestId);
				}
				dtoResponse.setRequestId(ensDto.getRequestId());
				
				ensDto.setCustomsOfficeOfPresentation("NO351001");
				
				String json = apiServicesPresentationICS.postEntrySummaryDeclarationDigitollV2(ensDto, tollTokenMap);
				logger.info("ICS2-ENS-json:" + json);
				
				/*
				List<SadmotfDto> list = sadmotfService.getSadmotfForUpdate(serverRoot, user, etlnrt, mrn);
				if(list != null && list.size()>0) {
					logger.warn("list size:" + list.size());
					
					for (SadmotfDto dto: list) {
						logger.warn(dto.toString());
						//get list of master for the mapping of some attributes
						dto.setMasterList(this.sadmomfService.getSadmomf(serverRoot, user, etlnrt)) ;
						//DEBUG
						//logger.info(dto.toString());
						
						//if(StringUtils.isNotEmpty(dto.getEtmid()) && StringUtils.isNotEmpty(dto.getEtuuid() )) {
						if( StringUtils.isNotEmpty(dto.getEtmid()) ) {
							Transport transport =  new MapperTransport().mapTransport(dto);
							logger.warn("Carrier name:" + transport.getCarrier().getName());
							//Debug
							logger.info(GenericJsonStringPrinter.debug(transport));
							//init response in case en ERROR occurs after apiSerivices...
							dtoResponse.setRequestId(dto.getEtuuid());
							
							//API - PROD
							Map tollTokenMap = new HashMap(); //will be populated within the put-method
							//API
							
							String json = apiServicesPresentationICS.postEntrySummaryDeclarationDigitollV2(transport, ensMrn, tollTokenMap);
							
							//At this point we now have a valid tollToken to use
							
							ApiRequestIdDto obj = new ObjectMapper().readValue(json, ApiRequestIdDto.class);
							logger.warn("JSON = " + json);
							logger.warn("requestId = " + obj.getRequestId());
							
							//put in response
							dtoResponse.setRequestId(obj.getRequestId()); //update
							dtoResponse.setEtlnrt(String.valueOf(dto.getEtlnrt()));
							dtoResponse.setAvd(String.valueOf(dto.getEtavd()));
							dtoResponse.setPro(String.valueOf(dto.getEtpro()));
							
							//In case there was an error at end-point and the LRN was not returned
							if(StringUtils.isEmpty(obj.getRequestId())){
								errMsg.append("requestId empty ?? <json raw>: " + json);
								dtoResponse.setErrMsg(errMsg.toString());
								break;
								
							}else {
								//(1) we have the lrn at this point. We must go an API-round trip again to get the MRN
								String requestId = obj.getRequestId();
								dtoResponse.setRequestId(requestId);
								
								//(2)now we have the new lrn for the updated mrn so we proceed with the SADMOTF-update-lrn at Transport
								if(StringUtils.isNotEmpty(requestId) && StringUtils.isNotEmpty(mrn)) {
									String mode = "UL";
									dtoResponse.setMrn(mrn);
									//we must update the send date as well. Only 8-numbers
									String sendDate = transport.getDocumentIssueDate().replaceAll("-", "").substring(0,8);
									dtoResponse.setDocumentIssueDate(sendDate);//as aux for logging purposes
									
									List<SadmotfDto> xx = sadmotfService.updateLrnMrnSadmotf(serverRoot, user, dtoResponse, sendDate, mode);
									if(xx!=null && xx.size()>0) {
										for (SadmotfDto rec: xx) {
											//logger.warn(rec.toString());
											if(StringUtils.isNotEmpty(rec.getEtmid()) ){
												//OK
												apiStatusAlreadyUpdated = true;
											}else {
												errMsg.append("MRN empty after SADMOTF-update:" + mrn);
												dtoResponse.setErrMsg(errMsg.toString());
											}
										}
									}
									//(3) now we make a final check for LRN-status since there might have being some validation errors with the newly acquired LRN that did not appear when we 
									//first received the LRN in the first PUT Master
									
									//Delay 6-10 seconds (as in POST) needed to avoid ERROR 404 on client ...
									logger.warn(PrettyLoggerOutputer.FRAME);
									logger.warn("START of delay: "+ new Date());
									Thread.sleep(GET_MRN_DELAY_MILLISECONDS); 
									logger.warn("END of delay: "+ new Date());
									logger.warn(PrettyLoggerOutputer.FRAME);
									
									this.checkLrnValidationStatusTransportDigitollV2FromApi(dtoResponse, requestId, tollTokenMap, isApiAir, isApiRail);
									if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())){
										logger.warn("ERROR: " + dtoResponse.getErrMsg()  + methodName);
										//Update ehst2(SADMOTF) with ERROR = M
										dtoResponse.setDb_st2(EnumSadmotfStatus2.M.toString());
										List<SadmotfDto> tmp = sadmotfService.updateLrnMrnSadmotf(serverRoot, user, dtoResponse, sendDate, mode);
									}else {
										//OK
										logger.warn("RequestId status is OK ... (no errors)");
										dtoResponse.setDb_st2(EnumSadmotfStatus2.S.toString());
										List<SadmotfDto> tmp = sadmotfService.updateLrnMrnSadmotf(serverRoot, user, dtoResponse, sendDate, mode);
									}
									
								}else {
									errMsg.append("RequestId empty after PUT ??: " + "-->RequestId:" + requestId + " -->MRN from db(SADMOTF): " + mrn);
									dtoResponse.setErrMsg(errMsg.toString());
									break;
								}
							 
							  
							}
							break; //only first in list
							
							
						}else {
							errMsg.append(" LRN/MRN are empty. This operation is invalid. Make sure this fields have values before any PUT ");
							dtoResponse.setErrMsg(errMsg.toString());
						}
						
					}
				}else {
					errMsg.append(" no records to fetch from SADMOTF ");
					dtoResponse.setErrMsg(errMsg.toString());
				}*/
				
			}else {
				errMsg.append(" invalid user " + user + " " + methodName);
				dtoResponse.setErrMsg(errMsg.toString());
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			//Get out stackTrace to the response (errMsg)
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			dtoResponse.setErrMsg(sw.toString());
		
		}finally {
			/*
			if(!apiStatusAlreadyUpdated) {
				//check on status
				if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())) {
					logger.info(SadDigitollConstants.LOG_PREFIX_LEGEND + dtoResponse.getErrMsg());
					dtoResponse.setDb_st2(EnumSadmotfStatus2.M.toString());
					logger.info("INSIDE setStatus:" + dtoResponse.getDb_st2());
					//
					List<SadmotfDto> xx = sadmotfService.updateLrnMrnSadmotf(serverRoot, user, dtoResponse, dtoResponse.getDocumentIssueDate(), "UL");
					logger.info("After update on status 2 (finally-clause)");
				}
			}*/
			//log in log file
			sadmologLogger.doLog(serverRoot, user, dtoResponse);
		}
		
		logger.warn(PrettyLoggerOutputer.FRAME + PrettyLoggerOutputer.FRAME);
		logger.warn("END of CALL<controller>: "+ new Date());
		logger.warn(PrettyLoggerOutputer.FRAME + PrettyLoggerOutputer.FRAME);
		
		
		//std output (browser)
		return dtoResponse;
	}
	
	
	
	
	
	
	/**
	 * 
	 * @param dtoResponse
	 * @param lrn
	 * @param apiType
	 * @return
	 */
	private String getMrnTransportDigitollV2FromApi( GenericDtoResponse dtoResponse, String lrn, String apiType) {
		
		String retval = "";
		
		try{
			String json = "";
			if(StringUtils.isNotEmpty(apiType) && apiType.equalsIgnoreCase("air")) {
				json = apiServicesAir.getValidationStatusTransportDigitollV2(lrn);
				
			}else if(StringUtils.isNotEmpty(apiType) && apiType.equalsIgnoreCase("rail")) {
				json = apiServicesRail.getValidationStatusTransportDigitollV2(lrn);
				
			}else {
				json = apiServices.getValidationStatusTransportDigitollV2(lrn);
			}
			
			
			ApiMrnDto obj = new ObjectMapper().readValue(json, ApiMrnDto.class);
			logger.warn("JSON = " + json);
			logger.warn("status:" + obj.getStatus());
			logger.warn("MRN = " + obj.getMrn());
			dtoResponse.setStatusApi(obj.getStatus());
			dtoResponse.setTimestamp(obj.getNotificationDate());
			
			if(StringUtils.isNotEmpty(obj.getMrn())) {
				retval = obj.getMrn();
			}else {
				dtoResponse.setErrMsg(json);
			}
		}catch(Exception e) {
			//e.printStackTrace();
			//Get out stackTrace to the response (errMsg)
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			dtoResponse.setErrMsg(sw.toString());
			
		}
		
		return retval;
	}
	
	
	/**
	 * 
	 * @param dtoResponse
	 * @param lrn
	 * @param tollTokenMap
	 * @param isApiAir
	 * 
	 * @return
	 */
	private String checkLrnValidationStatusTransportDigitollV2FromApi(GenericDtoResponse dtoResponse, String lrn, Map tollTokenMap, boolean isApiAir, boolean isApiRail) {
		
		String retval = "";
		
		try{
			String json = "";	
			if(isApiAir) {
				json = apiServicesAir.getValidationStatusTransportDigitollV2(lrn, tollTokenMap );
			}else if(isApiRail) {
				json = apiServicesRail.getValidationStatusTransportDigitollV2(lrn, tollTokenMap );
			}else {
				json = apiServices.getValidationStatusTransportDigitollV2(lrn, tollTokenMap );
			}
				
			ApiMrnDto obj = new ObjectMapper().readValue(json, ApiMrnDto.class);
			logger.warn("JSON = " + json);
			logger.warn("Status = " + obj.getStatus());
			logger.warn("requestID = " + obj.getRequestId());
			logger.warn("notificationDate = " + obj.getNotificationDate());
			if(obj.getValidationErrorList()!=null) {
				logger.warn("validationErrorList = " + obj.getValidationErrorList().toString());
				logger.warn("validationErrorList.length = " + obj.getValidationErrorList().length);
			}
			//
			dtoResponse.setStatusApi(obj.getStatus());
			dtoResponse.setTimestamp(obj.getNotificationDate());
			
			//check if any error to deserialize
			if(obj.getValidationErrorList()!=null && obj.getValidationErrorList().length > 0) {
				//logger.warn("AA");
				StringBuilder sbError = new StringBuilder();
				for( int i = 0; i < obj.getValidationErrorList().length; i++) {
					//logger.warn("BB");
					Map map = (Map)obj.getValidationErrorList()[i];
					logger.warn("Description:" + (String)map.get("description"));
					//error txt
					String errorTxt = obj.getValidationErrorList()[i].toString();
					logger.warn("###:" + errorTxt);
					sbError.append(errorTxt);
					dtoResponse.setErrMsg(sbError.toString());
				}
			}
			
		}catch(Exception e) {
			//e.printStackTrace();
			//Get out stackTrace to the response (errMsg)
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			dtoResponse.setErrMsg(sw.toString());
			
		}
		
		return retval;
	}
	
	/**
	 * 
	 * @param user
	 * @return
	 */
	private boolean checkUser(String user) {
		boolean retval = true;
		if (!bridfDaoService.userNameExist(user)) {
			retval = false;
			//throw new RuntimeException("ERROR: parameter, user, is not valid!");
		}
		return retval;
	}
}
