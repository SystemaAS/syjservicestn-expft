package no.systema.jservices.tvinn.kurermanifest.api;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import no.systema.jservices.common.util.CommonClientHttpRequestInterceptor;
import no.systema.jservices.common.util.CommonResponseErrorHandler;
import no.systema.jservices.common.util.FileManager;
import no.systema.jservices.tvinn.expressfortolling.api.Authorization;
import no.systema.jservices.tvinn.expressfortolling.api.TokenResponseDto;
import no.systema.jservices.tvinn.kurermanifest.util.Utils;


@Service
public class ApiKurerUploadClient  {
	private static final Logger logger = Logger.getLogger(ApiKurerUploadClient.class);
	private RestTemplate restTemplate;
	private FileManager fileMgr = new FileManager();
	
	@Value("${kurer.file.limit.per.loop}")
    private int maxLimitOfFilesPerLoop;
	
	@Autowired
	Authorization authorization;
	
	public URI uploadUrl;
	public void setUploadUrl(String value){
		try{
			this.uploadUrl = new URI(value);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public ApiKurerUploadClient(){
		restTemplate = this.restTemplate();
	}
	
	
	/**
	 * This method uploads files as they are found.
	 * All files are sent with the same token (fetched only once)
	 * 
	 * @return
	 */
	public String uploadPayloads(String baseDir, String sentDir, String errorDir){
		TokenResponseDto authTokenDto = null;
		String retval = "204 NO Content or NO CALL on REST"; 
		
		if (Files.exists(Paths.get(baseDir))) {
			this.fileMgr.secureTargetDir(sentDir);
			this.fileMgr.secureTargetDir(errorDir);
			int counter = 0;
		
			try{
				List<File> files = this.fileMgr.getValidFilesInDirectory(baseDir, new File(sentDir).getName().toString() , new File(errorDir).getName().toString());
				//files.forEach(System.out::println);

				//Send each file per restTemplate call
				for(File file: files){
					//String fileName = Paths.get(filePath).getFileName().toString();
					counter++;
					
					//get token authDto only for the first iteration
					if(counter==1){
						authTokenDto = authorization.accessTokenForKurerRequestPost();
					}
					String fileName = file.getAbsolutePath();
					//there is a bug in Toll.no for more than 2 files in the same REST loop ... ? To be researched ...
					if (counter <= this.maxLimitOfFilesPerLoop){
						try{
							//Toll.no takes a json-payload as String
							retval = upload_via_jsonString(new Utils().getFilePayloadStream(fileName), fileName, authTokenDto);
							logger.info("#########:" + retval);
							//Toll.no does not take a file as Multipart. Could be a reality in version 2
							//retval = this.upload_via_streaming(new Utils().getFilePayloadStream(fileName), fileName, authTokenDto);
							
							if(retval.startsWith("2")){
								logger.info(Paths.get(fileName) + " " + Paths.get(sentDir + Paths.get(fileName).getFileName().toString()));
								Path temp = Files.move( Paths.get(fileName), Paths.get(sentDir + Paths.get(fileName).getFileName().toString()));
							}else{
								logger.info(Paths.get(fileName) + " " + Paths.get(errorDir + Paths.get(fileName).getFileName().toString()));
								Path temp = Files.move( Paths.get(fileName), Paths.get(errorDir + Paths.get(fileName).getFileName().toString()));
							}
						}catch(Exception e){
							String AUTHENTICATON_FAIL = "403";
							if(e.toString().contains(AUTHENTICATON_FAIL)){
								//Do nothing with the file since the file could be grabbed in the next iteration
								//There seems to be a bug at toll.no and the sending just gets the 403 until some new try...
								logger.warn(e.toString());
								
							}else{
								logger.error(e.toString());
								logger.error("Moving error files to error-dir:" + Paths.get(fileName) + " " + Paths.get(errorDir + Paths.get(fileName).getFileName().toString()));
								Path temp = Files.move( Paths.get(fileName), Paths.get(errorDir + Paths.get(fileName).getFileName().toString()));
								
							}
						}
					}
					
				}
			
			}catch(Exception e){
				e.printStackTrace();
				retval = "ERROR on REST";
			}
			
		}else{
			logger.error("No directory found: " + baseDir);
		}
		
		return retval;
	}

	/**
	 * This method is a good streaming model that does not buffer entire files. 
	 * Large buffers would introduce latency and, more importantly, they could result in out-of-memory errors.
	 * @param inputStream
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	
	private String upload_via_streaming(InputStream inputStream, String fileName, TokenResponseDto authTokenDto) throws Exception {

	    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
	    requestFactory.setBufferRequestBody(false);
	    restTemplate.setRequestFactory(requestFactory);

	    InputStreamResource inputStreamResource = new InputStreamResource(inputStream) {
	        @Override public String getFilename() { return fileName; }
	        @Override public long contentLength() { return -1; }
	    };

	    MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
	    body.add("user-file", inputStreamResource);

	    HttpHeaders headerParams = new HttpHeaders();
	    headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + authTokenDto.getAccess_token());
	    headerParams.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
	    headerParams.setContentType(MediaType.MULTIPART_FORM_DATA);

	    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body,headerParams);

	    //String response = restTemplate.postForObject(this.uploadUrl, requestEntity, String.class);
	    final ParameterizedTypeReference<String> typeReference = new ParameterizedTypeReference<String>() {};
		final ResponseEntity<String> exchange = this.restTemplate.exchange(this.uploadUrl, HttpMethod.POST, requestEntity, typeReference);
		
		if(exchange.getStatusCode().is2xxSuccessful()) {
			logger.info("OK -----> File uploaded = " + exchange.getStatusCode().toString());
		}else{
			logger.info("ERROR : FATAL ... on File uploaded = " + exchange.getStatusCode().toString());
		}
		return exchange.getStatusCode().toString() ; 
	    
	   
	}
	
	/**
	 * This method make use of the ByteArrayResource. This model does buffer entire files which can cause latency and
	 * out of memory errors. 
	 * Therefore the method is good as an alternative test but do not scales well in production.
	 * 
	 * Use therefore the method: upload_via_streaming in this same class
	 * 
	 * @param inputStream
	 * @param fileName
	 * @return
	 */
	private String upload_via_byteArrayResource(String fileName, TokenResponseDto authTokenDto) throws Exception {
		
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
		HttpHeaders headerParams = new HttpHeaders();
	    headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + authTokenDto.getAccess_token());
	    headerParams.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
	    headerParams.setContentType(MediaType.MULTIPART_FORM_DATA);

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("user-file", partsEntity);
	
		final ParameterizedTypeReference<String> typeReference = new ParameterizedTypeReference<String>() {};
		final ResponseEntity<String> exchange = this.restTemplate.exchange(this.uploadUrl, HttpMethod.POST, new HttpEntity<>(body, headerParams), typeReference);
		if(exchange.getStatusCode().is2xxSuccessful()) {
			logger.info("OK -----> File uploaded = " + exchange.getStatusCode().toString());
		}else{
			logger.info("ERROR : FATAL ... on File uploaded = " + exchange.getStatusCode().toString());
		}
		return exchange.getStatusCode().toString(); 
	}
	
	/**
	 * This method is used as long as toll.no does not uses multipart file end-point
	 * @param fileName
	 * @param authTokenDto
	 * @return
	 * @throws Exception
	 */
	private String upload_via_jsonString(InputStream inputStream, String fileName, TokenResponseDto authTokenDto) throws Exception {
		
		logger.info("A----->" + fileName);
		HttpHeaders headerParams = new HttpHeaders();
		headerParams.setContentType(MediaType.APPLICATION_JSON_UTF8);
		headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + authTokenDto.getAccess_token());
	    //String input = "{\"id\": \"3b8e72e7-b543-4253-8d4f-4904756fe9de\"}";
		String payload = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
		//logger.info(payload);
		final ResponseEntity<String> exchange = this.restTemplate.exchange(this.uploadUrl, HttpMethod.POST, new HttpEntity<String>(payload, headerParams), String.class);
		
		if(exchange.getStatusCode().is2xxSuccessful()) {
			logger.info("OK -----> File uploaded = " + exchange.getStatusCode().toString());
		}else{
			logger.error("ERROR : FATAL ... on File uploaded = " + exchange.getStatusCode().toString());
		}
		return exchange.getStatusCode().toString(); 
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
		// Add the Jackson message converter for json-string payloads
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		
		return restTemplate;  
		
	}  
}

