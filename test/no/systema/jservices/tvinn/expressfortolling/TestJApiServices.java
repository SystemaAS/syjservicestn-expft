package no.systema.jservices.tvinn.expressfortolling;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import no.systema.jservices.tvinn.expressfortolling.api.ApiServices;


@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:test-configuration.xml")
@TestPropertySource(locations="classpath:application-test.properties")
public class TestJApiServices {

	@Autowired
	ApiServices apiServices;
	
	{
		System.setProperty("catalina.home", "/usr/local/Cellar/tomcat/8.0.33/libexec");
	}
	

	
	
	@Test
	public void testTransportationCompany() throws Exception {
		List<String> tc = apiServices.getTransportationCompany();
		
		System.out.println("tc = "+tc);
		
	}

}
