package no.systema.jservices.tvinn.expressfortolling.api;

import java.net.InetAddress;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;
import org.slf4j.*;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;

import no.systema.jservices.common.dto.expressfortolling.ManifestTypesOfExportDto;
import no.systema.main.util.ObjectMapperHalJson;


public class Tester   {
	//private static final Logger logger = LoggerFactory.getLogger(Tester.class);
	
	@Test
	public void run() throws Exception{
		final String data = "{\"_embedded\":{\"typesOfExport\":[{\"_links\":{\"self\":{\"href\":\"https://external-manifest-api-stage-exf-external.apps.toll.no/v2/ekspressfortolling/type-of-export/EUEIR_EXPORT\"}},\"code\":\"EUEIR_EXPORT\",\"name\":\"EUEIR\",\"translations\":{\"NOR\":\"EUEIR\"}},{\"_links\":{\"self\":{\"href\":\"https://external-manifest-api-stage-exf-external.apps.toll.no/v2/ekspressfortolling/type-of-export/TRANSIT_EXPORT\"}},\"code\":\"TRANSIT_EXPORT\",\"name\":\"Transitt\",\"translations\":{\"NOR\":\"Transitt\"}},{\"_links\":{\"self\":{\"href\":\"https://external-manifest-api-stage-exf-external.apps.toll.no/v2/ekspressfortolling/type-of-export/ALE_EXPORT\"}},\"code\":\"ALE_EXPORT\",\"name\":\"ALE\",\"translations\":{\"NOR\":\"ALE\"}},{\"_links\":{\"self\":{\"href\":\"https://external-manifest-api-stage-exf-external.apps.toll.no/v2/ekspressfortolling/type-of-export/UGE_EXPORT\"}},\"code\":\"UGE_EXPORT\",\"name\":\"UGE\",\"translations\":{\"NOR\":\"UGE\"}},{\"_links\":{\"self\":{\"href\":\"https://external-manifest-api-stage-exf-external.apps.toll.no/v2/ekspressfortolling/type-of-export/KVALITETSSEKRAD_EXPORT\"}},\"code\":\"KVALITETSSEKRAD_EXPORT\",\"name\":\"Kvalitetssäkrad\",\"translations\":{\"NOR\":\"Kvalitetssäkrad\"}}]},\"_links\":{\"self\":{\"href\":\"https://external-manifest-api-stage-exf-external.apps.toll.no/v2/ekspressfortolling/type-of-export\"}}}";
		
		ObjectMapperHalJson objMapper = new ObjectMapperHalJson(data, "/_embedded/typesOfExport");
		StringBuffer jsonToConvert = new StringBuffer();
		/*
		ArrayList<ManifestTypesOfExportDto> exports = objMapper.getObjectMapper(jsonToConvert).readValue(jsonToConvert.toString(), new TypeReference<List<ManifestTypesOfExportDto>>() {
        });
		System.out.println(exports.toString());
		*/
	}
	
	@Test
	public void test() throws Exception{
		String TIME_SERVER = "time-a.nist.gov";
		
		NTPUDPClient timeClient = new NTPUDPClient();
		timeClient.setDefaultTimeout(3000);
		InetAddress inetAddress = InetAddress.getByName(TIME_SERVER);
		TimeInfo timeInfo = timeClient.getTime(inetAddress);
		NtpV3Packet message = timeInfo.getMessage();
		long serverTime = message.getTransmitTimeStamp().getTime();
		Date time = new Date(serverTime);
		System.out.println("Time from " + TIME_SERVER + ": " + time);
		Clock clock = Clock.fixed(Instant.ofEpochMilli(serverTime),
                ZoneId.of("Europe/Oslo"));
		//Instant inst = Instant.now(clock);
		Instant issuedAt = Instant.now(clock).truncatedTo(ChronoUnit.SECONDS);
	    Instant expiration = issuedAt.plus(120, ChronoUnit.SECONDS);
		
		//System.out.println("Time from " + TIME_SERVER + ": " + Date.from(inst));
	    System.out.println("issuedAt:" + Date.from(issuedAt));
	    System.out.println("expiration:" + Date.from(expiration));
	}
	
}
