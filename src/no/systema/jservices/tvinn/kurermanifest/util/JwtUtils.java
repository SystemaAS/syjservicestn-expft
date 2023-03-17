package no.systema.jservices.tvinn.kurermanifest.util;

import java.net.InetAddress;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;
import org.slf4j.*;

import no.systema.jservices.tvinn.expressfortolling.jwt.DifiJwtCreator;

public class JwtUtils {
	private static Logger logger = LoggerFactory.getLogger(JwtUtils.class.getName());
	private final String TIME_SERVER = "time-a.nist.gov";
	/**
	 * adjusts to the atomic time from internet (if within the time out)
	 * @param issuedAt
	 * @param expiration_l
	 * @return
	 */
	public Instant adjustTimeFromInternet(){
		Instant instant = null;
		//Change if possible since the server time has been an issue towards Maskinporten. We use internet atomic time if possible
		try{
			
			NTPUDPClient timeClient = new NTPUDPClient();
			timeClient.setDefaultTimeout(3000);
			InetAddress inetAddress = InetAddress.getByName(TIME_SERVER);
			TimeInfo timeInfo = timeClient.getTime(inetAddress);
			NtpV3Packet message = timeInfo.getMessage();
			long serverTime = message.getTransmitTimeStamp().getTime();

			Clock clock = Clock.fixed(Instant.ofEpochMilli(serverTime), ZoneId.of("Europe/Oslo"));
			
			instant = Instant.now(clock).truncatedTo(ChronoUnit.SECONDS);
			logger.warn("Atomic internet time - issuedAt:" + Date.from(instant));
			
		}catch(Exception e){
			logger.warn("Atomic internet time ERROR: " + e.toString());
		}
		
		return instant;
		
	}
	
	
	/**
	 * adjust expiration instant
	 * @param issuedAt
	 * @param expiration_l
	 * @return
	 */
	public Instant adjustTimeFromInternet(Instant issuedAt, long expiration_l){
		Instant instant = null;
		try{
			instant = issuedAt.plus(expiration_l, ChronoUnit.SECONDS);
		    logger.warn("Atomic internet time - expiration:" + Date.from(instant));
		    
		}catch(Exception e){
			logger.warn("Atomic internet time ERROR: " + e.toString());
		}
		
		return instant;
		
	}
	
	public void showJWTTimeParamsOnRequest(Instant issuedAt, Instant expiration, String issuer ){
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
}
