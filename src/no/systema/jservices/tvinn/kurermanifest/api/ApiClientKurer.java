package no.systema.jservices.tvinn.kurermanifest.api;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
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
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import no.systema.jservices.common.util.CommonClientHttpRequestInterceptor;
import no.systema.jservices.common.util.CommonResponseErrorHandler;
import no.systema.jservices.tvinn.kurermanifest.util.Utils;


@Service
public class ApiClientKurer {
	private static final Logger logger = Logger.getLogger(ApiClientKurer.class);
	private RestTemplate restTemplate;
	
	
	public ApiClientKurer(){
		restTemplate = this.restTemplate();
	}
	
	/**
	 * 
	 * @return
	 */
	public String uploadPayloads(String baseDir){
		String retval = "init ?"; 
		try (Stream<Path> walk = Files.walk(Paths.get(baseDir))) {
			List<String> files = walk.filter(Files::isRegularFile)
					.map(x -> x.toString()).collect(Collectors.toList());
			//files.forEach(System.out::println);
			
			//Send each file per restTemplate call
			for(String fileName: files){
				logger.info("A----->" + fileName);
				//STEP (1) We extract the payload in bytes and put it in the HttpEntity as ByteArrayResource
				HttpHeaders parts = new HttpHeaders();  
				parts.setContentType(MediaType.TEXT_PLAIN);
				final ByteArrayResource byteArrayResource = new ByteArrayResource(new Utils().getFilePayload(fileName)) {
					@Override
					public String getFilename() {   
						return fileName;
					}
				};
				final HttpEntity<ByteArrayResource> partsEntity = new HttpEntity<>(byteArrayResource, parts);
				
				
				//STEP (2) We put the byteArrayResource as the body of the request. Content-type:multipart_form_data
				HttpHeaders headers = new HttpHeaders();
				headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
				headers.setContentType(MediaType.MULTIPART_FORM_DATA);
				
				MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
				body.add("user-file", partsEntity);
			
				final ParameterizedTypeReference<String> typeReference = new ParameterizedTypeReference<String>() {};
				final ResponseEntity<String> exchange = this.restTemplate.exchange("http://localhost:8080/syjservicestn-expft/upload", HttpMethod.POST, new HttpEntity<>(body, headers), typeReference);
				if(exchange.getStatusCode().is2xxSuccessful()) {
					logger.info("OK -----> File uploaded = " + exchange.getStatusCode().toString());
				}else{
					logger.info("ERROR : FATAL ... on File uploaded = " + exchange.getStatusCode().toString());
				}
				retval = exchange.getStatusCode().toString() ; 
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return retval;
		}
	
	/**
	 * 
	 * @return
	 */
	@Bean
	public RestTemplate restTemplate(){
    	//RestTemplate restTemplate = new RestTemplate(Arrays.asList(new MappingJackson2HttpMessageConverter(objectMapper())));
    	RestTemplate restTemplate = new RestTemplate();
		restTemplate.setInterceptors(Arrays.asList(new CommonClientHttpRequestInterceptor()));
		restTemplate.setErrorHandler(new CommonResponseErrorHandler());

		return restTemplate;  
		
	}  
}

