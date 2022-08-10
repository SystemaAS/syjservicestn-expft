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
import no.systema.jservices.tvinn.expressfortolling2.services.SadexService;
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
public class ExpressFortolling2Controller {
	private static Logger logger = LoggerFactory.getLogger(ExpressFortolling2Controller.class.getName());
	// pretty print
	private static ObjectMapper prettyErrorObjectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	private JsonParser prettyJsonParser = new JsonParser();
	private Gson prettyGsonObject = new GsonBuilder().setPrettyPrinting().create();
	
	@Autowired
	private BridfDaoService bridfDaoService;	
	
	@Autowired
	private SadexService sadexService;	
	
	
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
	 * Creates a new Master Consignment through an API - POST
	 * The operation is only valid when the lrn(emuuid) and mrn(emmid) are empty at SADEXMF
	 * It these fields are already in place your should use the PUT method OR erase the emuuid and emmid on db
	 * 
	 * @param session
	 * @param user
	 * @param emavd
	 * @param empro
	 * @throws Exception
	 */
	@RequestMapping(value="postMasterConsignment.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse postMasterConsignmentExpressMovementRoad(HttpServletRequest request , @RequestParam(value = "user", required = true) String user, 
																				@RequestParam(value = "emavd", required = true) String emavd,
																				@RequestParam(value = "empro", required = true) String empro) throws Exception {
		
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
				List<SadexmfDto> list = sadexService.getSadexmf(user, emavd, empro);
				if(list != null) {
					logger.warn("list size:" + list.size());
					
					for (SadexmfDto sadexmfDto: list) {
						//Only valid when those lrn(emuuid) and mrn(emmid) are empty
						if(StringUtils.isEmpty(sadexmfDto.getEmmid()) && StringUtils.isEmpty(sadexmfDto.getEmuuid() )) {
							MasterConsignment mc =  new MapperMasterConsignment().mapMasterConsignment(sadexmfDto);
							logger.warn("Representative:" + mc.getRepresentative().getName());
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
								//TEST-->String mrn = "XXXKKK";
								
								//(3)now we have lrn and mrn and proceed with the SADEXMF-update at master consignment
								if(StringUtils.isNotEmpty(lrn) && StringUtils.isNotEmpty(mrn)) {
									dtoResponse.setMrn(mrn);
									
									List<SadexmfDto> xx = sadexService.updateLrnMrnSadexmf(user, Integer.valueOf(emavd), Integer.valueOf(empro), lrn, mrn);
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
							errMsg.append(" LRN/MRN already exist. This operation is invalid. Make sure this fields are empty before any POST ");
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
		
		return dtoResponse;
	}
	
	
	/**
	 * Get the manifest by id
	 * @Example http://localhost:8080/syjservicestn-expft/getManifest.do?user=SYSTEMA&id=f2bfbb94-afae-4af3-a4ff-437f787d322f
	 * @param session
	 * @param user
	 * @param id
	 * @return
	 */
	@RequestMapping(value="XXgetManifest.do", method={RequestMethod.GET, RequestMethod.POST}) 
	public ManifestDto getManifest(HttpSession session, @RequestParam(value = "user", required = true) String user, 
														@RequestParam(value = "id", required = true) String id) throws Exception {
		logger.info("getManifest.do, id="+id);
		
		checkUser(user);
		try{
			String payload = apiServices.getManifest(id);
			//convert to Dto (we do not do this in the service since we must see the JSON string in case of errors. It is easier to follow...
			ObjectMapperHalJson objMapper = new ObjectMapperHalJson(payload, "");
			ObjectMapperHalJson objMapper_TC = new ObjectMapperHalJson(payload, "/_embedded/transportationCompany");
			ObjectMapperHalJson objMapper_AMT = new ObjectMapperHalJson(payload, "/_embedded/activeMeansOfTransport");
			ObjectMapperHalJson objMapper_MT = new ObjectMapperHalJson(payload, "/_embedded/modeOfTransport");
			ObjectMapperHalJson objMapper_PE = new ObjectMapperHalJson(payload, "/_embedded/placeOfEntry");
			ObjectMapperHalJson objMapper_CargoLines = new ObjectMapperHalJson(payload, "/_embedded/cargoLines/_embedded/cargoLines");
			
			//Manifest Parent
			StringBuffer jsonToConvert = new StringBuffer();
			ManifestDto manifestDto = objMapper.getObjectMapper(jsonToConvert).readValue(jsonToConvert.toString(), new TypeReference<ManifestDto>() {});
			
			//Transp.Company
			if(objMapper_TC.isValidTargetNode()){
				jsonToConvert.delete(0, jsonToConvert.length());
				ManifestTransportationCompanyDto transportationCompanyDto = objMapper_TC.getObjectMapper(jsonToConvert).readValue(jsonToConvert.toString(), new TypeReference<ManifestTransportationCompanyDto>() {});
				manifestDto.setTransportationCompany(transportationCompanyDto);
			}
			//Active means of transp.
			if(objMapper_AMT.isValidTargetNode()){
				jsonToConvert.delete(0, jsonToConvert.length());
				ManifestActiveMeansOfTransportDto activeMeansOfTransportDto = objMapper_AMT.getObjectMapper(jsonToConvert).readValue(jsonToConvert.toString(), new TypeReference<ManifestActiveMeansOfTransportDto>() {});
				manifestDto.setActiveMeansOfTransport(activeMeansOfTransportDto);
			}
			//Mode of transp.
			if(objMapper_MT.isValidTargetNode()){
				jsonToConvert.delete(0, jsonToConvert.length());
				ManifestModeOfTransportDto modeOfTransportDto = objMapper_MT.getObjectMapper(jsonToConvert).readValue(jsonToConvert.toString(), new TypeReference<ManifestModeOfTransportDto>() {});
				manifestDto.setModeOfTransport(modeOfTransportDto);
			}
			//Place of entry
			if(objMapper_PE.isValidTargetNode()){
				jsonToConvert.delete(0, jsonToConvert.length());
				ManifestPlaceOfEntryDto placeOfEntryDto = objMapper_PE.getObjectMapper(jsonToConvert).readValue(jsonToConvert.toString(), new TypeReference<ManifestPlaceOfEntryDto>() {});
				manifestDto.setPlaceOfEntry(placeOfEntryDto);
			}
			//Cargo lines
			if(objMapper_CargoLines.isValidTargetNode()){
				jsonToConvert.delete(0, jsonToConvert.length());
				ArrayList<ManifestCargoLinesDto> cargoList = objMapper_CargoLines.getObjectMapper(jsonToConvert).readValue(jsonToConvert.toString(), new TypeReference<List<ManifestCargoLinesDto>>() {});
				manifestDto.setCargoLines(cargoList);
			}
			return manifestDto;
			
		}catch(Exception e){
			ManifestDto manifestDto = new ManifestDto();
			manifestDto.setManifestId(e.toString());
			return manifestDto;
			
		}finally{
			
			session.invalidate();
			
		}
		
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
