package no.systema.jservices.tvinn.expressfortolling.api;

import java.io.File;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import no.systema.jservices.tvinn.expressfortolling.logger.RestTransmissionExpressManifestLogger;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-configuration.xml")
@TestPropertySource(locations="classpath:application-test.properties")
public class TestJApiUploadClient {
	
private static final Logger logger = Logger.getLogger(TestJApiUploadClient.class);
	
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

	
	/*
	@Test //OK --> Sep 2020
	public void testFileUpload() {
		apiUploadClient.setUploadUrlImmutable(uploadUrl);
		String result = apiUploadClient.uploadPayloads(baseDir, sentDir , errorDir);
		logger.info(result);	
	}
	
	@Test //OK --> Oct 9th, 2020
	public void testDocumentApiUpload() {
		apiUploadClient.setUploadUrlImmutable(uploadDocsUrl);
		String result = apiUploadClient.uploadDocuments(baseDir, sentDir , errorDir);
		logger.info(result);	
	}
	*/
	
	@Test //OK --> Oct 14th, 2020
	public void testDocumentApiUploadFileByUser() {
		String declarationId = "974309742-12102020-698";
		String documentType = "faktura";
		String fileName = "/zzzexpress/docsapi/skat_ncts.pdf";
		
		apiUploadClient.setUploadUrlImmutable(uploadDocsUrl);
		String result = apiUploadClient.uploadDocumentsByUser(declarationId, documentType, fileName);
		logger.info(result);	
	}
	
	
}
