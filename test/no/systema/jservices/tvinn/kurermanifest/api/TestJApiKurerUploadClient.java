package no.systema.jservices.tvinn.kurermanifest.api;

import java.io.File;

import org.apache.logging.log4j.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import no.systema.jservices.tvinn.kurermanifest.logger.RestTransmissionLogger;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-configuration-kurer.xml")
@TestPropertySource(locations="classpath:application-test.properties")
public class TestJApiKurerUploadClient {
	
private static final Logger logger = LogManager.getLogger(TestJApiKurerUploadClient.class);
	
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

	@Autowired
	RestTransmissionLogger transmissionLogger;

	
	@Test
	public void  testFileUpload() {
	
		apiKurerUploadClient.setUploadUrlImmutable(uploadUrl);
		
		String result = apiKurerUploadClient.uploadPayloads(baseDir, sentDir , errorDir);
		logger.info(result);	
	}
	
	/*
	@Test
	public void  testLogger() {
		String fileName = "/zzz/test/u_12345.txt";
		this.transmissionLogger.logTransmission(fileName, errorDir, null);
	}*/
	
	/*@Test
	public void  testFileUploadProd() {
	
		apiKurerUploadClient.setUploadUrlImmutable(uploadProdUrl);
		
		String result = apiKurerUploadClient.uploadPayloads(baseDir, sentDir , errorDir);
		if(result!=null && !result.toUpperCase().contains("ERROR")){
			logger.info(result);	
		}
	}*/
	
	
	
}
