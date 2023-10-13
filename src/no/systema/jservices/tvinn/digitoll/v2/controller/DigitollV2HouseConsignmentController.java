package no.systema.jservices.tvinn.digitoll.v2.controller;

import java.io.PrintWriter;



import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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
import com.google.gson.JsonParser;

import no.systema.jservices.common.dao.services.BridfDaoService;
import no.systema.jservices.tvinn.expressfortolling.api.ApiServices;
import no.systema.jservices.tvinn.expressfortolling.api.ApiServicesAir;
import no.systema.jservices.tvinn.digitoll.v2.dao.HouseConsignment;
import no.systema.jservices.tvinn.digitoll.v2.dto.ApiRequestIdDto;
import no.systema.jservices.tvinn.digitoll.v2.dto.EntryDto;
import no.systema.jservices.tvinn.digitoll.v2.dto.SadmohfDto;
import no.systema.jservices.tvinn.digitoll.v2.dto.SadmotfDto;
import no.systema.jservices.tvinn.digitoll.v2.enums.EnumSadmohfStatus2;
import no.systema.jservices.tvinn.digitoll.v2.enums.EnumSadmomfStatus2;
import no.systema.jservices.tvinn.digitoll.v2.enums.EnumSadmotfStatus2;
import no.systema.jservices.tvinn.expressfortolling2.dto.ApiMrnDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.GenericDtoResponse;
import no.systema.jservices.tvinn.digitoll.v2.services.MapperHouseConsignment;
import no.systema.jservices.tvinn.digitoll.v2.services.SadmohfService;
import no.systema.jservices.tvinn.digitoll.v2.services.SadmoifService;
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
 * 
 * ===================================================================
 * Flow description with respect to the API and the SADMOHF db table
 * ===================================================================
 * 	===============================
 * 	(A) Get MRN Digitoll - POST (API)
 *	=============================== 
 *	(1) Först POST (krav: ehuuid & etmid EMPTY)
 *	Systema AS
 *	toll-token expires_in:120 seconds
 *	/movement/road/v2/house-consignment
 *	JSON = {"requestId":"1bdf0f33-f42e-48e2-b334-3944722e3fe5"}
 *	requestId (old LRN) = 1bdf0f33-f42e-48e2-b334-3944722e3fe5
	
 *	(2) GET /house-consignment/validation-status/{requestId}
 *	Mrn and status on response. Update SADMOHF with requestId=ehuuid and mrn=ehmid  
	
 *	===========================
 * 	(B) Update Mrn - PUT (API)
 *	===========================
 *	(1) PUT /house-consignment/{masterReferenceNumber}
 *	Response returns new requestId which must be updated in SADMOHF. Only requestId update 
 *	IMPORTANT! -->Last requestId received is the one valid for GET validation-status 
 *	
 * 	
 *	=============================
 *	(C) Delete Mrn - DELETE (API)
 *	=============================
 *	(1) DELETE /house-consignment/{masterReferenceNumber}
 *	Response returns new requestId which we don't save.
 *	(2) Ehuuid and Ehmid (SADMOHF) are blanked. The record is not deleted in db but is ready for new POST
 *	This item (2) could change by deleting the record totally ...
 *
 *  NOTE! 
 *	(D) To see the status of a given MRN att any given moment use the end-point --> getTransport.do in this Controller
 * 
 * @author oscardelatorre
 * @date Aug 2023
 *
 *
 *
 *
 */
@RestController
public class DigitollV2HouseConsignmentController {
	private static Logger logger = LoggerFactory.getLogger(DigitollV2HouseConsignmentController.class.getName());
	// pretty print
	private static ObjectMapper prettyErrorObjectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	private JsonParser prettyJsonParser = new JsonParser();
	private Gson prettyGsonObject = new GsonBuilder().setPrettyPrinting().create();
	private final String LOG_PREFIX_LEGEND = "Logged on SADMOLOG >> ";
	
	@Autowired
	private BridfDaoService bridfDaoService;	
	
	@Autowired
	private SadmotfService sadmotfService;
	@Autowired
	private SadmomfService sadmomfService;
	@Autowired
	private SadmohfService sadmohfService;
	@Autowired
	private SadmoifService sadmoifService;	
	
	@Autowired
	private ApiServices apiServices; 
	@Autowired
	private ApiServicesAir apiServicesAir; 
	
	@Autowired
	private SadmologLogger sadmologLogger;	
	
	@RequestMapping(value="/digitollv2/jsonTest.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse jsonTest(HttpServletRequest request ) throws Exception {
		
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericDtoResponse dtoResponse = new GenericDtoResponse();
		dtoResponse.setErrMsg("myERROR in jsonTest !!!...");
		return dtoResponse;
	}
	
	/**
	 * Creates a new House Consignment through the API - POST
	 * The operation is only valid when the requestId(ehuuid) and mrn(ehmid) are empty at SADMOHF
	 * (1)If these fields are already in place your should use the PUT method OR 
	 * (2)erase the ehuuid and ehmid on db before using POST again
	 * 
	 * @param request
	 * @param user
	 * @param ehlnrt
	 * @param ehlnrm
	 * @param ehlnrh
	 * @return
	 * @throws Exception
	 * 
	 * http://localhost:8080/syjservicestn-expft/digitollv2/postHouseConsignment.do?user=NN&ehlnrt=1&ehlnrm=2&ehlnrh=3
	 * 
	 * test - OK
	 * 
	 */
	@RequestMapping(value="/digitollv2/postHouseConsignment.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse postHouseConsignmentDigitollV2(HttpServletRequest request , @RequestParam(value = "user", required = true) String user, 
																				@RequestParam(value = "ehlnrt", required = true) String ehlnrt,
																				@RequestParam(value = "ehlnrm", required = true) String ehlnrm,
																				@RequestParam(value = "ehlnrh", required = true) String ehlnrh) throws Exception {
		
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericDtoResponse dtoResponse = new GenericDtoResponse();
		dtoResponse.setUser(user);
		dtoResponse.setEhlnrt(ehlnrt);
		dtoResponse.setEllnrt(Integer.valueOf(ehlnrt));//for log purposes only
		dtoResponse.setEhlnrm(ehlnrm);
		dtoResponse.setEllnrm(Integer.valueOf(ehlnrm));//for log purposes only
		dtoResponse.setEhlnrh(ehlnrh);
		dtoResponse.setEllnrh(Integer.valueOf(ehlnrh));//for log purposes only
		dtoResponse.setRequestMethodApi("POST");
		boolean apiStatusAlreadyUpdated = false;
		boolean isApiAir = false;
		StringBuilder errMsg = new StringBuilder("ERROR ");
		
		String methodName = new Object() {}
	      .getClass()
	      .getEnclosingMethod()
	      .getName();
		
		logger.warn("Inside " + methodName );
		//create new - house consignment at toll.no
		try {
			if(checkUser(user)) {
				logger.warn("user OK:" + user);
				
				List<SadmohfDto> list = sadmohfService.getSadmohf(serverRoot, user, ehlnrt, ehlnrm, ehlnrh);
				if(list != null) {
					logger.warn("list size:" + list.size());
					
					for (SadmohfDto dto: list) {
						//DEBUG
						//logger.debug(dto.toString());
						
						//Get the transportDto and masterDto - level since some fields might be required in the mapping
						dto.setTransportDto(this.sadmotfService.getSadmotfDto(serverRoot, user, ehlnrt));
						dto.setMasterDto(this.sadmomfService.getSadmomfDto(serverRoot, user, ehlnrt, ehlnrm)) ;
						dto.setGoodsItemList(this.sadmoifService.getSadmoif(serverRoot, user, ehlnrt, ehlnrm, ehlnrh));
						
						
						//Only valid when those lrn(emuuid) and mrn(emmid) are empty
						//if(StringUtils.isEmpty(dto.getEhmid()) && StringUtils.isEmpty(dto.getEhuuid() )) {
						if(StringUtils.isEmpty(dto.getEhmid()) ) {
							HouseConsignment hc =  new MapperHouseConsignment().mapHouseConsignment(dto);
							logger.warn("DocumentIssueDate:" + hc.getDocumentIssueDate());
							//Debug
							//logger.debug(GenericJsonStringPrinter.debug(hc));
							//API
							
							Map tollTokenMap = new HashMap();
							//Check if we are using MO-Air and not road...
							if(ApiAirRecognizer.isAir(dto.getTransportDto().getEtktyp()))  { isApiAir = true; }
							String json = "";
							if(isApiAir) {
								json = apiServicesAir.postHouseConsignmentDigitollV2(hc, tollTokenMap);
							}else {
								json = apiServices.postHouseConsignmentDigitollV2(hc, tollTokenMap);
							}
							//At this point we now have a valid tollToken to use
							
							ApiRequestIdDto obj = new ObjectMapper().readValue(json, ApiRequestIdDto.class);
							logger.warn("JSON = " + json);
							logger.warn("requestId = " + obj.getRequestId());
							dtoResponse.setAvd(String.valueOf(dto.getEhavd()));
							dtoResponse.setPro(String.valueOf(dto.getEhpro()));
							
							//In case there was an error at end-point and the requestId was not returned
							if(StringUtils.isEmpty(obj.getRequestId())){
								errMsg.append("requestId empty ?? <json raw>: " + json);
								dtoResponse.setErrMsg(errMsg.toString());
								break;
							}else {
								//(1) we have the requestId at this point. We must go an API-round trip again to get the MRN
								String requestId = obj.getRequestId();
								dtoResponse.setRequestId(requestId);
								
								//Delay 6-10 seconds
								logger.warn(PrettyLoggerOutputer.FRAME);
								logger.warn("START of delay: "+ new Date());
								Thread.sleep(SadDigitollConstants.THREAD_DELAY_FOR_GET_MRN_MILLICSECONDS); 
								logger.warn("END of delay: "+ new Date());
								logger.warn(PrettyLoggerOutputer.FRAME);
								
								//(2) get mrn from API
								//PROD-->
								logger.info("********************:" + dtoResponse.getEhlnrt());logger.info(dtoResponse.getEhlnrm());logger.info(dtoResponse.getEhlnrh());
								
								String mrn = this.getMrnHouseConsignmentDigitollV2FromApi(dtoResponse, requestId, tollTokenMap, isApiAir);
								if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())){
									errMsg.append(dtoResponse.getErrMsg());
									dtoResponse.setErrMsg("");
									dtoResponse.setDb_st2(EnumSadmohfStatus2.M.toString());
								}else {
									dtoResponse.setDb_st2(EnumSadmohfStatus2.S.toString());
								}
								
								
								//(3)now we have lrn and mrn and proceed with the SADMOMF-update at master consignment
								if(StringUtils.isNotEmpty(requestId) && StringUtils.isNotEmpty(mrn)) {
									String mode = "ULM";
									dtoResponse.setMrn(mrn);
									//we must update the send date as well. Only 8-numbers
									String sendDate = hc.getDocumentIssueDate().replaceAll("-", "").substring(0,8);
									dtoResponse.setDocumentIssueDate(sendDate);//as aux for logging purposes
									
									List<SadmohfDto> xx = sadmohfService.updateLrnMrnSadmohf(serverRoot, user, dtoResponse, sendDate, mode);
									if(xx!=null && xx.size()>0) {
										for (SadmohfDto rec: xx) {
											if(StringUtils.isNotEmpty(rec.getEhmid()) ){
												//OK
												apiStatusAlreadyUpdated = true;
												//set MRN-BUP (only once and only here)
												GenericDtoResponse dtoResponseBup = dtoResponse;
												sadmohfService.setMrnBupSadmohf(serverRoot, user, dtoResponseBup);
											}else {
												errMsg.append("MRN empty after SADMOHF-update:" + mrn);
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
				errMsg.append(" invalid user ");
				dtoResponse.setErrMsg(errMsg.toString());
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			//Get out stackTrace to the response (errMsg)
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			dtoResponse.setErrMsg(sw.toString());
		
		}finally {
			
			if(!apiStatusAlreadyUpdated) {
				//check on status
				if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())) {
					logger.info(LOG_PREFIX_LEGEND + dtoResponse.getErrMsg());
					dtoResponse.setDb_st2(EnumSadmohfStatus2.M.toString());
					logger.info("INSIDE setStatus:" + dtoResponse.getDb_st2());
					//
					
					List<SadmohfDto> xx = sadmohfService.updateLrnMrnSadmohf(serverRoot, user, dtoResponse, dtoResponse.getDocumentIssueDate(), "ULM");
					logger.info("After update on status 2 (finally-clause)");
				}
			}
			//log in log file
			sadmologLogger.doLog(serverRoot, user, dtoResponse);
		}
		//std output (browser)
		return dtoResponse;
	}
	
	/**
	 * 
	 * @param request
	 * @param user
	 * @param ehlnrt
	 * @param ehlnrm
	 * @param ehlnrh
	 * @param mrn
	 * @return
	 * @throws Exception
	 * 
	 * http://localhost:8080/syjservicestn-expft/digitollv2/putHouseConsignment.do?user=NN&ehlnrt=1&ehlnrm=501941&ehlnrh=38&mrn=XXX
	 * 
	 * test - OK
	 * 
	 */
	@RequestMapping(value="/digitollv2/putHouseConsignment.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse putHouseConsignmentDigitollV2(HttpServletRequest request , @RequestParam(value = "user", required = true) String user, 
																				@RequestParam(value = "ehlnrt", required = true) String ehlnrt,
																				@RequestParam(value = "ehlnrm", required = true) String ehlnrm,
																				@RequestParam(value = "ehlnrh", required = true) String ehlnrh,
																				@RequestParam(value = "mrn", required = true) String mrn ) throws Exception {
		
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericDtoResponse dtoResponse = new GenericDtoResponse();
		dtoResponse.setUser(user);
		dtoResponse.setEhlnrt(ehlnrt);
		dtoResponse.setEllnrt(Integer.valueOf(ehlnrt));//for log purposes only
		dtoResponse.setEhlnrm(ehlnrm);
		dtoResponse.setEllnrm(Integer.valueOf(ehlnrm));//for log purposes only
		dtoResponse.setEhlnrh(ehlnrh);
		dtoResponse.setEllnrh(Integer.valueOf(ehlnrh));//for log purposes only
		dtoResponse.setMrn(mrn);
		dtoResponse.setRequestMethodApi("PUT");
		boolean apiStatusAlreadyUpdated = false;
		boolean isApiAir = false;
		
		StringBuilder errMsg = new StringBuilder("ERROR ");
		
		String methodName = new Object() {}
	      .getClass()
	      .getEnclosingMethod()
	      .getName();
		
		
		logger.warn("Inside " + methodName + " - MRNnr: " + mrn);
		logger.warn("serverRoot:" + serverRoot);
		
		//create new - master consignment at toll.no
		try {
			if(checkUser(user)) {
				logger.warn("user OK:" + user);
				List<SadmohfDto> list = sadmohfService.getSadmohfForUpdate(serverRoot, user, ehlnrt, ehlnrm, ehlnrh, mrn );
				
				if(list != null && list.size()>0) {
					logger.warn("SADMOHF list size:" + list.size());
					
					for (SadmohfDto dto: list) {
						logger.info(dto.toString());
						
						//Get the transportDto and masterDto - level since some fields might be required in the mapping
						dto.setTransportDto(this.sadmotfService.getSadmotfDto(serverRoot, user, ehlnrt));
						dto.setMasterDto(this.sadmomfService.getSadmomfDto(serverRoot, user, ehlnrt, ehlnrm)) ;
						dto.setGoodsItemList(this.sadmoifService.getSadmoif(serverRoot, user, ehlnrt, ehlnrm, ehlnrh));
						
						//Only valid when mrn(emmid) is NOT empty
						if(StringUtils.isNotEmpty(dto.getEhmid()) ) {
							HouseConsignment hc = new MapperHouseConsignment().mapHouseConsignment(dto);
							logger.warn("totalGrossMass:" + hc.getHouseConsignmentConsignmentHouseLevel().getTotalGrossMass());
							//Debug
							//logger.debug(GenericJsonStringPrinter.debug(hc));
							//init response in case en ERROR occurs after apiSerivices...
							dtoResponse.setRequestId(dto.getEhuuid());
							
							//API - PROD
							Map tollTokenMap = new HashMap();
							//API
							if(ApiAirRecognizer.isAir(dto.getTransportDto().getEtktyp())) { isApiAir = true; }
							String json = "";
							if(isApiAir) {
								json = apiServicesAir.putHouseConsignmentDigitollV2(hc, mrn, tollTokenMap);
							}else {
								json = apiServices.putHouseConsignmentDigitollV2(hc, mrn, tollTokenMap);
							}
							
							ApiRequestIdDto obj = new ObjectMapper().readValue(json, ApiRequestIdDto.class);
							logger.warn("JSON = " + json);
							logger.warn("requestId = " + obj.getRequestId());
							
							//put in response
							dtoResponse.setRequestId(obj.getRequestId());
							dtoResponse.setEhlnrt(String.valueOf(dto.getEhlnrt()));
							dtoResponse.setEhlnrm(String.valueOf(dto.getEhlnrm()));
							dtoResponse.setEhlnrh(String.valueOf(dto.getEhlnrh()));
							dtoResponse.setAvd(String.valueOf(dto.getEhavd()));
							dtoResponse.setPro(String.valueOf(dto.getEhpro()));
							dtoResponse.setTdn(String.valueOf(dto.getEhtdn()));
							//In case there was an error at end-point and the LRN was not returned
							if(StringUtils.isEmpty(obj.getRequestId())){
								errMsg.append("requestId empty ?? <json raw>: " + json);
								errMsg.append("-->" + methodName);
								dtoResponse.setErrMsg(errMsg.toString());
								break;
								
							}else {
								//(1) we have the lrn at this point. We must go an API-round trip again to get the MRN
								String requestId = obj.getRequestId();
								dtoResponse.setRequestId(requestId);
								
								//(2)now we have the new lrn for the updated mrn so we proceed with the SADEXHF-update-lrn at house consignment
								if(StringUtils.isNotEmpty(requestId) && StringUtils.isNotEmpty(mrn)) {
									String mode = "UL";
									dtoResponse.setMrn(mrn);
									//TODO Status ??
									//dtoResponse.setDb_st(EnumSadexhfStatus.O.toString());
									//we must update the send date as well. Only 8-numbers
									String sendDate = hc.getDocumentIssueDate().replaceAll("-", "").substring(0,8);
									dtoResponse.setDocumentIssueDate(sendDate);//as aux for logging purposes
									
									List<SadmohfDto> xx = sadmohfService.updateLrnMrnSadmohf(serverRoot, user, dtoResponse, sendDate, mode);
									if(xx!=null && xx.size()>0) {
										for (SadmohfDto rec: xx) {
											if(StringUtils.isNotEmpty(rec.getEhmid()) ){
												//OK
												apiStatusAlreadyUpdated = true;
											}else {
												errMsg.append("MRN empty after SADMOHF-update:" + mrn + " " + methodName);
												dtoResponse.setErrMsg(errMsg.toString());
											}
										}
									}
									//(3) now we make a final check for LRN-status since there might have being some validation errors with the newly acquired LRN that did not appear when we 
									//first received the LRN in the first PUT House
									 //Delay 10-seconds
									logger.warn(PrettyLoggerOutputer.FRAME);
									logger.warn("Start of delay: "+ new Date());
									Thread.sleep(SadDigitollConstants.THREAD_DELAY_FOR_GET_MRN_MILLICSECONDS); 
									logger.warn("End of delay: "+ new Date());
									logger.warn(PrettyLoggerOutputer.FRAME);
									
									this.checkLrnValidationStatusHouseConsignmentDigitollV2FromApi(dtoResponse, requestId, tollTokenMap, isApiAir);
									if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())){
										logger.warn("ERROR: " + dtoResponse.getErrMsg()  + methodName);
										//Udate ehst2(SADEXHF) with ERROR = M
										dtoResponse.setDb_st2(EnumSadmohfStatus2.M.toString());
										List<SadmohfDto> tmp = sadmohfService.updateLrnMrnSadmohf(serverRoot, user, dtoResponse, sendDate, mode);
									}else {
										//OK
										logger.warn("LRN status is OK ... (no errors)");
										//Update ehst2 (SADEXHF) with OK = C
										dtoResponse.setDb_st2(EnumSadmohfStatus2.S.toString());
										List<SadmohfDto> tmp = sadmohfService.updateLrnMrnSadmohf(serverRoot, user, dtoResponse, sendDate, mode);
									}
									
								}else {
									errMsg.append("LRN empty after PUT ??: " + "-->RequestId:" + requestId + " -->MRN from db(SADMOHF): " + mrn + " "  + methodName);
									dtoResponse.setErrMsg(errMsg.toString());
									break;
								}
								
							}
							break; //only first in list
							
							
						}else {
							errMsg.append(" LRN/MRN are empty. This operation is invalid. Make sure this fields have values before any PUT " + methodName);
							dtoResponse.setErrMsg(errMsg.toString());
						}
						
					}
				}else {
					errMsg.append(" no records to fetch from SADMOHF " + methodName);
					dtoResponse.setErrMsg(errMsg.toString());
				}
				
			}else {
				errMsg.append(" invalid user:" + user + " " + methodName);
				dtoResponse.setErrMsg(errMsg.toString());
				user = null;
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			//Get out stackTrace to the response (errMsg)
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			dtoResponse.setErrMsg(sw.toString());
		
		}finally {
			
			if(!apiStatusAlreadyUpdated) {
				//check on status
				if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())) {
					logger.info(LOG_PREFIX_LEGEND + dtoResponse.getErrMsg());
					dtoResponse.setDb_st2(EnumSadmohfStatus2.M.toString());
					logger.info("INSIDE setStatus:" + dtoResponse.getDb_st2());
					//
					List<SadmohfDto> xx = sadmohfService.updateLrnMrnSadmohf(serverRoot, user, dtoResponse, dtoResponse.getDocumentIssueDate(), "UL");
					logger.info("After update on status 2 (finally-clause)");
				}
			}
			//log in log file
			sadmologLogger.doLog(serverRoot, user, dtoResponse);
		}
		//std output (browser)
		return dtoResponse;
	}
	
	/**
	 * 
	 * @param request
	 * @param user
	 * @param ehlnrt
	 * @param ehlnrm
	 * @param ehlnrh
	 * @param mrn
	 * @return
	 * @throws Exception
	 * 
	 * http://localhost:8080/syjservicestn-expft/deleteHouseConsignment.do?user=NN&ehlnrt=1&ehlnrm=2&ehlnrh=3&mrn=XXX
	 * 
	 * test - OK 
	 * 
	 */
	@RequestMapping(value="/digitollv2/deleteHouseConsignment.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse deleteHouseConsignmentDigitollV2(HttpServletRequest request , @RequestParam(value = "user", required = true) String user,
																									@RequestParam(value = "ehlnrt", required = true) String ehlnrt,
																									@RequestParam(value = "ehlnrm", required = true) String ehlnrm,
																									@RequestParam(value = "ehlnrh", required = true) String ehlnrh,
																									@RequestParam(value = "mrn", required = true) String mrn ) throws Exception {
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericDtoResponse dtoResponse = new GenericDtoResponse();
		dtoResponse.setUser(user);
		dtoResponse.setEhlnrt(ehlnrt);
		dtoResponse.setEllnrt(Integer.valueOf(ehlnrt));//for log purposes only
		dtoResponse.setEhlnrm(ehlnrm);
		dtoResponse.setEllnrm(Integer.valueOf(ehlnrm));//for log purposes only
		dtoResponse.setEhlnrh(ehlnrh);
		dtoResponse.setEllnrh(Integer.valueOf(ehlnrh));//for log purposes only
		dtoResponse.setMrn(mrn);
		dtoResponse.setRequestMethodApi("DELETE");
		boolean apiStatusAlreadyUpdated = false;
		boolean isApiAir = false;
		
		
		StringBuilder errMsg = new StringBuilder("ERROR ");
		
		String methodName = new Object() {}
	      .getClass()
	      .getEnclosingMethod()
	      .getName();
		
		logger.warn("Inside " + methodName +  " - MRNnr: " + mrn );
		//create new - master consignment at toll.no
		try {
			if(checkUser(user)) {
				logger.warn("user OK:" + user);
				List<SadmohfDto> list = sadmohfService.getSadmohfForUpdate(serverRoot, user, ehlnrt, ehlnrm, ehlnrh, mrn);
				
				if(list != null && list.size()>0) {
					logger.warn("list size:" + list.size());
					
					for (SadmohfDto dto: list) {
						//Get the transportDto - level since some fields might be required in the mapping
						dto.setTransportDto(this.sadmotfService.getSadmotfDto(serverRoot, user, ehlnrt));
						
						//Only valid when mrn(emmid) is NOT empty
						if(StringUtils.isNotEmpty(dto.getEhmid() )) {
							HouseConsignment hc = new MapperHouseConsignment().mapHouseConsignmentForDelete(dto);
							logger.warn("documentIssueDate:" + hc.getDocumentIssueDate());
							//API
							if(ApiAirRecognizer.isAir(dto.getTransportDto().getEtktyp())) { isApiAir = true; }
							String json = "";
							if(isApiAir) {
								json = apiServicesAir.deleteHouseConsignmentDigitollV2(hc, mrn);
							}else {
								json = apiServices.deleteHouseConsignmentDigitollV2(hc, mrn);
							}
							
							
							ApiRequestIdDto obj = new ObjectMapper().readValue(json, ApiRequestIdDto.class);
							logger.warn("JSON = " + json);
							logger.warn("RequestId = " + obj.getRequestId());
							//put in response
							dtoResponse.setRequestId(obj.getRequestId());
							dtoResponse.setEhlnrt(String.valueOf(dto.getEhlnrt()));
							dtoResponse.setEhlnrm(String.valueOf(dto.getEhlnrm()));
							dtoResponse.setEhlnrh(String.valueOf(dto.getEhlnrh()));
							dtoResponse.setAvd(String.valueOf(dto.getEhavd()));
							dtoResponse.setPro(String.valueOf(dto.getEhpro()));
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
									dtoResponse.setDb_st2(EnumSadmomfStatus2.D.toString());
									//we must update the send date as well. Only 8-numbers
									String sendDate = hc.getDocumentIssueDate().replaceAll("-", "").substring(0,8);
									dtoResponse.setDocumentIssueDate(sendDate);//as aux for logging purposes
									
									List<SadmohfDto> xx = sadmohfService.updateLrnMrnSadmohf(serverRoot, user, dtoResponse, sendDate, mode);
									if(xx!=null && xx.size()>0) {
										for (SadmohfDto rec: xx) {
											if(StringUtils.isEmpty(rec.getEhmid()) ){
												//OK
												apiStatusAlreadyUpdated = true;
											}else {
												errMsg.append("MRN has not been removed after SADMOHF-delete-light mrn:" + mrn);
												dtoResponse.setErrMsg(errMsg.toString());
											}
										}
									}
									
								}else {
									errMsg.append("LRN empty after DELETE-LIGHT ??: " + "-->requestId:" + requestId + " -->MRN from db(SADMOHF): " + mrn);
									dtoResponse.setErrMsg(errMsg.toString());
									break;
								}
							
							}
							
							break; //only first in list
							
						}else {
							errMsg.append(" LRN/MRN are empty (SADMOHF). This operation is invalid. Make sure ehuuid(lrn)/ehmid(mrn) fields have values before any DELETE ");
							dtoResponse.setErrMsg(errMsg.toString());
						}
						
					}
				}else {
					errMsg.append(" no records to fetch from SADMOHF ");
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
		
		}finally {
			
			if(!apiStatusAlreadyUpdated) {
				//check on status
				if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())) {
					logger.info(LOG_PREFIX_LEGEND + dtoResponse.getErrMsg());
					dtoResponse.setDb_st2(EnumSadmohfStatus2.M.toString());
					logger.info("INSIDE setStatus:" + dtoResponse.getDb_st2());
					//
					List<SadmohfDto> xx = sadmohfService.updateLrnMrnSadmohf(serverRoot, user, dtoResponse, dtoResponse.getDocumentIssueDate(), "ULM");
					logger.info("After update on status 2 (finally-clause)");
				}
			}
			//log in log file
			sadmologLogger.doLog(serverRoot, user, dtoResponse);
		}
		
		return dtoResponse;
	}
	
	/**
	 * Gets House Consignment status through the API - GET - without having to check our db 
	 * @param request
	 * @param user
	 * @param lrn
	 * @return
	 * @throws Exception
	 * 
	 * http://localhost:8080/syjservicestn-expft/digitollv2/getHouseConsignment.do?user=NN&lrn=XXX
	 * 
	 * test - OK
	 * 
	 */
	@RequestMapping(value="/digitollv2/getHouseConsignment.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse getHouseConsignmentDigitollV2(HttpServletRequest request , @RequestParam(value = "user", required = true) String user,
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
		
		logger.warn("Inside " + methodName + " - LRNnr: " + lrn+ "- apiType: " + apiType );
		//create new - master consignment at toll.no
		try {
			if(checkUser(user)) {
					//(2)now we have the new lrn for the updated mrn so we proceed with the SADMOHF-update-lrn at master consignment
					if(StringUtils.isNotEmpty(lrn)) {
						dtoResponse.setLrn(lrn);
						
						String mrn = this.getMrnHouseConsignmentDigitollV2FromApi(dtoResponse, lrn, apiType);
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
		
		//NA - log in db before std-output
		//sadexlogLogger.doLog(serverRoot, user, dtoResponse);
		//log in log file
		if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())) { logger.error(dtoResponse.getErrMsg()); }
		
		return dtoResponse;
	}
	/**
	 * Only for Air (special case for testing towards toll.no). Requires Difi-scope: toll:movement/entry
	 * 
	 * @param request
	 * @param user
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/digitollv2/getRoutingHouseConsignment.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse getRoutingHouseConsignmentDigitollV2(HttpServletRequest request , @RequestParam(value = "user", required = true) String user) throws Exception {
		
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
					json = apiServicesAir.getRoutingHouseConsignmentDigitollV2();
					logger.warn("JSON = " + json);
					EntryDto[] obj = new ObjectMapper().readValue(json, EntryDto[].class);
					//DEBUG
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
	 * Gets House Consignment status through the API - GET - in order to get an MRN
	 * This method is used for the update of an MRN in SADEXHF. The need for doing so is based upon the fact that toll.no
	 * has an asynchronous routine with every POST that returns sometimes an empty MRN as soon as the LRN has been produced.
	 * This will trigger a defect post in our db since the LRN without an MRN will be wrong if the POST was OK.
	 * To correct the above this method will be used at some point in the GUI in order to prevent a user-POST and instead prompt a PUT (update at toll.no instead of a create new)
	 * 
	 * @param request
	 * @param user
	 * @param lrn
	 * @return
	 * @throws Exception
	 * 
	 * http://localhost:8080/syjservicestn-expft/digitollv2/setMrnHouseConsignment.do?user=NN&lrn=XXX
	 * 
	 */
	/*
	@RequestMapping(value="/digitollv2/setMrnHouseConsignment.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse setMrnHouseConsignmentDigitollV2(HttpServletRequest request , @RequestParam(value = "user", required = true) String user,
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
		
		logger.warn("Inside " + methodName + " - LRNnr: " + lrn);
		//create new - master consignment at toll.no
		try {
			if(checkUser(user)) {
					//(1)now we have the new lrn for the updated mrn so we proceed with the SADEXMF-update-lrn at master consignment
					if(StringUtils.isNotEmpty(lrn)) {
						dtoResponse.setLrn(lrn);
						
						String mrn = this.getMrnHouseFromApi(dtoResponse, lrn);
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
							List<SadexhfDto> list = sadexhfService.getSadexhfForUpdate(serverRoot, user, lrn);
							
							if(list != null && list.size()>0) {
								logger.warn("list size:" + list.size());
								for (SadexhfDto dto: list) {	
									String mode = "ULM";
									//Update ehst2(SADEXHF) with OK = C
									dtoResponse.setAvd(String.valueOf(dto.getEhavd()));
									dtoResponse.setPro(String.valueOf(dto.getEhpro()));
									dtoResponse.setTdn(String.valueOf(dto.getEhtdn()));
									dtoResponse.setDb_st(dto.getEhst());
									dtoResponse.setDb_st2(EnumSadexhfStatus2.C.toString());
									dtoResponse.setDb_st3(dto.getEhst3());
									
									//(3)now we have lrn and mrn. Proceed with the SADEXHF-update at house consignment
									logger.warn("About to updateLrnMrnSadexh ...");
									List<SadexhfDto> xx = sadexhfService.updateLrnMrnSadexhf(serverRoot, user, dtoResponse, null, mode);
									//logger.warn("C");
									if(xx!=null && xx.size()>0) {
										for (SadexhfDto rec: xx) {
											//logger.warn("D:" + rec.toString());
											if(StringUtils.isNotEmpty(rec.getEhmid()) ){
												//OK
											}else {
												errMsg.append("MRN empty after SADEXHF-update ??:" + mrn);
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
		
		//NA - log in db before std-output
		//sadexlogLogger.doLog(serverRoot, user, dtoResponse);
		//log in log file
		if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())) { logger.error(dtoResponse.getErrMsg()); }
		
		return dtoResponse;
	}
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
	 * @param dtoResponse
	 * @param lrn
	 * @param apiType
	 * @return
	 */
	private String getMrnHouseConsignmentDigitollV2FromApi(GenericDtoResponse dtoResponse, String lrn, String apiType) {
		
		String retval = "";
		
		try{
			String json = "";
			if(StringUtils.isNotEmpty(apiType) && apiType.equalsIgnoreCase("air")) {
				json = apiServicesAir.getValidationStatusHouseConsignmentDigitollV2(lrn);
			}else {
				json = apiServices.getValidationStatusHouseConsignmentDigitollV2(lrn);
			}
			
			ApiMrnDto obj = new ObjectMapper().readValue(json, ApiMrnDto.class);
			logger.warn("JSON = " + json);
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
	 * Only used from POST
	 * 
	 * @param dtoResponse
	 * @param lrn
	 * @param tollTokenMap
	 * @param isApiAir
	 * 
	 * @return
	 */
	private String getMrnHouseConsignmentDigitollV2FromApi(GenericDtoResponse dtoResponse, String lrn, Map tollTokenMap, boolean isApiAir) {
		
		String retval = "";
		
		try{
			String json = "";	
			if(isApiAir) {
				json = apiServicesAir.getValidationStatusHouseConsignmentDigitollV2(lrn, tollTokenMap);
			}else {
				json = apiServices.getValidationStatusHouseConsignmentDigitollV2(lrn, tollTokenMap);
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
	 */
	private void checkLrnValidationStatusHouseConsignmentDigitollV2FromApi(GenericDtoResponse dtoResponse, String lrn, Map tollTokenMap, boolean isApiAir) {
		
		try{
			String json = "";	
			if(isApiAir) {
				json = apiServicesAir.getValidationStatusHouseConsignmentDigitollV2(lrn, tollTokenMap);
			}else {
				json = apiServices.getValidationStatusHouseConsignmentDigitollV2(lrn, tollTokenMap);
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
		
		
	}
	
	
	
	
}
