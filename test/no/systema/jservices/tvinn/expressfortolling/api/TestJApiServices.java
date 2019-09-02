package no.systema.jservices.tvinn.expressfortolling.api;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import no.systema.jservices.common.dto.TransportationCompanyDto;
import no.systema.jservices.tvinn.expressfortolling.TestJBase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-configuration.xml")
@TestPropertySource(locations="classpath:application-test.properties")
public class TestJApiServices extends TestJBase {

	@Autowired
	ApiServices apiServices;
	
	private Authorization authorization;
	
	
	@Test
	public void testTransportationCompany() throws Exception {

		List<TransportationCompanyDto> tc = apiServices.getTransportationCompany();
		
		System.out.println("tc = "+tc);
		
	}

}
