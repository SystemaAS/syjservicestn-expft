package no.systema.jservices.tvinn.expressfortolling.api;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import no.systema.jservices.common.dto.ModeOfTransportDto;
import no.systema.jservices.common.dto.TypesOfExportDto;
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
	
	/**
	 * Get all transportation companies for which the logged in user is a customs representative.
	 * GET /api/exf/ekspressfortolling/transportation-company
	 * Accept: application/json
	 * Host: ekspressfortolling.toll.no
	 * Version: 1
	 * 
	 * @return List<TransportationCompanyDto>
	 */
	public TransportationCompanyDto getTransportationCompany() {
  
		TokenResponseDto responseDto = authorization.accessTokenRequestPost();
		//For test, couldn't get Mockito to work correct.
//		TokenResponseDto responseDto = new TokenResponseDto();
//		responseDto.setAccess_token("XYZ");
		
		Object postBody = null; //Not in use
        
        String path = UriComponentsBuilder.fromPath(basePathVersion + "/transportation-company/936809219").build().toUriString();
        
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();
        
        headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + responseDto.getAccess_token());

        final String[] accepts = { 
            "application/json", "text/json"
        };
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
	
	/**
	 * 
	 * @return
	 */
	public ModeOfTransportDto getModeOfTransport() {
		  
		TokenResponseDto responseDto = authorization.accessTokenRequestPost();
		//For test, couldn't get Mockito to work correct.
//		TokenResponseDto responseDto = new TokenResponseDto();
//		responseDto.setAccess_token("XYZ");
		
		Object postBody = null; //Not in use
        
        String path = UriComponentsBuilder.fromPath(basePathVersion + "/mode-of-transport/").build().toUriString();
        
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();
        
        headerParams.add(HttpHeaders.AUTHORIZATION, "Bearer " + responseDto.getAccess_token());

        final String[] accepts = { 
            "application/json", "text/json"
        };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        //
        final String[] contentTypes = { MediaTypes.HAL_JSON.toString() };
        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);

        //TODO refactor outside
        apiClient.setBasePath(basePath);
        
        ParameterizedTypeReference<ModeOfTransportDto> returnType = new ParameterizedTypeReference<ModeOfTransportDto>() {};
        return apiClient.invokeAPI(path, HttpMethod.GET, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);

//        return testGetDtos();
        
		
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

        final String[] accepts = { 
            "application/json", "text/json"
        };
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
        headerParams.add("Accept", "application/json;charset=utf-8");
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

        final String[] accepts = { "application/json", "text/json"};
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        final String[] contentTypes = {  };
        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);
        //TODO refactor outside
        apiClient.setBasePath(basePath);
        
        ParameterizedTypeReference<String> returnType = new ParameterizedTypeReference<String>() {};
        return apiClient.invokeAPI(path, HttpMethod.GET, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
        		
	}
	
	
}
