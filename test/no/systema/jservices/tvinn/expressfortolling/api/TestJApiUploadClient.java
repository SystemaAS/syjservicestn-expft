package no.systema.jservices.tvinn.expressfortolling.api;

import java.io.File;
import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.slf4j.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import no.systema.jservices.tvinn.expressfortolling.TestJBase;
import no.systema.jservices.tvinn.expressfortolling.logger.RestTransmissionExpressManifestLogger;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-configuration.xml")
@TestPropertySource(locations="classpath:application-test.properties")
public class TestJApiUploadClient extends TestJBase {
	
private static final Logger logger = LoggerFactory.getLogger(TestJApiUploadClient.class);
	
	@Value("${expft.file.source.directory}")
	private String baseDir;
	@Value("${expft.file.source.directory.sent}")
    private String sentDir;
	@Value("${expft.file.source.directory.error}")
    private String errorDir;
	
	@Value("${expft.upload.url}")
	private String uploadUrl;
	
	@Value("${expft.upload.prod.url}")
	private String uploadProdUrl;
	
	
	@Value("${expft.upload.docs.url}")
	private String uploadDocsUrl;
	
	@Autowired
	ApiUploadClient apiUploadClient;

	@Autowired
	RestTransmissionExpressManifestLogger transmissionLogger;

	
	
	@Test //OK --> Sep 2020
	public void testFileUpload() {
		apiUploadClient.setUploadUrlImmutable(uploadUrl);
		String result = apiUploadClient.uploadPayloads(baseDir, sentDir , errorDir);
		logger.info(result);	
	}
	
	
	@Test //OK --> Feb 2021
	public void testDocumentApiUpload() {
		apiUploadClient.setUploadUrlImmutable(uploadDocsUrl);
		String result = apiUploadClient.uploadDocuments(baseDir, sentDir , errorDir);
		logger.info(result);	
	}
	
	
	@Test //OK --> Feb 2021
	public void testClockOnJwt() {
		long expiration_l = 3;
		Instant issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
		Instant expiration = issuedAt.plus(expiration_l, ChronoUnit.MINUTES);
		logger.info(Date.from(issuedAt).toString());
		logger.info(Date.from(expiration).toString());
		
		//In format:2021-02-25T11:11:40
		//DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.from(ZoneOffset.UTC));
		DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault());
		logger.info(formatter.format(issuedAt));
		logger.info(formatter.format(expiration));
		
		//In seconds
		logger.info(String.valueOf(issuedAt.toEpochMilli()/1000));
		logger.info(String.valueOf(expiration.toEpochMilli()/1000));
		
	}
	/*
	@Test //OK --> Oct 14th, 2020
	public void testDocumentApiUploadFileByUser() {
		String declarationId = "TARZAN_974309742-12102020-698";
		String documentType = "faktura";
		String fileName = "/zzzexpress/docsapi/skat_ncts.pdf";
		
		apiUploadClient.setUploadUrlImmutable(uploadDocsUrl);
		String result = apiUploadClient.uploadDocumentsByUser(declarationId, documentType, fileName);
		logger.info(result);	
	}*/
	
	
}
