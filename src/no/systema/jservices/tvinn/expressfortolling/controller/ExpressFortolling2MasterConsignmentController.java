package no.systema.jservices.tvinn.expressfortolling.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpRequest;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import no.systema.jservices.common.dao.services.BridfDaoService;
import no.systema.jservices.common.dto.expressfortolling.ManifestActiveMeansOfTransportDto;
import no.systema.jservices.common.dto.expressfortolling.ManifestCargoLinesDto;
import no.systema.jservices.common.dto.expressfortolling.ManifestCargoLinesImportDeclarationDto;
import no.systema.jservices.common.dto.expressfortolling.ManifestDto;
import no.systema.jservices.common.dto.expressfortolling.ManifestModeOfTransportDto;
import no.systema.jservices.common.dto.expressfortolling.ManifestPlaceOfEntryDto;
import no.systema.jservices.common.dto.expressfortolling.ManifestStatusDto;
import no.systema.jservices.common.dto.expressfortolling.ManifestTypesOfExportDto;
import no.systema.jservices.common.dto.expressfortolling.ManifestTransportationCompanyDto;
import no.systema.jservices.tvinn.expressfortolling.api.ApiServices;
import no.systema.jservices.tvinn.expressfortolling.api.TestMasterConsignmentDao;
import no.systema.jservices.tvinn.expressfortolling.api.TesterLrn;
import no.systema.jservices.tvinn.expressfortolling2.dao.MasterConsignment;
import no.systema.jservices.tvinn.expressfortolling2.dto.ApiLrnDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.ApiMrnDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.ApiMrnStatusDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.ApiMrnStatusRecordDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.GenericDtoContainer;
import no.systema.jservices.tvinn.expressfortolling2.dto.GenericDtoResponse;
import no.systema.jservices.tvinn.expressfortolling2.dto.SadexmfDto;
import no.systema.jservices.tvinn.expressfortolling2.services.MapperMasterConsignment;
import no.systema.jservices.tvinn.expressfortolling2.services.SadexmfService;
import no.systema.jservices.tvinn.expressfortolling2.util.GenericJsonStringPrinter;
import no.systema.jservices.tvinn.expressfortolling2.util.ServerRoot;
import no.systema.main.util.ObjectMapperHalJson;
/**
 * Main entrance for accessing Express fortolling Version 2 API.
 * 
 * ===================================================================
 * Flow description with respect to the API and the SADEXMF db table
 * ===================================================================
 * 	===============================
 * 	(A) Get MRN Ekspres - POST (API)
 *	=============================== 
 *	(1) Först POST (krav: emuuid & emmid EMPTY)
 *	Systema AS
 *	toll-token expires_in:999
 *	/movement/road/v1/master-consignment
 *	JSON = {"lrn":"1bdf0f33-f42e-48e2-b334-3944722e3fe5"}
 *	LRN = 1bdf0f33-f42e-48e2-b334-3944722e3fe5
	
 *	(2) GET /master-consignment/validation-status/{lrn}
 *	Mrn and status on response. Update SADEXMF with lrn=emuuid and mrn=emmid  
	
 *	===========================
 * 	(B) Update Mrn - PUT (API)
 *	===========================
 *	(1) PUT /master-consignment/{masterReferenceNumber}
 *	Response returns new Lrn which must be updated in SADEXMF. Only Lrn update 
 *	IMPORTANT! -->Last Lrn received is the one valid for GET validation-status 
 *	
 * 	
 *	=============================
 *	(C) Delete Mrn - DELETE (API)
 *	=============================
 *	(1) DELETE /master-consignment/{masterReferenceNumber}
 *	Response returns new Lrn which we dont save.
 *	(2) Emuuid and Emmid (SADEXMF) are blanked. The record is not deleted in db but is ready for new POST
 *	This item (2) could change by deleting the record totally ...
 *
 *  NOTE! 
 *	(D) To see the status of a given MRN att any given moment use the end-point --> getMasterConsignment.do in this Controller
 * 
 * @author oscardelatorre
 * @date Aug 2022
 *
 */

@RestController
public class ExpressFortolling2MasterConsignmentController {
	private static Logger logger = LoggerFactory.getLogger(ExpressFortolling2MasterConsignmentController.class.getName());
	// pretty print
	private static ObjectMapper prettyErrorObjectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	private JsonParser prettyJsonParser = new JsonParser();
	private Gson prettyGsonObject = new GsonBuilder().setPrettyPrinting().create();
	
	@Autowired
	private BridfDaoService bridfDaoService;	
	
	@Autowired
	private SadexmfService sadexmfService;	
	
	
	@Autowired
	private ApiServices apiServices; 
	
	
	/**
	 * Test authorization towards Toll.no with certificates
	 * @Example http://localhost:8080/syjservicestn-expft/testAuth.do?user=SYSTEMA
	 * @param session
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="testAuth.do", method={RequestMethod.GET, RequestMethod.POST}) 
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
	 * http://localhost:8080/syjservicestn-expft/postMasterConsignment?user=NN&emavd=1&empro=501941
	 */
	@RequestMapping(value="postMasterConsignment.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse postMasterConsignmentExpressMovementRoad(HttpServletRequest request , @RequestParam(value = "user", required = true) String user, 
																				@RequestParam(value = "emavd", required = true) String emavd,
																				@RequestParam(value = "empro", required = true) String empro) throws Exception {
		
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericDtoResponse dtoResponse = new GenericDtoResponse();
		dtoResponse.setUser(user);
		dtoResponse.setAvd(emavd);
		dtoResponse.setPro(empro);
		StringBuilder errMsg = new StringBuilder("ERROR ");
		
		logger.warn("Inside postMasterConsignment");
		//create new - master consignment at toll.no
		try {
			if(checkUser(user)) {
				logger.warn("user OK:" + user);
				List<SadexmfDto> list = sadexmfService.getSadexmf(serverRoot, user, emavd, empro);
				if(list != null) {
					logger.warn("list size:" + list.size());
					
					for (SadexmfDto dto: list) {
						//DEBUG
						logger.info(dto.toString());
						//Only valid when those lrn(emuuid) and mrn(emmid) are empty
						
						if(StringUtils.isEmpty(dto.getEmmid()) && StringUtils.isEmpty(dto.getEmuuid() )) {
							MasterConsignment mc =  new MapperMasterConsignment().mapMasterConsignment(dto);
							logger.warn("Representative:" + mc.getRepresentative().getName());
							//Debug
							logger.debug(GenericJsonStringPrinter.debug(mc));
							//API
							
							String json = apiServices.postMasterConsignmentExpressMovementRoad(mc);
							ApiLrnDto obj = new ObjectMapper().readValue(json, ApiLrnDto.class);
							logger.warn("JSON = " + json);
							logger.warn("LRN = " + obj.getLrn());
							//In case there was an error at end-point and the LRN was not returned
							if(StringUtils.isEmpty(obj.getLrn())){
								errMsg.append("LRN empty ?? <json raw>: " + json);
								dtoResponse.setErrMsg(errMsg.toString());
								break;
							}else {
								//(1) we have the lrn at this point. We must go an API-round trip again to get the MRN
								String lrn = obj.getLrn();
								dtoResponse.setLrn(lrn);
								
								//(2) get mrn from API
								//PROD-->
								String mrn = this.getMrnMasterFromApi(dtoResponse, lrn);
								if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())){
									errMsg.append(dtoResponse.getErrMsg());
									dtoResponse.setErrMsg("");
								}
								
								//(3)now we have lrn and mrn and proceed with the SADEXMF-update at master consignment
								if(StringUtils.isNotEmpty(lrn) && StringUtils.isNotEmpty(mrn)) {
									String mode = "ULM";
									dtoResponse.setMrn(mrn);
									//TODO Status ??
									//dtoResponse.setDb_st(EnumSadexmfStatus.O.toString());
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
									errMsg.append("LRN and/or MRN empty ??: " + "-->LRN:" + lrn + " -->MRN from API (look at logback-logs): " + mrn);
									dtoResponse.setErrMsg(errMsg.toString());
									break;
								}
							
							}
							break; //only first in list
							
						}else {
							errMsg.append(" LRN/MRN already exist. This operation is invalid. Make sure this fields are empty before any POST or issue a PUT (with current MRN) ");
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
			//Get out stackTrace to the response (errMsg) and logger
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			dtoResponse.setErrMsg(sw.toString());
			logger.error(dtoResponse.getErrMsg());
		}
		
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
	 * http://localhost:8080/syjservicestn-expft/putMasterConsignment?user=NN&emavd=1&empro=501941&mrn=XXX
	 */
	@RequestMapping(value="putMasterConsignment.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse putMasterConsignmentExpressMovementRoad(HttpServletRequest request , @RequestParam(value = "user", required = true) String user, 
																				@RequestParam(value = "emavd", required = true) String emavd,
																				@RequestParam(value = "empro", required = true) String empro,
																				@RequestParam(value = "mrn", required = true) String mrn ) throws Exception {
		
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericDtoResponse dtoResponse = new GenericDtoResponse();
		dtoResponse.setUser(user);
		dtoResponse.setAvd(emavd);
		dtoResponse.setPro(empro);
		dtoResponse.setMrn(mrn);
		StringBuilder errMsg = new StringBuilder("ERROR ");
		
		logger.warn("Inside putMasterConsignment - MRNnr: " + mrn);
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
							logger.warn("Representative:" + mc.getRepresentative().getName());
							//API - PROD
							String json = apiServices.putMasterConsignmentExpressMovementRoad(mc, mrn);
							ApiLrnDto obj = new ObjectMapper().readValue(json, ApiLrnDto.class);
							logger.warn("JSON = " + json);
							logger.warn("LRN = " + obj.getLrn());
							
							//TEST
							/*ApiLrnDto obj = new ApiLrnDto();
							Integer x = new Random().ints(1, 100)
						      .findFirst()
						      .getAsInt();
							
							obj.setLrn("b-666-777-888" + x);
							String json = "";
							*/
							
							
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
									//TODO Status ??
									//dtoResponse.setStatus(EnumSadexmfStatus.O.toString());
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
									
								}else {
									errMsg.append("LRN empty after PUT ??: " + "-->LRN:" + lrn + " -->MRN from db(SADEXMF): " + mrn);
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
					errMsg.append(" no records to fetch from SADEXMF ");
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
			logger.error(dtoResponse.getErrMsg());
		}
		
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
	 * http://localhost:8080/syjservicestn-expft/deleteMasterConsignment?user=NN&mrn=XXX
	 */
	@RequestMapping(value="deleteMasterConsignment.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse deleteMasterConsignmentExpressMovementRoad(HttpServletRequest request , @RequestParam(value = "user", required = true) String user,
																				@RequestParam(value = "emavd", required = true) String emavd,
																				@RequestParam(value = "empro", required = true) String empro,
																				@RequestParam(value = "mrn", required = true) String mrn) throws Exception {
		
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericDtoResponse dtoResponse = new GenericDtoResponse();
		dtoResponse.setUser(user);
		dtoResponse.setAvd(emavd);
		dtoResponse.setPro(empro);
		dtoResponse.setMrn(mrn);
		StringBuilder errMsg = new StringBuilder("ERROR ");
		
		logger.warn("Inside deleteMasterConsignment - MRNnr: " + mrn);
		//create new - master consignment at toll.no
		try {
			if(checkUser(user)) {
				logger.warn("user OK:" + user);
				List<SadexmfDto> list = sadexmfService.getSadexmfForUpdate(serverRoot, user, emavd, empro,  mrn);
				
				if(list != null && list.size()>0) {
					logger.warn("list size:" + list.size());
					
					for (SadexmfDto sadexmfDto: list) {
						//Only valid when those lrn(emuuid) and mrn(emmid) are NOT empty
						if(StringUtils.isNotEmpty(sadexmfDto.getEmmid()) && StringUtils.isNotEmpty(sadexmfDto.getEmuuid() )) {
							MasterConsignment mc =  new MapperMasterConsignment().mapMasterConsignmentForDelete(sadexmfDto);
							logger.warn("Declarant:" + mc.getDeclarant().getName());
							//API
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
									dtoResponse.setMrn(mrn);
									String mode = "DL";
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
				errMsg.append(" invalid user ");
				dtoResponse.setErrMsg(errMsg.toString());
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			//Get out stackTrace to the response (errMsg)
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			dtoResponse.setErrMsg(sw.toString());
			logger.error(dtoResponse.getErrMsg());
		}
		
		return dtoResponse;
	}
	
	
	/**
	 * Gets Master Consignment status through the API - GET
	 * @Example http://localhost:8080/syjservicestn-expft/getManifest.do?user=SYSTEMA&id=f2bfbb94-afae-4af3-a4ff-437f787d322f
	 * @param session
	 * @param user
	 * @param id
	 * @return
	 */
	@RequestMapping(value="getMasterConsignment.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse getMasterConsignmentExpressMovementRoad(HttpServletRequest request , @RequestParam(value = "user", required = true) String user,
																				@RequestParam(value = "lrn", required = true) String lrn) throws Exception {
		
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericDtoResponse dtoResponse = new GenericDtoResponse();
		dtoResponse.setUser(user);
		dtoResponse.setLrn(lrn);
		StringBuilder errMsg = new StringBuilder("ERROR ");
		
		logger.warn("Inside getMasterConsignment - LRNnr: " + lrn);
		//create new - master consignment at toll.no
		try {
			if(checkUser(user)) {
					//(2)now we have the new lrn for the updated mrn so we proceed with the SADEXMF-update-lrn at master consignment
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
						}
						
					}else {
						errMsg.append("LRN empty ?" + "-->LRN:" + lrn);
						dtoResponse.setErrMsg(errMsg.toString());
						
					}
											
			}else {
				errMsg.append(" invalid user ");
				dtoResponse.setErrMsg(errMsg.toString());
			}
			
		}catch(Exception e) {
			//e.printStackTrace();
			//Get out stackTrace to the response (errMsg)
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			dtoResponse.setErrMsg(sw.toString());
			logger.error(dtoResponse.getErrMsg());
		}
		
		return dtoResponse;
	}
	
	/**
	 * Gets Master Consignment status through the API - GET - without having to check our db 
	 * @Example http://localhost:8080/syjservicestn-expft/getStatusMasterConsignment.do?user=SYSTEMA&mrn=22NOM6O19GRP8UQBT6
	 * @param request
	 * @param user
	 * @param mrn
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="getStatusMasterConsignment.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse getStatusMasterConsignmentExpressMovementRoad(HttpServletRequest request , @RequestParam(value = "user", required = true) String user, 
																				@RequestParam(value = "mrn", required = true) String mrn ) throws Exception {
		
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericDtoResponse dtoResponse = new GenericDtoResponse();
		dtoResponse.setUser(user);
		dtoResponse.setAvd(""); //dummy
		dtoResponse.setPro(""); //dummy
		dtoResponse.setMrn(mrn);
		StringBuilder errMsg = new StringBuilder("ERROR ");
		
		logger.warn("Inside getStatusMasterConsignment - MRNnr: " + mrn);
		try {
			if(checkUser(user)) {
				logger.warn("user OK:" + user);
				//API - PROD
				String json = apiServices.getMrnStatusMasterConsignmentExpressMovementRoad(mrn);
				logger.warn("JSON = " + json);
				if(StringUtils.isNotEmpty(json)) {
					ApiMrnStatusRecordDto[] obj = new ObjectMapper().readValue(json, ApiMrnStatusRecordDto[].class);
					if(obj!=null) {
						List list = Arrays.asList(obj);
						logger.warn("List = " + list);
						
						//In case there was an error at end-point and the LRN was not returned
						if(list!=null && !list.isEmpty()){
							dtoResponse.setList(list);
						}else {
							errMsg.append("MRN not existent ?? <json raw>: " + json);
							dtoResponse.setErrMsg(errMsg.toString());
						}
					}
				}else {
					errMsg.append("JSON toll.no EMPTY. The MRN does not exists ...? ");
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
			logger.error(dtoResponse.getErrMsg());
		}
		
		return dtoResponse;
	}
	
	
	
	private boolean checkUser(String user) {
		if (!bridfDaoService.userNameExist(user)) {
			throw new RuntimeException("ERROR: parameter, user, is not valid!");
		}
		return true;
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
			dtoResponse.setStatus(obj.getStatus());
			
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
			logger.error(dtoResponse.getErrMsg());
			
		}
		
		return retval;
	}
	
	
	/**
	 * http://localhost:8080/syjservicestn-expft/getManifestStatus.do?user=SYSTEMA&id=f2bfbb94-afae-4af3-a4ff-437f787d322f
	 * @param session
	 * @param user
	 * @param id
	 * @return
	 * @throws Exception
	 */
	/*
	@RequestMapping(value="getManifestStatus.do", method={RequestMethod.GET, RequestMethod.POST}) 
	public Object getManifestStatus(HttpSession session, @RequestParam(value = "user", required = true) String user, 
														@RequestParam(value = "id", required = true) String id) throws Exception {
		logger.warn("getManifestStatus.do, id="+id);
		
		checkUser(user);
		try{
			String payload = apiServices.getManifest(id);
			//convert to Dto (we do not do this in the service since we must see the JSON string in case of errors. It is easier to follow...
			ObjectMapperHalJson objMapper = new ObjectMapperHalJson(payload, "");
			ObjectMapperHalJson objMapper_TC = new ObjectMapperHalJson(payload, "/_embedded/transportationCompany");
			
			//Manifest Parent
			StringBuffer jsonToConvert = new StringBuffer();
			ManifestDto manifestDto = objMapper.getObjectMapper(jsonToConvert).readValue(jsonToConvert.toString(), new TypeReference<ManifestDto>() {});
			
			//Transp.Company
			if(objMapper_TC.isValidTargetNode()){
				jsonToConvert.delete(0, jsonToConvert.length());
				ManifestTransportationCompanyDto transportationCompanyDto = objMapper_TC.getObjectMapper(jsonToConvert).readValue(jsonToConvert.toString(), new TypeReference<ManifestTransportationCompanyDto>() {});
				manifestDto.setTransportationCompany(transportationCompanyDto);
			}
			
			ManifestStatusDto dto = new ManifestStatusDto();
			dto.setManifestId(manifestDto.getManifestId());
			dto.setStatus(manifestDto.getStatus());
			dto.setTimeOfDeparture(manifestDto.getTimeOfDeparture());
			dto.setTimeOfRelease(manifestDto.getTimeOfRelease());
			dto.setLastChanged(manifestDto.getLastChanged());
			return dto;
			
		}catch(Exception e){
			ManifestStatusDto dto = new ManifestStatusDto();
			dto.setManifestId(e.toString());
			return dto;
			
		}finally{
			
			session.invalidate();
			
		}
		
	}
	*/
}
