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
	
	@Autowired
	ApiUploadClient apiUploadClient;

	@Autowired
	RestTransmissionExpressManifestLogger transmissionLogger;

	
	
	@Test
	public void testFileUpload() {
		apiUploadClient.setUploadUrlImmutable(uploadUrl);
		String result = apiUploadClient.uploadPayloads(baseDir, sentDir , errorDir);
		logger.info(result);	
	}
	
	
}
