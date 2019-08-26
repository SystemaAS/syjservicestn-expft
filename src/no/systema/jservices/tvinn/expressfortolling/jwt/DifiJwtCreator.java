package no.systema.jservices.tvinn.expressfortolling.jwt;

import java.security.KeyFactory;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * @see <a href=
 *      "https://difi.github.io/idporten-oidc-dokumentasjon/oidc_auth_server-to-server-oauth2.html">
 *      Dokumentasjon hos difi</a>
 */
@Service
public class DifiJwtCreator {
	private static Logger logger = Logger.getLogger(DifiJwtCreator.class.getName());
	private Duration expiration;

	@Autowired
	private CertManager certificateManager;

	@Value("${expft.audience}")
	String difiTokenAudienceUrl;

	/**
	 * Create a JWT based on X.509 certificate.
	 * Certificate is issued per customer.
	 * 
	 * @return a String to provide in API calls.
	 */
	public String createRequestJwt() {

		String encodedCertificate;
		PrivateKey privateKey;
		try {
			encodedCertificate = certificateManager.getEncodedCertificate();
			privateKey = certificateManager.getPrivateKey();
		} catch (Exception e) {
			String message = "Could not manage X.509 in a correct way!";
			logger.error(message, e);
			throw new RuntimeException(message, e);
		}

		final long now = Clock.systemUTC().millis();
	    return Jwts.builder()
	            .setHeaderParam(JwsHeader.X509_CERT_CHAIN, Collections.singletonList(encodedCertificate))
	            .setAudience(difiTokenAudienceUrl)
	            .setIssuer("oidc_tolletaten")
	            .claim("iss_onbehalfof", "toll-onbehalfof")
	            .claim("scope", "toll:ekspressfortolling")
	            .setId(UUID.randomUUID().toString())
	            .setIssuedAt(new Date(now))
	            .setExpiration(new Date(now + expiration.toMillis()))
	            .signWith(SignatureAlgorithm.RS256, privateKey)
	            .compact();
		
	}

}
