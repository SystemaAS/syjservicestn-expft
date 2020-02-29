package no.systema.jservices.tvinn.kurermanifest.api;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import no.systema.jservices.common.util.CommonClientHttpRequestInterceptor;
import no.systema.jservices.common.util.CommonResponseErrorHandler;
import no.systema.jservices.tvinn.expressfortolling.api.ApiServices;
import no.systema.jservices.tvinn.kurermanifest.controller.KurerManifestController;
import no.systema.jservices.tvinn.kurermanifest.util.Utils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-configuration-kurer.xml")
@TestPropertySource(locations="classpath:applicationkurer-test.properties")
public class TestJApiKurerUploadClient {
	
private static final Logger logger = Logger.getLogger(TestJApiKurerUploadClient.class);
	
	@Value("${kurer.file.source.directory}")
	private String baseDir;
	
	@Value("${kurer.upload.url}")
	private String uploadUrl;

	@Autowired
	ApiKurerUploadClient apiKurerUploadClient;

	
	@Test
	public void  testFileUploadByteArrayResource() {
	
		apiKurerUploadClient.setUploadUrl(uploadUrl);
		
		String result = apiKurerUploadClient.uploadPayloads(baseDir);
		logger.info(result);	
	}
	
	
	
}
