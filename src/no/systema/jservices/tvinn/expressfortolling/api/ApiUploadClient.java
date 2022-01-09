package no.systema.jservices.tvinn.expressfortolling.api;

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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.*;
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
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import no.systema.jservices.common.util.CommonClientHttpRequestInterceptor;
import no.systema.jservices.common.util.CommonResponseErrorHandler;
import no.systema.jservices.common.util.FileManager;
import no.systema.jservices.tvinn.expressfortolling.api.Authorization;
import no.systema.jservices.tvinn.expressfortolling.api.TokenResponseDto;
import no.systema.jservices.tvinn.expressfortolling.logger.RestTransmissionExpressManifestLogger;
import no.systema.jservices.tvinn.kurermanifest.api.FileUpdateFlag;
import no.systema.jservices.tvinn.kurermanifest.util.Utils;


@Service
public class ApiUploadClient  {
	private static final Logger logger = LoggerFactory.getLogger(ApiUploadClient.class);
	private RestTemplate restTemplate;
	private FileManager fileMgr = new FileManager();
	
	@Value("${expft.file.limit.per.loop}")
    private int maxLimitOfFilesPerLoop;
	
	@Autowired
	ApiServices apiServices;
	
	@Autowired
	Authorization authorization;
	
	@Autowired
	RestTransmissionExpressManifestLogger transmissionLogger;

	
	public URI uploadUrlImmutable;
	public void setUploadUrlImmutable(String value){
		try{
			this.uploadUrlImmutable = new URI(value);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public ApiUploadClient(){
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
					authTokenDto = authorization.accessTokenRequestPost();

					String fileName = file.getAbsolutePath();
					//there is a bug in Toll.no for more than 2 files in the same REST loop ... ? To be researched ...
					if (counter <= this.maxLimitOfFilesPerLoop && authTokenDto!=null){
						try{
							//Toll.no takes a json-payload as String
							retval = this.upload_via_jsonString(new Utils().getFilePayloadStream(fileName), fileName, authTokenDto);
							logger.warn("######### OK:" + retval);
							//Toll.no does not take a file as Multipart. Could be a reality in version 2
							//retval = this.upload_via_streaming(new Utils().getFilePayloadStream(fileName), fileName, authTokenDto);
							
							if(retval.startsWith(OK_STATUS_INIT_NUMBER)){
								logger.info(Paths.get(fileName) + " " + Paths.get(sentDir + Paths.get(fileName).getFileName().toString()));
								//this.fileMgr.moveCopyFiles(fileName, this.backupDir, FileManager.COPY_FLAG);
								
								//log further in database via a service before moving the file. Send the errorDir in case there is an error on log
								this.transmissionLogger.logTransmission(fileName, errorDir, null, null);
								//now move the file to the OK-sent directory. The suffix in milliseconds won't match the file suffix in error db-log.
								this.fileMgr.moveCopyFiles(fileName, sentDir, FileManager.MOVE_FLAG, FileManager.TIME_STAMP_SUFFIX_FLAG);
								
							}else{
								logger.info(Paths.get(fileName) + " " + Paths.get(errorDir + Paths.get(fileName).getFileName().toString()));
								String errorFileRenamed= retval + "_" + Paths.get(fileName).getFileName().toString();
								//log further in database via a service before moving the file. Send the errorDir in case there is an error on log
								this.transmissionLogger.logTransmission(fileName, errorDir, retval, retval);
								this.fileMgr.moveCopyFiles(fileName, errorDir, FileManager.MOVE_FLAG, errorFileRenamed, FileManager.TIME_STAMP_SUFFIX_FLAG);
								
							}
						}catch(HttpClientErrorException e){
							//usually thrown with validation errors on json-paylaod such as invalid values in a field or lack of mandatory values.
							String responseBody = e.getResponseBodyAsString();
							logger.error("ERROR http code:" + e.getRawStatusCode());
							logger.error("Response body:" + responseBody);
							
							retval = this.getError(e);
							this.logError(retval, fileName, errorDir, responseBody);
						}
						catch(Exception e){
							//other more general exception 
							retval = this.getError(e);
							this.logError(retval, fileName, errorDir, "unexpected " + e.toString());	
						}
						
					}
					
				}
			
			}catch(Exception e){
				//e.printStackTrace();
				retval = "ERROR_on_REST";
				logger.error(retval + " " + e);
			}
			
		}else{
			logger.error("No directory found: " + baseDir);
		}
		
		return retval;
	}
	
	/**
	 * Document API for extra attachments (PDF, JPG, PNG, TXT, DOC, DOCX, XLS, XLSX)
	 * @param baseDir
	 * @param sentDir
	 * @param errorDir
	 * @return
	 */
	public String uploadDocuments(String baseDir, String sentDir, String errorDir){
		TokenResponseDto authTokenDto = null;
		String OK_STATUS_INIT_NUMBER = "2";
		String retval = "204_NO_Content"; 
		
		if (Files.exists(Paths.get(baseDir))) {
			this.fileMgr.secureTargetDir(sentDir);
			this.fileMgr.secureTargetDir(errorDir);
			int counter = 0;
		
			try{
				List<File> files = this.fileMgr.getValidFilesInDirectory(baseDir);
				//files.forEach(System.out::println);

				//Send each file per restTemplate call
				for(File file: files){
					//String fileName = Paths.get(filePath).getFileName().toString();
					counter++;
					
					//get token authDto only for the first iteration
					authTokenDto = authorization.accessTokenForDocsRequestPost();

					String fileName = file.getAbsolutePath();
					//there is a bug in Toll.no for more than 2 files in the same REST loop ... ? To be researched ...
					if (counter <= this.maxLimitOfFilesPerLoop && authTokenDto!=null){
						try{
							//Toll.no takes a json-payload as String
							//retval = upload_via_jsonString(new Utils().getFilePayloadStream(fileName), fileName, authTokenDto);
							String declarationId = "974309742-12102020-698";
							//996358232-24082020-298760/document
							String documentType = "faktura";
							retval = this.postFile(new Utils().getFilePayloadStream(fileName), fileName, authTokenDto, declarationId, documentType);
							logger.info("######### OK:" + retval);
							//Toll.no does not take a file as Multipart. Could be a reality in version 2
							//retval = this.upload_via_streaming(new Utils().getFilePayloadStream(fileName), fileName, authTokenDto);
							
							
							/*TODO
							if(retval.startsWith(OK_STATUS_INIT_NUMBER)){
								logger.info(Paths.get(fileName) + " " + Paths.get(sentDir + Paths.get(fileName).getFileName().toString()));
								//this.fileMgr.moveCopyFiles(fileName, this.backupDir, FileManager.COPY_FLAG);
								
								//log further in database via a service before moving the file. Send the errorDir in case there is an error on log
								this.transmissionLogger.logTransmission(fileName, errorDir, null, null);
								//now move the file to the OK-sent directory. The suffix in milliseconds won't match the file suffix in error db-log.
								this.fileMgr.moveCopyFiles(fileName, sentDir, FileManager.MOVE_FLAG, FileManager.TIME_STAMP_SUFFIX_FLAG);
								
							}else{
								logger.info(Paths.get(fileName) + " " + Paths.get(errorDir + Paths.get(fileName).getFileName().toString()));
								String errorFileRenamed= retval + "_" + Paths.get(fileName).getFileName().toString();
								//log further in database via a service before moving the file. Send the errorDir in case there is an error on log
								this.transmissionLogger.logTransmission(fileName, errorDir, retval, retval);
								this.fileMgr.moveCopyFiles(fileName, errorDir, FileManager.MOVE_FLAG, errorFileRenamed, FileManager.TIME_STAMP_SUFFIX_FLAG);
								
							}
							*/
						}catch(HttpClientErrorException e){
							//usually thrown with validation errors on json-paylaod such as invalid values in a field or lack of mandatory values.
							String responseBody = e.getResponseBodyAsString();
							logger.error("ERROR http code:" + e.getRawStatusCode());
							logger.error("Response body:" + responseBody);
							
							retval = this.getError(e);
							//this.logError(retval, fileName, errorDir, responseBody);
						}
						catch(Exception e){
							//other more general exception 
							retval = this.getError(e);
							//this.logError(retval, fileName, errorDir, "unexpected " + e.toString());	
						}
						
					}
					
				}
			
			}catch(Exception e){
				//e.printStackTrace();
				retval = "ERROR_on_REST";
				logger.error(retval);
			}
			
		}else{
			logger.error("No directory found: " + baseDir);
		}
		
		return retval;
	}
	
	/**
	 * This method is used when the end-user uploads a file manually (usually via a GUI)
	 * 
	 * @param declarationId
	 * @param documentType
	 * @param fileName
	 * @return
	 */
	public String uploadDocumentsByUser(String declarationId, String documentType, String fileName){
		TokenResponseDto authTokenDto = null;
		String OK_STATUS_INIT_NUMBER = "2";
		String retval = "204_NO_Content"; 
		
		if (StringUtils.isNotEmpty(declarationId) && StringUtils.isNotEmpty(documentType) && StringUtils.isNotEmpty(fileName)) {
			//check if exists
			if(Files.exists(Paths.get(fileName))){
				try{
					//get token authDto only for the first iteration
					authTokenDto = authorization.accessTokenForDocsRequestPost();
					//send file
					if (authTokenDto!=null){
						try{
							retval = this.postFile(new Utils().getFilePayloadStream(fileName), fileName, authTokenDto, declarationId, documentType);
							logger.info("######### OK:" + retval);
							
						}catch(HttpClientErrorException e){
							//usually thrown with validation errors on json-paylaod such as invalid values in a field or lack of mandatory values.
							String responseBody = e.getResponseBodyAsString();
							logger.error("ERROR http code:" + e.getRawStatusCode());
							logger.error("Response body:" + e.getStatusText());
							
							retval = this.getError(e) + "_" + e.getStatusText();
							
						}catch(Exception e){
							//other more general exception 
							retval = this.getError(e) + "_" + e.toString();
								
						}
					}
				}catch(Exception e){
					//e.printStackTrace();
					retval = "ERROR_on_REST";
					logger.error(retval);
				}
			}else{
				retval = "ERROR java.io.FileNotFoundException: " + fileName;
				logger.error(retval);	
			}
		}
		
		return retval;
		
	}
	
	
	/**
	 * 
	 * @param e
	 * @return
	 */
	private String getError(Exception e){
		logger.error(e.toString());
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
	 * 
	 * @param retval
	 * @param fileName
	 * @param errorDir
	 * @param errorText
	 * 
	 * @throws Exception
	 */
	private void logError(String retval, String fileName, String errorDir, String errorText) throws Exception{
		logger.error(retval);
		String errorFileRenamed= retval + "_" + Paths.get(fileName).getFileName().toString();
		//log further in database via a service before moving the file. Send the errorDir in case there is an error on log
		this.transmissionLogger.logTransmission(fileName, errorDir, retval, errorText);
		logger.error("######### ERROR: Moving error files to error-dir:" + Paths.get(fileName) + " " + Paths.get(errorDir + errorFileRenamed));
		this.fileMgr.moveCopyFiles(fileName, errorDir, FileManager.MOVE_FLAG, errorFileRenamed, FileManager.TIME_STAMP_SUFFIX_FLAG);
		
	}

	
	/**
	 * This method is used as long as toll.no does not uses multipart file end-point
	 * @param fileName
	 * @param authTokenDto
	 * 
	 * @return
	 * @throws Exception
	 */
	private String upload_via_jsonString(InputStream inputStream, String fileName, TokenResponseDto authTokenDto) throws Exception {
		
		logger.warn("A----->" + fileName);
		//json payload
		String jsonPayload = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
		jsonPayload = new Utils().clearCarriageReturn(jsonPayload);
			
		//json headers
		HttpHeaders jsonHeaders = new HttpHeaders();
		//it has to be JSON UTF8 otherwise it won't work
		jsonHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
		jsonHeaders.add(HttpHeaders.AUTHORIZATION, " Bearer " + authTokenDto.getAccess_token());
		jsonHeaders.add("Accept", "application/json;charset=utf-8");
		HttpEntity<?> entity = new HttpEntity<>(jsonPayload, jsonHeaders);
		
		//default method when file is sent for the first time (create)
		HttpMethod httpMethod = HttpMethod.POST;
		URI url = this.uploadUrlImmutable;
		//update file and not new...
		if(fileName.toLowerCase().contains("/" + FileUpdateFlag.U_.getCode()) ){
			//PATCH https://<env>/api/movement/manifest/{id}
			url = updateUrlForUpdate(fileName);
			logger.warn(url.toString());
			httpMethod = HttpMethod.PATCH; //all updates must be done with PATCH
		}else if(fileName.toLowerCase().contains("/" + FileUpdateFlag.D_.getCode()) ){
			//DELETE https://<env>/api/movement/manifest/{id}
			url = updateUrlForUpdate(fileName);
			logger.warn(url.toString());
			httpMethod = HttpMethod.DELETE; //all deletes must be done with DELETE
			//clear all content in json-payload but not the headers
			entity = new HttpEntity<>("", jsonHeaders);
			//update the status to REOPENED in order to be able to DELETE the manifest
			String manifestId = new Utils().getUUID(fileName);
			apiServices.updateStatusManifest(manifestId, "REOPENED");
		}
		
		//////START REST/////////
		ResponseEntity<String> exchange = null;
		try{
			//final ParameterizedTypeReference<String> typeReference = new ParameterizedTypeReference<String>() {};
			logger.info("before REST call");
			exchange = this.restTemplate.exchange(url, httpMethod, entity, String.class);
			logger.info("after REST call");
			if(exchange!=null){
				if(exchange.getStatusCode().is2xxSuccessful()) {
					logger.warn("OK -----> File uploaded = " + exchange.getStatusCode().toString());
				}else{
					logger.error("ERROR : FATAL ... on File uploaded = " + exchange.getStatusCode().toString() + exchange.getBody() );
					
				}
			}
		}catch(HttpClientErrorException e){
			//DEBUG -->String responseBody = e.getResponseBodyAsString();
			//DEBUG -->logger.error("ERROR http code:" + e.getRawStatusCode());
			//DEBUG -->logger.error("Response body:" + responseBody);
			throw e;
		}
		////////END REST/////////
		return exchange.getStatusCode().toString(); 
	}
	
	/**
	 * 
	 * @param manifestId
	 * @param inputStream
	 * @param fileName
	 * @param authTokenDto
	 * @return
	 * @throws Exception
	 */
	private String upload_via_jsonString(String manifestId, InputStream inputStream, String fileName, TokenResponseDto authTokenDto) throws Exception {
		
		logger.info("A----->" + fileName);
		//json payload
		String jsonPayload = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
		jsonPayload = new Utils().clearCarriageReturn(jsonPayload);
			
		//json headers
		HttpHeaders jsonHeaders = new HttpHeaders();
		//it has to be JSON UTF8 otherwise it won't work
		jsonHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
		jsonHeaders.add(HttpHeaders.AUTHORIZATION, " Bearer " + authTokenDto.getAccess_token());
		jsonHeaders.add("Accept", "application/json;charset=utf-8");
		HttpEntity<?> entity = new HttpEntity<>(jsonPayload, jsonHeaders);
		
		//default method when file is sent for the first time (create)
		HttpMethod httpMethod = HttpMethod.POST;
		String path = this.uploadUrlImmutable.toString() + "/" + manifestId + "/cargo-line/";
		URI url = new URI(path);
		//update file and not new...
		if(fileName.toLowerCase().contains("/" + FileUpdateFlag.U_.getCode()) || fileName.toLowerCase().contains("/" + FileUpdateFlag.D_.getCode()) ){
			//PATCH https://<env>/api/movement/manifest/{id}
			url = updateUrlForUpdate(fileName);
			logger.warn(url.toString());
			httpMethod = HttpMethod.PATCH; //all updates must be done with PATCH
		}
		
		//////START REST/////////
		ResponseEntity<String> exchange = null;
		try{
			//final ParameterizedTypeReference<String> typeReference = new ParameterizedTypeReference<String>() {};
			logger.info("before REST call");
			exchange = this.restTemplate.exchange(url, httpMethod, entity, String.class);
			logger.info("after REST call");
			if(exchange!=null){
				if(exchange.getStatusCode().is2xxSuccessful()) {
					logger.info("OK -----> File uploaded = " + exchange.getStatusCode().toString());
				}else{
					logger.error("ERROR : FATAL ... on File uploaded = " + exchange.getStatusCode().toString() + exchange.getBody() );
					
				}
			}
		}catch(HttpClientErrorException e){
			//DEBUG -->String responseBody = e.getResponseBodyAsString();
			//DEBUG -->logger.error("ERROR http code:" + e.getRawStatusCode());
			//DEBUG -->logger.error("Response body:" + responseBody);
			throw e;
		}
		////////END REST/////////
		return exchange.getStatusCode().toString(); 
	}
	
	
	
	
	/**
	 * This method is a good streaming model that does not buffer entire files. 
	 * Large buffers would introduce latency and, more importantly, they could result in out-of-memory errors.
	 * @param inputStream
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	
	private String upload_via_streaming(InputStream inputStream, String fileName, TokenResponseDto authTokenDto, String declarationId, String documentType) throws Exception {

	    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
	    requestFactory.setBufferRequestBody(false);
	    restTemplate.setRequestFactory(requestFactory);

	    InputStreamResource inputStreamResource = new InputStreamResource(inputStream) {
	        @Override public String getFilename() { return fileName; }
	        @Override public long contentLength() { return -1; }
	    };

	    MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
	    body.add("metadata", "{\"declarationId\": \"" + declarationId + "\"documentType\": \"" + documentType + "\"}");
	    body.add("file", inputStreamResource);

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
		//PATCH https://<env>/api/movement/manifest/{id}
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
		//Too simple-->RestTemplate restTemplate = new RestTemplate();
		
		//this factory is required in order not to lose the response body when getting a HttpClientErrorException on restTemplate (in an Interceptor)
		ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(new HttpComponentsClientHttpRequestFactory());
    	RestTemplate restTemplate = new RestTemplate(factory);
    	restTemplate.setInterceptors(Collections.singletonList(new CommonClientHttpRequestInterceptor()));
		restTemplate.setErrorHandler(new CommonResponseErrorHandler());
		
		// Add the Jackson message converter for json payloads
		//restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		restTemplate.getMessageConverters().add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
		
		return restTemplate;  
		
	} 
	
	
	
	/**Some code to look at on Multipart file send (Dokument API för Manifest maskinport)
	 * 
	 @Service
		public class FileUploadService {
		
		    private RestTemplate restTemplate;
		
		    @Autowired
		    public FileUploadService(RestTemplateBuilder builder) {
		        this.restTemplate = builder.build();
		    }
		
		    public void postFile(String filename, byte[] someByteArray) {
		        HttpHeaders headers = new HttpHeaders();
		        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		
		        // This nested HttpEntiy is important to create the correct
		        // Content-Disposition entry with metadata "name" and "filename"
		        MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
		        ContentDisposition contentDisposition = ContentDisposition
		                .builder("form-data")
		                .name("file")
		                .filename(filename)
		                .build();
		        fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
		        HttpEntity<byte[]> fileEntity = new HttpEntity<>(someByteArray, fileMap);
		
		        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		        body.add("file", fileEntity);
		
		        HttpEntity<MultiValueMap<String, Object>> requestEntity =
		                new HttpEntity<>(body, headers);
		        try {
		            ResponseEntity<String> response = restTemplate.exchange(
		                    "/urlToPostTo",
		                    HttpMethod.POST,
		                    requestEntity,
		                    String.class);
		        } catch (HttpClientErrorException e) {
		            e.printStackTrace();
		        }
		    }
		}
		
		*/
		
	 /**
	  * Lovliga filtyper: pdf, jpg, png, txt, doc, docx, xls, xlsx
	  * 
	  * @param inputStream
	  * @param fileName
	  * @param authTokenDto
	  * @param declarationId e.g (Declarant(9)/date(8)/sequence(6): 99620120129062020125771
	  * @param documentType (faktura, tillatelser, fraktregning, opprinnelsesdokumentasjon, fraktbrev)
	  * @return
	  * @throws Exception
	  */
	 private String postFile(InputStream inputStream, String fileName, TokenResponseDto authTokenDto, String declarationId, String documentType) throws Exception {

		    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		    requestFactory.setBufferRequestBody(false);
		    restTemplate.setRequestFactory(requestFactory);

		    
		    InputStreamResource inputStreamResource = new InputStreamResource(inputStream) {
		        @Override public String getFilename() { return fileName; }
		        @Override public long contentLength() { return -1; }
		    };
		    
		    logger.info("File path----->" + fileName);
			HttpHeaders parts = new HttpHeaders();  
			final HttpEntity<InputStreamResource> partsEntity = new HttpEntity<>(inputStreamResource, parts);
			String path = this.uploadUrlImmutable.toString() + "/" + declarationId + "/document";
		    logger.info(path);
			URI url = new URI(path);
			
		    MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
		    //Example: -->{"declarationId": "974309742-12102020-698", "documentType": "faktura"}
		    body.add("metadata", "{\"declarationId\": \"" + declarationId + "\"" + "," +  "\"documentType\": \"" + documentType + "\"}");
		    body.add("file", partsEntity);
		    
		    
		    HttpHeaders headerParams = new HttpHeaders();
		    headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + authTokenDto.getAccess_token());
		    headerParams.setContentType(MediaType.MULTIPART_FORM_DATA);
		    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body,headerParams);

		    final ParameterizedTypeReference<String> typeReference = new ParameterizedTypeReference<String>() {};
		    final ResponseEntity<String> exchange = this.restTemplate.exchange(url, HttpMethod.POST, requestEntity, typeReference);
			
			if(exchange.getStatusCode().is2xxSuccessful()) {
				logger.info("OK -----> File uploaded = " + exchange.getStatusCode().toString());
			}else{
				logger.info("ERROR : FATAL ... on File uploaded = " + exchange.getStatusCode().toString());
			}
			return exchange.getStatusCode().toString() ; 
		    
		   
		}
	 
	 
	 
	 
	
}

