package no.systema.jservices.tvinn.digitoll.v2.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

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
import no.systema.jservices.tvinn.digitoll.v2.dao.Transport;
import no.systema.jservices.tvinn.digitoll.v2.dto.ApiRequestIdDto;
import no.systema.jservices.tvinn.digitoll.v2.dto.EntryDto;
import no.systema.jservices.tvinn.digitoll.v2.dto.SadmotfDto;
import no.systema.jservices.tvinn.digitoll.v2.enums.EnumSadmotfStatus2;
import no.systema.jservices.tvinn.expressfortolling2.dto.ApiMrnDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.ApiMrnStatusRecordDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.GenericDtoResponse;
import no.systema.jservices.tvinn.digitoll.v2.services.AsynchTransportService;
import no.systema.jservices.tvinn.digitoll.v2.services.MapperTransport;
import no.systema.jservices.tvinn.digitoll.v2.services.SadmomfService;
import no.systema.jservices.tvinn.digitoll.v2.services.SadmotfService;
import no.systema.jservices.tvinn.digitoll.v2.util.ApiAirRecognizer;
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
						if(ApiAirRecognizer.isAir(dto.getEtktyp()))  { isApiAir = true; }
						
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
							}else {
								json = apiServices.postTransportDigitollV2(transport, tollTokenMap);
							}
							//At this point we now have a valid tollToken to use
							
							ApiRequestIdDto obj = new ObjectMapper().readValue(json, ApiRequestIdDto.class);
							logger.warn("JSON = " + json);
							logger.warn("requestId = " + obj.getRequestId());
							dtoResponse.setAvd(String.valueOf(dto.getEtavd()));
							dtoResponse.setPro(String.valueOf(dto.getEtpro()));
							
							//In case there was an error at end-point and the requestId was not returned
							if(StringUtils.isEmpty(obj.getRequestId())){
								errMsg.append("requestId empty ?? <json raw>: " + json);
								dtoResponse.setErrMsg(errMsg.toString());
								
							}else {
								//(1) we have the requestId at this point. We must go an API-round trip again to get the MRN
								String requestId = obj.getRequestId();
								dtoResponse.setRequestId(obj.getRequestId());
								
								//Delay 6-10 seconds
								logger.warn(PrettyLoggerOutputer.FRAME);
								logger.warn("START of delay: "+ new Date());
								Thread.sleep(GET_MRN_DELAY_MILLISECONDS); 
								logger.warn("END of delay: "+ new Date());
								logger.warn(PrettyLoggerOutputer.FRAME);
								
								
								//(2) get mrn from API
								//PROD-->
								String mrn = this.getMrnTransportDigitollV2FromApi(dtoResponse, requestId, tollTokenMap, isApiAir);
								
								if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())){
									errMsg.append(dtoResponse.getErrMsg());
									dtoResponse.setErrMsg("");
									dtoResponse.setDb_st2(EnumSadmotfStatus2.M.toString());
								}else {
									dtoResponse.setDb_st2(EnumSadmotfStatus2.S.toString());
								}
								
								
								//(3)now we have lrn and mrn and proceed with the SADMOTF-update at transport
								if(StringUtils.isNotEmpty(requestId) && StringUtils.isNotEmpty(mrn)) {
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
									errMsg.append("RequestId and/or MRN empty ??: " + "-->requestId:" + requestId + " -->MRN from API (look at logback-logs): " + mrn);
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
							if(ApiAirRecognizer.isAir(dto.getEtktyp())) { isApiAir = true; }
							String json = "";
							if(isApiAir) {
								json = apiServicesAir.putTransportDigitollV2(transport, mrn, tollTokenMap);
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
									
									this.checkLrnValidationStatusTransportDigitollV2FromApi(dtoResponse, requestId, tollTokenMap, isApiAir);
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
							if(ApiAirRecognizer.isAir(dto.getEtktyp())) { isApiAir = true; }
							String json = "";
							if(isApiAir) {
								json = apiServicesAir.deleteTransportDigitollV2(transport, mrn);
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
	
	
	@RequestMapping(value="/digitollv2/getRoutingTransport.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse getRoutingTransportDigitollV2(HttpServletRequest request , @RequestParam(value = "user", required = true) String user) throws Exception {
		
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
					json = apiServicesAir.getRoutingTransportDigitollV2();
					logger.warn("JSON = " + json);
					
					EntryDto[] obj = new ObjectMapper().readValue(json, EntryDto[].class);
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
	private String getMrnTransportDigitollV2FromApi( GenericDtoResponse dtoResponse, String lrn, Map tollTokenMap, boolean isApiAir) {
		
		String retval = "";
		
		try{
			String json = "";	
			if(isApiAir) {
				json = apiServicesAir.getValidationStatusTransportDigitollV2(lrn, tollTokenMap );
			}else {
				json = apiServices.getValidationStatusTransportDigitollV2(lrn, tollTokenMap );
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
	private String checkLrnValidationStatusTransportDigitollV2FromApi(GenericDtoResponse dtoResponse, String lrn, Map tollTokenMap, boolean isApiAir) {
		
		String retval = "";
		
		try{
			String json = "";	
			if(isApiAir) {
				json = apiServicesAir.getValidationStatusTransportDigitollV2(lrn, tollTokenMap );
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
	
}
