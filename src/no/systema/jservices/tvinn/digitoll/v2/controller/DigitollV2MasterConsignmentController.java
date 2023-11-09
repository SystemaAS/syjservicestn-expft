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
import no.systema.jservices.tvinn.digitoll.v2.controller.service.PoolExecutorControllerService;
import no.systema.jservices.tvinn.digitoll.v2.dao.MasterConsignment;
import no.systema.jservices.tvinn.digitoll.v2.dto.ApiRequestIdDto;
import no.systema.jservices.tvinn.digitoll.v2.dto.SadmomfDto;
import no.systema.jservices.tvinn.digitoll.v2.dto.SadmotfDto;
import no.systema.jservices.tvinn.digitoll.v2.enums.EnumSadmomfStatus2;
import no.systema.jservices.tvinn.digitoll.v2.enums.EnumSadmotfStatus2;
import no.systema.jservices.tvinn.expressfortolling2.dto.ApiMrnDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.ApiMrnStatusRecordDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.GenericDtoResponse;
import no.systema.jservices.tvinn.expressfortolling2.enums.EnumControllerMrnType;
import no.systema.jservices.tvinn.digitoll.v2.services.MapperMasterConsignment;
import no.systema.jservices.tvinn.digitoll.v2.services.SadmomfService;
import no.systema.jservices.tvinn.digitoll.v2.services.SadmotfService;
import no.systema.jservices.tvinn.digitoll.v2.util.ApiAirRecognizer;
import no.systema.jservices.tvinn.digitoll.v2.util.PrettyLoggerOutputer;
import no.systema.jservices.tvinn.digitoll.v2.util.SadDigitollConstants;
import no.systema.jservices.tvinn.digitoll.v2.util.SadmologLogger;
import no.systema.jservices.tvinn.expressfortolling2.util.GenericJsonStringPrinter;
import no.systema.jservices.tvinn.expressfortolling2.util.ServerRoot;
import no.systema.main.util.ObjectMapperHalJson;
/**
 * Main entrance for accessing Digitoll Version 2 API.
 * 
 * ===================================================================
 * Flow description with respect to the API and the SADMOMF db table
 * ===================================================================
 * 	===============================
 * 	(A) Get MRN Digitoll - POST (API)
 *	=============================== 
 *	(1) Först POST (krav: emuuid & emmid EMPTY)
 *	Systema AS
 *	toll-token expires_in:999
 *	/movement/road/v2/master-consignment
 *	JSON = {"requestId":"1bdf0f33-f42e-48e2-b334-3944722e3fe5"}
 *	requestId (old LRN) = 1bdf0f33-f42e-48e2-b334-3944722e3fe5
	
 *	(2) GET /master-consignment/validation-status/{requestId}
 *	Mrn and status on response. Update SADMOMF with requestId=emuuid and mrn=emmid  
	
 *	===========================
 * 	(B) Update Mrn - PUT (API)
 *	===========================
 *	(1) PUT /master-consignment/{masterReferenceNumber}
 *	Response returns new requestId which must be updated in SADMOMF. Only requestId update 
 *	IMPORTANT! -->Last requestId received is the one valid for GET validation-status 
 *	
 * 	
 *	=============================
 *	(C) Delete Mrn - DELETE (API)
 *	=============================
 *	(1) DELETE /master-consignment/{masterReferenceNumber}
 *	Response returns new requestId which we dont save.
 *	(2) Emuuid and Emmid (SADMOMF) are blanked. The record is not deleted in db but is ready for new POST
 *	This item (2) could change by deleting the record totally ...
 *
 *  NOTE! 
 *	(D) To see the status of a given MRN att any given moment use the end-point --> getMasterConsignment.do in this Controller
 * 
 * @author oscardelatorre
 * @date Aug 2023
 * 
 *
 */

@RestController
public class DigitollV2MasterConsignmentController {
	private static Logger logger = LoggerFactory.getLogger(DigitollV2MasterConsignmentController.class.getName());
	// pretty print
	private static ObjectMapper prettyErrorObjectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	private JsonParser prettyJsonParser = new JsonParser();
	private Gson prettyGsonObject = new GsonBuilder().setPrettyPrinting().create();
	
	@Value("${expft.getmrn.timeout.milliseconds}")
    private Integer GET_MRN_DELAY_MILLISECONDS;
	
	
	@Autowired
	private BridfDaoService bridfDaoService;	
	
	@Autowired
	private SadmomfService sadmomfService;
	@Autowired
	private SadmotfService sadmotfService;
	
	@Autowired
	private ApiServices apiServices; 
	@Autowired
	private ApiServicesAir apiServicesAir;
	
	@Autowired
	private PoolExecutorControllerService poolExecutorControllerService;
	
	@Autowired
	private SadmologLogger sadmologLogger;	
	
	/**
	 * Test authorization towards Toll.no with certificates
	 * @Example http://localhost:8080/syjservicestn-expft/testAuth.do?user=SYSTEMA
	 * @param session
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/digitollv2/testAuth.do", method={RequestMethod.GET, RequestMethod.POST}) 
	public String testAuth(HttpSession session , @RequestParam(value = "user", required = true) String user ) throws Exception {
		logger.warn("Inside = testAuth");
		if(checkUser(user)) {
			String json = apiServices.testAuthExpressMovementRoad();
		}
		return "Done ...";
		
	}
	/**
	 * Creates a new Master Consignment through the API - POST
	 * The operation is only valid when the lrn(emuuid) and mrn(emmid) are empty at SADEXMF
	 * (1)If these fields are already in place your should use the PUT method OR 
	 * (2)erase the emuuid and emmid on db before using POST again
	 * 
	 * @param session
	 * @param user
	 * @param emavd
	 * @param empro
	 * @throws Exception
	 * 
	 * http://localhost:8080/syjservicestn-expft/digitollv2/postMasterConsignment.do?user=NN&emavd=1&empro=501941
	 * 
	 * test - OK
	 * 
	 */
	@RequestMapping(value="/digitollv2/postMasterConsignment.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse postMasterConsignmentDigitollV2(HttpServletRequest request , @RequestParam(value = "user", required = true) String user, 
																				@RequestParam(value = "emlnrt", required = true) String emlnrt,
																				@RequestParam(value = "emlnrm", required = true) String emlnrm) throws Exception {
		
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericDtoResponse dtoResponse = new GenericDtoResponse();
		dtoResponse.setUser(user);
		dtoResponse.setEmlnrt(emlnrt);
		dtoResponse.setEllnrt(Integer.valueOf(emlnrt));//for log purposes only
		dtoResponse.setEmlnrm(emlnrm);
		dtoResponse.setEllnrm(Integer.valueOf(emlnrm));//for log purposes only
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
		//create new - master consignment at toll.no
		try {
			if(checkUser(user)) {
				logger.warn("user OK:" + user);
				List<SadmomfDto> list = sadmomfService.getSadmomf(serverRoot, user, emlnrt, emlnrm);
				if(list != null) {
					logger.warn("list size:" + list.size());
					
					for (SadmomfDto dto: list) {
						//DEBUG
						//logger.info(dto.toString());
						
						//Get the transportDto - level since some fields might be required in the mapping
						dto.setTransportDto(this.sadmotfService.getSadmotfDto(serverRoot, user, emlnrt));
						
						//Check if we are using MO-Air and not road...
						if(dto.getTransportDto()!=null) {
							if(ApiAirRecognizer.isAir(dto.getTransportDto().getEtktyp()))  { isApiAir = true; }
						}
						//Only valid when mrn(emmid) are empty
						//if(StringUtils.isEmpty(dto.getEmmid()) && StringUtils.isEmpty(dto.getEmuuid() )) {
						if(StringUtils.isEmpty(dto.getEmmid()) ) {
							MasterConsignment mc =  new MapperMasterConsignment().mapMasterConsignment(dto);
							logger.warn("GrossMass:" + mc.getConsignmentMasterLevel().getGrossMass());
							//Debug
							//logger.debug(GenericJsonStringPrinter.debug(mc));
							//API
							
							Map tollTokenMap = new HashMap();
							String json = "";
							if(isApiAir) {
								json = apiServicesAir.postMasterConsignmentDigitollV2(mc, tollTokenMap);
							}else {
								json = apiServices.postMasterConsignmentDigitollV2(mc, tollTokenMap);
							}
							
							//At this point we now have a valid tollToken to use
							
							ApiRequestIdDto obj = new ObjectMapper().readValue(json, ApiRequestIdDto.class);
							logger.warn("JSON = " + json);
							logger.warn("requestId = " + obj.getRequestId());
							dtoResponse.setAvd(String.valueOf(dto.getEmavd()));
							dtoResponse.setPro(String.valueOf(dto.getEmpro()));
							dtoResponse.setEmlnrt(String.valueOf(dto.getEmlnrt()));
							dtoResponse.setEmlnrm(String.valueOf(dto.getEmlnrm()));

							
							//In case there was an error at end-point and the requestId was not returned
							if(StringUtils.isEmpty(obj.getRequestId())){
								errMsg.append("requestId empty ?? <json raw>: " + json);
								dtoResponse.setErrMsg(errMsg.toString());
								break;
							}else {
								//(1) we have the requestId at this point. We must go an API-round trip again to get the MRN
								String requestIdForMrn = obj.getRequestId();
								dtoResponse.setRequestId(obj.getRequestId());
								
								//set RequestId-BUP (only once and only here) since the mrn could be lost as first-timer (Kakfa-queue not returning in time sometimes
								if(StringUtils.isNotEmpty(dtoResponse.getRequestId())) {
									if(StringUtils.isEmpty(dto.getEmuuid_own()) ){
										//this will happen only once (populate the fall-back uuid_own
										GenericDtoResponse dtoResponseBup = dtoResponse;
										sadmomfService.setRequestIdBupSadmomf(serverRoot, user, dtoResponseBup);
									}
								}
								//=====================
								//(2) get mrn from API
								//PROD-->
								//=====================
								/*//Use the first requestId until we get the MRN (only for getMRN)
								//We are expecting the user to SEND until the MRN is returned
								//This will happened only in special occasions in which the MRN did not arrive in the first try (despite the loop of 1-minute below...
								if(StringUtils.isNotEmpty(dto.getEmuuid_own()) && StringUtils.isEmpty(dto.getEmmid_own()) ){
									logger.info("Using first UUID_OWN until we get the MRN..." + dto.getEmuuid_own());
									requestIdForMrn = dto.getEmuuid_own();
								}*/
								//GET MRN right here...
								String mrn = poolExecutorControllerService.getMrnPOSTDigitollV2FromApi(dtoResponse, dtoResponse.getRequestId(), dto.getEmuuid_own(), tollTokenMap, isApiAir, EnumControllerMrnType.MASTER.toString());
								logger.info("####### MRN (MASTER):" + mrn + "#######");
								
								//(3) at this point we take actions depending on the mrn be or not to be
								if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())){
									errMsg.append(dtoResponse.getErrMsg());
									dtoResponse.setErrMsg("");
									dtoResponse.setDb_st2(EnumSadmomfStatus2.M.toString());
								}else {
									dtoResponse.setDb_st2(EnumSadmomfStatus2.S.toString());
								}
								
								
								//(3)now we have lrn and mrn and proceed with the SADMOMF-update at master consignment
								if(StringUtils.isNotEmpty(dtoResponse.getRequestId()) && StringUtils.isNotEmpty(mrn)) {
									String mode = "ULM";
									dtoResponse.setMrn(mrn);
									//we must update the send date as well. Only 8-numbers
									String sendDate = mc.getDocumentIssueDate().replaceAll("-", "").substring(0,8);
									dtoResponse.setDocumentIssueDate(sendDate);//as aux for logging purposes 
									
									List<SadmomfDto> xx = sadmomfService.updateLrnMrnSadmomf(serverRoot, user, dtoResponse, sendDate, mode);
									if(xx!=null && xx.size()>0) {
										for (SadmomfDto rec: xx) {
											if(StringUtils.isNotEmpty(rec.getEmmid()) ){
												//OK
												apiStatusAlreadyUpdated = true;
												//set MRN-BUP (only once and only here)
												GenericDtoResponse dtoResponseBup = dtoResponse;
												sadmomfService.setMrnBupSadmomf(serverRoot, user, dtoResponseBup);
											}else {
												errMsg.append("MRN empty after SADMOMF-update:" + mrn);
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
		
		}finally{
		
			//check on status
			if(!apiStatusAlreadyUpdated) {
				if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())) {
					logger.info(SadDigitollConstants.LOG_PREFIX_LEGEND + dtoResponse.getErrMsg());
					dtoResponse.setDb_st2(EnumSadmomfStatus2.M.toString());
					logger.info("INSIDE setStatus:" + dtoResponse.getDb_st2());
					//
					List<SadmomfDto> xx = sadmomfService.updateLrnMrnSadmomf(serverRoot, user, dtoResponse, dtoResponse.getDocumentIssueDate(), "ULM");
					logger.info("After update on status 2 (finally-clause)");
				}
			}
			//log in db before std-output
			sadmologLogger.doLog(serverRoot, user, dtoResponse);
			
		
		}
		
		//std output (browser)
		return dtoResponse;
	}
	
	/**
	 * 
	 * Updates an existing Master Consignment through the API - PUT. Requires an existing MRN (emmid at SADMOMF)
	 * @param request
	 * @param user
	 * @param emavd
	 * @param empro
	 * @return
	 * @throws Exception
	 * 
	 * http://localhost:8080/syjservicestn-expft/digitollv2/putMasterConsignment?user=NN&emlnrt=1&emlnrm=2&mrn=XXX
	 * 
	 * test - OK 
	 */
	@RequestMapping(value="/digitollv2/putMasterConsignment.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse putMasterConsignmentDigitollV2(HttpServletRequest request , @RequestParam(value = "user", required = true) String user, 
																				@RequestParam(value = "emlnrt", required = true) String emlnrt,
																				@RequestParam(value = "emlnrm", required = true) String emlnrm,
																				@RequestParam(value = "mrn", required = true) String mrn ) throws Exception {
		
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericDtoResponse dtoResponse = new GenericDtoResponse();
		dtoResponse.setUser(user);
		dtoResponse.setEmlnrt(emlnrt);
		dtoResponse.setEllnrt(Integer.valueOf(emlnrt));//for log purposes only
		dtoResponse.setEmlnrm(emlnrm);
		dtoResponse.setEllnrm(Integer.valueOf(emlnrm));//for log purposes only
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
		
		//create new - master consignment at toll.no
		try {
			if(checkUser(user)) {
				logger.warn("user OK:" + user);
				List<SadmomfDto> list = sadmomfService.getSadmomfForUpdate(serverRoot, user, emlnrt, emlnrm, mrn);
				
				if(list != null && list.size()>0) {
					logger.warn("list size:" + list.size());
					
					for (SadmomfDto dto: list) {
						//logger.warn(dto.toString());
						//Get the transportDto - level since some fields might be required in the mapping
						dto.setTransportDto(this.sadmotfService.getSadmotfDto(serverRoot, user, emlnrt));
						
						//Only valid when mrn(emmid) is NOT empty
						if(StringUtils.isNotEmpty(dto.getEmmid()) ) {
							MasterConsignment mc =  new MapperMasterConsignment().mapMasterConsignment(dto);
							logger.warn("GrossMass:" + mc.getConsignmentMasterLevel().getGrossMass());
							//Debug
							//logger.debug(GenericJsonStringPrinter.debug(mc));
							//init response in case en ERROR occurs after apiSerivices...
							dtoResponse.setRequestId(dto.getEmuuid());
							
							//API - PROD
							Map tollTokenMap = new HashMap();
							//API
							if(dto.getTransportDto()!=null) {
								if(ApiAirRecognizer.isAir(dto.getTransportDto().getEtktyp())) { isApiAir = true; }
							}
							String json = "";
							if(isApiAir) {
								json = apiServicesAir.putMasterConsignmentDigitollV2(mc, mrn, tollTokenMap);
							}else {
								json = apiServices.putMasterConsignmentDigitollV2(mc, mrn, tollTokenMap);
							}
							
							ApiRequestIdDto obj = new ObjectMapper().readValue(json, ApiRequestIdDto.class);
							logger.warn("JSON = " + json);
							logger.warn("requestId = " + obj.getRequestId());
							//At this point we now have a valid tollToken to use
							
							//put in response
							dtoResponse.setRequestId(obj.getRequestId());
							dtoResponse.setEmlnrt(String.valueOf(dto.getEmlnrt()));
							dtoResponse.setEmlnrm(String.valueOf(dto.getEmlnrm()));
							dtoResponse.setAvd(String.valueOf(dto.getEmavd()));
							dtoResponse.setPro(String.valueOf(dto.getEmpro()));
							
							//In case there was an error at end-point and the LRN was not returned
							if(StringUtils.isEmpty(obj.getRequestId())){
								errMsg.append("requestId empty ?? <json raw>: " + json);
								dtoResponse.setErrMsg(errMsg.toString());
								break;
								
							}else {
								//(1) we have the lrn at this point. We must go an API-round trip again to get the MRN
								String requestId = obj.getRequestId();
								dtoResponse.setRequestId(requestId);
								
								//(2)now we have the new lrn for the updated mrn so we proceed with the SADMOMF-update-lrn at master consignment
								if(StringUtils.isNotEmpty(requestId) && StringUtils.isNotEmpty(mrn)) {
									String mode = "UL";
									dtoResponse.setMrn(mrn);
									//we must update the send date as well. Only 8-numbers
									String sendDate = mc.getDocumentIssueDate().replaceAll("-", "").substring(0,8);
									dtoResponse.setDocumentIssueDate(sendDate);//as aux for logging purposes
									
									List<SadmomfDto> xx = sadmomfService.updateLrnMrnSadmomf(serverRoot, user, dtoResponse, sendDate, mode);
									if(xx!=null && xx.size()>0) {
										for (SadmomfDto rec: xx) {
											//logger.warn(rec.toString());
											if(StringUtils.isNotEmpty(rec.getEmmid()) ){
												//OK
												apiStatusAlreadyUpdated = true;
											}else {
												errMsg.append("MRN empty after SADMOMF-update:" + mrn);
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
									
									this.checkLrnValidationStatusMasterConsignmentDigitollV2FromApi(dtoResponse, requestId, tollTokenMap, isApiAir);
									if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())){
										logger.warn("ERROR: " + dtoResponse.getErrMsg()  + methodName);
										//Update ehst2(SADMOMF) with ERROR = M
										dtoResponse.setDb_st2(EnumSadmomfStatus2.M.toString());
										List<SadmomfDto> tmp = sadmomfService.updateLrnMrnSadmomf(serverRoot, user, dtoResponse, sendDate, mode);
									}else {
										//OK
										logger.warn("RequestId status is OK ... (no errors)");
										dtoResponse.setDb_st2(EnumSadmomfStatus2.S.toString());
										List<SadmomfDto> tmp = sadmomfService.updateLrnMrnSadmomf(serverRoot, user, dtoResponse, sendDate, mode);
									}
									
								}else {
									errMsg.append("RequestId empty after PUT ??: " + "-->RequestId:" + requestId + " -->MRN from db(SADMOMF): " + mrn);
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
					errMsg.append(" no records to fetch from SADMOMF ");
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
		
		}finally{
			//check on status
			if(!apiStatusAlreadyUpdated) {
				if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())) {
					logger.info(SadDigitollConstants.LOG_PREFIX_LEGEND + dtoResponse.getErrMsg());
					dtoResponse.setDb_st2(EnumSadmomfStatus2.M.toString());
					logger.info("INSIDE setStatus:" + dtoResponse.getDb_st2());
					//
					List<SadmomfDto> xx = sadmomfService.updateLrnMrnSadmomf(serverRoot, user, dtoResponse, dtoResponse.getDocumentIssueDate(), "UL");
					logger.info("After update on status 2 (finally-clause)");
				}
			}
			//log in db before std-output
			sadmologLogger.doLog(serverRoot, user, dtoResponse);
		
		}
		
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
	 * http://localhost:8080/syjservicestn-expft/digitollv2/deleteMasterConsignment?user=NN&emlnrt=XXX&mrn=XXX
	 */
	@RequestMapping(value="/digitollv2/deleteMasterConsignment.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse deleteMasterConsignmentDigitollV2(HttpServletRequest request , @RequestParam(value = "user", required = true) String user,
																				@RequestParam(value = "emlnrt", required = true) String emlnrt,
																				@RequestParam(value = "emlnrm", required = true) String emlnrm,
																				@RequestParam(value = "mrn", required = true) String mrn) throws Exception {
		
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericDtoResponse dtoResponse = new GenericDtoResponse();
		dtoResponse.setUser(user);
		dtoResponse.setEmlnrt(emlnrt);
		dtoResponse.setEllnrt(Integer.valueOf(emlnrt));//for log purposes only
		dtoResponse.setEmlnrm(emlnrm);
		dtoResponse.setEllnrm(Integer.valueOf(emlnrm));//for log purposes only
		
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
				List<SadmomfDto> list = sadmomfService.getSadmomfForUpdate(serverRoot, user, emlnrt, emlnrm,  mrn);
				
				if(list != null && list.size()>0) {
					logger.warn("list size:" + list.size());
					
					
					for (SadmomfDto dto: list) {
						//Get the transportDto - level since some fields might be required in the mapping
						dto.setTransportDto(this.sadmotfService.getSadmotfDto(serverRoot, user, emlnrt));
						
						//Only valid when mrn(emmid) is NOT empty
						if(StringUtils.isNotEmpty(dto.getEmmid() )) {
							MasterConsignment mc =  new MapperMasterConsignment().mapMasterConsignmentForDelete();
							//API
							if(dto.getTransportDto()!=null) {
								if(ApiAirRecognizer.isAir(dto.getTransportDto().getEtktyp())) { isApiAir = true; }
							}
							String json = "";
							if(isApiAir) {
								json = apiServicesAir.deleteMasterConsignmentDigitollV2(mc, mrn);
							}else {
								json = apiServices.deleteMasterConsignmentDigitollV2(mc, mrn);
							}
							
							ApiRequestIdDto obj = new ObjectMapper().readValue(json, ApiRequestIdDto.class);
							logger.warn("JSON = " + json);
							logger.warn("RequestId = " + obj.getRequestId());
							//put in response
							dtoResponse.setRequestId(obj.getRequestId());
							dtoResponse.setEmlnrt(String.valueOf(dto.getEmlnrt()));
							dtoResponse.setEmlnrm(String.valueOf(dto.getEmlnrm()));
							dtoResponse.setAvd(String.valueOf(dto.getEmavd()));
							dtoResponse.setPro(String.valueOf(dto.getEmpro()));
							
							//In case there was an error at end-point and the LRN was not returned
							if(StringUtils.isEmpty(obj.getRequestId())){
								errMsg.append("requestId empty ?? <json raw>: " + json);
								dtoResponse.setErrMsg(errMsg.toString());
								break;
								
							}else {
								//(1) we have the lrn at this point. We must go an API-round trip again to get the MRN
								String requestId = obj.getRequestId();
								dtoResponse.setRequestId(requestId);
								
								//(2)now we have the new requestId for the updated mrn so we proceed with the SADMOMF-update-lrn at master consignment
								if(StringUtils.isNotEmpty(requestId) && StringUtils.isNotEmpty(mrn)) {
									String mode = "DL";
									dtoResponse.setMrn(mrn);
									dtoResponse.setDb_st2(EnumSadmomfStatus2.D.toString());
									//we must update the send date as well. Only 8-numbers
									String sendDate = mc.getDocumentIssueDate().replaceAll("-", "").substring(0,8);
									dtoResponse.setDocumentIssueDate(sendDate);//as aux for logging purposes
									
									List<SadmomfDto> xx = sadmomfService.updateLrnMrnSadmomf(serverRoot, user, dtoResponse, sendDate, mode);
									if(xx!=null && xx.size()>0) {
										for (SadmomfDto rec: xx) {
											if(StringUtils.isEmpty(rec.getEmmid()) ){
												//OK
												apiStatusAlreadyUpdated = true;
											}else {
												errMsg.append("MRN has not been removed after SADMOMF-delete-light mrn:" + mrn);
												dtoResponse.setErrMsg(errMsg.toString());
											}
										}
									}
									
								}else {
									errMsg.append("LRN empty after DELETE-LIGHT ??: " + "-->requestId:" + requestId + " -->MRN from db(SADMOMF): " + mrn);
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
		
		}finally{
		
			//check on status
			if(!apiStatusAlreadyUpdated) {
				if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())) {
					logger.info(SadDigitollConstants.LOG_PREFIX_LEGEND + dtoResponse.getErrMsg());
					dtoResponse.setDb_st2(EnumSadmomfStatus2.M.toString());
					logger.info("INSIDE setStatus:" + dtoResponse.getDb_st2());
					//
					List<SadmomfDto> xx = sadmomfService.updateLrnMrnSadmomf(serverRoot, user, dtoResponse, dtoResponse.getDocumentIssueDate(), "ULM");
					logger.info("After update on status 2 (finally-clause)");
				}
			}
			//log in db before std-output
			sadmologLogger.doLog(serverRoot, user, dtoResponse);
			
		}
		
		//std output (browser)
		return dtoResponse;
	}
	
	
	/**
	 * Gets Master Consignment status through the API - GET - without having to check our db 
	 * @Example http://localhost:8080/syjservicestn-expft/digitollv2/getMasterConsignment.do?user=SYSTEMA&lrn=f2bfbb94-afae-4af3-a4ff-437f787d322f
	 * @param session
	 * @param user
	 * @param id
	 * @return
	 * 
	 * test- OK
	 */
	@RequestMapping(value="/digitollv2/getMasterConsignment.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse getMasterConsignmentDigitollV2(HttpServletRequest request , @RequestParam(value = "user", required = true) String user,
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
		
		logger.warn("Inside " + methodName + "- LRNnr: " + lrn );
		
		try {
			if(checkUser(user)) {
					//(2)now we have the new lrn for the updated mrn so we proceed with the SADEXMF-update-lrn at master consignment
					if(StringUtils.isNotEmpty(lrn)) {
						dtoResponse.setLrn(lrn);
						
						String mrn = this.getMrnMasterConsignmentDigitollV2FromApi(dtoResponse, lrn, apiType);
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
	 * @Example http://localhost:8080/syjservicestn-expft/digitollv2/getDocsStatusMasterConsignment.do?user=NN&emlnrt=1&emlnrm=2&mrn=22NOM6O19GRP8UQBT6
	 * @param request
	 * @param user
	 * @param mrn
	 * @return
	 * @throws Exception
	 */
	
	@RequestMapping(value="/digitollv2/getDocsRecMasterConsignment.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse getDocsReceivedMasterConsignmentDigitollV2(HttpServletRequest request , @RequestParam(value = "user", required = true) String user, 
																				@RequestParam(value = "mrn", required = true) String mrn ) throws Exception {
		
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericDtoResponse dtoResponse = new GenericDtoResponse();
		dtoResponse.setUser(user);
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
				String json = apiServices.getDocsReceivedMasterConsignmentDigitollV2(mrn);
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
							//This stage is necessary only to change a house status3 on whether it exist in Master at toll.no or not
							/*for (Object record: list) {
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
								
							}*/
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
	 * @param dtoResponse
	 * @param lrn
	 * @param tollTokenMap
	 * @param apiType
	 * 
	 * @return
	 */
	private String getMrnMasterConsignmentDigitollV2FromApi(GenericDtoResponse dtoResponse, String lrn, String apiType) {
		
		String retval = "";
		
		try{
			String json = "";
			if(StringUtils.isNotEmpty(apiType) && apiType.equalsIgnoreCase("air")) {
				json = apiServicesAir.getValidationStatusMasterConsignmentDigitollV2(lrn);
			}else {
				json = apiServices.getValidationStatusMasterConsignmentDigitollV2(lrn);
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
	private String checkLrnValidationStatusMasterConsignmentDigitollV2FromApi(GenericDtoResponse dtoResponse, String lrn, Map tollTokenMap, boolean isApiAir) {
		
		String retval = "";
		
		try{
			String json = "";	
			if(isApiAir) {
				json = apiServicesAir.getValidationStatusMasterConsignmentDigitollV2(lrn, tollTokenMap);
			}else {
				json = apiServices.getValidationStatusMasterConsignmentDigitollV2(lrn, tollTokenMap);
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
