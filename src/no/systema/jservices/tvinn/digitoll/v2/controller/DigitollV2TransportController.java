package no.systema.jservices.tvinn.digitoll.v2.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import no.systema.jservices.common.dao.services.BridfDaoService;
import no.systema.jservices.tvinn.expressfortolling.api.ApiServices;
import no.systema.jservices.tvinn.expressfortolling.api.TokenResponseDto;
import no.systema.jservices.tvinn.digitoll.v2.dao.MasterConsignment;
import no.systema.jservices.tvinn.digitoll.v2.dao.Transport;
import no.systema.jservices.tvinn.digitoll.v2.dto.ApiRequestIdDto;
import no.systema.jservices.tvinn.digitoll.v2.dto.SadmotfDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.ApiLrnDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.ApiMrnDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.ApiMrnStatusDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.ApiMrnStatusRecordDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.GenericDtoContainer;
import no.systema.jservices.tvinn.expressfortolling2.dto.GenericDtoResponse;
import no.systema.jservices.tvinn.expressfortolling2.dto.SadexmfDto;
import no.systema.jservices.tvinn.expressfortolling2.enums.EnumSadexhfStatus2;
import no.systema.jservices.tvinn.expressfortolling2.enums.EnumSadexhfStatus3;
import no.systema.jservices.tvinn.expressfortolling2.enums.EnumSadexmfStatus2;
import no.systema.jservices.tvinn.digitoll.v2.services.MapperMasterConsignment;
import no.systema.jservices.tvinn.digitoll.v2.services.MapperTransport;
import no.systema.jservices.tvinn.digitoll.v2.services.SadmotfService;
import no.systema.jservices.tvinn.expressfortolling2.services.SadexhfService;
import no.systema.jservices.tvinn.expressfortolling2.services.SadexmfService;
import no.systema.jservices.tvinn.expressfortolling2.util.GenericJsonStringPrinter;
import no.systema.jservices.tvinn.expressfortolling2.util.SadexlogLogger;
import no.systema.jservices.tvinn.expressfortolling2.util.ServerRoot;
import no.systema.main.util.ObjectMapperHalJson;
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
	private int THREAD_DELAY_FOR_GET_MRN_MILLICSECONDS = 6000;
	
	@Autowired
	private BridfDaoService bridfDaoService;	
	
	@Autowired
	private SadexmfService sadexmfService;	
	
	@Autowired
	private SadmotfService sadmotfService;	
	
	@Autowired
	private SadexhfService sadexhfService;	
	
	@Autowired
	private ApiServices apiServices; 
	
	@Autowired
	private SadexlogLogger sadexlogLogger;	
	
	
	/**
	 * Creates a new Transport through the API - POST
	 * The operation is only valid when the lrn(etuuid) and mrn(etmid) are empty at SADMOTF
	 * (1)If these fields are already in place your should use the PUT method OR 
	 * (2)erase the etuuid and etmid on db before using POST again
	 * 
	 * @param session
	 * @param user
	 * @param emavd
	 * @param empro
	 * @throws Exception
	 * 
	 * http://localhost:8080/syjservicestn-expft/digitollv2/postTransport.do?user=NN&emavd=1&empro=501941
	 */
	@RequestMapping(value="/digitollv2/postTransport.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse postTransportDigitollV2(HttpServletRequest request , @RequestParam(value = "user", required = true) String user, 
																				@RequestParam(value = "etavd", required = true) String etavd,
																				@RequestParam(value = "etpro", required = true) String etpro) throws Exception {
		
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericDtoResponse dtoResponse = new GenericDtoResponse();
		dtoResponse.setUser(user);
		dtoResponse.setAvd(etavd);
		dtoResponse.setPro(etpro);
		dtoResponse.setTdn("0"); //dummy (needed for db-log on table SADEXLOG)
		dtoResponse.setRequestMethodApi("POST");
		
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
				List<SadmotfDto> list = sadmotfService.getSadmotf(serverRoot, user, etavd, etpro);
				if(list != null) {
					logger.warn("list size:" + list.size());
					
					for (SadmotfDto dto: list) {
						//DEBUG
						logger.info(dto.toString());
						//Only valid when those lrn(emuuid) and mrn(emmid) are empty
						
						if(StringUtils.isEmpty(dto.getEtmid()) && StringUtils.isEmpty(dto.getEtuuid() )) {
							Transport transport =  new MapperTransport().mapTransport(dto);
							logger.warn("Carrier name:" + transport.getCarrier().getName());
							//Debug
							logger.debug(GenericJsonStringPrinter.debug(transport));
							//API
							
							
							String json = apiServices.postTransportDigitollV2(transport);
							//at this point we have a valid token for 2 minutes (120 sec). we use it for the MRN fetch via .../validation-status/...
							//otherwise we have been experienced problems with the maskinport-token per call. Could be hiccups ...
							if(this.apiServices.tokenLightDto != null) {
								logger.warn("######### DEBUG for tokenLightDto to use in GET--validation-status...:" + this.apiServices.tokenLightDto.getExpires_in());
							}
							ApiRequestIdDto obj = new ObjectMapper().readValue(json, ApiRequestIdDto.class);
							logger.warn("JSON = " + json);
							logger.warn("requestId = " + obj.getRequestId());
							break;
							//In case there was an error at end-point and the requestId was not returned
							/*if(StringUtils.isEmpty(obj.getRequestId())){
								errMsg.append("requestId empty ?? <json raw>: " + json);
								dtoResponse.setErrMsg(errMsg.toString());
								break;
							}else {
								//(1) we have the requestId at this point. We must go an API-round trip again to get the MRN
								String requestId = obj.getRequestId();
								dtoResponse.setRequestId(requestId);
								
								//Delay 10-seconds
								logger.warn("Start of delay: "+ new Date());
								Thread.sleep(this.THREAD_DELAY_FOR_GET_MRN_MILLICSECONDS); 
								logger.warn("End of delay: "+ new Date());
								
								//(2) get mrn from API
								//PROD-->
								String mrn = this.getMrnMasterFromApi(dtoResponse, requestId);
								if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())){
									errMsg.append(dtoResponse.getErrMsg());
									dtoResponse.setErrMsg("");
									dtoResponse.setDb_st2(EnumSadexmfStatus2.M.toString());
								}else {
									dtoResponse.setDb_st2(EnumSadexmfStatus2.C.toString());
								}
								
								//(3)now we have lrn and mrn and proceed with the SADEXMF-update at master consignment
								if(StringUtils.isNotEmpty(requestId) && StringUtils.isNotEmpty(mrn)) {
									String mode = "ULM";
									dtoResponse.setMrn(mrn);
									//we must update the send date as well. Only 8-numbers
									String sendDate = mc.getDocumentIssueDate().replaceAll("-", "").substring(0,8);
									
									List<SadexmfDto> xx = sadexmfService.updateLrnMrnSadexmf(serverRoot, user, dtoResponse, sendDate, mode);
									if(xx!=null && xx.size()>0) {
										for (SadexmfDto rec: xx) {
											if(StringUtils.isNotEmpty(rec.getEmmid()) ){
												//OK
											}else {
												errMsg.append("MRN empty after SADEXMF-update:" + mrn);
												dtoResponse.setErrMsg(errMsg.toString());
											}
										}
									}
									
								}else {
									errMsg.append("LRN and/or MRN empty ??: " + "-->requestId:" + requestId + " -->MRN from API (look at logback-logs): " + mrn);
									dtoResponse.setErrMsg(errMsg.toString());
									break;
								}
							
							}
							break; //only first in list
							
							*/
							
							
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
		}
		
		/*
		//log in db before std-output
		sadexlogLogger.doLog(serverRoot, user, dtoResponse);
		//log in log file
		if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())) { logger.error(dtoResponse.getErrMsg()); }
		*/
		//std output (browser)
		return dtoResponse;
	}
	
	/**
	 * 
	 * Updates an existing Master Consignment through the API - PUT. Requires an existing MRN (emmid at SADEXMF)
	 * @param request
	 * @param user
	 * @param emavd
	 * @param empro
	 * @return
	 * @throws Exception
	 * 
	 * http://localhost:8080/syjservicestn-expft/digitollv2/putTransport?user=NN&emavd=1&empro=501941&mrn=XXX
	 */
	@RequestMapping(value="/digitollv2/putTransport.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse putMasterConsignmentDigitollV2(HttpServletRequest request , @RequestParam(value = "user", required = true) String user, 
																				@RequestParam(value = "emavd", required = true) String emavd,
																				@RequestParam(value = "empro", required = true) String empro,
																				@RequestParam(value = "mrn", required = true) String mrn ) throws Exception {
		
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericDtoResponse dtoResponse = new GenericDtoResponse();
		dtoResponse.setUser(user);
		dtoResponse.setAvd(emavd);
		dtoResponse.setPro(empro);
		dtoResponse.setTdn("0"); //dummy (needed for db-log on table SADEXLOG)
		dtoResponse.setMrn(mrn);
		dtoResponse.setRequestMethodApi("PUT");
		
		StringBuilder errMsg = new StringBuilder("ERROR ");
		String methodName = new Object() {}
	      .getClass()
	      .getEnclosingMethod()
	      .getName();
		
		logger.warn("Inside " + methodName + "- MRNnr: " + mrn );
		
		//create new - master consignment at toll.no
		try {
			if(checkUser(user)) {
				logger.warn("user OK:" + user);
				List<SadexmfDto> list = sadexmfService.getSadexmfForUpdate(serverRoot, user, emavd, empro, mrn);
				
				if(list != null && list.size()>0) {
					logger.warn("list size:" + list.size());
					
					for (SadexmfDto dto: list) {
						logger.warn(dto.toString());
						//Only valid when those lrn(emuuid) and mrn(emmid) are NOT empty
						if(StringUtils.isNotEmpty(dto.getEmmid()) && StringUtils.isNotEmpty(dto.getEmuuid() )) {
							MasterConsignment mc =  new MapperMasterConsignment().mapMasterConsignment(dto);
							logger.warn("GrossMass:" + mc.getConsignmentMasterLevel().getGrossMass());
							//API - PROD
							
							/* TODO 
							
							String json = apiServices.putMasterConsignmentExpressMovementRoad(mc, mrn);
							ApiLrnDto obj = new ObjectMapper().readValue(json, ApiLrnDto.class);
							logger.warn("JSON = " + json);
							logger.warn("LRN = " + obj.getLrn());
							
							//put in response
							dtoResponse.setLrn(obj.getLrn());
							dtoResponse.setAvd(String.valueOf(dto.getEmavd()));
							dtoResponse.setPro(String.valueOf(dto.getEmpro()));
							
							//In case there was an error at end-point and the LRN was not returned
							if(StringUtils.isEmpty(obj.getLrn())){
								errMsg.append("LRN empty ?? <json raw>: " + json);
								dtoResponse.setErrMsg(errMsg.toString());
								break;
								
							}else {
								//(1) we have the lrn at this point. We must go an API-round trip again to get the MRN
								String lrn = obj.getLrn();
								dtoResponse.setLrn(lrn);
								
								//(2)now we have the new lrn for the updated mrn so we proceed with the SADEXMF-update-lrn at master consignment
								if(StringUtils.isNotEmpty(lrn) && StringUtils.isNotEmpty(mrn)) {
									String mode = "UL";
									dtoResponse.setMrn(mrn);
									//we must update the send date as well. Only 8-numbers
									String sendDate = mc.getDocumentIssueDate().replaceAll("-", "").substring(0,8);
									
									List<SadexmfDto> xx = sadexmfService.updateLrnMrnSadexmf(serverRoot, user, dtoResponse, sendDate, mode);
									if(xx!=null && xx.size()>0) {
										for (SadexmfDto rec: xx) {
											//logger.warn(rec.toString());
											if(StringUtils.isNotEmpty(rec.getEmmid()) ){
												//OK
											}else {
												errMsg.append("MRN empty after SADEXMF-update:" + mrn);
												dtoResponse.setErrMsg(errMsg.toString());
											}
										}
									}
									//(3) now we make a final check for LRN-status since there might have being some validation errors with the newly acquired LRN that did not appear when we 
									//first received the LRN in the first PUT Master
									
									//Delay 10-seconds (as in POST) needed to avoid ERROR 404 on client ...
									logger.warn("Start of delay: "+ new Date());
									Thread.sleep(this.THREAD_DELAY_FOR_GET_MRN_MILLICSECONDS); 
									logger.warn("End of delay: "+ new Date());
									
									this.checkLrnValidationStatus(dtoResponse, lrn);
									if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())){
										logger.warn("ERROR: " + dtoResponse.getErrMsg()  + methodName);
										//Update ehst2(SADEXMF) with ERROR = M
										dtoResponse.setDb_st2(EnumSadexmfStatus2.M.toString());
										List<SadexmfDto> tmp = sadexmfService.updateLrnMrnSadexmf(serverRoot, user, dtoResponse, sendDate, mode);
									}else {
										//OK
										logger.warn("LRN status is OK ... (no errors)");
										dtoResponse.setDb_st2(EnumSadexmfStatus2.C.toString());
										List<SadexmfDto> tmp = sadexmfService.updateLrnMrnSadexmf(serverRoot, user, dtoResponse, sendDate, mode);
									}
									
								}else {
									errMsg.append("LRN empty after PUT ??: " + "-->LRN:" + lrn + " -->MRN from db(SADEXMF): " + mrn);
									dtoResponse.setErrMsg(errMsg.toString());
									break;
								}
							 
							  
							}
							break; //only first in list
							*/
							
						}else {
							errMsg.append(" LRN/MRN are empty. This operation is invalid. Make sure this fields have values before any PUT ");
							dtoResponse.setErrMsg(errMsg.toString());
						}
						
					}
				}else {
					errMsg.append(" no records to fetch from SADEXMF ");
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
		}
		
		//log in db before std-output
		sadexlogLogger.doLog(serverRoot, user, dtoResponse);
		//log in log file
		if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())) { logger.error(dtoResponse.getErrMsg()); }
		
		//std output (browser)
		return dtoResponse;
	}
	/**
	 * Delete MasterConsignment through the API - DELETE
	 * @param request
	 * @param user
	 * @param mrn
	 * @return
	 * @throws Exception
	 * 
	 * http://localhost:8080/syjservicestn-expft/digitollv2/deleteTransport?user=NN&mrn=XXX
	 */
	@RequestMapping(value="/digitollv2/deleteTransport.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse deleteMasterConsignmentDigitollV2(HttpServletRequest request , @RequestParam(value = "user", required = true) String user,
																				@RequestParam(value = "emavd", required = true) String emavd,
																				@RequestParam(value = "empro", required = true) String empro,
																				@RequestParam(value = "mrn", required = true) String mrn) throws Exception {
		
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericDtoResponse dtoResponse = new GenericDtoResponse();
		dtoResponse.setUser(user);
		dtoResponse.setAvd(emavd);
		dtoResponse.setPro(empro);
		dtoResponse.setTdn("0");
		dtoResponse.setMrn(mrn);
		dtoResponse.setRequestMethodApi("DELETE");
		
		StringBuilder errMsg = new StringBuilder("ERROR ");
		String methodName = new Object() {}
	      .getClass()
	      .getEnclosingMethod()
	      .getName();
		
		logger.warn("Inside " + methodName + "- MRNnr: " + mrn );

		try {
			if(checkUser(user)) {
				logger.warn("user OK:" + user);
				List<SadexmfDto> list = sadexmfService.getSadexmfForUpdate(serverRoot, user, emavd, empro,  mrn);
				
				if(list != null && list.size()>0) {
					logger.warn("list size:" + list.size());
					
					
					for (SadexmfDto sadexmfDto: list) {
						//Only valid when those requestId(emuuid) and mrn(emmid) are NOT empty
						if(StringUtils.isNotEmpty(sadexmfDto.getEmmid()) && StringUtils.isNotEmpty(sadexmfDto.getEmuuid() )) {
							MasterConsignment mc =  new MapperMasterConsignment().mapMasterConsignmentForDelete();
							//API
							
							/*
							String json = apiServices.deleteMasterConsignmentExpressMovementRoad(mc, mrn);
							ApiLrnDto obj = new ObjectMapper().readValue(json, ApiLrnDto.class);
							logger.warn("JSON = " + json);
							logger.warn("LRN = " + obj.getLrn());
							//put in response
							dtoResponse.setLrn(obj.getLrn());
							dtoResponse.setAvd(String.valueOf(sadexmfDto.getEmavd()));
							dtoResponse.setPro(String.valueOf(sadexmfDto.getEmpro()));
							//In case there was an error at end-point and the LRN was not returned
							if(StringUtils.isEmpty(obj.getLrn())){
								errMsg.append("LRN empty ?? <json raw>: " + json);
								dtoResponse.setErrMsg(errMsg.toString());
								break;
								
							}else {
								//(1) we have the lrn at this point. We must go an API-round trip again to get the MRN
								String lrn = obj.getLrn();
								dtoResponse.setLrn(lrn);
								
								//(2)now we have the new lrn for the updated mrn so we proceed with the SADEXMF-update-lrn at master consignment
								if(StringUtils.isNotEmpty(lrn) && StringUtils.isNotEmpty(mrn)) {
									String mode = "DL";
									dtoResponse.setMrn(mrn);
									dtoResponse.setDb_st2(EnumSadexmfStatus2.D.toString());
									//we must update the send date as well. Only 8-numbers
									String sendDate = mc.getDocumentIssueDate().replaceAll("-", "").substring(0,8);
									
									List<SadexmfDto> xx = sadexmfService.updateLrnMrnSadexmf(serverRoot, user, dtoResponse, sendDate, mode);
									if(xx!=null && xx.size()>0) {
										for (SadexmfDto rec: xx) {
											if(StringUtils.isEmpty(rec.getEmmid()) ){
												//OK
											}else {
												errMsg.append("MRN has not been removed after SADEXMF-delete-light mrn:" + mrn);
												dtoResponse.setErrMsg(errMsg.toString());
											}
										}
									}
									
								}else {
									errMsg.append("LRN empty after DELETE-LIGHT ??: " + "-->LRN:" + lrn + " -->MRN from db(SADEXMF): " + mrn);
									dtoResponse.setErrMsg(errMsg.toString());
									break;
								}
							
							}
							
							break; //only first in list
							*/
							
						}else {
							errMsg.append(" LRN/MRN are empty (SADEXMF). This operation is invalid. Make sure emuuid(lrn)/emmid(mrn) fields have values before any DELETE ");
							dtoResponse.setErrMsg(errMsg.toString());
						}
						
					} 
				}else {
					errMsg.append(" no records to fetch from SADEXMF ");
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
		}
		
		//log in db before std-output
		sadexlogLogger.doLog(serverRoot, user, dtoResponse);
		//log in log file
		if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())) { logger.error(dtoResponse.getErrMsg()); }
		
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
	 */
	@RequestMapping(value="/digitollv2/getTransport.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse getTransportDigitollV2(HttpServletRequest request , @RequestParam(value = "user", required = true) String user,
																				@RequestParam(value = "lrn", required = true) String lrn) throws Exception {
		
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
		
		logger.warn("Inside " + methodName + "- LRNnr: " + lrn );
		
		try {
			if(checkUser(user)) {
					//(2)now we have the new lrn for the updated mrn so we proceed with the SADEXMF-update-lrn at master consignment
					if(StringUtils.isNotEmpty(lrn)) {
						dtoResponse.setLrn(lrn);
						
						String mrn = this.getMrnTransportDigitollV2FromApi(dtoResponse, lrn);
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
	 * Gets Master Consignment status through the API - GET - without having to check our db 
	 * This method returns all documenNumbers that Toll.no has after having sent these HOUSES
	 * 
	 * @Example http://localhost:8080/syjservicestn-expft/digitollv2/getStatusTransport.do?user=NN&emavd=1&empro=500086&mrn=22NOM6O19GRP8UQBT6
	 * @param request
	 * @param user
	 * @param mrn
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/digitollv2/getStatusTransport.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse getStatusMasterConsignmentDigitollV2(HttpServletRequest request , @RequestParam(value = "user", required = true) String user, 
																				@RequestParam(value = "emavd", required = true) String emavd,
																				@RequestParam(value = "empro", required = true) String empro,	
																				@RequestParam(value = "mrn", required = true) String mrn ) throws Exception {
		
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericDtoResponse dtoResponse = new GenericDtoResponse();
		dtoResponse.setUser(user);
		dtoResponse.setAvd(emavd); 
		dtoResponse.setPro(empro);
		dtoResponse.setTdn("0"); //dummy
		dtoResponse.setMrn(mrn);
		dtoResponse.setRequestMethodApi("GET all documentNumbers in MASTER-level at toll.no");
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
				String json = apiServices.getMrnStatusMasterConsignmentExpressMovementRoad(mrn);
				logger.warn("JSON = " + json);
				if(StringUtils.isNotEmpty(json)) {
					ApiMrnStatusRecordDto[] obj = new ObjectMapper().readValue(json, ApiMrnStatusRecordDto[].class);
					if(obj!=null) {
						List<Object> list = Arrays.asList(obj);
						logger.warn("List = " + list);
						
						//Check for OK or Error in order to proceed
						if(list!=null && !list.isEmpty()){
							dtoResponse.setList(list);
							
							//(1)Proceed with every documentNumber and match with its respective house
							//This stage is necessary only to change a house status3 on wether it exist in Master at toll.no or not
							for (Object record: list) {
								//(2)Update now the status-3 (SADEXHF.ehst3) on the valid house-documentNumber (SADEXHF.ehdkh)
								ApiMrnStatusRecordDto apiDto = (ApiMrnStatusRecordDto)record;
								String MODE_STATUS3 = "US3";
								if(apiDto.getReceived()) {
									dtoResponse.setDb_st3(EnumSadexhfStatus3.T.toString());
								}else {
									dtoResponse.setDb_st3(EnumSadexhfStatus3.F.toString());
								}
								logger.warn("documentNumber:" + apiDto.getDocumentNumber());
								logger.warn("status3:" + dtoResponse.getDb_st3());
								//TODO or OBSOLETE talk with CHANG regarding status3 ...
								//sadexhfService.updateStatus3Sadexhf(serverRoot, user, apiDto.getDocumentNumber(), dtoResponse.getDb_st3(), MODE_STATUS3);
								
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
	/**
	 * Gets Master Consignment status through the API - GET - in order to get an MRN
	 * This method is used for the update of an MRN in SADEXMF. The need for doing so is based upon the fact that toll.no
	 * has an asynchronous routine with every POST that returns sometimes an empty MRN as soon as the LRN has been produced.
	 * This will trigger a defect post in our db since the LRN without an MRN will be wrong if the POST was OK.
	 * To correct the above this method will be used at some point in the GUI in order to prevent a user-POST and instead prompt a PUT (update at toll.no instead of a create new)
	 * 
	 * @param request
	 * @param user
	 * @param lrn
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/digitollv2/setMrnTransport.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse setMrnMasterConsignmentDigitollV2(HttpServletRequest request , @RequestParam(value = "user", required = true) String user,
																				@RequestParam(value = "lrn", required = true) String lrn) throws Exception {
		
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
		
		logger.warn("Inside " + methodName + "- LRNnr: " + lrn );
		
		try {
			if(checkUser(user)) {
					//(1)now we have the new lrn for the updated mrn so we proceed with the SADEXMF-update-lrn at master consignment
					if(StringUtils.isNotEmpty(lrn)) {
						dtoResponse.setLrn(lrn);
						
						String mrn = this.getMrnMasterFromApi(dtoResponse, lrn);
						if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())){
							errMsg.append(dtoResponse.getErrMsg());
							
							if(StringUtils.isNotEmpty(mrn)) {
								dtoResponse.setErrMsg("");
							}else {
								dtoResponse.setErrMsg(errMsg.toString());
							}
						}else {
							dtoResponse.setMrn(mrn);
							//(2) get the record to update
							List<SadexmfDto> list = sadexmfService.getSadexmfForUpdate(serverRoot, user, lrn);
							if(list != null) {
								logger.warn("list size:" + list.size());
								
								for (SadexmfDto dto: list) {
									String mode = "ULM";
									logger.info("empro:" + dto.getEmpro());
									//Update emst2(SADEXMF) with OK = C
									dtoResponse.setAvd(String.valueOf(dto.getEmavd()));
									dtoResponse.setPro(String.valueOf(dto.getEmpro()));
									dtoResponse.setDb_st(dto.getEmst());
									dtoResponse.setDb_st2(EnumSadexhfStatus2.C.toString());
									dtoResponse.setDb_st3(dto.getEmst3());
									String sendDate = String.valueOf(dto.getEmdtin());
									//(3)now we have lrn and mrn. Proceed with the SADEXMF-update at master consignment
									List<SadexmfDto> xx = sadexmfService.updateLrnMrnSadexmf(serverRoot, user, dtoResponse, sendDate, mode);
									if(xx!=null && xx.size()>0) {
										for (SadexmfDto rec: xx) {
											if(StringUtils.isNotEmpty(rec.getEmmid()) ){
												//OK
											}else {
												errMsg.append("MRN empty after SADEXMF-update:" + mrn);
												dtoResponse.setErrMsg(errMsg.toString());
											}
										}
									}
								}
							}
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
	
	/**
	 *  
	 * @param tokenLightDto
	 * @param dtoResponse
	 * @param lrn
	 * @return
	 */
	private String getMrnTransportFromApi( TokenResponseDto tokenLightDto, GenericDtoResponse dtoResponse, String lrn) {
		
		String retval = "";
		
		try{
			
			String json = apiServices.getValidationStatusTransportDigitollV2(tokenLightDto, lrn);
			
			ApiMrnDto obj = new ObjectMapper().readValue(json, ApiMrnDto.class);
			logger.warn("JSON = " + json);
			logger.warn("status:" + obj.getStatus());
			logger.warn("MRN = " + obj.getMasterReferenceNumber());
			dtoResponse.setStatusApi(obj.getStatus());
			dtoResponse.setTimestamp(obj.getNotificationDate());
			
			if(StringUtils.isNotEmpty(obj.getMasterReferenceNumber())) {
				retval = obj.getMasterReferenceNumber();
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
	 * @return
	 */
	private String getMrnTransportDigitollV2FromApi( GenericDtoResponse dtoResponse, String lrn) {
		
		String retval = "";
		
		try{
			
			String json = apiServices.getValidationStatusTransportDigitollV2(lrn);
			
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
	 * @return
	 */
	private String getMrnMasterFromApi(GenericDtoResponse dtoResponse, String lrn) {
		
		String retval = "";
		
		try{
			
			String json = apiServices.getValidationStatusMasterConsignmentExpressMovementRoad(lrn);
			ApiMrnDto obj = new ObjectMapper().readValue(json, ApiMrnDto.class);
			logger.warn("JSON = " + json);
			logger.warn("status:" + obj.getStatus());
			logger.warn("MRN = " + obj.getMasterReferenceNumber());
			dtoResponse.setStatusApi(obj.getStatus());
			dtoResponse.setTimestamp(obj.getNotificationDate());
			
			if(StringUtils.isNotEmpty(obj.getMasterReferenceNumber())) {
				retval = obj.getMasterReferenceNumber();
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
	
private String checkLrnValidationStatus(GenericDtoResponse dtoResponse, String lrn) {
		
		String retval = "";
		
		try{
			
			String json = apiServices.getValidationStatusMasterConsignmentExpressMovementRoad(lrn);
			ApiMrnDto obj = new ObjectMapper().readValue(json, ApiMrnDto.class);
			logger.warn("JSON = " + json);
			logger.warn("Status = " + obj.getStatus());
			logger.warn("localRefNumber = " + obj.getLocalReferenceNumber());
			logger.warn("notificationDate = " + obj.getNotificationDate());
			logger.warn("validationErrorList = " + obj.getValidationErrorList().toString());
			logger.warn("validationErrorList.length = " + obj.getValidationErrorList().length);
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
