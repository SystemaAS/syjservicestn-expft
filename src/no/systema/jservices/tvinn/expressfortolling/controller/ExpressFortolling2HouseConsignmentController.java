package no.systema.jservices.tvinn.expressfortolling.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

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
import no.systema.jservices.tvinn.expressfortolling2.services.ApiLrnDto;
import no.systema.jservices.tvinn.expressfortolling2.services.ApiMrnDto;
import no.systema.jservices.tvinn.expressfortolling2.services.GenericDtoResponse;
import no.systema.jservices.tvinn.expressfortolling2.services.MapperMasterConsignment;
import no.systema.jservices.tvinn.expressfortolling2.services.SadexhfDto;
import no.systema.jservices.tvinn.expressfortolling2.services.SadexhfService;
import no.systema.jservices.tvinn.expressfortolling2.services.SadexmfService;
import no.systema.jservices.tvinn.expressfortolling2.services.SadexmfDto;
import no.systema.main.util.ObjectMapperHalJson;
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
	 */
	@RequestMapping(value="postHouseConsignment.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse postHouseConsignmentExpressMovementRoad(HttpServletRequest request , @RequestParam(value = "user", required = true) String user, 
																				@RequestParam(value = "ehavd", required = true) String ehavd,
																				@RequestParam(value = "ehpro", required = true) String ehpro,
																				@RequestParam(value = "ehtdn", required = true) String ehtdn) throws Exception {
		
		GenericDtoResponse dtoResponse = new GenericDtoResponse();
		dtoResponse.setUser(user);
		dtoResponse.setAvd(ehavd);
		dtoResponse.setPro(ehpro);
		dtoResponse.setTdn(ehtdn);
		
		StringBuilder errMsg = new StringBuilder("ERROR ");
		
		logger.warn("Inside postHouseConsignment");
		//create new - master consignment at toll.no
		try {
			if(checkUser(user)) {
				logger.warn("user OK:" + user);
				List<SadexhfDto> list = sadexhfService.getSadexhf(user, ehavd, ehpro, ehtdn);
				if(list != null) {
					logger.warn("list size:" + list.size());
					
					for (SadexhfDto dto: list) {
						//Always shoot for a new POST. Since we can have orphan House (without MRN) we are not able to send a PUT until the master has been sent.
						//Therefore it will be always a POST
						
						/*MasterConsignment mc =  new MapperMasterConsignment().mapMasterConsignment(dto);
						logger.warn("Representative:" + mc.getRepresentative().getName());
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
							//String mrn = this.getMrnMasterFromApi(dtoResponse, lrn);
							//TEST-->
							String mrn = "22NO4TU2HUD59UCBT2";
							
							//(3)now we have lrn and mrn and proceed with the SADEXMF-update at master consignment
							if(StringUtils.isNotEmpty(lrn) && StringUtils.isNotEmpty(mrn)) {
								dtoResponse.setMrn(mrn);
								
								List<SadexmfDto> xx = sadexhfService.updateLrnMrnSadexmf(user, Integer.valueOf(emavd), Integer.valueOf(empro), lrn, mrn);
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
						*/
						
						
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
		
		return dtoResponse;
	}
	
	/*
	@RequestMapping(value="putMasterConsignment.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse putMasterConsignmentExpressMovementRoad(HttpServletRequest request , @RequestParam(value = "user", required = true) String user, 
																				@RequestParam(value = "mrn", required = true) String mrn) throws Exception {
		
		GenericDtoResponse dtoResponse = new GenericDtoResponse();
		dtoResponse.setUser(user);
		dtoResponse.setMrn(mrn);
		StringBuilder errMsg = new StringBuilder("ERROR ");
		
		logger.warn("Inside putMasterConsignment - MRNnr: " + mrn);
		//create new - master consignment at toll.no
		try {
			if(checkUser(user)) {
				logger.warn("user OK:" + user);
				List<SadexmfDto> list = sadexhfService.getSadexmfForUpdate(user, mrn);
				
				if(list != null && list.size()>0) {
					logger.warn("list size:" + list.size());
					
					for (SadexmfDto sadexmfDto: list) {
						//Only valid when those lrn(emuuid) and mrn(emmid) are NOT empty
						if(StringUtils.isNotEmpty(sadexmfDto.getEmmid()) && StringUtils.isNotEmpty(sadexmfDto.getEmuuid() )) {
							MasterConsignment mc =  new MapperMasterConsignment().mapMasterConsignment(sadexmfDto);
							logger.warn("Representative:" + mc.getRepresentative().getName());
							//API
							String json = apiServices.putMasterConsignmentExpressMovementRoad(mc, mrn);
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
		}
		
		return dtoResponse;
	}
	*/
	/**
	 * Delete MasterConsignment in API-server
	 * @param request
	 * @param user
	 * @param mrn
	 * @return
	 * @throws Exception
	 */
	/*
	@RequestMapping(value="deleteMasterConsignment.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse deleteMasterConsignmentExpressMovementRoad(HttpServletRequest request , @RequestParam(value = "user", required = true) String user, 
																				@RequestParam(value = "mrn", required = true) String mrn) throws Exception {
		
		GenericDtoResponse dtoResponse = new GenericDtoResponse();
		dtoResponse.setUser(user);
		dtoResponse.setMrn(mrn);
		StringBuilder errMsg = new StringBuilder("ERROR ");
		
		logger.warn("Inside deleteMasterConsignment - MRNnr: " + mrn);
		//create new - master consignment at toll.no
		try {
			if(checkUser(user)) {
				logger.warn("user OK:" + user);
				List<SadexmfDto> list = sadexhfService.getSadexmfForUpdate(user, mrn);
				
				if(list != null && list.size()>0) {
					logger.warn("list size:" + list.size());
					
					for (SadexmfDto sadexmfDto: list) {
						//Only valid when those lrn(emuuid) and mrn(emmid) are NOT empty
						if(StringUtils.isNotEmpty(sadexmfDto.getEmmid()) && StringUtils.isNotEmpty(sadexmfDto.getEmuuid() )) {
							MasterConsignment mc =  new MapperMasterConsignment().mapMasterConsignment(sadexmfDto);
							logger.warn("Representative:" + mc.getRepresentative().getName());
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
							}
							break; //only first in list
							
						}else {
							errMsg.append(" LRN/MRN are empty. This operation is invalid. Make sure this fields have values before any DELETE ");
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
		}
		
		return dtoResponse;
	}
	
	*/
	
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
	private String getMrnHouseFromApi(GenericDtoResponse dtoResponse, String lrn) {
		
		String retval = "";
		
		try{
			
			String json = apiServices.getValidationStatusMasterConsignmentExpressMovementRoad(lrn);
			ApiMrnDto obj = new ObjectMapper().readValue(json, ApiMrnDto.class);
			logger.warn("JSON = " + json);
			logger.warn("MRN = " + obj.getMasterReferenceNumber());
			if(StringUtils.isEmpty(obj.getMasterReferenceNumber())) {
				retval = obj.getMasterReferenceNumber();
			}
		}catch(Exception e) {
			e.printStackTrace();
			//Get out stackTrace to the response (errMsg)
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			dtoResponse.setErrMsg(sw.toString());
			
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
