package no.systema.jservices.tvinn.expressfortolling.api;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.*;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import no.systema.jservices.common.dao.services.BridfDaoService;
import no.systema.jservices.common.dto.*;
import no.systema.jservices.common.dto.expressfortolling.ManifestActiveMeansOfTransportDto;
import no.systema.jservices.common.dto.expressfortolling.ManifestCargoLinesDto;
import no.systema.jservices.common.dto.expressfortolling.ManifestCountryDto;
import no.systema.jservices.common.dto.expressfortolling.ManifestDto;
import no.systema.jservices.common.dto.expressfortolling.ManifestModeOfTransportDto;
import no.systema.jservices.common.dto.expressfortolling.ManifestPlaceOfEntryDto;
import no.systema.jservices.common.dto.expressfortolling.ManifestTypesOfExportDto;
import no.systema.jservices.common.dto.expressfortolling.ManifestTypesOfMeansOfTransportDto;
import no.systema.jservices.common.dto.expressfortolling.ManifestTransportationCompanyDto;
import no.systema.jservices.common.dto.expressfortolling.ManifestUserDto;
import no.systema.jservices.tvinn.digitoll.v2.dao.Transport;
import no.systema.jservices.tvinn.digitoll.v2.services.MapperTransport;
import no.systema.jservices.tvinn.expressfortolling.TestJBase;
import no.systema.jservices.tvinn.expressfortolling2.dao.HouseConsignment;
import no.systema.jservices.tvinn.expressfortolling2.dao.MasterConsignment;
import no.systema.jservices.tvinn.expressfortolling2.dto.SadexhfDto;
import no.systema.jservices.tvinn.expressfortolling2.services.MapperHouseConsignment;
import no.systema.jservices.tvinn.expressfortolling2.util.GenericJsonStringPrinter;
import no.systema.main.util.ObjectMapperHalJson;

@RunWith(SpringJUnit4ClassRunner.class)
//@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:test-configuration.xml")
@TestPropertySource(locations="classpath:application-test.properties")
//@ContextConfiguration(classes = {ApiServices.class})

public class TestJApiServicesDigitollV2 extends TestJBase {

	@Autowired
	ApiServices apiServices;
	
	
	private Authorization authorization;
	private static final Logger logger = LoggerFactory.getLogger(TestJApiServicesDigitollV2.class);
	//0eb9f81d-3385-4baa-95aa-07c73d4d8fd3 ORIG-simple-test
	//private final String manifestId = "2350cab2-98f0-4b54-a4f7-a2ae453e61bd";
	private final String manifestId = "e35a52a6-18ae-4746-a4b4-9e3f0edbacc6";
	
	
	
	
	
		//////////////////////////////
	//nya exprf. movement road
	/////////////////////////////
	@Test //OK
	public void testAuthExpressMovementRoad() throws Exception {
		String json = apiServices.testAuthExpressMovementRoad();
		System.out.println("JSON = " + json);
	}
	
	@Test //for validating the raw json swagger spec
	public void createTransport() throws Exception {
		//this will be populated by the SADEXMF Dto in real-world. We can not test it here unfortunately ...
		Transport transport = new MapperTransport().mapTransport(new Object()); 
		//Debug
		System.out.println(GenericJsonStringPrinter.debug(transport));
		
	}
	
	
	
	
	
	
}
