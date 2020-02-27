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
import no.systema.jservices.tvinn.kurermanifest.controller.KurerManifestController;
import no.systema.jservices.tvinn.kurermanifest.util.Utils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-configuration-kurer.xml")
@TestPropertySource(locations="classpath:applicationkurer-test.properties")

public class TestJApiClientKurer {
	
private static final Logger logger = Logger.getLogger(TestJApiClientKurer.class);
	
	private RestTemplate restTemplate = restTemplate();
	
	@Value("${kurer.file.source.directory}")
    private String basePath;
    

	
	@Test
	public void  testFileUploadByteArrayResource() {
	
		ApiClientKurer api = new ApiClientKurer();
		String result = api.uploadPayloads(basePath);
		logger.info(result);	
	}
	
	
	@Bean
	public RestTemplate restTemplate(){
    	//RestTemplate restTemplate = new RestTemplate(Arrays.asList(new MappingJackson2HttpMessageConverter(objectMapper())));
    	RestTemplate restTemplate = new RestTemplate();
		restTemplate.setInterceptors(Arrays.asList(new CommonClientHttpRequestInterceptor()));
		restTemplate.setErrorHandler(new CommonResponseErrorHandler());

		return restTemplate;  
		
	}  
}
