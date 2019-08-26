package no.systema.jservices.tvinn.expressfortolling.jwt;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
//import javax.security.cert.X509Certificate;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Enumeration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:test-configuration.xml")
@TestPropertySource(locations="classpath:application-test.properties")
public class TestJCertManager {

	@Autowired
	CertManager certManager;
	
	{
		System.setProperty("catalina.home", "/usr/local/Cellar/tomcat/8.0.33/libexec");
	}
	
	@Test
	public void testCertManager() throws Exception {
		
		String encodedCertificate =certManager.getEncodedCertificate();
		
		System.out.println("encodedCertificate= "+encodedCertificate);
		
		PrivateKey privateKey = certManager.getPrivateKey();
		
		
	}
	
	
	@Test
	public void WTF() throws Exception {

		CertificateFactory cf = CertificateFactory.getInstance("X.509");

		
		char[] password = "NYzHtVLzasnPDbhe".toCharArray();
		KeyStore keyStore = KeyStore.getInstance("PKCS12");
		keyStore.load(getFile(), password);

		System.out.println("alias = "+keyStore.aliases());

		Enumeration<String> al = keyStore.aliases();
		while (al.hasMoreElements()) {
			String string = (String) al.nextElement();
			System.out.println("string="+string);
		}
		
		X509Certificate cert = (X509Certificate) keyStore.getCertificate("systema as test");

		System.out.println("X509Certificate cert is = " + cert.getSignature());
		

		 final String certificateS = Base64.getEncoder().encodeToString(cert.getEncoded());

		 
		 System.out.println("certificateS  = "+certificateS); //Jippi!!!!
		

	}

	private InputStream getFile() {
		String certFile = "Buypass ID-SYSTEMA AS-serienummer2580954729092660806470032-2019-08-19.p12";

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		InputStream is = classLoader.getResourceAsStream("no/systema/jservices/tvinn/expressfortolling/jwt/" + certFile);

	
		
		
		
		
		System.out.println("InputStream is = " + is);

		return is;

	}

	// @Test
	// public void WTF2() throws Exception {
	// String certFile =
	// "Buypass_ID-BAREKSTAD_OG_YTTERVÅG_REGNSKAP-serienummer550498454741797052332932-2015-06-24.p12";
	//
	// ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
	//
	// InputStream is =
	// classLoader.getResourceAsStream("no/systema/jservices/common/jwt/nimbus/"
	// + certFile);
	//
	//
	// String targetString = IOUtils.readInputStreamToString(is,
	// StandardCharset.UTF_8);
	//
	//
	// X509Certificate cert = X509CertUtils.parse(targetString);
	//
	// System.out.println("WTF2, cert="+cert);
	//
	// }

}
