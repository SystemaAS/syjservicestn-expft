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

import com.eori.validation.soap.ws.client.generated.EORIValidation;
import com.eori.validation.soap.ws.client.generated.EoriResponse;
import com.eori.validation.soap.ws.client.generated.EoriValidationResult;
import com.eori.validation.soap.ws.client.generated.Validation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import no.systema.jservices.common.dao.services.BridfDaoService;
import no.systema.jservices.tvinn.expressfortolling.api.ApiServices;
import no.systema.jservices.tvinn.expressfortolling.api.ApiServicesAir;
import no.systema.jservices.tvinn.expressfortolling.api.ApiServicesRail;
import no.systema.jservices.tvinn.digitoll.entry.road.EntryMovRoadDto;
import no.systema.jservices.tvinn.digitoll.v2.controller.service.PoolExecutorControllerService;
import no.systema.jservices.tvinn.digitoll.v2.dao.Transport;
import no.systema.jservices.tvinn.digitoll.v2.dto.ApiRequestIdDto;
import no.systema.jservices.tvinn.digitoll.v2.dto.EoriValidationDto;
import no.systema.jservices.tvinn.digitoll.v2.dto.SadmotfDto;
import no.systema.jservices.tvinn.digitoll.v2.dto.routing.EntryRoutingDto;
import no.systema.jservices.tvinn.digitoll.v2.enums.EnumSadmotfStatus2;
import no.systema.jservices.tvinn.expressfortolling2.dto.ApiMrnDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.ApiMrnStatusRecordDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.ApiMrnStatusWithDescendantsRecordDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.GenericDtoContainer;
import no.systema.jservices.tvinn.expressfortolling2.dto.GenericDtoResponse;
import no.systema.jservices.tvinn.expressfortolling2.dto.GenericDtoResponseEORIValidation;
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
 * Flow description with respect to the API and the SADMOTF db table
 * ===================================================================
 * 	===============================
 * 	(A) Get MRN Digitoll - POST (API)
 *	=============================== 
 *	(1) Först POST (krav: etuuid & etmid EMPTY)
 *	Systema AS
 *	toll-token expires_in:120 seconds
 *	/movement/road/v2/transport
 *	JSON = {"requestId":"1bdf0f33-f42e-48e2-b334-3944722e3fe5"}
 *	requestId (old LRN) = 1bdf0f33-f42e-48e2-b334-3944722e3fe5
	
 *	(2) GET /transport/validation-status/{requestId}
 *	Mrn and status on response. Update SADMOTF with requestId=etuuid and mrn=etmid  
	
 *	===========================
 * 	(B) Update Mrn - PUT (API)
 *	===========================
 *	(1) PUT /transport/{masterReferenceNumber}
 *	Response returns new requestId which must be updated in SADMOTF. Only requestId update 
 *	IMPORTANT! -->Last requestId received is the one valid for GET validation-status 
 *	
 * 	
 *	=============================
 *	(C) Delete Mrn - DELETE (API)
 *	=============================
 *	(1) DELETE /transport/{masterReferenceNumber}
 *	Response returns new requestId which we dont save.
 *	(2) Etuuid and Etmid (SADMOTF) are blanked. The record is not deleted in db but is ready for new POST
 *	This item (2) could change by deleting the record totally ...
 *
 *  NOTE! 
 *	(D) To see the status of a given MRN att any given moment use the end-point --> getTransport.do in this Controller
 * 
 * @author oscardelatorre
 * @date Aug 2023
 * 
 *
 */



@RestController
public class DigitollV2TransportController {
	private static Logger logger = LoggerFactory.getLogger(DigitollV2TransportController.class.getName());
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
	private PoolExecutorControllerService poolExecutorControllerService;
	
	@Autowired
	private SadmologLogger sadmologLogger;	
	
	@Autowired
	private AsynchTransportService asynchTransportService;	
	
	
	/**
	 * Creates a new Transport through the API - POST
	 * The operation is only valid when the lrn(etuuid) and mrn(etmid) are empty at SADMOTF
	 * (1)If these fields are already in place your should use the PUT method OR 
	 * (2)erase the etuuid and etmid on db before using POST again
	 * 
	 * @param session
	 * @param user
	 * @param etlnrt
	 * @throws Exception
	 * 
	 * http://localhost:8080/syjservicestn-expft/digitollv2/postTransport.do?user=NN&etlnrt=1...
	 * 
	 * test - OK
	 * 
	 */
	@RequestMapping(value="/digitollv2/postTransport.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse postTransportDigitollV2(HttpServletRequest request , @RequestParam(value = "user", required = true) String user, 
																				@RequestParam(value = "etlnrt", required = true) String etlnrt) throws Exception {
		
		logger.warn(PrettyLoggerOutputer.FRAME + PrettyLoggerOutputer.FRAME);
		logger.warn("START of CALL<controller>: "+ new Date());
		logger.warn(PrettyLoggerOutputer.FRAME + PrettyLoggerOutputer.FRAME);
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericDtoResponse dtoResponse = new GenericDtoResponse();
		dtoResponse.setUser(user);
		dtoResponse.setEtlnrt(etlnrt);
		dtoResponse.setEllnrt(Integer.valueOf(etlnrt));//for log purposes only
		dtoResponse.setTdn("0"); //dummy (needed for db-log on table SADMOLOG)
		dtoResponse.setRequestMethodApi("POST");
		boolean apiStatusAlreadyUpdated = false;
		boolean isApiAir = false;
		boolean isApiRail = false;
		
		ScheduledExecutorService pool = Executors.newSingleThreadScheduledExecutor();
		
		StringBuilder errMsg = new StringBuilder("ERROR ");
		
		String methodName = new Object() {}
	      .getClass()
	      .getEnclosingMethod()
	      .getName();
		
		logger.warn("Inside " + methodName );
		//create new - transport at toll.no
		try {
			if(checkUser(user)) {
				logger.warn("user OK:" + user);
				List<SadmotfDto> list = sadmotfService.getSadmotf(serverRoot, user, etlnrt);
				if(list != null) {
					logger.warn("list size:" + list.size());
					
					for (SadmotfDto dto: list) {
						//get list of master for the mapping of some attributes
						dto.setMasterList(this.sadmomfService.getSadmomf(serverRoot, user, etlnrt)) ;
						//DEBUG
						//logger.info(dto.toString());
						
						//Check if we are using MO-Air and not road...
						if(ApiRecognizer.isAir(dto.getEtktyp()))  { 
							isApiAir = true; 
						}else if(ApiRecognizer.isRail(dto.getEtktyp()))  { 
							isApiRail = true; 
						}
						
						if(StringUtils.isEmpty(dto.getEtmid()) ) {
							Transport transport =  new MapperTransport().mapTransport(dto);
							logger.warn("Carrier name:" + transport.getCarrier().getName());
							//Debug
							logger.info(GenericJsonStringPrinter.debug(transport));
							
							//API
							Map tollTokenMap = new HashMap(); //will be populated within the put-method
							String json = "";
							if(isApiAir) {
								json = apiServicesAir.postTransportDigitollV2(transport, tollTokenMap);
							}else if(isApiRail) {
								json = apiServicesRail.postTransportDigitollV2(transport, tollTokenMap);
							}else {
								json = apiServices.postTransportDigitollV2(transport, tollTokenMap);
							}
							//At this point we now have a valid tollToken to use
							
							ApiRequestIdDto obj = new ObjectMapper().readValue(json, ApiRequestIdDto.class);
							logger.warn("JSON = " + json);
							logger.warn("requestId = " + obj.getRequestId());
							dtoResponse.setAvd(String.valueOf(dto.getEtavd()));
							dtoResponse.setPro(String.valueOf(dto.getEtpro()));
							dtoResponse.setEtlnrt(String.valueOf(dto.getEtlnrt()));
							
							
							//In case there was an error at end-point and the requestId was not returned
							if(StringUtils.isEmpty(obj.getRequestId())){
								errMsg.append("requestId empty ?? <json raw>: " + json);
								dtoResponse.setErrMsg(errMsg.toString());
								
							}else {
								//(1) we have the requestId at this point. We must go an API-round trip again to get the MRN
								// requestIdForMrn = obj.getRequestId();
								dtoResponse.setRequestId(obj.getRequestId());
								
								//=====================
								//(2) get mrn from API
								//PROD-->
								//=====================
								StringBuilder errorCode = new StringBuilder();
								//GET MRN right here...
								String mrn = poolExecutorControllerService.getMrnPOSTDigitollV2FromApi(dtoResponse, dtoResponse.getRequestId(), dto.getEtuuid_own(), tollTokenMap, isApiAir, isApiRail,
																										EnumControllerMrnType.TRANSPORT.toString(), errorCode); 
								logger.info("####### MRN (TRANSPORT):" + mrn + "#######");
								
								//(2.1) save a uuid_own BUP(back-up) of the first requestId, if applicable
								//set RequestId-BUP (only once and only here) since the mrn could be lost as first-timer (Kafka-queue not returning MRN in time sometimes)
								if(StringUtils.isNotEmpty(dtoResponse.getRequestId()) ) {
									if(StringUtils.isEmpty(dto.getEtuuid_own()) ){
										if(StringUtils.isNotEmpty(mrn)) {
											//this will happen only once (populate the fall-back uuid_own)
											GenericDtoResponse dtoResponseBup = dtoResponse;
											sadmotfService.setRequestIdBupSadmotf(serverRoot, user, dtoResponseBup);
										}else {
											if(StringUtils.isNotEmpty(errorCode.toString())&& "404".equals(errorCode.toString())) {
												//this will make sure that the current requestId is certainly a valid request that only had a http-communication error while waiting for the MRN... and will wait for the next SEND
												GenericDtoResponse dtoResponseBup = dtoResponse;
												sadmotfService.setRequestIdBupSadmotf(serverRoot, user, dtoResponseBup);
											}
										}
									}
								}
								
								
								
								
								
								//(3) at this point we take actions depending on the mrn be or not to be
								if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())){
									errMsg.append(dtoResponse.getErrMsg());
									dtoResponse.setErrMsg("");
									dtoResponse.setDb_st2(EnumSadmotfStatus2.M.toString());
									
								}else {
									dtoResponse.setDb_st2(EnumSadmotfStatus2.S.toString());
								}
								
								
								//(3)now we have lrn and mrn and proceed with the SADMOTF-update at transport
								if(StringUtils.isNotEmpty(dtoResponse.getRequestId()) && StringUtils.isNotEmpty(mrn)) {
									String mode = "ULM";
									dtoResponse.setMrn(mrn);
									//we must update the send date as well. Only 8-numbers
									String sendDate = transport.getDocumentIssueDate().replaceAll("-", "").substring(0,8);
									dtoResponse.setDocumentIssueDate(sendDate);//as aux for logging purposes 
									
									List<SadmotfDto> xx = sadmotfService.updateLrnMrnSadmotf(serverRoot, user, dtoResponse, sendDate, mode);
									
									if(xx!=null && xx.size()>0) {
										for (SadmotfDto rec: xx) {
											if(StringUtils.isNotEmpty(rec.getEtmid()) ){
												//OK
												apiStatusAlreadyUpdated = true;
												//set MRN-BUP (only once and only here)
												GenericDtoResponse dtoResponseBup = dtoResponse;
												sadmotfService.setMrnBupSadmotf(serverRoot, user, dtoResponseBup);
											}else {
												errMsg.append("MRN empty after SADMOTF-update:" + mrn);
												dtoResponse.setErrMsg(errMsg.toString());
											}
										}
									}
									
								}else {
									errMsg.append("RequestId and/or MRN empty ??: " + "-->requestId:" + dtoResponse.getRequestId() + " -->MRN from API (look at logback-logs): " + mrn);
									dtoResponse.setErrMsg(errMsg.toString());
									break;
								}
								
							}
							break; //only first in list
							
							
						}else {
							errMsg.append(" requestId/MRN already exist. This operation is invalid. Make sure this fields are empty before any POST or issue a PUT (with current MRN) ");
							dtoResponse.setErrMsg(errMsg.toString());
						}
						
					}
				}else {
					errMsg.append(" no records ");
					dtoResponse.setErrMsg(errMsg.toString());
				}
				
			}else {
				errMsg.append(" invalid user " + user + " " + methodName);
				dtoResponse.setErrMsg(errMsg.toString());
			}
			
		}catch(Exception e) {
			
			e.printStackTrace();
			//Get out stackTrace to the response (errMsg) and logger
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			dtoResponse.setErrMsg(sw.toString());
			
			
		}finally {
			
			//check on status
			if(!apiStatusAlreadyUpdated) {
				if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())) {
					logger.info(SadDigitollConstants.LOG_PREFIX_LEGEND + dtoResponse.getErrMsg());
					dtoResponse.setDb_st2(EnumSadmotfStatus2.M.toString());
					logger.info("INSIDE setStatus:" + dtoResponse.getDb_st2());
					//
					List<SadmotfDto> xx = sadmotfService.updateLrnMrnSadmotf(serverRoot, user, dtoResponse, dtoResponse.getDocumentIssueDate(), "ULM");
					logger.info("After update on status 2 (finally-clause)");
				}
			}
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
	 * Test for Async RestController. The service must have @Service @EnableAsync in order for the method to use @Async
	 * 
	 * @param request
	 * @param user
	 * @param etlnrt
	 * @param mrn
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/digitollv2/putTransportTestAsyncTest.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public ResponseEntity<GenericDtoResponse> putTransportDigitollV2Async(HttpServletRequest request , @RequestParam(value = "user", required = true) String user, 
																				@RequestParam(value = "etlnrt", required = true) String etlnrt,
																				@RequestParam(value = "mrn", required = true) String mrn ) throws Exception {
		String serverRoot = ServerRoot.getServerRoot(request);
		asynchTransportService.putTransportDigitollV2Test(serverRoot, user, etlnrt, mrn);

		GenericDtoResponse dtoResponse = new GenericDtoResponse();
		dtoResponse.setUser(user); dtoResponse.setEtlnrt(etlnrt); dtoResponse.setMrn(mrn);
		
		return new ResponseEntity<GenericDtoResponse>(dtoResponse, HttpStatus.OK);
		
	}
	
	/**
	 * 
	 * @param request
	 * @param user
	 * @param etlnrt
	 * @param mrn
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/digitollv2/putTransport.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse putTransportDigitollV2(HttpServletRequest request , @RequestParam(value = "user", required = true) String user, 
																				@RequestParam(value = "etlnrt", required = true) String etlnrt,
																				@RequestParam(value = "mrn", required = true) String mrn ) throws Exception {
		logger.warn(PrettyLoggerOutputer.FRAME + PrettyLoggerOutputer.FRAME);
		logger.warn("START of CALL<controller>: "+ new Date());
		logger.warn(PrettyLoggerOutputer.FRAME + PrettyLoggerOutputer.FRAME);
		
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericDtoResponse dtoResponse = new GenericDtoResponse();
		dtoResponse.setUser(user);
		dtoResponse.setEtlnrt(etlnrt);
		dtoResponse.setEllnrt(Integer.valueOf(etlnrt));//for log purposes only
		dtoResponse.setTdn("0"); //dummy (needed for db-log on table SADEXLOG)
		dtoResponse.setMrn(mrn);
		dtoResponse.setRequestMethodApi("PUT");
		boolean apiStatusAlreadyUpdated = false;
		boolean isApiAir = false;
		boolean isApiRail = false;
		
		StringBuilder errMsg = new StringBuilder("ERROR ");
		String methodName = new Object() {}
	      .getClass()
	      .getEnclosingMethod()
	      .getName();
		
		logger.warn("Inside " + methodName + "- MRNnr: " + mrn );
		
		try {
			if(checkUser(user)) {
				logger.warn("user OK:" + user);
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
							if(ApiRecognizer.isAir(dto.getEtktyp())) { 
								isApiAir = true; 
							}else if(ApiRecognizer.isRail(dto.getEtktyp()))  { 
								isApiRail = true; 
							}
							String json = "";
							if(isApiAir) {
								json = apiServicesAir.putTransportDigitollV2(transport, mrn, tollTokenMap);
							}else if(isApiRail) {
								json = apiServicesRail.putTransportDigitollV2(transport, mrn, tollTokenMap);
							}else {
								json = apiServices.putTransportDigitollV2(transport, mrn, tollTokenMap);
							}
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
				}
				
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
			dtoResponse.setDb_st2(EnumSadmotfStatus2.M.toString());
		
		}finally {
			
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
			}
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
	 * Delete MasterConsignment through the API - DELETE
	 * @param request
	 * @param user
	 * @param etlnrt
	 * @param mrn
	 * @return
	 * @throws Exception
	 * 
	 * http://localhost:8080/syjservicestn-expft/digitollv2/deleteTransport?user=NN&etlnrt=1&mrn=XXX
	 * 
	 * TEST - OK
	 */
	@RequestMapping(value="/digitollv2/deleteTransport.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse deleteTransportDigitollV2(HttpServletRequest request , @RequestParam(value = "user", required = true) String user,
																				@RequestParam(value = "etlnrt", required = true) String etlnrt,
																				@RequestParam(value = "mrn", required = true) String mrn) throws Exception {
		
		logger.warn(PrettyLoggerOutputer.FRAME + PrettyLoggerOutputer.FRAME);
		logger.warn("START of CALL<controller>: "+ new Date());
		logger.warn(PrettyLoggerOutputer.FRAME + PrettyLoggerOutputer.FRAME);
		
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericDtoResponse dtoResponse = new GenericDtoResponse();
		dtoResponse.setUser(user);
		dtoResponse.setEtlnrt(etlnrt);
		dtoResponse.setEllnrt(Integer.valueOf(etlnrt));//for log purposes only
		dtoResponse.setTdn("0");
		dtoResponse.setMrn(mrn);
		dtoResponse.setRequestMethodApi("DELETE");
		boolean apiStatusAlreadyUpdated = false;
		boolean isApiAir = false;
		boolean isApiRail = false;
		
		
		StringBuilder errMsg = new StringBuilder("ERROR ");
		String methodName = new Object() {}
	      .getClass()
	      .getEnclosingMethod()
	      .getName();
		
		logger.warn("Inside " + methodName + "- MRNnr: " + mrn );

		try {
			if(checkUser(user)) {
				logger.warn("user OK:" + user);
				List<SadmotfDto> list = sadmotfService.getSadmotfForUpdate(serverRoot, user, etlnrt,  mrn);
				
				if(list != null && list.size()>0) {
					logger.warn("list size:" + list.size());
					
					
					for (SadmotfDto dto: list) {
						//Only valid when mrn(emmid) is NOT empty
						if(StringUtils.isNotEmpty(dto.getEtmid()) ) {
							Transport transport =  new MapperTransport().mapTransportForDelete();
							//API
							if(ApiRecognizer.isAir(dto.getEtktyp())) { 
								isApiAir = true; 
							}else if(ApiRecognizer.isRail(dto.getEtktyp()))  { 
								isApiRail = true; 
							}
							String json = "";
							if(isApiAir) {
								json = apiServicesAir.deleteTransportDigitollV2(transport, mrn);
							}else if(isApiRail) {
								json = apiServicesRail.deleteTransportDigitollV2(transport, mrn);
							}else {
								json = apiServices.deleteTransportDigitollV2(transport, mrn);
							}
							
							ApiRequestIdDto obj = new ObjectMapper().readValue(json, ApiRequestIdDto.class);
							logger.warn("JSON = " + json);
							logger.warn("RequestId = " + obj.getRequestId());
							//put in response
							dtoResponse.setRequestId(obj.getRequestId());
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
								
								//(2)now we have the new lrn(requestId for the updated mrn so we proceed with the SADMOTF-update-lrn at transport-level
								if(StringUtils.isNotEmpty(requestId) && StringUtils.isNotEmpty(mrn)) {
									String mode = "DL";
									dtoResponse.setMrn(mrn);
									dtoResponse.setDb_st2(EnumSadmotfStatus2.D.toString());
									//we must update the send date as well. Only 8-numbers
									String sendDate = transport.getDocumentIssueDate().replaceAll("-", "").substring(0,8);
									dtoResponse.setDocumentIssueDate(sendDate);//as aux for logging purposes
									
									List<SadmotfDto> xx = sadmotfService.updateLrnMrnSadmotf(serverRoot, user, dtoResponse, sendDate, mode);
									if(xx!=null && xx.size()>0) {
										for (SadmotfDto rec: xx) {
											if(StringUtils.isEmpty(rec.getEtmid()) ){
												//OK
												apiStatusAlreadyUpdated = true;
											}else {
												errMsg.append("MRN has not been removed after SADMOTF-delete-light mrn:" + mrn);
												dtoResponse.setErrMsg(errMsg.toString());
											}
										}
									}
									
								}else {
									errMsg.append("LRN empty after DELETE-LIGHT ??: " + "-->requestId:" + requestId + " -->MRN from db(SADMOTF): " + mrn);
									dtoResponse.setErrMsg(errMsg.toString());
									break;
								}
							
							}
							
							break; //only first in list
							
							
						}else {
							errMsg.append(" LRN/MRN are empty (SADMOTF). This operation is invalid. Make sure emuuid(lrn)/emmid(mrn) fields have values before any DELETE ");
							dtoResponse.setErrMsg(errMsg.toString());
						}
						
					} 
				}else {
					errMsg.append(" no records to fetch from SADMOTF ");
					dtoResponse.setErrMsg(errMsg.toString());
				}
				
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
			dtoResponse.setDb_st2(EnumSadmotfStatus2.M.toString());
			
		}finally {
			
			//check on status
			if(!apiStatusAlreadyUpdated) {
				if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())) {
					logger.info(SadDigitollConstants.LOG_PREFIX_LEGEND + dtoResponse.getErrMsg());
					dtoResponse.setDb_st2(EnumSadmotfStatus2.M.toString());
					logger.info("INSIDE setStatus:" + dtoResponse.getDb_st2());
					//
					List<SadmotfDto> xx = sadmotfService.updateLrnMrnSadmotf(serverRoot, user, dtoResponse, dtoResponse.getDocumentIssueDate(), "ULM");
					logger.info("After update on status 2 (finally-clause)");
				}
			}
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
	 * Gets Master Consignment status through the API - GET - without having to check our db 
	 * @Example http://localhost:8080/syjservicestn-expft/digitollv2/getTransport.do?user=SYSTEMA&lrn=f2bfbb94-afae-4af3-a4ff-437f787d322f
	 * @param session
	 * @param user
	 * @param id
	 * @return
	 * 
	 * http://localhost:8080/syjservicestn-expft/digitollv2/getTransport.do?user=SYSTEMA&lrn=f2bfbb94-afae-4af3-a4ff-437f787d322f
	 * 
	 * test - OK
	 */
	@RequestMapping(value="/digitollv2/getTransport.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse getTransportDigitollV2(HttpServletRequest request , @RequestParam(value = "user", required = true) String user,
																				@RequestParam(value = "lrn", required = true) String lrn,
																				@RequestParam(value = "apiType", required = true) String apiType) throws Exception {
		
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericDtoResponse dtoResponse = new GenericDtoResponse();
		dtoResponse.setUser(user);
		dtoResponse.setLrn(lrn);
		dtoResponse.setRequestMethodApi("GET");
		StringBuilder errMsg = new StringBuilder("ERROR ");
		String methodName = new Object() {}
	      .getClass()
	      .getEnclosingMethod()
	      .getName();
		
		logger.warn("Inside " + methodName + "- LRNnr: " + lrn + "- apiType: " + apiType );
		
		try {
			if(checkUser(user)) {
					//(2)now we have the new lrn for the updated mrn so we proceed with the SADEXMF-update-lrn at master consignment
					if(StringUtils.isNotEmpty(lrn)) {
						dtoResponse.setLrn(lrn);
						
						
						String mrn = this.getMrnTransportDigitollV2FromApi(dtoResponse, lrn, apiType);
						if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())){
							errMsg.append(dtoResponse.getErrMsg());
							
							if(StringUtils.isNotEmpty(mrn)) {
								dtoResponse.setErrMsg("");
							}else {
								dtoResponse.setErrMsg(errMsg.toString());
							}
						}else {
							dtoResponse.setMrn(mrn);
						}
						
					}else {
						errMsg.append("LRN empty ?" + "-->LRN:" + lrn);
						dtoResponse.setErrMsg(errMsg.toString());
						
					}
											
			}else {
				errMsg.append(" invalid user " + user + " " + methodName);
				dtoResponse.setErrMsg(errMsg.toString());
			}
			
		}catch(Exception e) {
			//e.printStackTrace();
			//Get out stackTrace to the response (errMsg)
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			dtoResponse.setErrMsg(sw.toString());
		}
		
		//NA --> log in db before std-output -- since this is a help method to be executed from the browser only ...
		//sadexlogLogger.doLog(serverRoot, user, dtoResponse);
		//log in log file
		if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())) { logger.error(dtoResponse.getErrMsg()); }
		
		//std output (browser)
		return dtoResponse;
	}
	
	/**
	 * /**
	 * This uses the wsdl generated code from:
	 * 
	 * with Java 8 (command line where JAVA_HOME/bin/wsimport...
	 * >wsimport -s . -keep -p com.eori.validation.soap.ws.client.generated "https://ec.europa.eu/taxation_customs/dds2/eos/validation/services/validation?wsdl"
	 * 
	 * As a test for EORI-Validation
	 * 
	 * Important: open for firewall on DSV:
	 * https://ec.europa.eu/taxation_customs/dds2/eos/validation/services/validation?wsdl
	 * http://eori.ws.eos.dds.s/
	 * 
	 * Works fine from http:localhost:8080/... and https://gw.systema.no.8443...
	 * 
	 * 
	 * Client call: http://localhost:8080/syjservicestn-expft/digitollv2/getEORIValidation.do?user=OSCAR&eori=SE4441976109
	 * @param request
	 * @param user
	 * @param eori
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/digitollv2/getEORIValidation.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponseEORIValidation getEORIValidation(HttpServletRequest request , @RequestParam(value = "user", required = true) String user,
																				@RequestParam(value = "eori", required = true) String eori) throws Exception {
		
		GenericDtoResponseEORIValidation dtoResponse = new GenericDtoResponseEORIValidation();
		dtoResponse.setUser(user);
		dtoResponse.setEori(eori);
		
		StringBuilder errMsg = new StringBuilder("ERROR ");
		String methodName = new Object() {}
	      .getClass()
	      .getEnclosingMethod()
	      .getName();
		
		logger.warn("Inside " + methodName + "- eori: " + eori );
		
		try {
			if(checkUser(user)) {
					//
					if(StringUtils.isNotEmpty(eori)) {
						//(1) add eori to list
						List<String> eoriList = new ArrayList();
						eoriList.add(eori);
						//(2) validate towards external API-EORI-service 
						Validation validation = new Validation();
						EORIValidation eoriValidation = validation.getEORIValidationImplPort();
						EoriValidationResult result = eoriValidation.validateEORI(eoriList);
						List<EoriResponse> responseList = result.getResult();
						//(3) got the result now
						for (EoriResponse response: responseList ) {
							//DEBUG	
							logger.info(response.getEori() + "XXX" + response.getName() + " Status:" + response.getStatus() + "-" + response.getStatusDescr());
							logger.info(response.getCity() + " " + response.getCountry() + " " + response.getPostalCode());
							//
							EoriValidationDto dto = new EoriValidationDto();
							if(response.getStatus()==0) {
								dto.setEori(response.getEori());
								dto.setName(response.getName());
								dto.setStatus(response.getStatus());
								dto.setStatusDescr(response.getStatusDescr());
								dto.setCity(response.getCity());
								dto.setPostalCode(response.getPostalCode());
								dto.setCountry(response.getCountry());
							}
							//
							dtoResponse.getList().add(dto);
							
						}
						
		
					}else {
						errMsg.append("EORI empty ?" + "-->EORI:" + eori);
						dtoResponse.setErrMsg(errMsg.toString());
						
					}
											
			}else {
				errMsg.append(" invalid user " + user + " " + methodName);
				dtoResponse.setErrMsg(errMsg.toString());
			}
			
		}catch(Exception e) {
			//e.printStackTrace();
			//Get out stackTrace to the response (errMsg)
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			dtoResponse.setErrMsg(sw.toString());
		}
		
		//NA --> log in db before std-output -- since this is a help method to be executed from the browser only ...
		//sadexlogLogger.doLog(serverRoot, user, dtoResponse);
		//log in log file
		if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())) { logger.error(dtoResponse.getErrMsg()); }
		
		//std output (browser)
		return dtoResponse;
	}
	
	
	@RequestMapping(value="/digitollv2/getDocsRecTransport.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponseLight getDocsReceivedTransportDigitollV2(HttpServletRequest request , @RequestParam(value = "user", required = true) String user, 
																				@RequestParam(value = "mrn", required = true) String mrn ) throws Exception {
		
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericDtoResponseLight dtoResponse = new GenericDtoResponseLight();
		dtoResponse.setUser(user);
		dtoResponse.setTdn("0"); //dummy
		dtoResponse.setMrn(mrn);
		dtoResponse.setRequestMethodApi("GET all documentNumbers in TRANSPORT-level at toll.no");
		StringBuilder errMsg = new StringBuilder("ERROR ");
		
		String methodName = new Object() {}
	      .getClass()
	      .getEnclosingMethod()
	      .getName();
		
		logger.warn("Inside " + methodName + "- MRNnr: " + mrn );
		
		try {
			if(checkUser(user)) {
				logger.warn("user OK:" + user);
				//API - PROD
				String json = apiServices.getDocsReceivedTransportDigitollV2(mrn);
				logger.warn("JSON = " + json);
				if(StringUtils.isNotEmpty(json)) {
					ApiMrnStatusRecordDto[] obj = new ObjectMapper().readValue(json, ApiMrnStatusRecordDto[].class);
					if(obj!=null) {
						List<Object> list = Arrays.asList(obj);
						logger.warn("List = " + list);
						
						//Check for OK or Error in order to proceed
						if(list!=null && !list.isEmpty()){
							dtoResponse.setList(list);
							//now try-further with house-ref and fill the list even more
							json = apiServices.getDocsHousesReceivedTransportDigitollV2(mrn);
							ApiMrnStatusRecordDto[] objHouses = new ObjectMapper().readValue(json, ApiMrnStatusRecordDto[].class);
							if(objHouses!=null) {
								logger.info("Prepare list of houses..." + objHouses.toString());
								List<Object> listHouses = Arrays.asList(objHouses);
								if(listHouses!=null && !listHouses.isEmpty()) {
									logger.info("list of houses-size:" + listHouses.size());
									dtoResponse.setListAux(listHouses);
								}
								
							}
						}else {
							errMsg.append(methodName + " -->MRN not existent ?? <json raw>: " + json);
							dtoResponse.setErrMsg(errMsg.toString());
						}
					}
				}else {
					errMsg.append(methodName + " -->JSON toll.no EMPTY. The MRN does not exists ...? ");
					dtoResponse.setErrMsg(errMsg.toString());
				}
				
			}else {
				errMsg.append(methodName + " -->invalid user " + user + " " + methodName);
				dtoResponse.setErrMsg(errMsg.toString());
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			//Get out stackTrace to the response (errMsg)
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			dtoResponse.setErrMsg(sw.toString());
		}
		
		//NA --> log in db before std-output. Only from browser. No logging needed 
		//sadexlogLogger.doLog(serverRoot, user, dtoResponse);
		//log in log file
		if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())) { logger.error(dtoResponse.getErrMsg()); }
		
		//std output (browser)
		return dtoResponse;
	}
	
	
	@RequestMapping(value="/digitollv2/getDocsRecTransport_withDescendants.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericWithDescendantsDtoContainer getDocsReceivedTransportWithDescendantsDigitollV2(HttpServletRequest request , @RequestParam(value = "user", required = true) String user, 
																				@RequestParam(value = "mrn", required = true) String mrn ) throws Exception {
		
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericWithDescendantsDtoContainer dtoResponse = new GenericWithDescendantsDtoContainer();
		dtoResponse.setUser(user);
		StringBuilder errMsg = new StringBuilder("ERROR ");
		
		String methodName = new Object() {}
	      .getClass()
	      .getEnclosingMethod()
	      .getName();
		
		logger.warn("Inside " + methodName + "- MRNnr: " + mrn );
		
		try {
			if(checkUser(user)) {
				logger.warn("user OK:" + user);
				//API - PROD
				String json = apiServices.getDocsReceivedTransportWithDescendantsDigitollV2(mrn);
				logger.warn("JSON = " + json);
				if(StringUtils.isNotEmpty(json)) {
					ApiMrnStatusWithDescendantsRecordDto dto = new ObjectMapper().readValue(json, ApiMrnStatusWithDescendantsRecordDto.class);
					if(dto!=null) {
						logger.info(dto.toString());
						dtoResponse.setObject(dto);
					}
				}else {
					errMsg.append(methodName + " -->JSON toll.no EMPTY. The MRN does not exists ...? ");
					dtoResponse.setErrMsg(errMsg.toString());
				}
				
			}else {
				errMsg.append(methodName + " -->invalid user " + user + " " + methodName);
				dtoResponse.setErrMsg(errMsg.toString());
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			//Get out stackTrace to the response (errMsg)
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			dtoResponse.setErrMsg(sw.toString());
		}
		
		//NA --> log in db before std-output. Only from browser. No logging needed 
		//sadexlogLogger.doLog(serverRoot, user, dtoResponse);
		//log in log file
		if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())) { logger.error(dtoResponse.getErrMsg()); }
		
		//std output (browser)
		return dtoResponse;
	}
	
	@RequestMapping(value="/digitollv2/getDocsRecTransportRail.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponseLight getDocsReceivedTransportRailDigitollV2(HttpServletRequest request , @RequestParam(value = "user", required = true) String user, 
																				@RequestParam(value = "mrn", required = true) String mrn ) throws Exception {
		
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericDtoResponseLight dtoResponse = new GenericDtoResponseLight();
		dtoResponse.setUser(user);
		dtoResponse.setTdn("0"); //dummy
		dtoResponse.setMrn(mrn);
		dtoResponse.setRequestMethodApi("GET all documentNumbers in TRANSPORT-level at toll.no");
		StringBuilder errMsg = new StringBuilder("ERROR ");
		
		String methodName = new Object() {}
	      .getClass()
	      .getEnclosingMethod()
	      .getName();
		
		logger.warn("Inside " + methodName + "- MRNnr: " + mrn );
		
		try {
			if(checkUser(user)) {
				logger.warn("user OK:" + user);
				//API - PROD
				String json = apiServicesRail.getDocsReceivedTransportDigitollV2(mrn);
				logger.warn("JSON = " + json);
				if(StringUtils.isNotEmpty(json)) {
					ApiMrnStatusRecordDto[] obj = new ObjectMapper().readValue(json, ApiMrnStatusRecordDto[].class);
					if(obj!=null) {
						List<Object> list = Arrays.asList(obj);
						logger.warn("List = " + list);
						
						//Check for OK or Error in order to proceed
						if(list!=null && !list.isEmpty()){
							dtoResponse.setList(list);
							//now try-further with house-ref and fill the list even more
							json = apiServicesRail.getDocsHousesReceivedTransportDigitollV2(mrn);
							ApiMrnStatusRecordDto[] objHouses = new ObjectMapper().readValue(json, ApiMrnStatusRecordDto[].class);
							if(objHouses!=null) {
								logger.info("Prepare list of houses..." + objHouses.toString());
								List<Object> listHouses = Arrays.asList(objHouses);
								if(listHouses!=null && !listHouses.isEmpty()) {
									logger.info("list of houses-size:" + listHouses.size());
									dtoResponse.setListAux(listHouses);
								}
								
							}
						}else {
							errMsg.append(methodName + " -->MRN not existent ?? <json raw>: " + json);
							dtoResponse.setErrMsg(errMsg.toString());
						}
					}
				}else {
					errMsg.append(methodName + " -->JSON toll.no EMPTY. The MRN does not exists ...? ");
					dtoResponse.setErrMsg(errMsg.toString());
				}
				
			}else {
				errMsg.append(methodName + " -->invalid user " + user + " " + methodName);
				dtoResponse.setErrMsg(errMsg.toString());
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			//Get out stackTrace to the response (errMsg)
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			dtoResponse.setErrMsg(sw.toString());
		}
		
		//NA --> log in db before std-output. Only from browser. No logging needed 
		//sadexlogLogger.doLog(serverRoot, user, dtoResponse);
		//log in log file
		if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())) { logger.error(dtoResponse.getErrMsg()); }
		
		//std output (browser)
		return dtoResponse;
	}
	
	
	@RequestMapping(value="/digitollv2/getDocsRecTransportRail_withDescendants.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericWithDescendantsDtoContainer getDocsReceivedTransportRailWithDescendantsDigitollV2(HttpServletRequest request , @RequestParam(value = "user", required = true) String user, 
																				@RequestParam(value = "mrn", required = true) String mrn ) throws Exception {
		
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericWithDescendantsDtoContainer dtoResponse = new GenericWithDescendantsDtoContainer();
		dtoResponse.setUser(user);
		StringBuilder errMsg = new StringBuilder("ERROR ");
		
		String methodName = new Object() {}
	      .getClass()
	      .getEnclosingMethod()
	      .getName();
		
		logger.warn("Inside " + methodName + "- MRNnr: " + mrn );
		
		try {
			if(checkUser(user)) {
				logger.warn("user OK:" + user);
				//API - PROD
				String json = apiServicesRail.getDocsReceivedTransportWithDescendantsDigitollV2(mrn);
				logger.warn("JSON = " + json);
				if(StringUtils.isNotEmpty(json)) {
					ApiMrnStatusWithDescendantsRecordDto dto = new ObjectMapper().readValue(json, ApiMrnStatusWithDescendantsRecordDto.class);
					
					if(dto!=null) {
						logger.info(dto.toString());
						dtoResponse.setObject(dto);
					}
					
				}else {
					errMsg.append(methodName + " -->JSON toll.no EMPTY. The MRN does not exists ...? ");
					dtoResponse.setErrMsg(errMsg.toString());
				}
				
			}else {
				errMsg.append(methodName + " -->invalid user " + user + " " + methodName);
				dtoResponse.setErrMsg(errMsg.toString());
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			//Get out stackTrace to the response (errMsg)
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			dtoResponse.setErrMsg(sw.toString());
		}
		
		//NA --> log in db before std-output. Only from browser. No logging needed 
		//sadexlogLogger.doLog(serverRoot, user, dtoResponse);
		//log in log file
		if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())) { logger.error(dtoResponse.getErrMsg()); }
		
		//std output (browser)
		return dtoResponse;
	}
	
	@RequestMapping(value="/digitollv2/getDocsRecTransportAir.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponseLight getDocsReceivedTransportAirDigitollV2(HttpServletRequest request , @RequestParam(value = "user", required = true) String user, 
																				@RequestParam(value = "mrn", required = true) String mrn ) throws Exception {
		
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericDtoResponseLight dtoResponse = new GenericDtoResponseLight();
		dtoResponse.setUser(user);
		dtoResponse.setTdn("0"); //dummy
		dtoResponse.setMrn(mrn);
		dtoResponse.setRequestMethodApi("GET all documentNumbers in TRANSPORT-level at toll.no");
		StringBuilder errMsg = new StringBuilder("ERROR ");
		
		String methodName = new Object() {}
	      .getClass()
	      .getEnclosingMethod()
	      .getName();
		
		logger.warn("Inside " + methodName + "- MRNnr: " + mrn );
		
		try {
			if(checkUser(user)) {
				logger.warn("user OK:" + user);
				//API - PROD
				String json = apiServicesAir.getDocsReceivedTransportDigitollV2(mrn);
				logger.warn("JSON = " + json);
				if(StringUtils.isNotEmpty(json)) {
					ApiMrnStatusRecordDto[] obj = new ObjectMapper().readValue(json, ApiMrnStatusRecordDto[].class);
					if(obj!=null) {
						List<Object> list = Arrays.asList(obj);
						logger.warn("List = " + list);
						
						//Check for OK or Error in order to proceed
						if(list!=null && !list.isEmpty()){
							dtoResponse.setList(list);
							//now try-further with house-ref and fill the list even more
							json = apiServicesAir.getDocsHousesReceivedTransportDigitollV2(mrn);
							ApiMrnStatusRecordDto[] objHouses = new ObjectMapper().readValue(json, ApiMrnStatusRecordDto[].class);
							if(objHouses!=null) {
								logger.info("Prepare list of houses..." + objHouses.toString());
								List<Object> listHouses = Arrays.asList(objHouses);
								if(listHouses!=null && !listHouses.isEmpty()) {
									logger.info("list of houses-size:" + listHouses.size());
									dtoResponse.setListAux(listHouses);
								}
								
							}
						}else {
							errMsg.append(methodName + " -->MRN not existent ?? <json raw>: " + json);
							dtoResponse.setErrMsg(errMsg.toString());
						}
					}
				}else {
					errMsg.append(methodName + " -->JSON toll.no EMPTY. The MRN does not exists ...? ");
					dtoResponse.setErrMsg(errMsg.toString());
				}
				
			}else {
				errMsg.append(methodName + " -->invalid user " + user + " " + methodName);
				dtoResponse.setErrMsg(errMsg.toString());
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			//Get out stackTrace to the response (errMsg)
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			dtoResponse.setErrMsg(sw.toString());
		}
		
		//NA --> log in db before std-output. Only from browser. No logging needed 
		//sadexlogLogger.doLog(serverRoot, user, dtoResponse);
		//log in log file
		if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())) { logger.error(dtoResponse.getErrMsg()); }
		
		//std output (browser)
		return dtoResponse;
	}
	
	
	@RequestMapping(value="/digitollv2/getDocsRecTransportAir_withDescendants.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericWithDescendantsDtoContainer getDocsReceivedTransportAirWithDescendantsDigitollV2(HttpServletRequest request , @RequestParam(value = "user", required = true) String user, 
																				@RequestParam(value = "mrn", required = true) String mrn ) throws Exception {
		
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericWithDescendantsDtoContainer dtoResponse = new GenericWithDescendantsDtoContainer();
		dtoResponse.setUser(user);
		StringBuilder errMsg = new StringBuilder("ERROR ");
		
		String methodName = new Object() {}
	      .getClass()
	      .getEnclosingMethod()
	      .getName();
		
		logger.warn("Inside " + methodName + "- MRNnr: " + mrn );
		
		try {
			if(checkUser(user)) {
				logger.warn("user OK:" + user);
				//API - PROD
				String json = apiServicesAir.getDocsReceivedTransportWithDescendantsDigitollV2(mrn);
				logger.warn("JSON = " + json);
				if(StringUtils.isNotEmpty(json)) {
					ApiMrnStatusWithDescendantsRecordDto dto = new ObjectMapper().readValue(json, ApiMrnStatusWithDescendantsRecordDto.class);
					
					if(dto!=null) {
						logger.info(dto.toString());
						dtoResponse.setObject(dto);
					}
					
				}else {
					errMsg.append(methodName + " -->JSON toll.no EMPTY. The MRN does not exists ...? ");
					dtoResponse.setErrMsg(errMsg.toString());
				}
				
			}else {
				errMsg.append(methodName + " -->invalid user " + user + " " + methodName);
				dtoResponse.setErrMsg(errMsg.toString());
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			//Get out stackTrace to the response (errMsg)
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			dtoResponse.setErrMsg(sw.toString());
		}
		
		//NA --> log in db before std-output. Only from browser. No logging needed 
		//sadexlogLogger.doLog(serverRoot, user, dtoResponse);
		//log in log file
		if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())) { logger.error(dtoResponse.getErrMsg()); }
		
		//std output (browser)
		return dtoResponse;
	}
	
	
	
	/**
	 * 
	 * To get the final status when the carrier has passed the border
	 * Movement Road V2
	 * 
	 * {
	 *	  "validEntry": false,
	 *	  "customsOfficeOfEntry": "NO01018C",
	 *	  "timeOfEntry": "2022-04-08T11:51:00Z",
	 *	  "mrn": "22NO4TU2HUD59UCBT8"
	 * }
	 * 
	 * @param request
	 * @param user
	 * @param mrn
	 * @return
	 */
	@RequestMapping(value="/digitollv2/getMovementRoadEntry.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse getMovementRoadEntryDigitollV2FromApi(HttpServletRequest request , @RequestParam(value = "user", required = true) String user, @RequestParam(value = "mrn", required = true) String mrn) {
		logger.info("Inside: getMovementRoadEntryDigitollV2FromApi");
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericDtoResponse dtoResponse = new GenericDtoResponse();
		dtoResponse.setUser(user);
		dtoResponse.setRequestMethodApi("GET");
		StringBuilder errMsg = new StringBuilder("ERROR ");
		
		String methodName = new Object() {}
	      .getClass()
	      .getEnclosingMethod()
	      .getName();
		
		logger.warn("Inside " + methodName );
		try {
			if(checkUser(user)) {
					
				
					String json = "";
					/*
					String testOS = System.getProperty("os.name");
					if(testOS!=null && testOS.startsWith("Mac")) {
						//Test playground is not working therefore we fake...
						json = getFakeEntry(mrn);
					}else {
						//PROD
						json = apiServices.getMovementRoadEntryDigitollV2(mrn);
					}*/
					json = apiServices.getMovementRoadEntryDigitollV2(mrn);
					logger.warn("JSON = " + json);
					if(StringUtils.isNotEmpty(json)) {
						EntryMovRoadDto obj = new ObjectMapper().readValue(json, EntryMovRoadDto.class);
						//DEBUG
						/*for (EntryDto dto: obj) {
							logger.warn(dto.getEntrySummaryDeclarationMRN());
							logger.warn(dto.getTransportDocumentHouseLevel().getReferenceNumber());
							logger.warn(dto.getRoutingResult().getId());
						}*/
						dtoResponse.setEntryMovementRoad(obj);
					}
											
			}else {
				errMsg.append(" invalid user " + user + " " + methodName);
				dtoResponse.setErrMsg(errMsg.toString());
			}
			
		}catch(Exception e) {
			//e.printStackTrace();
			//Get out stackTrace to the response (errMsg)
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			dtoResponse.setErrMsg(sw.toString());
		}
		
		//NA - log in db before std-output
		//sadexlogLogger.doLog(serverRoot, user, dtoResponse);
		//log in log file
		if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())) { logger.error(dtoResponse.getErrMsg()); }
		
		return dtoResponse;
	}
	
	@RequestMapping(value="/digitollv2/getMovementRailEntry.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse getMovementRailEntryDigitollV2FromApi(HttpServletRequest request , @RequestParam(value = "user", required = true) String user, @RequestParam(value = "mrn", required = true) String mrn) {
		logger.info("Inside: getMovementRailEntryDigitollV2FromApi");
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericDtoResponse dtoResponse = new GenericDtoResponse();
		dtoResponse.setUser(user);
		dtoResponse.setRequestMethodApi("GET");
		StringBuilder errMsg = new StringBuilder("ERROR ");
		
		String methodName = new Object() {}
	      .getClass()
	      .getEnclosingMethod()
	      .getName();
		
		logger.warn("Inside " + methodName );
		try {
			if(checkUser(user)) {
					
					String json = "";
					/*
					String testOS = System.getProperty("os.name");
					if(testOS!=null && testOS.startsWith("Mac")) {
						//Test playground is not working therefore we fake...
						json = getFakeEntry(mrn);
					}else {
						//PROD
						json = apiServicesRail.getMovementRailEntryDigitollV2(mrn);
					}*/
					
					json = apiServicesRail.getMovementRailEntryDigitollV2(mrn);
					
					logger.warn("JSON = " + json);
					if(StringUtils.isNotEmpty(json)) {
						EntryMovRoadDto obj = new ObjectMapper().readValue(json, EntryMovRoadDto.class);
						//DEBUG
						/*for (EntryDto dto: obj) {
							logger.warn(dto.getEntrySummaryDeclarationMRN());
							logger.warn(dto.getTransportDocumentHouseLevel().getReferenceNumber());
							logger.warn(dto.getRoutingResult().getId());
						}*/
						dtoResponse.setEntryMovementRoad(obj);
					}
											
			}else {
				errMsg.append(" invalid user " + user + " " + methodName);
				dtoResponse.setErrMsg(errMsg.toString());
			}
			
		}catch(Exception e) {
			//e.printStackTrace();
			//Get out stackTrace to the response (errMsg)
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			dtoResponse.setErrMsg(sw.toString());
		}
		
		//NA - log in db before std-output
		//sadexlogLogger.doLog(serverRoot, user, dtoResponse);
		//log in log file
		if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())) { logger.error(dtoResponse.getErrMsg()); }
		
		return dtoResponse;
	}
	
	@RequestMapping(value="/digitollv2/getMovementAirEntry.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse getMovementAirEntryDigitollV2FromApi(HttpServletRequest request , @RequestParam(value = "user", required = true) String user, @RequestParam(value = "mrn", required = true) String mrn) {
		logger.info("Inside: getMovementAirEntryDigitollV2FromApi");
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericDtoResponse dtoResponse = new GenericDtoResponse();
		dtoResponse.setUser(user);
		dtoResponse.setRequestMethodApi("GET");
		StringBuilder errMsg = new StringBuilder("ERROR ");
		
		String methodName = new Object() {}
	      .getClass()
	      .getEnclosingMethod()
	      .getName();
		
		logger.warn("Inside " + methodName );
		try {
			if(checkUser(user)) {
					
					String json = "";
					
					json = apiServicesAir.getMovementAirEntryDigitollV2(mrn);
					
					logger.warn("JSON = " + json);
					if(StringUtils.isNotEmpty(json)) {
						EntryMovRoadDto obj = new ObjectMapper().readValue(json, EntryMovRoadDto.class);
						//DEBUG
						/*for (EntryDto dto: obj) {
							logger.warn(dto.getEntrySummaryDeclarationMRN());
							logger.warn(dto.getTransportDocumentHouseLevel().getReferenceNumber());
							logger.warn(dto.getRoutingResult().getId());
						}*/
						dtoResponse.setEntryMovementRoad(obj);
					}
											
			}else {
				errMsg.append(" invalid user " + user + " " + methodName);
				dtoResponse.setErrMsg(errMsg.toString());
			}
			
		}catch(Exception e) {
			//e.printStackTrace();
			//Get out stackTrace to the response (errMsg)
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			dtoResponse.setErrMsg(sw.toString());
		}
		
		//NA - log in db before std-output
		//sadexlogLogger.doLog(serverRoot, user, dtoResponse);
		//log in log file
		if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())) { logger.error(dtoResponse.getErrMsg()); }
		
		return dtoResponse;
	}
	
	private String getFakeEntry (String mrn) {
		String fakeResult = "";
		if(mrn.equals("23NO7LCOADR1DZSBT0")) {
			fakeResult = "{\"validEntry\":true,\"customsOfficeOfEntry\":\"NO372001\",\"timeOfEntry\":\"2023-11-08T14:32:40.235Z\",\"mrn\":\"23NO7LCOADR1DZSBT0\"}";
		}
		return fakeResult;
	}
	/**
	 * This belongs to another USE-CASE for ICS2 and not movement road. It works but it is not like the above
	 * 
	 * @param request
	 * @param user
	 * @param uuid: this parameter could exists; if the end-user sends it...(optional)
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/digitollv2/getRoutingTransport.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse getRoutingTransportDigitollV2(HttpServletRequest request , 
			@RequestParam(value = "user", required = true) String user,
			@RequestParam(value = "uuid", required = true) String uuid) throws Exception {
		
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericDtoResponse dtoResponse = new GenericDtoResponse();
		dtoResponse.setUser(user);
		dtoResponse.setLrn(uuid);
		dtoResponse.setRequestMethodApi("GET");
		StringBuilder errMsg = new StringBuilder("ERROR ");
		
		String methodName = new Object() {}
	      .getClass()
	      .getEnclosingMethod()
	      .getName();
		
		logger.warn("Inside " + methodName );
		try {
			if(checkUser(user)) {
					
				
					String json = "";
					json = apiServicesAir.getRoutingTransportDigitollV2(uuid);
					logger.warn("JSON = " + json);
					
					EntryRoutingDto[] obj = new ObjectMapper().readValue(json, EntryRoutingDto[].class);
					/*DEBUG
					/*for (EntryDto dto: obj) {
						logger.warn(dto.getEntrySummaryDeclarationMRN());
						logger.warn(dto.getTransportDocumentHouseLevel().getReferenceNumber());
						logger.warn(dto.getRoutingResult().getId());
					}*/
					dtoResponse.setEntryList(Arrays.asList(obj));
				
											
			}else {
				errMsg.append(" invalid user " + user + " " + methodName);
				dtoResponse.setErrMsg(errMsg.toString());
			}
			
		}catch(Exception e) {
			//e.printStackTrace();
			//Get out stackTrace to the response (errMsg)
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			dtoResponse.setErrMsg(sw.toString());
		}
		
		//NA - log in db before std-output
		//sadexlogLogger.doLog(serverRoot, user, dtoResponse);
		//log in log file
		if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())) { logger.error(dtoResponse.getErrMsg()); }
		
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
			dtoResponse.setEoriValidation(obj.getEoriValidation());
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
			logger.warn("eoriValidation = " + obj.getEoriValidation());
			if(obj.getEoriValidation() !=null) {
				logger.warn("eoriValidation = " + obj.getEoriValidation());
			}
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
