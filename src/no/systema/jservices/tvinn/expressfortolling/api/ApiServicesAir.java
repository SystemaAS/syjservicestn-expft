package no.systema.jservices.tvinn.expressfortolling.api;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
import no.systema.jservices.tvinn.digitoll.v2.dao.Transport;
import no.systema.jservices.tvinn.expressfortolling.jwt.DifiJwtCreator;
import no.systema.jservices.tvinn.expressfortolling2.dao.HouseConsignment;
import no.systema.jservices.tvinn.expressfortolling2.dao.MasterConsignment;
import no.systema.jservices.tvinn.kurermanifest.util.Utils;

/**
 * This class hosts all needed REST calls for Digitoll Air.
 * 
 * @author oscardelatorre
 * @date 2023-09
 */
@Service
public class ApiServicesAir {
	private static Logger logger = LoggerFactory.getLogger(ApiServicesAir.class.getName());
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
	
	
	
	@Value("${expft.basepath.movement.air}")
    private String basePathMovementAir;
	
	@Value("${expft.basepath.movement.air.version}")
    private String basePathMovementAirVersion;
	
	@Value("${expft.basepath.movement.air.status.version}")
    private String basePathMovementStatusAirVersion;
	
	@Value("${expft.basepath.movement.routing.version}")
    private String basePathMovementRoutingVersion;
	
	
	
	
	@Value("${digitoll.access.use.proxy}")
	String proxyIsUsed;
	
	@Value("${digitoll.access.proxy.host}")
	String proxyHost;
	
	@Value("${digitoll.access.proxy.port}")
	Integer proxyPort;
	
	

	/**
	 * 
	 * @param transport
	 * @param tollTokenMap
	 * @return
	 */
	public String postTransportDigitollV2(Transport transport, Map tollTokenMap ) {
		  
		TokenResponseDto maskinPortenResponseDto = authorization.accessTokenRequestPostMovementAir();
		//System.out.println("difi-token:" + maskinPortenResponseDto.getAccess_token());
		TokenResponseDto tollResponseDto = authorization.accessTokenRequestPostToll(maskinPortenResponseDto);
		//System.out.println("toll-token:" + tollResponseDto.getAccess_token());
		System.out.println("toll-token expires_in:" + tollResponseDto.getExpires_in());
		tollTokenMap.put(1, tollResponseDto);
		
		//Debug for JSON string
		try {
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(transport);
			//logger.debug(json);
			
		}catch(Exception e) {
			e.toString();
			
		}
		
		//reset for proxy if needed
    	logger.info("proxy:" + this.proxyIsUsed);
        if(Boolean.parseBoolean(proxyIsUsed)) {
        	apiClient.resetRestTemplateWithProxy(this.proxyHost, this.proxyPort);
        }
        
		Object postBody = transport;
		
        //https://api-test.toll.no/api/movement/air/v1/test-auth
		String path = UriComponentsBuilder.fromPath(this.basePathMovementAirVersion + "/transport").build().toUriString();
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
        apiClient.setBasePath(this.basePathMovementAir);
       
        ParameterizedTypeReference<String> returnType = new ParameterizedTypeReference<String>() {};
        
        
        return apiClient.invokeAPI(path, HttpMethod.POST, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
        		
	}
	/**
	 * 
	 * @param transport
	 * @param mrn
	 * @param tollTokenMap -> in order to return the final token to the controller (per request)
	 * @return
	 */
	public String putTransportDigitollV2(Transport transport, String mrn, Map tollTokenMap ) {
		  
		TokenResponseDto maskinPortenResponseDto = authorization.accessTokenRequestPostMovementAir();
		//System.out.println("difi-token:" + maskinPortenResponseDto.getAccess_token());
		TokenResponseDto tollResponseDto = authorization.accessTokenRequestPostToll(maskinPortenResponseDto);
		//System.out.println("toll-token:" + tollResponseDto.getAccess_token());
		System.out.println("toll-token expires_in:" + tollResponseDto.getExpires_in());
		tollTokenMap.put(1, tollResponseDto);
		//Debug for JSON string
		try {
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(transport);
			logger.warn(json);
			
		}catch(Exception e) {
			e.toString();
			
		}
		
		//reset for proxy if needed
    	logger.info("proxy:" + this.proxyIsUsed);
        if(Boolean.parseBoolean(proxyIsUsed)) {
        	apiClient.resetRestTemplateWithProxy(this.proxyHost, this.proxyPort);
        }
        
		Object postBody = transport;
		
        //https://api-test.toll.no/api/movement/road/v1/test-auth
		String path = UriComponentsBuilder.fromPath(this.basePathMovementAirVersion + "/transport/" + mrn).build().toUriString();
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
        apiClient.setBasePath(this.basePathMovementAir);
       
        ParameterizedTypeReference<String> returnType = new ParameterizedTypeReference<String>() {};
        
        
        return apiClient.invokeAPI(path, HttpMethod.PUT, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
        		
	}
	
	/**
	 * 
	 * @param transport
	 * @param mrn
	 * @return
	 */
	public String deleteTransportDigitollV2(Transport transport, String mrn) {
		  
		TokenResponseDto maskinPortenResponseDto = authorization.accessTokenRequestPostMovementAir();
		//System.out.println("difi-token:" + maskinPortenResponseDto.getAccess_token());
		TokenResponseDto tollResponseDto = authorization.accessTokenRequestPostToll(maskinPortenResponseDto);
		//System.out.println("toll-token:" + tollResponseDto.getAccess_token());
		System.out.println("toll-token expires_in:" + tollResponseDto.getExpires_in());
		
		//reset for proxy if needed
    	logger.info(this.proxyIsUsed);
        if(Boolean.parseBoolean(proxyIsUsed)) {
        	apiClient.resetRestTemplateWithProxy(this.proxyHost, this.proxyPort);
        }
        
		Object postBody = transport;
		
        //https://api-test.toll.no/api/movement/road/v1/test-auth
		String path = UriComponentsBuilder.fromPath(this.basePathMovementAirVersion + "/transport/" + mrn).build().toUriString();
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
        apiClient.setBasePath(this.basePathMovementAir);
       
        ParameterizedTypeReference<String> returnType = new ParameterizedTypeReference<String>() {};
        
        
        return apiClient.invokeAPI(path, HttpMethod.DELETE, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
        		
	}
	
	/**
	 * 
	 * @param lrn
	 * @param tollTokenMap
	 * @return
	 * @throws Exception
	 */
	public String getValidationStatusTransportDigitollV2(String lrn, Map tollTokenMap) throws Exception {
		
		TokenResponseDto tollResponseDto = (TokenResponseDto)tollTokenMap.get(1);
		
		//reset for proxy if needed
    	logger.info(this.proxyIsUsed);
        if(Boolean.parseBoolean(proxyIsUsed)) {
        	apiClient.resetRestTemplateWithProxy(this.proxyHost, this.proxyPort);
        }
        
		Object postBody = null; //Not in use
		
        //https://api-test.toll.no/api/movement/road/status/v2/ -->check the difference against all other end-points that do not have "status" in the path
		String path = UriComponentsBuilder.fromPath(this.basePathMovementStatusAirVersion + "/transport/validation-status/" + lrn).build().toUriString();
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
        //headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + tollResponseDto.getAccess_token());
        apiClient.setBasePath(this.basePathMovementAir);
       
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
	
	/**
	 * 
	 * @param lrn
	 * @return
	 * @throws Exception
	 */
	public String getValidationStatusTransportDigitollV2(String lrn) throws Exception {
		
		TokenResponseDto maskinPortenResponseDto = authorization.accessTokenRequestPostMovementAir();
		//System.out.println("difi-token:" + maskinPortenResponseDto.getAccess_token());
		TokenResponseDto tollResponseDto = authorization.accessTokenRequestPostToll(maskinPortenResponseDto);
		logger.warn("toll-token:" + tollResponseDto.getAccess_token());
		
		//reset for proxy if needed
    	logger.info(this.proxyIsUsed);
        if(Boolean.parseBoolean(proxyIsUsed)) {
        	apiClient.resetRestTemplateWithProxy(this.proxyHost, this.proxyPort);
        }
        
		Object postBody = null; //Not in use
		
        //https://api-test.toll.no/api/movement/road/status/v2/ -->check the difference against all other end-points that do not have "status" in the path
		String path = UriComponentsBuilder.fromPath(this.basePathMovementStatusAirVersion + "/transport/validation-status/" + lrn).build().toUriString();
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
        //headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + tollResponseDto.getAccess_token());
        apiClient.setBasePath(this.basePathMovementAir);
       
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
	
	/**
	 * 
	 * @param mc
	 * @param tollTokenMap
	 * @return
	 */
	public String postMasterConsignmentDigitollV2(no.systema.jservices.tvinn.digitoll.v2.dao.MasterConsignment mc, Map tollTokenMap) {
		  
		TokenResponseDto maskinPortenResponseDto = authorization.accessTokenRequestPostMovementAir();
		//System.out.println("difi-token:" + maskinPortenResponseDto.getAccess_token());
		TokenResponseDto tollResponseDto = authorization.accessTokenRequestPostToll(maskinPortenResponseDto);
		//System.out.println("toll-token:" + tollResponseDto.getAccess_token());
		System.out.println("toll-token expires_in:" + tollResponseDto.getExpires_in());
		tollTokenMap.put(1, tollResponseDto);
		logger.warn("toll-token expires_in:" + tollResponseDto.getExpires_in());
		
		//reset for proxy if needed
    	logger.info(this.proxyIsUsed);
        if(Boolean.parseBoolean(proxyIsUsed)) {
        	apiClient.resetRestTemplateWithProxy(this.proxyHost, this.proxyPort);
        }
		
		//Debug for JSON string
		try {
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(mc);
			logger.warn(json);
			
		}catch(Exception e) {
			e.toString();
			
		}
		Object postBody = mc;
		
        //https://api-test.toll.no/api/movement/road/v1/test-auth
		String path = UriComponentsBuilder.fromPath(this.basePathMovementAirVersion + "/master-consignment").build().toUriString();
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
        apiClient.setBasePath(this.basePathMovementAir);
       
        ParameterizedTypeReference<String> returnType = new ParameterizedTypeReference<String>() {};
        
        
        return apiClient.invokeAPI(path, HttpMethod.POST, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
        		
	}

	/**
	 * 
	 * @param mc
	 * @param mrn
	 * @param tollTokenMap
	 * @return
	 */
	public String putMasterConsignmentDigitollV2(no.systema.jservices.tvinn.digitoll.v2.dao.MasterConsignment mc, String mrn, Map tollTokenMap) {
		  
		TokenResponseDto maskinPortenResponseDto = authorization.accessTokenRequestPostMovementAir();
		//System.out.println("difi-token:" + maskinPortenResponseDto.getAccess_token());
		TokenResponseDto tollResponseDto = authorization.accessTokenRequestPostToll(maskinPortenResponseDto);
		//System.out.println("toll-token:" + tollResponseDto.getAccess_token());
		System.out.println("toll-token expires_in:" + tollResponseDto.getExpires_in());
		tollTokenMap.put(1, tollResponseDto);
		//Debug for JSON string
		try {
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(mc);
			logger.warn(json);
			
		}catch(Exception e) {
			e.toString();
			
		}
		
		//reset for proxy if needed
    	logger.info(this.proxyIsUsed);
        if(Boolean.parseBoolean(proxyIsUsed)) {
        	apiClient.resetRestTemplateWithProxy(this.proxyHost, this.proxyPort);
        }
		
		Object postBody = mc;
		
        //https://api-test.toll.no/api/movement/road/v1/test-auth
		String path = UriComponentsBuilder.fromPath(this.basePathMovementAirVersion + "/master-consignment/" + mrn).build().toUriString();
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
        apiClient.setBasePath(this.basePathMovementAir);
       
        ParameterizedTypeReference<String> returnType = new ParameterizedTypeReference<String>() {};
        
        
        return apiClient.invokeAPI(path, HttpMethod.PUT, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
        		
	}
	/**
	 * 
	 * @param mc
	 * @param mrn
	 * @return
	 */
	public String deleteMasterConsignmentDigitollV2(no.systema.jservices.tvinn.digitoll.v2.dao.MasterConsignment mc, String mrn) {
		  
		TokenResponseDto maskinPortenResponseDto = authorization.accessTokenRequestPostMovementAir();
		//System.out.println("difi-token:" + maskinPortenResponseDto.getAccess_token());
		TokenResponseDto tollResponseDto = authorization.accessTokenRequestPostToll(maskinPortenResponseDto);
		//System.out.println("toll-token:" + tollResponseDto.getAccess_token());
		System.out.println("toll-token expires_in:" + tollResponseDto.getExpires_in());
		
		//reset for proxy if needed
    	logger.info(this.proxyIsUsed);
        if(Boolean.parseBoolean(proxyIsUsed)) {
        	apiClient.resetRestTemplateWithProxy(this.proxyHost, this.proxyPort);
        }
        
		Object postBody = mc;
		
        //https://api-test.toll.no/api/movement/road/v1/test-auth
		String path = UriComponentsBuilder.fromPath(this.basePathMovementAirVersion + "/master-consignment/" + mrn).build().toUriString();
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
        apiClient.setBasePath(this.basePathMovementAir);
       
        ParameterizedTypeReference<String> returnType = new ParameterizedTypeReference<String>() {};
        
        
        return apiClient.invokeAPI(path, HttpMethod.DELETE, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
        		
	}
	/**
	 * 
	 * @param lrn
	 * @param tollTokenMap
	 * @return
	 * @throws Exception
	 */
	public String getValidationStatusMasterConsignmentDigitollV2(String lrn, Map tollTokenMap) throws Exception {
		
		TokenResponseDto tollResponseDto = (TokenResponseDto)tollTokenMap.get(1);
		
		//reset for proxy if needed
    	logger.info(this.proxyIsUsed);
        if(Boolean.parseBoolean(proxyIsUsed)) {
        	apiClient.resetRestTemplateWithProxy(this.proxyHost, this.proxyPort);
        }
        
		Object postBody = null; //Not in use
		
        //https://api-test.toll.no/api/movement/road/status/v2/ -->check the difference against all other end-points that do not have "status" in the path
		String path = UriComponentsBuilder.fromPath(this.basePathMovementStatusAirVersion + "/master-consignment/validation-status/" + lrn).build().toUriString();
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
        //headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + tollResponseDto.getAccess_token());
        apiClient.setBasePath(this.basePathMovementAir);
       
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
	
	/**
	 * 
	 * @param lrn
	 * @return
	 * @throws Exception
	 */
	public String getValidationStatusMasterConsignmentDigitollV2(String lrn) throws Exception {
		
		TokenResponseDto maskinPortenResponseDto = authorization.accessTokenRequestPostMovementAir();
		//System.out.println("difi-token:" + maskinPortenResponseDto.getAccess_token());
		TokenResponseDto tollResponseDto = authorization.accessTokenRequestPostToll(maskinPortenResponseDto);
		logger.warn("toll-token:" + tollResponseDto.getAccess_token());
		
		//reset for proxy if needed
    	logger.info(this.proxyIsUsed);
        if(Boolean.parseBoolean(proxyIsUsed)) {
        	apiClient.resetRestTemplateWithProxy(this.proxyHost, this.proxyPort);
        }
		
		Object postBody = null; //Not in use
		
        //https://api-test.toll.no/api/movement/road/status/v2/ -->check the difference against all other end-points that do not have "status" in the path
		String path = UriComponentsBuilder.fromPath(this.basePathMovementStatusAirVersion + "/master-consignment/validation-status/" + lrn).build().toUriString();
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
        //headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + tollResponseDto.getAccess_token());
        apiClient.setBasePath(this.basePathMovementAir);
       
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

	/**
	 * This method returns a list with all document references received (at toll.no) so far ...
	 * @param mrn
	 * @return
	 * @throws Exception
	 */
	public String getDocsReceivedMasterConsignmentDigitollV2(String mrn) throws Exception {
		
		TokenResponseDto maskinPortenResponseDto = authorization.accessTokenRequestPostMovementAir();
		//System.out.println("difi-token:" + maskinPortenResponseDto.getAccess_token());
		TokenResponseDto tollResponseDto = authorization.accessTokenRequestPostToll(maskinPortenResponseDto);
		logger.warn("toll-token:" + tollResponseDto.getAccess_token());
		
		//reset for proxy if needed
    	logger.info(this.proxyIsUsed);
        if(Boolean.parseBoolean(proxyIsUsed)) {
        	apiClient.resetRestTemplateWithProxy(this.proxyHost, this.proxyPort);
        }
        
        
		Object postBody = null; //Not in use
		
        //https://api-test.toll.no/api/movement/road/status/v2/master-consignment/23NONJB08UP98SOBT7/transport-document/status
		String path = UriComponentsBuilder.fromPath(this.basePathMovementStatusAirVersion + "/master-consignment/" + mrn + "/transport-document/status").build().toUriString();
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
        apiClient.setBasePath(this.basePathMovementAir);
       
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

	/**
	 * 
	 * @param hc
	 * @param tollTokenMap
	 * @return
	 */
	public String postHouseConsignmentDigitollV2(no.systema.jservices.tvinn.digitoll.v2.dao.HouseConsignment hc, Map tollTokenMap) {
		  
		TokenResponseDto maskinPortenResponseDto = authorization.accessTokenRequestPostMovementAir();
		//System.out.println("difi-token:" + maskinPortenResponseDto.getAccess_token());
		TokenResponseDto tollResponseDto = authorization.accessTokenRequestPostToll(maskinPortenResponseDto);
		//System.out.println("toll-token:" + tollResponseDto.getAccess_token());
		System.out.println("toll-token expires_in:" + tollResponseDto.getExpires_in());
		tollTokenMap.put(1, tollResponseDto);
		logger.warn("toll-token expires_in:" + tollResponseDto.getExpires_in());
		
		//Debug for JSON string
		try {
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(hc);
			logger.warn(json);
			
		}catch(Exception e) {
			e.toString();
			
		}
		
		//reset for proxy if needed
    	logger.info(this.proxyIsUsed);
        if(Boolean.parseBoolean(proxyIsUsed)) {
        	apiClient.resetRestTemplateWithProxy(this.proxyHost, this.proxyPort);
        }
        
		Object postBody = hc;
		
        //https://api-test.toll.no/api/movement/road/v1/test-auth
		String path = UriComponentsBuilder.fromPath(this.basePathMovementAirVersion + "/house-consignment").build().toUriString();
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
        apiClient.setBasePath(this.basePathMovementAir);
       
        ParameterizedTypeReference<String> returnType = new ParameterizedTypeReference<String>() {};
        
        
        return apiClient.invokeAPI(path, HttpMethod.POST, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
        		
	}
	
	/**
	 * 
	 * @param mc
	 * @param mrn
	 * @param tollTokenMap
	 * @return
	 */
	public String putHouseConsignmentDigitollV2(no.systema.jservices.tvinn.digitoll.v2.dao.HouseConsignment hc, String mrn, Map tollTokenMap) {
		  
		TokenResponseDto maskinPortenResponseDto = authorization.accessTokenRequestPostMovementAir();
		//System.out.println("difi-token:" + maskinPortenResponseDto.getAccess_token());
		TokenResponseDto tollResponseDto = authorization.accessTokenRequestPostToll(maskinPortenResponseDto);
		//System.out.println("toll-token:" + tollResponseDto.getAccess_token());
		System.out.println("toll-token expires_in:" + tollResponseDto.getExpires_in());
		tollTokenMap.put(1, tollResponseDto);
		//Debug for JSON string
		try {
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(hc);
			logger.warn(json);
			
		}catch(Exception e) {
			e.toString();
			
		}
		
		//reset for proxy if needed
    	logger.info(this.proxyIsUsed);
        if(Boolean.parseBoolean(proxyIsUsed)) {
        	apiClient.resetRestTemplateWithProxy(this.proxyHost, this.proxyPort);
        }
        
		Object postBody = hc;
		
        //https://api-test.toll.no/api/movement/road/v1/test-auth
		String path = UriComponentsBuilder.fromPath(this.basePathMovementAirVersion + "/house-consignment/" + mrn).build().toUriString();
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
        apiClient.setBasePath(this.basePathMovementAir);
       
        ParameterizedTypeReference<String> returnType = new ParameterizedTypeReference<String>() {};
        
        
        return apiClient.invokeAPI(path, HttpMethod.PUT, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
        		
	}
	
	/**
	 * 
	 * @param lrn
	 * @param tollTokenMap
	 * @return
	 * @throws Exception
	 */
	public String getValidationStatusHouseConsignmentDigitollV2(String lrn, Map tollTokenMap) throws Exception {
		
		TokenResponseDto tollResponseDto = (TokenResponseDto)tollTokenMap.get(1);
		
		//reset for proxy if needed
    	logger.info(this.proxyIsUsed);
        if(Boolean.parseBoolean(proxyIsUsed)) {
        	apiClient.resetRestTemplateWithProxy(this.proxyHost, this.proxyPort);
        }
        
		Object postBody = null; //Not in use
		
        //https://api-test.toll.no/api/movement/road/status/v2/ -->check the difference against all other end-points that do not have "status" in the path
		String path = UriComponentsBuilder.fromPath(this.basePathMovementStatusAirVersion + "/house-consignment/validation-status/" + lrn).build().toUriString();
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
        //headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + tollResponseDto.getAccess_token());
        apiClient.setBasePath(this.basePathMovementAir);
       
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
	
	/**
	 * 
	 * @param hc
	 * @param mrn
	 * @return
	 */
	public String deleteHouseConsignmentDigitollV2(no.systema.jservices.tvinn.digitoll.v2.dao.HouseConsignment hc, String mrn) {
		  
		TokenResponseDto maskinPortenResponseDto = authorization.accessTokenRequestPostMovementAir();
		//System.out.println("difi-token:" + maskinPortenResponseDto.getAccess_token());
		TokenResponseDto tollResponseDto = authorization.accessTokenRequestPostToll(maskinPortenResponseDto);
		//System.out.println("toll-token:" + tollResponseDto.getAccess_token());
		System.out.println("toll-token expires_in:" + tollResponseDto.getExpires_in());
		
		//reset for proxy if needed
    	logger.info(this.proxyIsUsed);
        if(Boolean.parseBoolean(proxyIsUsed)) {
        	apiClient.resetRestTemplateWithProxy(this.proxyHost, this.proxyPort);
        }
        
		Object postBody = hc;
		
        //https://api-test.toll.no/api/movement/road/v1/test-auth
		String path = UriComponentsBuilder.fromPath(this.basePathMovementAirVersion + "/house-consignment/" + mrn).build().toUriString();
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
        apiClient.setBasePath(this.basePathMovementAir);
       
        ParameterizedTypeReference<String> returnType = new ParameterizedTypeReference<String>() {};
        
        
        return apiClient.invokeAPI(path, HttpMethod.DELETE, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
        		
	}
	/**
	 * 
	 * @param lrn
	 * @return
	 * @throws Exception
	 */
	public String getValidationStatusHouseConsignmentDigitollV2(String lrn) throws Exception {
		
		TokenResponseDto maskinPortenResponseDto = authorization.accessTokenRequestPostMovementAir();
		//System.out.println("difi-token:" + maskinPortenResponseDto.getAccess_token());
		TokenResponseDto tollResponseDto = authorization.accessTokenRequestPostToll(maskinPortenResponseDto);
		logger.warn("toll-token:" + tollResponseDto.getAccess_token());
		
		//reset for proxy if needed
    	logger.info(this.proxyIsUsed);
        if(Boolean.parseBoolean(proxyIsUsed)) {
        	apiClient.resetRestTemplateWithProxy(this.proxyHost, this.proxyPort);
        }
        
		Object postBody = null; //Not in use
		
        //https://api-test.toll.no/api/movement/road/status/v2/ -->check the difference against all other end-points that do not have "status" in the path
		String path = UriComponentsBuilder.fromPath(this.basePathMovementStatusAirVersion + "/house-consignment/validation-status/" + lrn).build().toUriString();
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
        //headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + tollResponseDto.getAccess_token());
        apiClient.setBasePath(this.basePathMovementAir);
       
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
	/**
	 * Only for Air and testing requirement end-point
	 * @return
	 * @throws Exception
	 */
	public String getRoutingHouseConsignmentDigitollV2() throws Exception {
		
		TokenResponseDto maskinPortenResponseDto = authorization.accessTokenRequestPostMovementEntry();
		//System.out.println("difi-token:" + maskinPortenResponseDto.getAccess_token());
		TokenResponseDto tollResponseDto = authorization.accessTokenRequestPostToll(maskinPortenResponseDto);
		logger.warn("toll-token:" + tollResponseDto.getAccess_token());
		
		//reset for proxy if needed
    	logger.info(this.proxyIsUsed);
        if(Boolean.parseBoolean(proxyIsUsed)) {
        	apiClient.resetRestTemplateWithProxy(this.proxyHost, this.proxyPort);
        }
        
		Object postBody = null; //Not in use
		
        //https://api-test.toll.no/api/movement/routing/v1/houseConsignment -->check the difference against all other end-points that do not have "status" in the path
		String path = UriComponentsBuilder.fromPath(this.basePathMovementRoutingVersion + "/house-consignment").build().toUriString();
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
        //headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + tollResponseDto.getAccess_token());
        apiClient.setBasePath(this.basePathMovementAir);
       
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
	
	/**
	 * Only for Air and testing requirement end-point
	 * @return
	 * @throws Exception
	 */
	public String getRoutingTransportDigitollV2() throws Exception {
		
		TokenResponseDto maskinPortenResponseDto = authorization.accessTokenRequestPostMovementEntry();
		//System.out.println("difi-token:" + maskinPortenResponseDto.getAccess_token());
		TokenResponseDto tollResponseDto = authorization.accessTokenRequestPostToll(maskinPortenResponseDto);
		logger.warn("toll-token:" + tollResponseDto.getAccess_token());
		
		//reset for proxy if needed
    	logger.info(this.proxyIsUsed);
        if(Boolean.parseBoolean(proxyIsUsed)) {
        	apiClient.resetRestTemplateWithProxy(this.proxyHost, this.proxyPort);
        }
        
		Object postBody = null; //Not in use
		
        //https://api-test.toll.no/api/movement/routing/v1/houseConsignment -->check the difference against all other end-points that do not have "status" in the path
		String path = UriComponentsBuilder.fromPath(this.basePathMovementRoutingVersion + "/transport").build().toUriString();
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
        //headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + tollResponseDto.getAccess_token());
        apiClient.setBasePath(this.basePathMovementAir);
       
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
