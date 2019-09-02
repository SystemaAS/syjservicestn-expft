package no.systema.jservices.tvinn.expressfortolling.jwt;

import java.security.PrivateKey;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import no.systema.jservices.tvinn.expressfortolling.TestJBase;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:test-configuration.xml")
@TestPropertySource(locations="classpath:application-test.properties")
public class TestJCertManager extends TestJBase {

	@Autowired
	CertManager certManager;

	@Test
	public void testCertManager() throws Exception {
		String encodedCertificate =certManager.getEncodedCertificate();
		System.out.println("encodedCertificate= "+encodedCertificate);
		
		PrivateKey privateKey = certManager.getPrivateKey();
		
	}
	
}
