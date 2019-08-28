package no.systema.jservices.tvinn.expressfortolling.jwt;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import io.jsonwebtoken.Claims;


@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:test-configuration.xml")
@TestPropertySource(locations="classpath:application-test.properties")
public class TestJDifiJwtCreator {

	@Autowired
	DifiJwtCreator difiJwtCreator;
	
	{
		System.setProperty("catalina.home", "/usr/local/Cellar/tomcat/8.0.33/libexec");
	}
	
	@Test
	public void testCreateJwt() throws Exception {
		
		String jwt = difiJwtCreator.createRequestJwt();
		
		System.out.println("jwt = "+jwt);
		
	}

	@Test
	public void testDecodeJwt() throws Exception {
		
		Claims jwtClaims = difiJwtCreator.decodeJWT();
		
		System.out.println("jwtClaims = "+jwtClaims);
		
	}	
	
	
	
}
