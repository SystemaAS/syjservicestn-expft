package no.systema.jservices.tvinn.kurermanifest.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Calendar;
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
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import no.systema.jservices.common.util.CommonClientHttpRequestInterceptor;
import no.systema.jservices.common.util.CommonResponseErrorHandler;
import no.systema.jservices.common.util.FileManager;
import no.systema.jservices.tvinn.expressfortolling.api.Authorization;
import no.systema.jservices.tvinn.expressfortolling.api.TokenResponseDto;
import no.systema.jservices.tvinn.kurermanifest.logger.RestTransmissionLogger;
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
	
	@Autowired
	RestTransmissionLogger transmissionLogger;

	
	public URI uploadUrlImmutable;
	public void setUploadUrlImmutable(String value){
		try{
			this.uploadUrlImmutable = new URI(value);
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
		String OK_STATUS_INIT_NUMBER = "2";
		String retval = "204_NO_Content"; 
		
		if (Files.exists(Paths.get(baseDir))) {
			this.fileMgr.secureTargetDir(sentDir);
			this.fileMgr.secureTargetDir(errorDir);
			int counter = 0;
		
			try{
				List<File> files = this.fileMgr.getValidFilesInDirectory(baseDir);
				files.forEach(System.out::println);

				//Send each file per restTemplate call
				for(File file: files){
					//String fileName = Paths.get(filePath).getFileName().toString();
					counter++;
					
					//get token authDto only for the first iteration
					authTokenDto = authorization.accessTokenForKurerRequestPost(this.uploadUrlImmutable.toString());

					String fileName = file.getAbsolutePath();
					//there is a bug in Toll.no for more than 2 files in the same REST loop ... ? To be researched ...
					if (counter <= this.maxLimitOfFilesPerLoop && authTokenDto!=null){
						try{
							//Toll.no takes a json-payload as String
							retval = upload_via_jsonString(new Utils().getFilePayloadStream(fileName), fileName, authTokenDto);
							logger.info("######### OK:" + retval);
							//Toll.no does not take a file as Multipart. Could be a reality in version 2
							//retval = this.upload_via_streaming(new Utils().getFilePayloadStream(fileName), fileName, authTokenDto);
							
							if(retval.startsWith(OK_STATUS_INIT_NUMBER)){
								logger.info(Paths.get(fileName) + " " + Paths.get(sentDir + Paths.get(fileName).getFileName().toString()));
								//this.fileMgr.moveCopyFiles(fileName, this.backupDir, FileManager.COPY_FLAG);
								
								//log further in database via a service before moving the file. Send the errorDir in case there is an error on log
								this.transmissionLogger.logTransmission(fileName, errorDir, null);
								//now move the file to the OK-sent directory. The suffix in milliseconds won't match the file suffix in error db-log.
								this.fileMgr.moveCopyFiles(fileName, sentDir, FileManager.MOVE_FLAG, FileManager.TIME_STAMP_SUFFIX_FLAG);
								
							}else{
								logger.info(Paths.get(fileName) + " " + Paths.get(errorDir + Paths.get(fileName).getFileName().toString()));
								String errorFileRenamed= retval + "_" + Paths.get(fileName).getFileName().toString();
								//log further in database via a service before moving the file. Send the errorDir in case there is an error on log
								this.transmissionLogger.logTransmission(fileName, errorDir, retval);
								this.fileMgr.moveCopyFiles(fileName, errorDir, FileManager.MOVE_FLAG, errorFileRenamed, FileManager.TIME_STAMP_SUFFIX_FLAG);
							}
						}catch(Exception e){
							retval = this.getError(e);
							
							logger.error(retval);
							String errorFileRenamed= retval + "_" + Paths.get(fileName).getFileName().toString();
							logger.error("######### ERROR: Moving error files to error-dir:" + Paths.get(fileName) + " " + Paths.get(errorDir + errorFileRenamed));
							//log further in database via a service before moving the file. Send the errorDir in case there is an error on log
							this.transmissionLogger.logTransmission(fileName, errorDir, retval);
							this.fileMgr.moveCopyFiles(fileName, errorDir, FileManager.MOVE_FLAG, errorFileRenamed, FileManager.TIME_STAMP_SUFFIX_FLAG);
							
						}
					}
					
				}
			
			}catch(Exception e){
				e.printStackTrace();
				retval = "ERROR_on_REST";
			}
			
		}else{
			logger.error("No directory found: " + baseDir);
		}
		
		return retval;
	}
	/**
	 * 
	 * @param e
	 * @return
	 */
	private String getError(Exception e){
		logger.error(e);
		String ERROR_CODE = "xxxError";
		//types 
		String AUTHENTICATON_FAIL = "403";
		String CLIENT_FAIL = "4xxError";
		String SERVER_FAIL = "5xxError";
		
		if(e.toString().contains(AUTHENTICATON_FAIL)){
			ERROR_CODE = AUTHENTICATON_FAIL;
		}else if(e.toString().contains("4")){
			ERROR_CODE = CLIENT_FAIL;
		}else if(e.toString().contains("5")){
			ERROR_CODE = SERVER_FAIL;
		}
		return ERROR_CODE;
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
		final ResponseEntity<String> exchange = this.restTemplate.exchange(this.uploadUrlImmutable, HttpMethod.POST, requestEntity, typeReference);
		
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
		final ResponseEntity<String> exchange = this.restTemplate.exchange(this.uploadUrlImmutable, HttpMethod.POST, new HttpEntity<>(body, headerParams), typeReference);
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
		//it has to be JSON UTF8 otherwise it won't work
		headerParams.setContentType(MediaType.APPLICATION_JSON_UTF8);
		headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + authTokenDto.getAccess_token());
		//payload
		String body = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
		body = new Utils().clearCarriageReturn(body);
		HttpEntity<String> entity = new HttpEntity<String>(body, headerParams);
		
		//default method when file is sent for the first time (create)
		HttpMethod httpMethod = HttpMethod.POST;
		//if it is an update/delete file it will have a prefix: <.../u_xxxxxxx.yyy> or <.../d_xxxxxxx.yyy>> 
		logger.info(fileName.toLowerCase());
		
		URI uploadUrlTemp = this.uploadUrlImmutable;
		if(fileName.toLowerCase().contains("/" + FileUpdateFlag.U_.getCode()) || fileName.toLowerCase().contains("/" + FileUpdateFlag.D_.getCode()) ){
			//PUT https://<env>/api/movement/manifest/{id}
			uploadUrlTemp = updateUrlForUpdate(fileName);
			httpMethod = HttpMethod.PUT;
		}
		
		//////START REST/////////
		ResponseEntity<String> exchange = null;
		try{
			exchange = this.restTemplate.exchange(uploadUrlTemp, httpMethod, entity, String.class);
			
			if(exchange.getStatusCode().is2xxSuccessful()) {
				logger.info("OK -----> File uploaded = " + exchange.getStatusCode().toString());
			}else{
				logger.error("ERROR : FATAL ... on File uploaded = " + exchange.getStatusCode().toString() + exchange.getBody() );
				
			}
		}catch(HttpClientErrorException e){
			logger.error(e);
			if(exchange!=null){
				String responseBody = new String(this.getResponseBody(exchange), this.getCharset(exchange));
				logger.error(responseBody);
			}
			throw e;
		}
		////////END REST/////////
		
		return exchange.getStatusCode().toString(); 
	}
	/**
	 * test
	 * @param exchange
	 * @return
	 */
	private byte[] getResponseBody(ResponseEntity<String> exchange) {
		byte[] retval = new byte[0];
		try {
			if(exchange!=null){
				retval = exchange.getBody().getBytes();
				return exchange.getBody().getBytes();
			}
		}
		catch (Exception e) {
			logger.info("::getResponseBody, ignoring Exception...::");
			// ignore
		}
		
		return retval;
	}
	
	/**
	 * Determine the charset of the response (for inclusion in a status exception).
	 * @param response the response to inspect
	 * @return the associated charset, or Charset.defaultCharset() if none
	 */
	private Charset getCharset(ResponseEntity<String> exchange) {
		Charset charSet = Charset.defaultCharset();
		if(exchange!=null){
			HttpHeaders headers = exchange.getHeaders();
			MediaType contentType = headers.getContentType();
			if (contentType != null) {
				if (contentType.getCharset() != null) {
					charSet = contentType.getCharset();
				}
			}
		}

		return charSet;
	}	

		
	/**
	 * 
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	private URI updateUrlForUpdate(String fileName) throws Exception{
		URI retval = this.uploadUrlImmutable;
		//PUT https://<env>/api/movement/manifest/{id}
		String url = this.uploadUrlImmutable.toString() + "/"  + new Utils().getUUID(fileName);
		retval = new URI(url);
		
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
		// Add the Jackson message converter for json-string payloads
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		
		return restTemplate;  
		
	} 
	
}

