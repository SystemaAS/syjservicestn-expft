package no.systema.jservices.tvinn.expressfortolling.jwt;

import java.security.PrivateKey;
import java.time.Clock;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtBuilder;
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

	@Value("${expft.issuer}")
	String issuer;
	
	@Value("${kurer.issuer}")
	String issuerKurer;
	
	/**
	 * Audience - identifikator for ID-portens OIDC Provider. 
	 * Se ID-portens well-known-endepunkt for aktuelt miljø for å finne riktig verdi.
	 */
	@Value("${expft.audience}")
	String difiTokenAudienceUrl;
	
	@Value("${expft.scope}")
	String scopeExpft;
	
	@Value("${expft.scope.docs}")
	String scopeExpftDocs;
	
	
	@Value("${kurer.audience}")
	String difiTokenAudienceKurerUrl;
	
	@Value("${kurer.scope}")
	String scopeKurer;

	/**
	 * expiration time - tidsstempel for når jwt’en utløper - 
	 * MERK: Tidsstempelet tar utgangspunkt i UTC-tid 
	 * MERK: ID-porten godtar kun maks levetid på jwt’en til 120 sekunder (exp - iat <= 120 )
	 */
	@Value("${expft.token.expiration}")
	int expiration;
	
	@Value("${kurer.token.expiration}")
	int expirationKurer;
	
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
		
		String result =  Jwts.builder()
	            	.setHeaderParam(JwsHeader.X509_CERT_CHAIN, Collections.singletonList(encodedCertificate))
	            	.setAudience(difiTokenAudienceUrl)
	            	.setIssuer(issuer)
//	            	.claim("iss_onbehalfof", "toll-onbehalfof") //TODO when the sun is shining
	            	.claim("scope", this.scopeExpft)
	            	.setId(UUID.randomUUID().toString())
	            	.setIssuedAt(new Date(now))
	            	.setExpiration(new Date(now + expiration))
	            	.signWith(SignatureAlgorithm.RS256, privateKey)
	            	.compact();
	    logger.info("createRequestJwt:" + result);
		return result;
	    
	    
	}
	
	
	/**
	 * Create a JWT based on X.509 certificate.
	 * Certificate is issued per customer.
	 * 
	 * @return a String to provide in API calls.
	 */
	public String createRequestKurerJwt() {

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
		
		
		String result =  Jwts.builder()
	            	.setHeaderParam(JwsHeader.X509_CERT_CHAIN, Collections.singletonList(encodedCertificate))
	            	.setHeaderParam(JwsHeader.TYPE, JwsHeader.JWT_TYPE)
	            	.setAudience(this.difiTokenAudienceKurerUrl)
	            	.setIssuer(this.issuerKurer)
//	            	.claim("iss_onbehalfof", "toll-onbehalfof") //TODO when the sun is shining
	            	.claim("scope", this.scopeKurer)
	            	.setId(UUID.randomUUID().toString())
	            	.setIssuedAt(new Date(now))
	            	.setExpiration(new Date(now + this.expirationKurer))
	            	.signWith(SignatureAlgorithm.RS256, privateKey)
	            	.compact();
		
		/* DEBUG payload 
		try{
			String[] pieces = result.split("\\.");
			String b64header = pieces[0];
			String b64payload = pieces[1];
			String header = new String(Base64.decodeBase64(b64header), "UTF-8");
			String payload = new String(Base64.decodeBase64(b64payload), "UTF-8");
			logger.info(header);
			logger.info(payload);
			
		}catch(Exception e){
			e.printStackTrace();
		} 
		*/
		return result;  
	    
	}
	
	/**
	 * 
	 * @return
	 */
	public String createRequestDocsJwt() {

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
		
		String result =  Jwts.builder()
	            	.setHeaderParam(JwsHeader.X509_CERT_CHAIN, Collections.singletonList(encodedCertificate))
	            	.setAudience(difiTokenAudienceUrl)
	            	.setIssuer(issuer)
	            	.claim("scope", this.scopeExpftDocs)
	            	.setId(UUID.randomUUID().toString())
	            	.setIssuedAt(new Date(now))
	            	.setExpiration(new Date(now + expiration))
	            	.signWith(SignatureAlgorithm.RS256, privateKey)
	            	.compact();
	    logger.info("createRequestJwt:" + result);
		return result;
	    
	    
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
