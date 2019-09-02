package no.systema.jservices.tvinn.expressfortolling.api;

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
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import no.systema.jservices.tvinn.expressfortolling.api.ApiClient.CollectionFormat;
import no.systema.jservices.tvinn.expressfortolling.jwt.DifiJwtCreator;

@Service
public class Authorization {
	private static Logger logger = Logger.getLogger(Authorization.class);

	@Autowired
    private ApiClient apiClient;

	@Autowired
	private DifiJwtCreator difiJwtCreator;	
	
	/**
	 * Audience - identifikator for ID-portens OIDC Provider. 
	 * Se ID-portens well-known-endepunkt for aktuelt miljø for å finne riktig verdi.
	 */
	@Value("${expft.audience}")
	String difiTokenAudienceUrl;
	
	 /**
     * Get the access token
     * 
     * Response Message has StatusCode Created if POST operation succeed
     * <p><b>201</b> - Created
     * @return TokenResponseDto the response, including the accesstoken
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TokenResponseDto accessTokenRequestPost() throws RestClientException {
    	logger.info("accessTokenRequestPost(TokenRequestDto tokenRequest)");
    	Object postBody = null;  //Not in use
    	
    	TokenRequestDto tokenRequest = new TokenRequestDto();
    	tokenRequest.setAssertion(difiJwtCreator.createRequestJwt());
        
        String path = UriComponentsBuilder.fromPath("/token").build().toUriString();
        
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();
              
        formParams.putAll(apiClient.parameterToMultiObjectValueMap(CollectionFormat.MULTI, "grant_type", tokenRequest.getGrantType()));       
        formParams.putAll(apiClient.parameterToMultiObjectValueMap(CollectionFormat.MULTI, "assertion", tokenRequest.getAssertion()));       
        
        final String[] accepts = { 
            "application/json", "text/json"
        };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        final String[] contentTypes = { 
        	"application/x-www-form-urlencoded"
        };
        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);

        ParameterizedTypeReference<TokenResponseDto> returnType = new ParameterizedTypeReference<TokenResponseDto>() {};
        
        //TODO refactor outside
        apiClient.setBasePath(difiTokenAudienceUrl);
        
        return apiClient.invokeAPI(path, HttpMethod.POST, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
    }	
	
}
