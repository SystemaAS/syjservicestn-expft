package no.systema.jservices.tvinn.kurermanifest.api;

import java.io.File;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-configuration-kurer.xml")
@TestPropertySource(locations="classpath:application-test.properties")
public class TestJApiKurerUploadClient {
	
private static final Logger logger = Logger.getLogger(TestJApiKurerUploadClient.class);
	
	@Value("${kurer.file.source.directory}")
	private String baseDir;
	@Value("${kurer.file.source.directory.sent}")
    private String sentDir;
	@Value("${kurer.file.source.directory.error}")
    private String errorDir;
	
	@Value("${kurer.upload.url}")
	private String uploadUrl;
	
	@Value("${kurer.upload.prod.url}")
	private String uploadProdUrl;

	
	@Autowired
	ApiKurerUploadClient apiKurerUploadClient;

	
	@Test
	public void  testFileUpload() {
	
		apiKurerUploadClient.setUploadUrl(uploadUrl);
		
		String result = apiKurerUploadClient.uploadPayloads(baseDir, sentDir , errorDir);
		logger.info(result);	
	}
	
	/*@Test
	public void  testFileUploadProd() {
	
		apiKurerUploadClient.setUploadUrl(uploadProdUrl);
		
		String result = apiKurerUploadClient.uploadPayloads(baseDir, sentDir , errorDir);
		logger.info(result);	
	}*/
	
	
	
}
