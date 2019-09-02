package no.systema.jservices.tvinn.expressfortolling.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import no.systema.jservices.tvinn.expressfortolling.TestJBase;
import no.systema.jservices.tvinn.expressfortolling.api.Authorization;
import no.systema.jservices.tvinn.expressfortolling.api.TokenResponseDto;


@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:test-configuration.xml")
@TestPropertySource(locations="classpath:application-test.properties")
public class TestJAuthorization extends TestJBase {

	@Autowired
	Authorization authorization;
	
	@Test
	public void testAccessTokenRequestPost() throws Exception {
		TokenResponseDto responseDto  = authorization.accessTokenRequestPost();
		
		System.out.println("responseDto = "+responseDto);
		
	}

}
