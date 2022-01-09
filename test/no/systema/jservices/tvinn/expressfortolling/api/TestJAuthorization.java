package no.systema.jservices.tvinn.expressfortolling.api;

import org.slf4j.*;
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
	private static Logger logger = LoggerFactory.getLogger(TestJAuthorization.class);

	@Autowired
	Authorization authorization;
	
	
	@Test
	public void testAccessToken() throws Exception {
		TokenResponseDto responseDto  = authorization.accessTokenRequestPost();
		
		logger.info("responseDto = "+responseDto);
		
	}
	
	/*@Test
	public void accessTokenForKurer() throws Exception {
		String urlUploadImmutable = "test";
		TokenResponseDto responseDto  = authorization.accessTokenForKurerRequestPost(urlUploadImmutable);
		
		logger.info("responseDto = "+responseDto);
		
	}*/
	
	@Test
	public void accessTokenForDocs() throws Exception {
		TokenResponseDto responseDto  = authorization.accessTokenForDocsRequestPost();
		
		logger.info("responseDto = "+responseDto);
		
	}
	

}
