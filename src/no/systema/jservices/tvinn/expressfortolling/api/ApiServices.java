package no.systema.jservices.tvinn.expressfortolling.api;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import no.systema.jservices.common.dto.expressfortolling.ManifestCountryDto;
import no.systema.jservices.common.dto.expressfortolling.ManifestModeOfTransportDto;
import no.systema.jservices.common.dto.expressfortolling.ManifestTypesOfExportDto;
import no.systema.jservices.common.dto.expressfortolling.ManifestTransportationCompanyDto;
import no.systema.jservices.common.dto.expressfortolling.ManifestUserDto;
import no.systema.jservices.common.util.CommonClientHttpRequestInterceptor;
import no.systema.jservices.common.util.CommonResponseErrorHandler;
import no.systema.jservices.tvinn.expressfortolling.jwt.DifiJwtCreator;
import no.systema.jservices.tvinn.expressfortolling2.dao.HouseConsignment;
import no.systema.jservices.tvinn.expressfortolling2.dao.MasterConsignment;
import no.systema.jservices.tvinn.kurermanifest.util.Utils;

/**
 * This class hosts all needed REST calls for Express Fortolling.
 * 
 * @author fredrikmoller
 * @date 2019-08-28
 */
@Service
public class ApiServices {
	private static Logger logger = LoggerFactory.getLogger(ApiServices.class.getName());
	private RestTemplate restTemplate;
	
	@Autowired
    private ApiClient apiClient;
	
	@Autowired
    private ApiUploadClient apiUploadClient;
	
	@Autowired
	DifiJwtCreator difiJwtCreator;

	@Autowired
	Authorization authorization;

	@Value("${expft.basepath}")
    private String basePath;
	
	@Value("${expft.basepath.version}")
    private String basePathVersion;	

	@Value("${expft.upload.url}")
	private String uploadUrl;
	
	@Value("${expft.upload.prod.url}")
	private String uploadProdUrl;
	
	
	
	@Value("${expft.basepath.movement.road}")
    private String basePathMovementRoad;
	
	@Value("${expft.basepath.movement.road.version}")
    private String basePathMovementRoadVersion;
	
	
	
	/**
	 * Get all transportation companies for which the logged in user is a customs representative.
	 * GET /api/exf/ekspressfortolling/transportation-company
	 * Accept: application/json
	 * Host: ekspressfortolling.toll.no
	 * Version: 1
	 * 
	 * @return List<TransportationCompanyDto>
	 */
	public ManifestTransportationCompanyDto getTransportationCompany(String orgNr) {
		TokenResponseDto responseDto = authorization.accessTokenRequestPost();
		//For test, couldn't get Mockito to work correct.
//		TokenResponseDto responseDto = new TokenResponseDto();
//		responseDto.setAccess_token("XYZ");
		
		Object postBody = null; //Not in use
        
        String path = UriComponentsBuilder.fromPath(basePathVersion + "/transportation-company/" + orgNr).build().toUriString();
        
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();
        
        headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + responseDto.getAccess_token());
        headerParams.add("Accept-Charset", "utf-8");
        final String[] accepts = { "application/json" };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        final String[] contentTypes = {  };
        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);

        //TODO refactor outside
        apiClient.setBasePath(basePath);
        
        ParameterizedTypeReference<ManifestTransportationCompanyDto> returnType = new ParameterizedTypeReference<ManifestTransportationCompanyDto>() {};
        return apiClient.invokeAPI(path, HttpMethod.GET, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);

//        return testGetDtos();
        
		
	}
	
	private List<ManifestTransportationCompanyDto> testGetDtos() {
		List<ManifestTransportationCompanyDto> list = new ArrayList<ManifestTransportationCompanyDto>();
		
		ManifestTransportationCompanyDto dto = new ManifestTransportationCompanyDto();
		dto.setId("5");
		dto.setName("Transportør Blåveis");
		
		list.add(dto);
		
		return list;
		
	}
	
	
	public String getManifest(String manifestId) {
		  
		TokenResponseDto authTokenDto = authorization.accessTokenRequestPost();
		
		Object postBody = null; //Not in use
        
        String path = UriComponentsBuilder.fromPath(basePathVersion + "/manifest" + "/" + manifestId).build().toUriString();
        logger.warn(path);
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();
       
        final HttpHeaders headerParams = new HttpHeaders();
        headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + authTokenDto.getAccess_token());
        headerParams.add("Accept-Charset", "utf-8");
        final String[] accepts = { "application/json" };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        final String[] contentTypes = {  };
        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);

        //TODO refactor outside
        apiClient.setBasePath(basePath);
        
        String resultDto;
        try{
        	ParameterizedTypeReference<String> returnType = new ParameterizedTypeReference<String>() {};
        	resultDto = apiClient.invokeAPI(path, HttpMethod.GET, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
        }catch(HttpClientErrorException e){
        	String responseBody = e.getResponseBodyAsString();
			logger.error("ERROR http code:" + e.getRawStatusCode());
			logger.error("Response body:" + responseBody);
			logger.error("e:" + e);
			
			throw e;
		}
        return resultDto;
        		
	}
	
	public String getManifestCargoLines(String manifestId) {
		  
		TokenResponseDto authTokenDto = authorization.accessTokenRequestPost();
		
		Object postBody = null; //Not in use
        
        String path = UriComponentsBuilder.fromPath(basePathVersion + "/manifest/" + manifestId + "/cargo-line").build().toUriString();
        logger.warn(path);
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();
       
        final HttpHeaders headerParams = new HttpHeaders();
        headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + authTokenDto.getAccess_token());
        headerParams.add("Accept-Charset", "utf-8");
        final String[] accepts = { "application/json" };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        final String[] contentTypes = {  };
        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);

        //TODO refactor outside
        apiClient.setBasePath(basePath);
        
        
        String resultDto;
        try{
        	ParameterizedTypeReference<String> returnType = new ParameterizedTypeReference<String>() {};
        	resultDto = apiClient.invokeAPI(path, HttpMethod.GET, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
        }catch(HttpClientErrorException e){
        	String responseBody = e.getResponseBodyAsString();
			logger.error("ERROR http code:" + e.getRawStatusCode());
			logger.error("Response body:" + responseBody);
			logger.error("e:" + e);
			
			throw e;
		}
        return resultDto;
        		
	}
	/**
	 * 
	 * @param manifestId
	 * @return
	 */
	public String deleteManifest(String manifestId) throws Exception {
		restTemplate = this.restTemplate();
		//extra step to change status
		this.updateStatusManifest(manifestId, "REOPENED");
		
		TokenResponseDto authTokenDto = authorization.accessTokenRequestPost();
		//String path = UriComponentsBuilder.fromPath(basePathVersion + "/manifest" + "/" + manifestId).build().toUriString();
        
		String jsonPayload = "";
        
        //json headers
		HttpHeaders jsonHeaders = new HttpHeaders();
  		//it has to be JSON UTF8 otherwise it won't work
  		jsonHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
  		jsonHeaders.add(HttpHeaders.AUTHORIZATION, " Bearer " + authTokenDto.getAccess_token());
  		jsonHeaders.add("Accept", "application/json;charset=utf-8");
  		
  		HttpMethod httpMethod = HttpMethod.DELETE;
  		URI url = new URI(uploadProdUrl + "/" + manifestId);
        
  		HttpEntity<?> entity = new HttpEntity<>(jsonPayload, jsonHeaders);
  		
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
	 * 
	 * @param manifestId
	 * @param status
	 * @return
	 */
	public String updateStatusManifest(String manifestId, String status) throws Exception {
		restTemplate = this.restTemplate();
		
		TokenResponseDto authTokenDto = authorization.accessTokenRequestPost();
		//String path = UriComponentsBuilder.fromPath(basePathVersion + "/manifest" + "/" + manifestId).build().toUriString();
        
		String jsonPayload = "{ \"status\" : \"" + status + "\" }";
        
        //json headers
		HttpHeaders jsonHeaders = new HttpHeaders();
  		//it has to be JSON UTF8 otherwise it won't work
  		jsonHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
  		jsonHeaders.add(HttpHeaders.AUTHORIZATION, " Bearer " + authTokenDto.getAccess_token());
  		jsonHeaders.add("Accept", "application/json;charset=utf-8");
  		
  		HttpMethod httpMethod = HttpMethod.PATCH;
  		URI url = new URI(uploadProdUrl + "/" + manifestId);
        
  		HttpEntity<?> entity = new HttpEntity<>(jsonPayload, jsonHeaders);
  		
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
	 * 
	 * @param manifestId
	 * @param jsonPayload
	 * @return
	 */
	public String createManifestCargoLine(String manifestId, String jsonPayload) {
		restTemplate = this.restTemplate();
		
		TokenResponseDto authTokenDto = authorization.accessTokenRequestPost();
		
		//json headers
		HttpHeaders jsonHeaders = new HttpHeaders();
		//it has to be JSON UTF8 otherwise it won't work
		jsonHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
		jsonHeaders.add(HttpHeaders.AUTHORIZATION, " Bearer " + authTokenDto.getAccess_token());
		jsonHeaders.add("Accept", "application/json;charset=utf-8");
		HttpEntity<?> entity = new HttpEntity<>(jsonPayload, jsonHeaders);
		
		//default method when file is sent for the first time (create)
		HttpMethod httpMethod = HttpMethod.POST;
		String path = this.uploadProdUrl + "/" + manifestId + "/cargo-line/";
		
		//////START REST/////////
		ResponseEntity<String> exchange = null;
		try{
			URI url = new URI(path);
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
		}catch(URISyntaxException ex){
			logger.info(ex.toString());
		}
		////////END REST/////////
		return exchange.getStatusCode().toString(); 
		
	}
	
	
	public String createActiveMeansOfTransport(String manifestId) {
		restTemplate = this.restTemplate();
		
		TokenResponseDto authTokenDto = authorization.accessTokenRequestPost();
		String jsonPayload = "{ \"typeOfMeansOfTransport\" : \"TREKKVOGN\", \"identificationCode\" : \"ABC-12-12-12\", \"countryCode\" : \"NO\", \"driver\" : {\"firstName\" : \"Test\",\"lastName\" : \"Testersen\",\"countryCode\" : \"NO\",\"dateOfBirth\" : \"1976-01-30\",\"sendReleasedConfirmation\" : true,\"sendReleasedConfirmationEmail\" : \"test@testeren.no\"}}";
		
		
		
        String path = UriComponentsBuilder.fromPath(basePathVersion + "/manifest/" + manifestId).build().toUriString() + "/active-means-of-transport" ;
        //json headers
		HttpHeaders jsonHeaders = new HttpHeaders();
		//it has to be JSON UTF8 otherwise it won't work
		jsonHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
		jsonHeaders.add(HttpHeaders.AUTHORIZATION, " Bearer " + authTokenDto.getAccess_token());
		jsonHeaders.add("Accept", "application/json;charset=utf-8");
		HttpEntity<?> entity = new HttpEntity<>(jsonPayload, jsonHeaders);
		
		//default method when file is sent for the first time (create)
		HttpMethod httpMethod = HttpMethod.POST;
        
		//////START REST/////////
		ResponseEntity<String> exchange = null;
		try{
			URI url = new URI(path);
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
		}catch(URISyntaxException ex){
			logger.info(ex.toString());
		}
		////////END REST/////////
		return exchange.getStatusCode().toString(); 
		
	}
	
	
	
	/**
	 * The method returns Json string (raw) in order to unmarshall at the callers point with ObjectMapper.
	 * RestTemplate has good support to fix it here BUT we must upgrade to Spring 5
	 * @return
	 */
	public String getAllTypeOfExport() {
		  
		TokenResponseDto responseDto = authorization.accessTokenRequestPost();
		
		Object postBody = null; //Not in use
        
        String path = UriComponentsBuilder.fromPath(basePathVersion + "/type-of-export").build().toUriString();
        
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();
        
        headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + responseDto.getAccess_token());
        headerParams.add("Accept-Charset", "utf-8");
        final String[] accepts = { "application/json" };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        final String[] contentTypes = {  };
        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);
        //TODO refactor outside
        apiClient.setBasePath(basePath);
        
        ParameterizedTypeReference<String> returnType = new ParameterizedTypeReference<String>() {};
        return apiClient.invokeAPI(path, HttpMethod.GET, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
        		
	}
	
	
	public ManifestTypesOfExportDto getTypeOfExport(String type) {
		  
		TokenResponseDto authTokenDto = authorization.accessTokenRequestPost();
		
		Object postBody = null; //Not in use
        
        String path = UriComponentsBuilder.fromPath(basePathVersion + "/type-of-export" + "/" + type).build().toUriString();
        
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();
        
        final HttpHeaders headerParams = new HttpHeaders();
        headerParams.setContentType(MediaType.APPLICATION_JSON_UTF8);
        
        headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + authTokenDto.getAccess_token());
        headerParams.add("Accept-Charset", "utf-8");
        final String[] accepts = { "application/json" };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        final String[] contentTypes = {  };
        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);
        //TODO refactor outside
        apiClient.setBasePath(basePath);
        
        ManifestTypesOfExportDto resultDto;
        try{
        	ParameterizedTypeReference<ManifestTypesOfExportDto> returnType = new ParameterizedTypeReference<ManifestTypesOfExportDto>() {};
        	resultDto = apiClient.invokeAPI(path, HttpMethod.GET, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
        }catch(HttpClientErrorException e){
        	String responseBody = e.getResponseBodyAsString();
			logger.error("ERROR http code:" + e.getRawStatusCode());
			logger.error("Response body:" + responseBody);
			logger.error("e:" + e);
			
			throw e;
		}
        return resultDto;
        		
	}
	
	
	
	public String getAllTypeOfMeansOfTransport() {
		  
		TokenResponseDto responseDto = authorization.accessTokenRequestPost();
		
		Object postBody = null; //Not in use
        
        String path = UriComponentsBuilder.fromPath(basePathVersion + "/type-of-means-of-transport").build().toUriString();
        
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();
        
        headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + responseDto.getAccess_token());
        headerParams.add("Accept-Charset", "utf-8");
        final String[] accepts = { "application/json" };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        final String[] contentTypes = {  };
        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);
        //TODO refactor outside
        apiClient.setBasePath(basePath);
        
        ParameterizedTypeReference<String> returnType = new ParameterizedTypeReference<String>() {};
        return apiClient.invokeAPI(path, HttpMethod.GET, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
        		
	}
	
	public String getTypeOfMeansOfTransport(String type) {
		  
		TokenResponseDto responseDto = authorization.accessTokenRequestPost();
		
		Object postBody = null; //Not in use
        
        String path = UriComponentsBuilder.fromPath(basePathVersion + "/type-of-means-of-transport" + "/" + type).build().toUriString();
        
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();
        
        headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + responseDto.getAccess_token());
        headerParams.add("Accept-Charset", "utf-8");
        final String[] accepts = { "application/json" };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        final String[] contentTypes = {  };
        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);
        //TODO refactor outside
        apiClient.setBasePath(basePath);
        
        ParameterizedTypeReference<String> returnType = new ParameterizedTypeReference<String>() {};
        return apiClient.invokeAPI(path, HttpMethod.GET, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
        		
	}
	
	public String getActiveMeansOfTransport(String manifestId) {
		  
		TokenResponseDto responseDto = authorization.accessTokenRequestPost();
		
		Object postBody = null; //Not in use
        
        String path = UriComponentsBuilder.fromPath(basePathVersion +  "/manifest/" + manifestId + "/active-means-of-transport").build().toUriString();
        
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();
        
        headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + responseDto.getAccess_token());
        headerParams.add("Accept-Charset", "utf-8");
        final String[] accepts = { "application/json" };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        final String[] contentTypes = { };
        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);
        //TODO refactor outside
        apiClient.setBasePath(basePath);
        
        ParameterizedTypeReference<String> returnType = new ParameterizedTypeReference<String>() {};
        return apiClient.invokeAPI(path, HttpMethod.GET, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
        		
	}
	
	
	/**
	 * 
	 * @return
	 */
	public String getModeOfTransportAll() {
		  
		TokenResponseDto responseDto = authorization.accessTokenRequestPost();
		
		Object postBody = null; //Not in use
        
        String path = UriComponentsBuilder.fromPath(basePathVersion + "/mode-of-transport").build().toUriString();
        
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();
        
        headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + responseDto.getAccess_token());
        headerParams.add("Accept-Charset", "utf-8");
        final String[] accepts = { "application/json" };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        //
        final String[] contentTypes = { };
        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);

        //TODO refactor outside
        apiClient.setBasePath(basePath);
        
        ParameterizedTypeReference<String> returnType = new ParameterizedTypeReference<String>() {};
        return apiClient.invokeAPI(path, HttpMethod.GET, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);

//        return testGetDtos();
        
		
	}
	
	
	public ManifestModeOfTransportDto getModeOfTransport(String code) {
		  
		TokenResponseDto responseDto = authorization.accessTokenRequestPost();
		
		Object postBody = null; //Not in use
        
        String path = UriComponentsBuilder.fromPath(basePathVersion + "/mode-of-transport/" + code).build().toUriString();
        
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();
        
        headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + responseDto.getAccess_token());
        headerParams.add("Accept-Charset", "utf-8");
        final String[] accepts = { "application/json" };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        final String[] contentTypes = { };
        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);

        //TODO refactor outside
        apiClient.setBasePath(basePath);
        
        ParameterizedTypeReference<ManifestModeOfTransportDto> returnType = new ParameterizedTypeReference<ManifestModeOfTransportDto>() {};
        return apiClient.invokeAPI(path, HttpMethod.GET, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);

//        return testGetDtos();
        
		
	}
	
	/**
	 * 
	 * @return
	 */
	public String getAllCountries() {
		  
		TokenResponseDto responseDto = authorization.accessTokenRequestPost();
		
		Object postBody = null; //Not in use
        
        String path = UriComponentsBuilder.fromPath(basePathVersion + "/country").build().toUriString();
        
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();
        
        headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + responseDto.getAccess_token());
        headerParams.add("Accept-Charset", "utf-8");
        final String[] accepts = { "application/json" };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        //
        final String[] contentTypes = { };
        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);

        //TODO refactor outside
        apiClient.setBasePath(basePath);
        
        ParameterizedTypeReference<String> returnType = new ParameterizedTypeReference<String>() {};
        return apiClient.invokeAPI(path, HttpMethod.GET, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);

//        return testGetDtos();
        
		
	}
	
	public ManifestCountryDto getCountry(String code) {
		  
		TokenResponseDto responseDto = authorization.accessTokenRequestPost();
		
		Object postBody = null; //Not in use
        
        String path = UriComponentsBuilder.fromPath(basePathVersion + "/country/" + code).build().toUriString();
        
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();
        
        headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + responseDto.getAccess_token());
        headerParams.add("Accept-Charset", "utf-8");
        final String[] accepts = { "application/json" };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        //
        final String[] contentTypes = { };
        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);

        //TODO refactor outside
        apiClient.setBasePath(basePath);
        
        ParameterizedTypeReference<ManifestCountryDto> returnType = new ParameterizedTypeReference<ManifestCountryDto>() {};
        return apiClient.invokeAPI(path, HttpMethod.GET, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);

//        return testGetDtos();
        
		
	}
	
	public String getAllPlaceOfEntry() {
		  
		TokenResponseDto responseDto = authorization.accessTokenRequestPost();
		
		Object postBody = null; //Not in use
        
        String path = UriComponentsBuilder.fromPath(basePathVersion + "/place-of-entry").build().toUriString();
        
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();
        
        headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + responseDto.getAccess_token());
        headerParams.add("Accept-Charset", "utf-8");
        final String[] accepts = { "application/json" };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        final String[] contentTypes = {  };
        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);
        //TODO refactor outside
        apiClient.setBasePath(basePath);
        
        ParameterizedTypeReference<String> returnType = new ParameterizedTypeReference<String>() {};
        return apiClient.invokeAPI(path, HttpMethod.GET, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
        		
	}
	
	/**
	 * 
	 * @return
	 */
	public String getUser() {
		  
		TokenResponseDto responseDto = authorization.accessTokenRequestPost();
		
		Object postBody = null; //Not in use
        
        String path = UriComponentsBuilder.fromPath(basePathVersion + "/user").build().toUriString();
        
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();
        
        headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + responseDto.getAccess_token());
        headerParams.add("Accept-Charset", "utf-8");
        final String[] accepts = { "application/json" };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        final String[] contentTypes = { };
        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);

        //TODO refactor outside
        apiClient.setBasePath(this.basePath);
        
        ParameterizedTypeReference<String> returnType = new ParameterizedTypeReference<String>() {};
        return apiClient.invokeAPI(path, HttpMethod.GET, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);

//        return testGetDtos();
        
		
	}
	
	
	/**
	 * 
	 * @return
	 */
	public String testAuthExpressMovementRoad() {
		  
		TokenResponseDto maskinPortenResponseDto = authorization.accessTokenRequestPostMovementRoad();
		//System.out.println("difi-token:" + maskinPortenResponseDto.getAccess_token());
		TokenResponseDto tollResponseDto = authorization.accessTokenRequestPostToll(maskinPortenResponseDto);
		logger.warn("toll-token:" + tollResponseDto.getAccess_token());
		logger.warn("toll-token expires_in:" + tollResponseDto.getExpires_in());
		
		
		Object postBody = null; //Not in use
		
        //https://api-test.toll.no/api/movement/road/v1/test-auth
		String path = UriComponentsBuilder.fromPath(this.basePathMovementRoadVersion + "/test-auth").build().toUriString();
		System.out.println(path);
        
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();
        
        headerParams.add("Accept-Charset", "utf-8");
        final String[] accepts = { "application/json" };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        final String[] contentTypes = { };
        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);

        
        headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + tollResponseDto.getAccess_token());
        apiClient.setBasePath(this.basePathMovementRoad);
       
        ParameterizedTypeReference<String> returnType = new ParameterizedTypeReference<String>() {};
        
        return apiClient.invokeAPI(path, HttpMethod.GET, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
        		
	}

	/**
	 * Create new - POST
	 * @param mc
	 * @return
	 */
	public String postMasterConsignmentExpressMovementRoad(MasterConsignment mc) {
		  
		TokenResponseDto maskinPortenResponseDto = authorization.accessTokenRequestPostMovementRoad();
		//System.out.println("difi-token:" + maskinPortenResponseDto.getAccess_token());
		TokenResponseDto tollResponseDto = authorization.accessTokenRequestPostToll(maskinPortenResponseDto);
		//System.out.println("toll-token:" + tollResponseDto.getAccess_token());
		System.out.println("toll-token expires_in:" + tollResponseDto.getExpires_in());
		
		
		Object postBody = mc;
		
        //https://api-test.toll.no/api/movement/road/v1/test-auth
		String path = UriComponentsBuilder.fromPath(this.basePathMovementRoadVersion + "/master-consignment").build().toUriString();
		System.out.println(path);
		logger.warn(path);
		
        
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();
        
        headerParams.add("Accept-Charset", "utf-8");
        final String[] accepts = { "application/json" };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        final String[] contentTypes = { };
        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);

        headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + tollResponseDto.getAccess_token());
        apiClient.setBasePath(this.basePathMovementRoad);
       
        ParameterizedTypeReference<String> returnType = new ParameterizedTypeReference<String>() {};
        
        
        return apiClient.invokeAPI(path, HttpMethod.POST, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
        		
	}
	/**
	 * Update - PUT
	 * @param mc
	 * @param mrn
	 * @return
	 */
	public String putMasterConsignmentExpressMovementRoad(MasterConsignment mc, String mrn) {
		  
		TokenResponseDto maskinPortenResponseDto = authorization.accessTokenRequestPostMovementRoad();
		//System.out.println("difi-token:" + maskinPortenResponseDto.getAccess_token());
		TokenResponseDto tollResponseDto = authorization.accessTokenRequestPostToll(maskinPortenResponseDto);
		//System.out.println("toll-token:" + tollResponseDto.getAccess_token());
		System.out.println("toll-token expires_in:" + tollResponseDto.getExpires_in());
		
		
		Object postBody = mc;
		
        //https://api-test.toll.no/api/movement/road/v1/test-auth
		String path = UriComponentsBuilder.fromPath(this.basePathMovementRoadVersion + "/master-consignment/" + mrn).build().toUriString();
		System.out.println(path);
		logger.warn(path);
		
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();
        
        headerParams.add("Accept-Charset", "utf-8");
        final String[] accepts = { "application/json" };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        final String[] contentTypes = { };
        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);

        headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + tollResponseDto.getAccess_token());
        apiClient.setBasePath(this.basePathMovementRoad);
       
        ParameterizedTypeReference<String> returnType = new ParameterizedTypeReference<String>() {};
        
        
        return apiClient.invokeAPI(path, HttpMethod.PUT, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
        		
	}
	/**
	 * DELETE Master
	 * @param mc
	 * @param mrn
	 * @return
	 */
	public String deleteMasterConsignmentExpressMovementRoad(MasterConsignment mc, String mrn) {
		  
		TokenResponseDto maskinPortenResponseDto = authorization.accessTokenRequestPostMovementRoad();
		//System.out.println("difi-token:" + maskinPortenResponseDto.getAccess_token());
		TokenResponseDto tollResponseDto = authorization.accessTokenRequestPostToll(maskinPortenResponseDto);
		//System.out.println("toll-token:" + tollResponseDto.getAccess_token());
		System.out.println("toll-token expires_in:" + tollResponseDto.getExpires_in());
		
		
		Object postBody = mc;
		
        //https://api-test.toll.no/api/movement/road/v1/test-auth
		String path = UriComponentsBuilder.fromPath(this.basePathMovementRoadVersion + "/master-consignment/" + mrn).build().toUriString();
		System.out.println(path);
		logger.warn(path);
		
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();
        
        headerParams.add("Accept-Charset", "utf-8");
        final String[] accepts = { "application/json" };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        final String[] contentTypes = { };
        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);

        headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + tollResponseDto.getAccess_token());
        apiClient.setBasePath(this.basePathMovementRoad);
       
        ParameterizedTypeReference<String> returnType = new ParameterizedTypeReference<String>() {};
        
        
        return apiClient.invokeAPI(path, HttpMethod.DELETE, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
        		
	}
	/**
	 * For testing purposes - use the above (without json-String)
	 * @param jsonPayload
	 * @return
	 */
	public String postHouseConsignmentExpressMovementRoad(HouseConsignment hc) {
		  
		TokenResponseDto maskinPortenResponseDto = authorization.accessTokenRequestPostMovementRoad();
		//System.out.println("difi-token:" + maskinPortenResponseDto.getAccess_token());
		TokenResponseDto tollResponseDto = authorization.accessTokenRequestPostToll(maskinPortenResponseDto);
		//System.out.println("toll-token:" + tollResponseDto.getAccess_token());
		System.out.println("toll-token expires_in:" + tollResponseDto.getExpires_in());
		
		//Debug for JSON string
		try {
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(hc);
			logger.warn(json);
		}catch(Exception e) {
			e.toString();
			
		}
		Object postBody = hc;
		
		
        //https://api-test.toll.no/api/movement/road/v1/test-auth
		String path = UriComponentsBuilder.fromPath(this.basePathMovementRoadVersion + "/house-consignment").build().toUriString();
		System.out.println(path);
		logger.warn(path);
		
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();
        
        headerParams.add("Accept-Charset", "utf-8");
        final String[] accepts = { "application/json" };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        final String[] contentTypes = { };
        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);

        headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + tollResponseDto.getAccess_token());
        apiClient.setBasePath(this.basePathMovementRoad);
       
        ParameterizedTypeReference<String> returnType = new ParameterizedTypeReference<String>() {};
        
        
        return apiClient.invokeAPI(path, HttpMethod.POST, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
        		
	}
	
	
	/**
	 * 
	 * @param lrn
	 * @return
	 * @throws Exception
	 */
	public String getValidationStatusMasterConsignmentExpressMovementRoad(String lrn) throws Exception {
		  
		TokenResponseDto maskinPortenResponseDto = authorization.accessTokenRequestPostMovementRoad();
		//System.out.println("difi-token:" + maskinPortenResponseDto.getAccess_token());
		TokenResponseDto tollResponseDto = authorization.accessTokenRequestPostToll(maskinPortenResponseDto);
		System.out.println("toll-token:" + tollResponseDto.getAccess_token());
		System.out.println("toll-token expires_in:" + tollResponseDto.getExpires_in());
		
		
		Object postBody = null; //Not in use
		
        //https://api-test.toll.no/api/movement/road/v1/test-auth
		String path = UriComponentsBuilder.fromPath(this.basePathMovementRoadVersion + "/master-consignment/validation-status/" + lrn).build().toUriString();
		System.out.println(path);
		logger.warn(path);
		
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();
        
        headerParams.add("Accept-Charset", "utf-8");
        final String[] accepts = { "application/json" };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        final String[] contentTypes = { };
        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);
        
        headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + tollResponseDto.getAccess_token());
        apiClient.setBasePath(this.basePathMovementRoad);
       
        ParameterizedTypeReference<String> returnType = new ParameterizedTypeReference<String>() {};
        String tmpResponse = "";
        
        try {
        	tmpResponse = apiClient.invokeAPI(path, HttpMethod.GET, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
        	
        }catch(Exception e) {
        	//e.printStackTrace();
			//Get out stackTrace to the response (errMsg)
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			throw new Exception(sw.toString());
        }
        
        
        return tmpResponse;
        		
	}

	

	
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
	
}
