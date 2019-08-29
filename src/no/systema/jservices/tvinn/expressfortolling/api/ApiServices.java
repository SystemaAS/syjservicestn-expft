package no.systema.jservices.tvinn.expressfortolling.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import no.systema.jservices.common.dto.TransportationCompanyDto;
import no.systema.jservices.tvinn.expressfortolling.jwt.DifiJwtCreator;

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
	DifiJwtCreator difiJwtCreator;

	@Autowired
	Authorization authorization;

	@Value("${expft.basepath.version}")
    private String basePathVersion;	
	 
	/**
	 * Get all transportation companies for which the logged in user is a customs representative.
	 * GET /api/exf/ekspressfortolling/transportation-company
	 * Accept: application/json
	 * Host: ekspressfortolling.toll.no
	 * Version: 1
	 * 
	 * @return List<TransportationCompanyDto>
	 */
	public List<TransportationCompanyDto> getTransportationCompany() {
  
		TokenResponseDto responseDto = authorization.accessTokenRequestPost();
		//For test, couldn't get Mockito to work correct.
//		TokenResponseDto responseDto = new TokenResponseDto();
//		responseDto.setAccess_token("XYZ");
		
		Object postBody = null; //Not in use
        
        String path = UriComponentsBuilder.fromPath(basePathVersion + "/transportation-company").build().toUriString();
        
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

        ParameterizedTypeReference<List<TransportationCompanyDto>> returnType = new ParameterizedTypeReference<List<TransportationCompanyDto>>() {};
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
	
	
}
