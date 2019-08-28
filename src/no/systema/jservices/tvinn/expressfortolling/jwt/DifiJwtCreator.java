package no.systema.jservices.tvinn.expressfortolling.jwt;

import java.security.PrivateKey;
import java.time.Clock;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
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

	@Autowired
	private CertManager certificateManager;

	/**
	 * Audience - identifikator for ID-portens OIDC Provider. 
	 * Se ID-portens well-known-endepunkt for aktuelt miljø for å finne riktig verdi.
	 */
	@Value("${expft.audience}")
	String difiTokenAudienceUrl;

	/**
	 * expiration time - tidsstempel for når jwt’en utløper - 
	 * MERK: Tidsstempelet tar utgangspunkt i UTC-tid 
	 * MERK: ID-porten godtar kun maks levetid på jwt’en til 120 sekunder (exp - iat <= 120 )
	 */
	@Value("${expft.token.expiration}")
	int expiration;
	
	/**
	 * Create a JWT based on X.509 certificate.
	 * Certificate is issued per customer.
	 * 
	 * @return a String to provide in API calls.
	 */
	public String createRequestJwt() {

		String encodedCertificate;
		PrivateKey privateKey;
		String jwt;
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
	            .setExpiration(new Date(now + expiration))
	            .signWith(SignatureAlgorithm.RS256, privateKey)
	            .compact();
	    
	}
	
	/**
	 * Convenience method for checking the JWT result.
	 * 
	 * @return payload values inside created JWT
	 * @throws Exception
	 */
	public Claims decodeJWT() throws Exception{
	    Claims claims = Jwts.parser()
	            .setSigningKey(certificateManager.getPrivateKey())
	            .parseClaimsJws(createRequestJwt()).getBody();
	    return claims;
	}

}
