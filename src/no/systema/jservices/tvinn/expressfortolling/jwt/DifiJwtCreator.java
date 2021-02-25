package no.systema.jservices.tvinn.expressfortolling.jwt;

import java.security.PrivateKey;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
		
		//final long now = Clock.systemUTC().millis();
		long expiration_l = expiration;
		Instant issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
	    Instant expiration = issuedAt.plus(expiration_l, ChronoUnit.SECONDS);
	    
		String result =  Jwts.builder()
	            	.setHeaderParam(JwsHeader.X509_CERT_CHAIN, Collections.singletonList(encodedCertificate))
	            	.setAudience(difiTokenAudienceUrl)
	            	.setIssuer(issuer)
	            	//.claim("iss_onbehalfof", "toll-onbehalfof") //TODO when the sun is shining
	            	.claim("scope", this.scopeExpft)
	            	.setId(UUID.randomUUID().toString())
	            	.setIssuedAt(Date.from(issuedAt))
	            	.setExpiration(Date.from(expiration))
	            	.signWith(SignatureAlgorithm.RS256, privateKey)
	            	.compact();
	    logger.info("createRequestJwt:" + result);
	    //for debugging purposes at customer site
		this.showJWTTimeParamsOnRequest(issuedAt, expiration);
		
		return result;
	    
	    
	}
	private void showJWTTimeParamsOnRequest(Instant issuedAt, Instant expiration ){
		//In format:2021-02-25T11:11:40
		////DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.from(ZoneOffset.UTC));
		//DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault());
		//logger.warn(formatter.format(issuedAt));
		//logger.warn(formatter.format(expiration));
		//UTC debug
		logger.info("UTC time - issuedAt:" + Date.from(issuedAt));
		logger.info("UTC time - expiration:" + Date.from(expiration));
		//In seconds
		logger.warn("As in JWT(issuedAt - seconds):" + String.valueOf(issuedAt.toEpochMilli()/1000));
		logger.warn("As in JWT(expiration - seconds):" + String.valueOf(expiration.toEpochMilli()/1000));
		logger.warn("As in JWT(issuer - clientID):" + issuer);
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

	
		long expirationKurer_l = expirationKurer;
		Instant issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
	    Instant expiration = issuedAt.plus(expirationKurer_l, ChronoUnit.SECONDS);
		
		String result =  Jwts.builder()
	            	.setHeaderParam(JwsHeader.X509_CERT_CHAIN, Collections.singletonList(encodedCertificate))
	            	.setHeaderParam(JwsHeader.TYPE, JwsHeader.JWT_TYPE)
	            	.setAudience(this.difiTokenAudienceKurerUrl)
	            	.setIssuer(this.issuerKurer)
//	            	.claim("iss_onbehalfof", "toll-onbehalfof") //TODO when the sun is shining
	            	.claim("scope", this.scopeKurer)
	            	.setId(UUID.randomUUID().toString())
	            	.setIssuedAt(Date.from(issuedAt))
	            	.setExpiration(Date.from(expiration))
	            	.signWith(SignatureAlgorithm.RS256, privateKey)
	            	.compact();
		
		logger.info("createRequestJwt:" + result);
	    
		//for debugging purposes at customer site
		this.showJWTTimeParamsOnRequest(issuedAt, expiration);
		
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

		
		long expiration_l = expiration;
		Instant issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
	    Instant expiration = issuedAt.plus(expiration_l, ChronoUnit.MINUTES);
		
		String result =  Jwts.builder()
	            	.setHeaderParam(JwsHeader.X509_CERT_CHAIN, Collections.singletonList(encodedCertificate))
	            	.setAudience(difiTokenAudienceUrl)
	            	.setIssuer(issuer)
	            	.claim("scope", this.scopeExpftDocs)
	            	.setId(UUID.randomUUID().toString())
	            	.setIssuedAt(Date.from(issuedAt))
	            	.setExpiration(Date.from(expiration))
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
