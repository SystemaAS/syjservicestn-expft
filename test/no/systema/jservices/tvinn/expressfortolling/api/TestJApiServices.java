package no.systema.jservices.tvinn.expressfortolling.api;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import no.systema.jservices.common.dto.ModeOfTransportDto;
import no.systema.jservices.common.dto.TransportationCompanyDto;
import no.systema.jservices.tvinn.expressfortolling.TestJBase;
import no.systema.jservices.tvinn.kurermanifest.api.TestJApiKurerUploadClient;

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

}
