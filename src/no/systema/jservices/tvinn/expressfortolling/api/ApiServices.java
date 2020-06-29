package no.systema.jservices.tvinn.expressfortolling.api;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import no.systema.jservices.common.dto.CountryDto;
import no.systema.jservices.common.dto.ModeOfTransportDto;
import no.systema.jservices.common.dto.TypesOfExportDto;
import no.systema.jservices.common.dto.UserDto;
import no.systema.jservices.common.util.CommonClientHttpRequestInterceptor;
import no.systema.jservices.common.util.CommonResponseErrorHandler;
import no.systema.jservices.common.dto.TransportationCompanyDto;
import no.systema.jservices.tvinn.expressfortolling.jwt.DifiJwtCreator;
import no.systema.jservices.tvinn.kurermanifest.util.Utils;

/**
 * This class hosts all needed REST calls for Express Fortolling.
 * 
 * @author fredrikmoller
 * @date 2019-08-28
 */
@Service
public class ApiServices {
	private static Logger logger = Logger.getLogger(ApiServices.class.getName());
	private RestTemplate restTemplate;
	
	@Autowired
    private ApiClient apiClient;
	
	@Autowired
    private ApiUploadClient apiUploadClient;
	
	@Autowired
	DifiJwtCreator difiJwtCreator;

	@Autowired
	Authorization authorization;

	@Value("${expft.basepath.version}")
    private String basePathVersion;	

	@Value("${expft.basepath}")
    private String basePath;		
	
	@Value("${expft.upload.url}")
	private String uploadUrl;
	
	@Value("${expft.upload.prod.url}")
	private String uploadProdUrl;
	/**
	 * Get all transportation companies for which the logged in user is a customs representative.
	 * GET /api/exf/ekspressfortolling/transportation-company
	 * Accept: application/json
	 * Host: ekspressfortolling.toll.no
	 * Version: 1
	 * 
	 * @return List<TransportationCompanyDto>
	 */
	public TransportationCompanyDto getTransportationCompany(String orgNr) {
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
        
        ParameterizedTypeReference<TransportationCompanyDto> returnType = new ParameterizedTypeReference<TransportationCompanyDto>() {};
        return apiClient.invokeAPI(path, HttpMethod.GET, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);

//        return testGetDtos();
        
		
	}
	
	private List<TransportationCompanyDto> testGetDtos() {
		List<TransportationCompanyDto> list = new ArrayList<TransportationCompanyDto>();
		
		TransportationCompanyDto dto = new TransportationCompanyDto();
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
        headerParams.setContentType(MediaType.APPLICATION_JSON_UTF8);
        
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
        
        String path = UriComponentsBuilder.fromPath(basePathVersion + "/manifest" + "/" + manifestId).build().toUriString() + "/cargo-line";
        logger.warn(path);
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
	
	public String deleteManifest(String manifestId) {
		  
		TokenResponseDto authTokenDto = authorization.accessTokenRequestPost();
		
		Object postBody = null; //Not in use
        
        String path = UriComponentsBuilder.fromPath(basePathVersion + "/manifest" + "/" + manifestId).build().toUriString();
        
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
        
        String resultDto;
        try{
        	ParameterizedTypeReference<String> returnType = new ParameterizedTypeReference<String>() {};
        	resultDto = apiClient.invokeAPI(path, HttpMethod.DELETE, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
        }catch(HttpClientErrorException e){
        	String responseBody = e.getResponseBodyAsString();
			logger.error("ERROR http code:" + e.getRawStatusCode());
			logger.error("Response body:" + responseBody);
			logger.error("e:" + e);
			
			throw e;
		}
        return resultDto;
        		
	}
	
	
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
		String path = this.uploadUrl.toString() + "/" + manifestId + "/cargo-line/";
		
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
	
	
	public TypesOfExportDto getTypeOfExport(String type) {
		  
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
        
        TypesOfExportDto resultDto;
        try{
        	ParameterizedTypeReference<TypesOfExportDto> returnType = new ParameterizedTypeReference<TypesOfExportDto>() {};
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
	
	
	public ModeOfTransportDto getModeOfTransport(String code) {
		  
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
        
        ParameterizedTypeReference<ModeOfTransportDto> returnType = new ParameterizedTypeReference<ModeOfTransportDto>() {};
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
	
	public CountryDto getCountry(String code) {
		  
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
        
        ParameterizedTypeReference<CountryDto> returnType = new ParameterizedTypeReference<CountryDto>() {};
        return apiClient.invokeAPI(path, HttpMethod.GET, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);

//        return testGetDtos();
        
		
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
        apiClient.setBasePath(basePath);
        
        ParameterizedTypeReference<String> returnType = new ParameterizedTypeReference<String>() {};
        return apiClient.invokeAPI(path, HttpMethod.GET, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);

//        return testGetDtos();
        
		
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
