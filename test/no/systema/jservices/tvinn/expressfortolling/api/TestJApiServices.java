package no.systema.jservices.tvinn.expressfortolling.api;

import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
//@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration("classpath:test-configuration.xml")
@TestPropertySource(locations="classpath:application-test.properties")
public class TestJApiServices {

	{
		System.setProperty("catalina.home", "/usr/local/Cellar/tomcat/8.0.33/libexec");
	}
	
	@Autowired
	ApiServices apiServices;
	
	@Mock
	private Authorization authorization;
	
	
	@Test
	public void testTransportationCompany() throws Exception {

//		MockitoAnnotations.initMocks(Authorization.class);
//		
//		TokenResponseDto responseDto = new TokenResponseDto();
//		responseDto.setAccess_token("XYZ");
//		when(authorization.accessTokenRequestPost()).thenReturn(responseDto);
//		

	    List<String> tc = apiServices.getTransportationCompany();
		
		System.out.println("tc = "+tc);
		
	}

}
