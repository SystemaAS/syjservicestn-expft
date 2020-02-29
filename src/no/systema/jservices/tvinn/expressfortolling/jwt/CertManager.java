package no.systema.jservices.tvinn.expressfortolling.jwt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CertManager {
	private static Logger logger = Logger.getLogger(CertManager.class.getName());
	private static String CATALINA_HOME = System.getProperty("catalina.home");

    @Value("${expft.keystore.file}")
    private String file;		

    @Value("${expft.keystore.password}")
    private String keystorePassword;    

    @Value("${expft.keystore.alias}")
    private String keystoreAlias;     
    
    private KeyStore keyStore;
    
    @PostConstruct
    public void init() {
    	try {
    		
			loadCertificate();
			
		} catch (Exception e) {
			String message = "Error loading certificate!";
			logger.error(message, e);
			throw new RuntimeException(message, e);
		}
    }

    /**
     * loads certificate into Keystore and extract it into encoded form.
     * 
     * @return a Base64 encoded string
     */
	public String getEncodedCertificate() {
		final String encodedCertificate;
		try {

			X509Certificate cert = (X509Certificate) keyStore.getCertificate(keystoreAlias);
			encodedCertificate = Base64.getEncoder().encodeToString(cert.getEncoded());

		} catch (Exception e) {
			String message = "Error retrieving encoded certificate!";
			logger.error(message, e);
			throw new RuntimeException(message, e);
		}

		return encodedCertificate;

	}
	
	
	/**
	 * Extract PrivateKey from Keystore.
	 * 
	 * @return a PrivateKey on alias.
	 * @throws UnrecoverableKeyException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 */
	public PrivateKey getPrivateKey() throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
		PrivateKey privateKey = (PrivateKey) keyStore.getKey(keystoreAlias, keystorePassword.toCharArray()); // Read from KeyStore		
		
		return privateKey;
		
	}
	
    /**
	 * Looks i catalina.home/espedsg2/certificates/expft after certificate-file
	 * and loads it into KeyStore
	 * 
	 * @return InputStream the located .p12 file
	 * @throws KeyStoreException 
	 * @throws IOException 
	 * @throws CertificateException 
	 * @throws NoSuchAlgorithmException 
	 */
	private void loadCertificate() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		char[] password = keystorePassword.toCharArray();
		keyStore = KeyStore.getInstance("PKCS12");  //JKS?
		keyStore.load(getCertificateFile(), password);
	}	
	
	private InputStream getCertificateFile() throws FileNotFoundException {
		String fullFilePath = file;
		
		if(CATALINA_HOME!=null){
			fullFilePath = CATALINA_HOME + file;
		}
		File certificateFile = FileUtils.getFile(fullFilePath);
		
		return new FileInputStream(certificateFile);
	}

	
}
