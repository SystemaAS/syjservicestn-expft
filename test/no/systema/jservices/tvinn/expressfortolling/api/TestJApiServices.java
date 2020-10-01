package no.systema.jservices.tvinn.expressfortolling.api;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.systema.jservices.common.dto.*;
import no.systema.jservices.common.dto.expressfortolling.ManifestCargoLinesDto;
import no.systema.jservices.common.dto.expressfortolling.ManifestCountryDto;
import no.systema.jservices.common.dto.expressfortolling.ManifestDto;
import no.systema.jservices.common.dto.expressfortolling.ManifestModeOfTransportDto;
import no.systema.jservices.common.dto.expressfortolling.ManifestTypesOfExportDto;
import no.systema.jservices.common.dto.expressfortolling.ManifestTypesOfMeansOfTransportDto;
import no.systema.jservices.common.dto.expressfortolling.ManifestTransportationCompanyDto;
import no.systema.jservices.common.dto.expressfortolling.ManifestUserDto;
import no.systema.jservices.tvinn.expressfortolling.TestJBase;
import no.systema.main.util.ObjectMapperHalJson;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-configuration.xml")
@TestPropertySource(locations="classpath:application-test.properties")
public class TestJApiServices extends TestJBase {

	@Autowired
	ApiServices apiServices;
	
	private Authorization authorization;
	private static final Logger logger = Logger.getLogger(TestJApiServices.class);
	//0eb9f81d-3385-4baa-95aa-07c73d4d8fd3 ORIG-simple-test
	//private final String manifestId = "2350cab2-98f0-4b54-a4f7-a2ae453e61bd";
	private final String manifestId = "e35a52a6-18ae-4746-a4b4-9e3f0edbacc6"; //YANGs test with Updates
	
	
	
	@Test //OK
	public void getTransportationCompany() throws Exception {
		String SYSTEMA_ORGNR = "936809219";
		ManifestTransportationCompanyDto dto = apiServices.getTransportationCompany(SYSTEMA_ORGNR);
		logger.info("DTO = "+dto);
		
	}
	
	@Test //OK
	public void getManifest() throws Exception {
		String json = apiServices.getManifest(this.manifestId);
		logger.info("JSON = " + json);
		//map to Dto
		ObjectMapperHalJson objMapper = new ObjectMapperHalJson(json, "");
		ObjectMapperHalJson objMapper_TC = new ObjectMapperHalJson(json, "/_embedded/transportationCompany");
		
		StringBuffer jsonToConvert = new StringBuffer();
		ManifestDto manifestDto = objMapper.getObjectMapper(jsonToConvert).readValue(jsonToConvert.toString(), new TypeReference<ManifestDto>() { });
		//initialize buffer
		jsonToConvert.delete(0, jsonToConvert.length());
		ManifestTransportationCompanyDto transportationCompanyDto = objMapper_TC.getObjectMapper(jsonToConvert).readValue(jsonToConvert.toString(), new TypeReference<ManifestTransportationCompanyDto>() {
        });
		logger.info("DTO raw transpComp:" + transportationCompanyDto.toString());
		manifestDto.setTransportationCompany(transportationCompanyDto);
		
		logger.info("DTO raw:" + manifestDto.toString());
		logger.info("Manifest STATUS:" + manifestDto.getStatus());
		//return DTO in JSON
		ObjectMapper mapper = new ObjectMapper();
		logger.info("Dto as JSON:" + mapper.writeValueAsString(manifestDto));
	}
	
	@Test //OK
	public void getManifestCargoLines() throws Exception {
		String json = apiServices.getManifestCargoLines(this.manifestId);
		logger.info("JSON = " + json);
		//map to Dto
		ObjectMapperHalJson objMapper = new ObjectMapperHalJson(json, "/_embedded/cargoLines");
		StringBuffer jsonToConvert = new StringBuffer();
		//get list of DTOs
		ArrayList<ManifestCargoLinesDto> exports = objMapper.getObjectMapper(jsonToConvert).readValue(jsonToConvert.toString(), new TypeReference<List<ManifestCargoLinesDto>>() {
        });
		logger.info("DTO = " + exports.toString());
					
	}
	@Test //OK
	public void deleteManifest() throws Exception {
		String json = apiServices.deleteManifest(this.manifestId);
		logger.info("JSON = " + json);	
	}
	
	@Test //OK
	public void updateStatusManifest() throws Exception {
		String json = apiServices.updateStatusManifest(this.manifestId, "REOPENED");
		logger.info("JSON = " + json);	
	}
	
	//TEST CREATE and UPDATE
	//OBS!!! Create and update manifest are tested from ApiUploadClient (from file)
	
	
	
	@Test //OK
	public void createManifestCargoLine() throws Exception {
		String payload = " { \"typeOfImportProcedure\" : \"IMMEDIATE_RELEASE_IMPORT\" } ";
		String json = apiServices.createManifestCargoLine(this.manifestId, payload);
		logger.info("JSON = " + json);	
	}
	
	
	
	@Test //OK
	public void testTypeOfMeansOfTransportAll() throws Exception {
		String json = apiServices.getAllTypeOfMeansOfTransport();
		logger.info("JSON = " + json);
		//map to Dto
		ObjectMapperHalJson objMapper = new ObjectMapperHalJson(json, "/_embedded/typesOfMeansOfTransport");
		StringBuffer jsonToConvert = new StringBuffer();
		//get list of DTOs
		ArrayList<ManifestTypesOfMeansOfTransportDto> exports = objMapper.getObjectMapper(jsonToConvert).readValue(jsonToConvert.toString(), new TypeReference<List<ManifestTypesOfMeansOfTransportDto>>() {
        });
		logger.info("DTO = " + exports.toString());
			
	}
	
	@Test //OK
	public void testTypeOfMeansOfTransport() throws Exception {
		String json = apiServices.getTypeOfMeansOfTransport("VEHICLE_WITH_TRAILER");
		logger.info("JSON = " + json);
		//map to Dto
		ObjectMapperHalJson objMapper = new ObjectMapperHalJson(json,"");
		StringBuffer jsonToConvert = new StringBuffer();
		//get list of DTOs
		ManifestTypesOfMeansOfTransportDto dto = objMapper.getObjectMapper(jsonToConvert).readValue(jsonToConvert.toString(), new TypeReference<ManifestTypesOfMeansOfTransportDto>() {
        });
		logger.info("DTO = " + dto.toString());
			
	}
	
	@Test //OK
	public void getActiveMeansOfTransport() throws Exception {
		String json = apiServices.getActiveMeansOfTransport(this.manifestId);
		logger.info("JSON = " + json);
	}
	
	
	@Test //OK
	public void testModeOfTransportAll() throws Exception {
		String json = apiServices.getModeOfTransportAll();
		logger.info("JSON = " + json);
		//map to Dto
		ObjectMapperHalJson objMapper = new ObjectMapperHalJson(json, "/_embedded/modesOfTransport");
		StringBuffer jsonToConvert = new StringBuffer();
		//get list of DTOs
		ArrayList<ManifestModeOfTransportDto> list = objMapper.getObjectMapper(jsonToConvert).readValue(jsonToConvert.toString(), new TypeReference<List<ManifestModeOfTransportDto>>() {
        });
		logger.info("DTO = " + list.toString());
	}
	
	@Test //OK
	public void testModeOfTransport() throws Exception {
		ManifestModeOfTransportDto dto = apiServices.getModeOfTransport("BIL");
		logger.info("DTO = "+dto);
	}
	
	@Test //OK
	public void testAllCountries() throws Exception {
		String json = apiServices.getAllCountries();
		logger.info("JSON = " + json);
		//map to Dto
		//map to Dto
		ObjectMapperHalJson objMapper = new ObjectMapperHalJson(json, "/_embedded/countries");
		StringBuffer jsonToConvert = new StringBuffer();
		//get list of DTOs
		ArrayList<ManifestCountryDto> list = objMapper.getObjectMapper(jsonToConvert).readValue(jsonToConvert.toString(), new TypeReference<List<ManifestCountryDto>>() {
        });
		logger.info("DTO = " + list.toString());
	}
	
	@Test //OK
	public void testCountry() throws Exception {
		ManifestCountryDto dto = apiServices.getCountry("NO");
		System.out.println(dto);
		logger.info("DTO = "+dto);
	}
	
	@Test //OK
	public void testUser() throws Exception {
		String json = apiServices.getUser();
		logger.info("JSON = " + json);
		//map to Dto
		ObjectMapperHalJson objMapper = new ObjectMapperHalJson(json, "");
		StringBuffer jsonToConvert = new StringBuffer();
		//get list of DTOs
		ManifestUserDto dto = objMapper.getObjectMapper(jsonToConvert).readValue(jsonToConvert.toString(), new TypeReference<ManifestUserDto>() {
        });
		logger.info("DTO = " + dto.toString());
	}

	
	@Test //OK
	public void testTypeOfExportAll() throws Exception {
		String json = apiServices.getAllTypeOfExport();
		logger.info("JSON = " + json);
		//map to Dto
		ObjectMapperHalJson objMapper = new ObjectMapperHalJson(json, "/_embedded/typesOfExport");
		StringBuffer jsonToConvert = new StringBuffer();
		//get list of DTOs
		ArrayList<ManifestTypesOfExportDto> exports = objMapper.getObjectMapper(jsonToConvert).readValue(jsonToConvert.toString(), new TypeReference<List<ManifestTypesOfExportDto>>() {
        });
		logger.info("DTO = " + exports.toString());	
	}
	
	@Test //OK
	public void testTypeOfExport() throws Exception {
		ManifestTypesOfExportDto dto = apiServices.getTypeOfExport("UGE_EXPORT");
		logger.info("DTO = " + dto.toString());
	}
	
	@Test //OK
	public void getPlaceOfEntryAll() throws Exception {
		String json = apiServices.getAllPlaceOfEntry();
		logger.info("JSON = " + json);
	}
	
	
	
	
}
