package no.systema.jservices.tvinn.expressfortolling.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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

import no.systema.jservices.common.dto.ModeOfTransportDto;
import no.systema.jservices.common.dto.TypesOfExportDto;
import no.systema.jservices.common.dto.TransportationCompanyDto;
import no.systema.jservices.tvinn.expressfortolling.TestJBase;
import no.systema.jservices.tvinn.kurermanifest.api.TestJApiKurerUploadClient;
import no.systema.main.util.ObjectMapperHalJson;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-configuration.xml")
@TestPropertySource(locations="classpath:application-test.properties")
public class TestJApiServices extends TestJBase {

	@Autowired
	ApiServices apiServices;
	
	private Authorization authorization;
	private static final Logger logger = Logger.getLogger(TestJApiServices.class);
	
	@Test
	public void testTransportationCompany() throws Exception {

		TransportationCompanyDto dto = apiServices.getTransportationCompany();
		System.out.println(dto);
		logger.info("DTO = "+dto);
		
	}
	
	@Test
	public void testModeOfTransport() throws Exception {

		ModeOfTransportDto dto = apiServices.getModeOfTransport();
		System.out.println(dto);
		logger.info("DTO = "+dto);
		
	}
	
	
	@Test
	public void testTypeOfExportAll() throws Exception {

		String json = apiServices.getAllTypeOfExport();
		logger.info("JSON = " + json);
		//map to Dto
		ObjectMapperHalJson objMapper = new ObjectMapperHalJson(json, "/_embedded/typesOfExport");
		StringBuffer jsonToConvert = new StringBuffer();
		//get list of DTOs
		ArrayList<TypesOfExportDto> exports = objMapper.getObjectMapper(jsonToConvert).readValue(jsonToConvert.toString(), new TypeReference<List<TypesOfExportDto>>() {
        });
		logger.info("DTO = " + exports.toString());
			
	}
	@Test
	public void testTypeOfExport() throws Exception {

		TypesOfExportDto dto = apiServices.getTypeOfExport("UGE_EXPORT");
		logger.info("DTO = " + dto.toString());
		
	}

}
