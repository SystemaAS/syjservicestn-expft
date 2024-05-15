package no.systema.jservices.tvinn.expressfortolling.jwt;

import java.net.InetAddress;
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
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import no.systema.jservices.tvinn.kurermanifest.util.JwtUtils;

/**
 * @see <a href=
 *      "https://difi.github.io/idporten-oidc-dokumentasjon/oidc_auth_server-to-server-oauth2.html">
 *      Dokumentasjon hos difi</a>
 */
@Service
public class DifiJwtCreator {
	private static Logger logger = LoggerFactory.getLogger(DifiJwtCreator.class.getName());
	private final boolean IS_KURER = true;
	private final boolean ISNOT_KURER = false;
	
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
	
	@Value("${expft.scope.movement.road}")
	String scopeExpftMovementRoad;
	@Value("${expft.scope.movement.air}")
	String scopeExpftMovementAir;
	@Value("${expft.scope.movement.rail}")
	String scopeExpftMovementRail;
	//for air/routing
	@Value("${expft.scope.movement.entry}")
	String scopeExpftMovementEntry;
	
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

		try {
			encodedCertificate = certificateManager.getEncodedCertificate();
			privateKey = certificateManager.getPrivateKey();
		} catch (Exception e) {
			String message = "Could not manage X.509 in a correct way!";
			logger.error(message, e);
			throw new RuntimeException(message, e);
		}
		
		String result = this.getJwtString(this.expiration, this.scopeExpft, encodedCertificate, privateKey, this.ISNOT_KURER);
		return result;
		
	    
	}
	
	/**
	 * Nya expressfortolling (movement/road)
	 * @return
	 */
	public String createRequestMovementRoadJwt() {

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
		
		String result = this.getJwtString(this.expiration, this.scopeExpftMovementRoad, encodedCertificate, privateKey, this.ISNOT_KURER);
		return result;
		
	    
	}
	/**
	 * 
	 * @return
	 */
	public String createRequestMovementAirJwt() {
		logger.info("Inside createRequestMovementAirJwt");
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
		
		String result = this.getJwtString(this.expiration, this.scopeExpftMovementAir, encodedCertificate, privateKey, this.ISNOT_KURER);
		return result;
		
	    
	}
	public String createRequestMovementRailJwt() {
		logger.info("Inside createRequestMovementRailJwt");
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
		
		String result = this.getJwtString(this.expiration, this.scopeExpftMovementRail, encodedCertificate, privateKey, this.ISNOT_KURER);
		return result;
		
	    
	}
	public String createRequestMovementRoutingJwt() {
		logger.info("Inside: createRequestMovementRoutingJwt" );
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
		//Entry must use MovementRoad and NOT Movement Entry (ref to https://toll.github.io/api/maskinporten.html#scopes)
		//This Entry belongs to movement-road-query-api-v2
		String result = this.getJwtString(this.expiration, this.scopeExpftMovementEntry, encodedCertificate, privateKey, this.ISNOT_KURER);
		return result;
		
	    
	}
	
	public String createRequestMovementRoadEntryJwt() {
		logger.info("Inside: createRequestMovementRoadEntryJwt" );
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
		//Entry must use MovementRoad and NOT Movement Entry (ref to https://toll.github.io/api/maskinporten.html#scopes)
		//This Entry belongs to movement-road-query-api-v2
		String result = this.getJwtString(this.expiration, this.scopeExpftMovementRoad, encodedCertificate, privateKey, this.ISNOT_KURER);
		return result;
		
	    
	}
	public String createRequestMovementRailEntryJwt() {
		logger.info("Inside: createRequestMovementRailEntryJwt" );
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
		//Entry must use MovementRoad and NOT Movement Entry (ref to https://toll.github.io/api/maskinporten.html#scopes)
		//This Entry belongs to movement-road-query-api-v2
		String result = this.getJwtString(this.expiration, this.scopeExpftMovementRail, encodedCertificate, privateKey, this.ISNOT_KURER);
		return result;
		
	    
	}
	
	public String createRequestMovementAirEntryJwt() {
		logger.info("Inside: createRequestMovementAirEntryJwt" );
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
		//Entry must use MovementRoad and NOT Movement Entry (ref to https://toll.github.io/api/maskinporten.html#scopes)
		//This Entry belongs to movement-road-query-api-v2
		String result = this.getJwtString(this.expiration, this.scopeExpftMovementAir, encodedCertificate, privateKey, this.ISNOT_KURER);
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

		try {
			encodedCertificate = certificateManager.getEncodedCertificate();
			privateKey = certificateManager.getPrivateKey();
		} catch (Exception e) {
			String message = "Could not manage X.509 in a correct way!";
			logger.error(message, e);
			throw new RuntimeException(message, e);
		}

		String result = this.getJwtString(this.expirationKurer, this.scopeKurer, encodedCertificate, privateKey, this.IS_KURER);
		return result;
	
		
	}
	
	/**
	 * 
	 * @return
	 */
	public String createRequestDocsJwt() {

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
		
		String result = this.getJwtString(this.expiration, this.scopeExpftDocs, encodedCertificate, privateKey, this.ISNOT_KURER);
		return result;

		
	}
	
	/**
	 * Returns compact JWT string
	 * 
	 * @param expirationTime
	 * @param encodedCertificate
	 * @param scope
	 * @param privateKey
	 * @return
	 */
	private String getJwtString(int expirationLimitParam, String scopeParam, String encodedCertificateParam, 
								PrivateKey privateKeyParam, boolean isKurer){
		
		String result = null;
		
		//default
  		long expiration_l = expirationLimitParam;
  		
  		Instant issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
  	    Instant expiration = issuedAt.plus(expiration_l, ChronoUnit.SECONDS);
  	    
  	    JwtUtils jwtUtils = new JwtUtils();
  	    Instant atomicIssuedAt = jwtUtils.adjustTimeFromInternet();
  	    Instant atomicexpiration = jwtUtils.adjustTimeFromInternet(atomicIssuedAt, expiration_l);
  	    if(atomicIssuedAt!=null && atomicexpiration!=null){
  	    	issuedAt = atomicIssuedAt;
  	    	expiration = atomicexpiration;
  	    }
	    //Default
  	    String audience = this.difiTokenAudienceUrl;
  	    String issuer = this.issuer;
  	    //only kurer
  	    if(isKurer){
  	    	audience = this.difiTokenAudienceKurerUrl;
  	    	issuer = this.issuerKurer;
  	    }
  	    
		result =  Jwts.builder()
            	.setHeaderParam(JwsHeader.X509_CERT_CHAIN, Collections.singletonList(encodedCertificateParam))
            	.setAudience(audience)
            	.setIssuer(issuer)
            	.claim("scope", scopeParam)
            	.setId(UUID.randomUUID().toString())
            	.setIssuedAt(Date.from(issuedAt))
            	.setExpiration(Date.from(expiration))
            	.signWith(SignatureAlgorithm.RS256, privateKeyParam)
            	.compact();
	
				if(result!=null) {
					logger.info("createRequestJwt:" + result.substring(0,20) + " ... ");
				}else {
					logger.error("createRequestJwt: null ??? --> ERROR towards Difi Jwt ...") ;
				}
    			//for debugging purposes at customer site
				//logger.info("audience:" + audience);
				logger.info("scope:" + scopeParam);
				
    			jwtUtils.showJWTTimeParamsOnRequest(issuedAt, expiration, issuer);
	
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
