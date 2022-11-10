package no.systema.jservices.tvinn.expressfortolling.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
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
import no.systema.jservices.tvinn.expressfortolling2.dao.HouseConsignment;
import no.systema.jservices.tvinn.expressfortolling2.dto.ApiLrnDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.ApiMrnDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.GenericDtoResponse;
import no.systema.jservices.tvinn.expressfortolling2.dto.SadexhfDto;
import no.systema.jservices.tvinn.expressfortolling2.enums.EnumSadexhfStatus2;
import no.systema.jservices.tvinn.expressfortolling2.services.MapperHouseConsignment;
import no.systema.jservices.tvinn.expressfortolling2.services.SadexhfService;
import no.systema.jservices.tvinn.expressfortolling2.util.SadexlogLogger;
import no.systema.jservices.tvinn.expressfortolling2.util.ServerRoot;
/**
 * Main entrance for accessing Express fortolling API.
 * 
 * @author oscardelatorre
 * @date Aug 2022
 *
 */
@RestController
public class ExpressFortolling2HouseConsignmentController {
	private static Logger logger = LoggerFactory.getLogger(ExpressFortolling2HouseConsignmentController.class.getName());
	// pretty print
	private static ObjectMapper prettyErrorObjectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	private JsonParser prettyJsonParser = new JsonParser();
	private Gson prettyGsonObject = new GsonBuilder().setPrettyPrinting().create();
	
	@Autowired
	private BridfDaoService bridfDaoService;	
	
	@Autowired
	private SadexhfService sadexhfService;	
	
	@Autowired
	private ApiServices apiServices; 
	
	@Autowired
	private SadexlogLogger sadexlogLogger;	
	
	
	
	/**
	 * Creates a new House Consignment through the API - POST
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
	 * http://localhost:8080/syjservicestn-expft/postHouseConsignment?user=NN&ehavd=1&ehpro=501941&ehtdn=38
	 */
	@RequestMapping(value="postHouseConsignment.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse postHouseConsignmentExpressMovementRoad(HttpServletRequest request , @RequestParam(value = "user", required = true) String user, 
																				@RequestParam(value = "ehavd", required = true) String ehavd,
																				@RequestParam(value = "ehpro", required = true) String ehpro,
																				@RequestParam(value = "ehtdn", required = true) String ehtdn) throws Exception {
		
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericDtoResponse dtoResponse = new GenericDtoResponse();
		dtoResponse.setUser(user);
		dtoResponse.setAvd(ehavd);
		dtoResponse.setPro(ehpro);
		dtoResponse.setTdn(ehtdn);
		dtoResponse.setRequestMethodApi("POST");
		
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
				
				List<SadexhfDto> list = sadexhfService.getSadexhf(serverRoot, user, ehavd, ehpro, ehtdn);
				if(list != null) {
					logger.warn("list size:" + list.size());
					
					for (SadexhfDto dto: list) {
						//DEBUG logger.warn(dto.toString());
						//Only valid when mrn(emmid) are empty
						if(StringUtils.isEmpty(dto.getEhmid()) ) {
							
							HouseConsignment hc = new MapperHouseConsignment().mapHouseConsignment(dto);
							logger.warn("Representative:" + hc.getRepresentative().getName());
							logger.warn("House documentNumber:" + hc.getHouseConsignmentConsignmentHouseLevel().getTransportDocumentHouseLevel().getDocumentNumber());
							logger.warn("House totalAmountInvoiced:" + hc.getHouseConsignmentConsignmentHouseLevel().getTotalAmountInvoiced().getValue() 
																	+ hc.getHouseConsignmentConsignmentHouseLevel().getTotalAmountInvoiced().getCurrency());
							//API
							String json = apiServices.postHouseConsignmentExpressMovementRoad(hc);
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
								
								 //Delay 2-seconds
								logger.warn("Start of delay: "+ new Date());
								Thread.sleep(2000); 
								logger.warn("End of delay: "+ new Date());
								
								//(2) get mrn from API
								//PROD-->
								String mrn = this.getMrnHouseFromApi(dtoResponse, lrn);
								dtoResponse.setMrn(mrn);
								String mode = "ULM";
								String sendDate = hc.getDocumentIssueDate().replaceAll("-", "").substring(0,8);
								//
								if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())){
									errMsg.append(dtoResponse.getErrMsg());
									dtoResponse.setErrMsg("");
									//Update ehst2(SADEXHF) with ERROR = M
									logger.warn("ERROR: " + dtoResponse.getErrMsg()  + methodName);
									dtoResponse.setDb_st2(EnumSadexhfStatus2.M.toString());
								}else {
									//Update ehst2(SADEXHF) with OK = C
									dtoResponse.setDb_st2(EnumSadexhfStatus2.C.toString());
								}
								
								//(3)now we have lrn and mrn and proceed with the SADEXHF-update at house consignment
								logger.warn("About to updateLrnMrnSadexh ...");
								List<SadexhfDto> xx = sadexhfService.updateLrnMrnSadexhf(serverRoot, user, dtoResponse, sendDate, mode);
								//logger.warn("C");
								if(xx!=null && xx.size()>0) {
									for (SadexhfDto rec: xx) {
										//logger.warn("D:" + rec.toString());
										if(StringUtils.isNotEmpty(rec.getEhmid()) ){
											//OK
										}else {
											errMsg.append("MRN empty after SADEXHF-update:" + mrn);
											dtoResponse.setErrMsg(errMsg.toString());
										}
									}
								}
								
							}
							break; //only first in list
							
						}else {
							errMsg.append(" LRN/MRN already exist. This operation is invalid. Make sure this fields are empty before any POST or issue a PUT (with current MRN) ");
							logger.error(errMsg.toString());
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
		}
		
		//log in db before std-output
		sadexlogLogger.doLog(serverRoot, user, dtoResponse);
		//log in log file
		if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())) { logger.error(dtoResponse.getErrMsg()); }
		
		//std output (browser)
		return dtoResponse;
	}
	
	/**
	 * 
	 * @param request
	 * @param user
	 * @param ehavd
	 * @param ehpro
	 * @param ehtdn
	 * @param mrn
	 * @return
	 * @throws Exception
	 * 
	 * http://localhost:8080/syjservicestn-expft/putHouseConsignment?user=NN&ehavd=1&ehpro=501941&ehtdn=38&mrn=XXX
	 */
	@RequestMapping(value="putHouseConsignment.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse putHouseConsignmentExpressMovementRoad(HttpServletRequest request , @RequestParam(value = "user", required = true) String user, 
																				@RequestParam(value = "ehavd", required = true) String ehavd,
																				@RequestParam(value = "ehpro", required = true) String ehpro,
																				@RequestParam(value = "ehtdn", required = true) String ehtdn,
																				@RequestParam(value = "mrn", required = true) String mrn ) throws Exception {
		
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericDtoResponse dtoResponse = new GenericDtoResponse();
		dtoResponse.setUser(user);
		dtoResponse.setAvd(ehavd);
		dtoResponse.setPro(ehpro);
		dtoResponse.setTdn(ehtdn);
		dtoResponse.setMrn(mrn);
		dtoResponse.setRequestMethodApi("PUT");
		
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
				List<SadexhfDto> list = sadexhfService.getSadexhfForUpdate(serverRoot, user, mrn, ehavd, ehpro, ehtdn);
				
				if(list != null && list.size()>0) {
					logger.warn("SADEXHF list size:" + list.size());
					
					for (SadexhfDto dto: list) {
						logger.warn(dto.toString());
						//Only valid when those lrn(emuuid) and mrn(emmid) are NOT empty
						if(StringUtils.isNotEmpty(dto.getEhmid()) && StringUtils.isNotEmpty(dto.getEhuuid() )) {
							HouseConsignment hc = new MapperHouseConsignment().mapHouseConsignment(dto);
							logger.warn("Declarant:" + hc.getDeclarant().getName());
							//API - PROD
							String json = apiServices.putHouseConsignmentExpressMovementRoad(hc, mrn);
							ApiLrnDto obj = new ObjectMapper().readValue(json, ApiLrnDto.class);
							logger.warn("JSON = " + json);
							logger.warn("LRN = " + obj.getLrn());
							
							//put in response
							dtoResponse.setLrn(obj.getLrn());
							dtoResponse.setAvd(String.valueOf(dto.getEhavd()));
							dtoResponse.setPro(String.valueOf(dto.getEhpro()));
							dtoResponse.setTdn(String.valueOf(dto.getEhtdn()));
							//In case there was an error at end-point and the LRN was not returned
							if(StringUtils.isEmpty(obj.getLrn())){
								errMsg.append("LRN empty ?? <json raw>: " + json);
								errMsg.append("-->" + methodName);
								dtoResponse.setErrMsg(errMsg.toString());
								break;
								
							}else {
								//(1) we have the lrn at this point. We must go an API-round trip again to get the MRN
								String lrn = obj.getLrn();
								dtoResponse.setLrn(lrn);
								
								//(2)now we have the new lrn for the updated mrn so we proceed with the SADEXHF-update-lrn at house consignment
								if(StringUtils.isNotEmpty(lrn) && StringUtils.isNotEmpty(mrn)) {
									String mode = "UL";
									dtoResponse.setMrn(mrn);
									//TODO Status ??
									//dtoResponse.setDb_st(EnumSadexhfStatus.O.toString());
									//we must update the send date as well. Only 8-numbers
									String sendDate = hc.getDocumentIssueDate().replaceAll("-", "").substring(0,8);
									
									List<SadexhfDto> xx = sadexhfService.updateLrnMrnSadexhf(serverRoot, user, dtoResponse, sendDate, mode);
									if(xx!=null && xx.size()>0) {
										for (SadexhfDto rec: xx) {
											if(StringUtils.isNotEmpty(rec.getEhmid()) ){
												//OK
											}else {
												errMsg.append("MRN empty after SADEXHF-update:" + mrn + " " + methodName);
												dtoResponse.setErrMsg(errMsg.toString());
											}
										}
									}
									//(3) now we make a final check for LRN-status since there might have being some validation errors with the newly acquired LRN that did not appear when we 
									//first received the LRN in the first PUT House
									this.checkLrnValidationStatus(dtoResponse, lrn);
									if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())){
										logger.warn("ERROR: " + dtoResponse.getErrMsg()  + methodName);
										//Udate ehst2(SADEXHF) with ERROR = M
										dtoResponse.setDb_st2(EnumSadexhfStatus2.M.toString());
										List<SadexhfDto> tmp = sadexhfService.updateLrnMrnSadexhf(serverRoot, user, dtoResponse, sendDate, mode);
									}else {
										//OK
										logger.warn("LRN status is OK ... (no errors)");
										//Update ehst2 (SADEXHF) with OK = C
										dtoResponse.setDb_st2(EnumSadexhfStatus2.C.toString());
										List<SadexhfDto> tmp = sadexhfService.updateLrnMrnSadexhf(serverRoot, user, dtoResponse, sendDate, mode);
									}
									
								}else {
									errMsg.append("LRN empty after PUT ??: " + "-->LRN:" + lrn + " -->MRN from db(SADEXHF): " + mrn + " "  + methodName);
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
					errMsg.append(" no records to fetch from SADEXHF " + methodName);
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
		}
		
		
		//log in db before std-output
		sadexlogLogger.doLog(serverRoot, user, dtoResponse);
		//log in log file
		if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())) { logger.error(dtoResponse.getErrMsg()); }
		
		//std output (browser)
		return dtoResponse;
	}
	
	/**
	 * 
	 * @param request
	 * @param user
	 * @param ehavd
	 * @param ehpro
	 * @param ehtdn
	 * @param mrn
	 * @return
	 * @throws Exception
	 * 
	 * http://localhost:8080/syjservicestn-expft/deleteHouseConsignment?user=NN&ehavd=1&ehpro=501941&ehtdn=38&mrn=XXX
	 * 
	 */
	@RequestMapping(value="deleteHouseConsignment.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse deleteHouseConsignmentExpressMovementRoad(HttpServletRequest request , @RequestParam(value = "user", required = true) String user,
																									@RequestParam(value = "ehavd", required = true) String ehavd,
																									@RequestParam(value = "ehpro", required = true) String ehpro,
																									@RequestParam(value = "ehtdn", required = true) String ehtdn,
																									@RequestParam(value = "mrn", required = true) String mrn ) throws Exception {
		String serverRoot = ServerRoot.getServerRoot(request);
		GenericDtoResponse dtoResponse = new GenericDtoResponse();
		dtoResponse.setUser(user);
		dtoResponse.setAvd(ehavd);
		dtoResponse.setPro(ehpro);
		dtoResponse.setTdn(ehtdn);
		dtoResponse.setMrn(mrn);
		dtoResponse.setRequestMethodApi("DELETE");
		
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
				List<SadexhfDto> list = sadexhfService.getSadexhfForUpdate(serverRoot, user, mrn, ehavd, ehpro, ehtdn);
				
				if(list != null && list.size()>0) {
					logger.warn("list size:" + list.size());
					
					for (SadexhfDto dto: list) {
						//Only valid when those lrn(emuuid) and mrn(emmid) are NOT empty
						if(StringUtils.isNotEmpty(dto.getEhmid()) && StringUtils.isNotEmpty(dto.getEhuuid() )) {
							HouseConsignment hc = new MapperHouseConsignment().mapHouseConsignment(dto);
							logger.warn("Declarant:" + hc.getDeclarant().getName());
							//API
							String json = apiServices.deleteHouseConsignmentExpressMovementRoad(hc, mrn);
							ApiLrnDto obj = new ObjectMapper().readValue(json, ApiLrnDto.class);
							logger.warn("JSON = " + json);
							logger.warn("LRN = " + obj.getLrn());
							//put in response
							dtoResponse.setLrn(obj.getLrn());
							dtoResponse.setAvd(String.valueOf(dto.getEhavd()));
							dtoResponse.setPro(String.valueOf(dto.getEhpro()));
							dtoResponse.setTdn(String.valueOf(dto.getEhtdn()));
							
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
									dtoResponse.setDb_st2(EnumSadexhfStatus2.D.toString());
									//we must update the send date as well. Only 8-numbers
									String sendDate = hc.getDocumentIssueDate().replaceAll("-", "").substring(0,8);
									
									List<SadexhfDto> xx = sadexhfService.updateLrnMrnSadexhf(serverRoot, user, dtoResponse, sendDate, mode);
									if(xx!=null && xx.size()>0) {
										for (SadexhfDto rec: xx) {
											if(StringUtils.isEmpty(rec.getEhmid()) ){
												//OK
											}else {
												errMsg.append("MRN has not been removed after SADEXHF-delete-light mrn:" + mrn);
												dtoResponse.setErrMsg(errMsg.toString());
											}
										}
									}
									
									
								}else {
									errMsg.append("LRN empty after DELETE-LIGHT ??: " + "-->LRN:" + lrn + " -->MRN from db(SADEXHF): " + mrn);
									dtoResponse.setErrMsg(errMsg.toString());
									break;
								}
							
							}
							
							break; //only first in list
							
						}else {
							errMsg.append(" LRN/MRN are empty (SADEXHF). This operation is invalid. Make sure emuuid(lrn)/emmid(mrn) fields have values before any DELETE ");
							dtoResponse.setErrMsg(errMsg.toString());
						}
						
					}
				}else {
					errMsg.append(" no records to fetch from SADEXHF ");
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
	 * http://localhost:8080/syjservicestn-expft/getHouseConsignment?user=NN&lrn=XXX
	 * 
	 */
	@RequestMapping(value="getHouseConsignment.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse getHouseConsignmentExpressMovementRoad(HttpServletRequest request , @RequestParam(value = "user", required = true) String user,
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
					//(2)now we have the new lrn for the updated mrn so we proceed with the SADEXMF-update-lrn at master consignment
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
	 * http://localhost:8080/syjservicestn-expft/setMrnHouseConsignment?user=NN&lrn=XXX
	 * 
	 */
	@RequestMapping(value="setMrnHouseConsignment.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse setMrnHouseConsignmentExpressMovementRoad(HttpServletRequest request , @RequestParam(value = "user", required = true) String user,
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
	 * @return
	 */
	private String getMrnHouseFromApi(GenericDtoResponse dtoResponse, String lrn) {
		
		String retval = "";
		
		try{
			
			String json = apiServices.getValidationStatusHouseConsignmentExpressMovementRoad(lrn);
			ApiMrnDto obj = new ObjectMapper().readValue(json, ApiMrnDto.class);
			logger.warn("JSON = " + json);
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
	 */
	private void checkLrnValidationStatus(GenericDtoResponse dtoResponse, String lrn) {
		
		try{
			
			String json = apiServices.getValidationStatusHouseConsignmentExpressMovementRoad(lrn);
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
		
		
	}
	
	
	
	
}
