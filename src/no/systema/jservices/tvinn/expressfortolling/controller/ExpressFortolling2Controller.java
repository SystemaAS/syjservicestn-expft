package no.systema.jservices.tvinn.expressfortolling.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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
	 * 
	 * @param session
	 * @param user
	 * @param emavd
	 * @param empro
	 * @throws Exception
	 */
	@RequestMapping(value="postMasterConsignment.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public GenericDtoResponse createMasterConsignmentExpressMovementRoad(HttpServletRequest request , @RequestParam(value = "user", required = true) String user, 
																				@RequestParam(value = "emavd", required = true) String emavd,
																				@RequestParam(value = "empro", required = true) String empro) throws Exception {
		
		GenericDtoResponse dtoResponse = new GenericDtoResponse();
		logger.warn("Inside postMasterConsignment");
		//create new - master consignment at toll.no
		try {
			if(checkUser(user)) {
				logger.warn("user OK:" + user);
				List<SadexmfDto> list = sadexService.getSadexmf(user, emavd, empro);
				if(list != null) {
					logger.warn("list size:" + list.size());
					
					for (SadexmfDto sadexmfDto: list) {
						MasterConsignment mc =  new MapperMasterConsignment().mapMasterConsignment(sadexmfDto);
						logger.warn("Representative:" + mc.getRepresentative().getName());
						String json = apiServices.postMasterConsignmentExpressMovementRoad(mc);
						TesterLrn obj = new ObjectMapper().readValue(json, TesterLrn.class);
						logger.warn("JSON = " + json);
						logger.warn("LRN = " + obj.getLrn());
						
						dtoResponse.setUser(user);
						dtoResponse.setAvd(emavd);
						dtoResponse.setPro(empro);
						
					}
				}
			}
			
		}catch(Exception e) {
			e.printStackTrace();
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
