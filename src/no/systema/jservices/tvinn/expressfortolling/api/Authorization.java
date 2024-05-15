package no.systema.jservices.tvinn.expressfortolling.api;

import java.util.List;

import org.slf4j.*;
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
	private static Logger logger = LoggerFactory.getLogger(Authorization.class);

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
	
	@Value("${kurer.audience}")
	String difiTokenKurerAudienceUrl;
	
	@Value("${expft.basepath.movement.road.tolltoken}")
	String tollAccessTokenUrl;
	
	@Value("${digitoll.access.use.proxy}")
	String proxyIsUsed;
	
	@Value("${digitoll.access.proxy.host}")
	String proxyHost;
	
	@Value("${digitoll.access.proxy.port}")
	Integer proxyPort;
	
	
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
    	//reset for proxy if needed
    	logger.info(this.proxyIsUsed);
        if(Boolean.parseBoolean(proxyIsUsed)) {
        	apiClient.resetRestTemplateWithProxy(this.proxyHost, this.proxyPort);
        }
    	
    	
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
        
        apiClient.setBasePath(difiTokenAudienceUrl); 
        
        return apiClient.invokeAPI(path, HttpMethod.POST, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
    }	
    
    /**
     * 
     * @return
     * @throws RestClientException
     */
    public TokenResponseDto accessTokenRequestPostMovementRoad() throws RestClientException {
    	logger.info("accessTokenRequestPostMovementRoad()");
    	
    	//reset for proxy if needed
    	logger.info(this.proxyIsUsed);
        if(Boolean.parseBoolean(proxyIsUsed)) {
        	apiClient.resetRestTemplateWithProxy(this.proxyHost, this.proxyPort);
        }
        
        
    	Object postBody = null;  //Not in use
    	
    	TokenRequestDto tokenRequest = new TokenRequestDto();
    	tokenRequest.setAssertion(difiJwtCreator.createRequestMovementRoadJwt());
    	logger.info("AFTER --> difiJwtCreator.createRequestMovementRoadJwt()");
        String path = UriComponentsBuilder.fromPath("/token").build().toUriString();
        logger.info("PATH --> " + path);
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
        
        
        apiClient.setBasePath(difiTokenAudienceUrl); 
        
        return apiClient.invokeAPI(path, HttpMethod.POST, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
    }	
    /**
     * 
     * @return
     * @throws RestClientException
     */
    public TokenResponseDto accessTokenRequestPostMovementAir() throws RestClientException {
    	logger.info("accessTokenRequestPostMovementAir()");
    	
    	//reset for proxy if needed
    	logger.info(this.proxyIsUsed);
        if(Boolean.parseBoolean(proxyIsUsed)) {
        	apiClient.resetRestTemplateWithProxy(this.proxyHost, this.proxyPort);
        }
        
        
    	Object postBody = null;  //Not in use
    	
    	TokenRequestDto tokenRequest = new TokenRequestDto();
    	tokenRequest.setAssertion(difiJwtCreator.createRequestMovementAirJwt());
        
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
        
        
        apiClient.setBasePath(difiTokenAudienceUrl); 
        
        return apiClient.invokeAPI(path, HttpMethod.POST, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
    }
    
    /**
     * 
     * @return
     * @throws RestClientException
     */
    public TokenResponseDto accessTokenRequestPostMovementRail() throws RestClientException {
    	logger.info("accessTokenRequestPostMovementRail()");
    	
    	//reset for proxy if needed
    	logger.info(this.proxyIsUsed);
        if(Boolean.parseBoolean(proxyIsUsed)) {
        	apiClient.resetRestTemplateWithProxy(this.proxyHost, this.proxyPort);
        }
        
        
    	Object postBody = null;  //Not in use
    	
    	TokenRequestDto tokenRequest = new TokenRequestDto();
    	tokenRequest.setAssertion(difiJwtCreator.createRequestMovementRailJwt());
        
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
        
        
        apiClient.setBasePath(difiTokenAudienceUrl); 
        
        return apiClient.invokeAPI(path, HttpMethod.POST, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
    }
    
    /**
     * 
     * @return
     * @throws RestClientException
     */
    public TokenResponseDto accessTokenRequestPostMovementRouting() throws RestClientException {
    	logger.info("accessTokenRequestPostMovementRouting()");
    	
    	//reset for proxy if needed
    	logger.info(this.proxyIsUsed);
        if(Boolean.parseBoolean(proxyIsUsed)) {
        	apiClient.resetRestTemplateWithProxy(this.proxyHost, this.proxyPort);
        }
        
        
    	Object postBody = null;  //Not in use
    	
    	TokenRequestDto tokenRequest = new TokenRequestDto();
    	tokenRequest.setAssertion(difiJwtCreator.createRequestMovementRoutingJwt());
        
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
        
        
        apiClient.setBasePath(difiTokenAudienceUrl); 
        
        return apiClient.invokeAPI(path, HttpMethod.POST, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
    }	
    
    
    public TokenResponseDto accessTokenRequestPostMovementRoadEntry() throws RestClientException {
    	logger.info("accessTokenRequestPostMovementRoadEntry()");
    	
    	//reset for proxy if needed
    	logger.info(this.proxyIsUsed);
        if(Boolean.parseBoolean(proxyIsUsed)) {
        	apiClient.resetRestTemplateWithProxy(this.proxyHost, this.proxyPort);
        }
        
        
    	Object postBody = null;  //Not in use
    	
    	TokenRequestDto tokenRequest = new TokenRequestDto();
    	tokenRequest.setAssertion(difiJwtCreator.createRequestMovementRoadEntryJwt());
        
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
        
        
        apiClient.setBasePath(difiTokenAudienceUrl); 
        
        return apiClient.invokeAPI(path, HttpMethod.POST, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
    }	
    
    public TokenResponseDto accessTokenRequestPostMovementRailEntry() throws RestClientException {
    	logger.info("accessTokenRequestPostMovementRailEntry()");
    	
    	//reset for proxy if needed
    	logger.info(this.proxyIsUsed);
        if(Boolean.parseBoolean(proxyIsUsed)) {
        	apiClient.resetRestTemplateWithProxy(this.proxyHost, this.proxyPort);
        }
        
        
    	Object postBody = null;  //Not in use
    	
    	TokenRequestDto tokenRequest = new TokenRequestDto();
    	tokenRequest.setAssertion(difiJwtCreator.createRequestMovementRailEntryJwt());
        
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
        
        
        apiClient.setBasePath(difiTokenAudienceUrl); 
        
        return apiClient.invokeAPI(path, HttpMethod.POST, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
    }	
    
    public TokenResponseDto accessTokenRequestPostMovementAirEntry() throws RestClientException {
    	logger.info("accessTokenRequestPostMovementAirEntry()");
    	
    	//reset for proxy if needed
    	logger.info(this.proxyIsUsed);
        if(Boolean.parseBoolean(proxyIsUsed)) {
        	apiClient.resetRestTemplateWithProxy(this.proxyHost, this.proxyPort);
        }
        
        
    	Object postBody = null;  //Not in use

    	TokenRequestDto tokenRequest = new TokenRequestDto();
    	tokenRequest.setAssertion(difiJwtCreator.createRequestMovementAirEntryJwt());
        
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
        
        
        apiClient.setBasePath(difiTokenAudienceUrl); 
        
        return apiClient.invokeAPI(path, HttpMethod.POST, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
    }	
    
    /**
     * switch Maskinporten Token with Toll token. Must be switch
     * 
     * ref. to--> https://toll.github.io/api/maskinporten.html#test-tilgang
     * 
     * 
     * @param maskinPortenToken
     * @return
     * @throws RestClientException
     */
    public TokenResponseDto accessTokenRequestPostToll(TokenResponseDto maskinPortenToken) throws RestClientException {
    	logger.info("accessTokenRequestPostToll(TokenRequestDto tokenRequest)");
    	//reset for proxy if needed
    	logger.info(this.proxyIsUsed);
        if(Boolean.parseBoolean(proxyIsUsed)) {
        	apiClient.resetRestTemplateWithProxy(this.proxyHost, this.proxyPort);
        }
    	
    	Object postBody = null;  //Not in use
    	
    	
    	//https://api-test.toll.no/api/access/external/oauth/token
        String path = UriComponentsBuilder.fromPath("/token").build().toUriString();
        
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();
              
        formParams.putAll(apiClient.parameterToMultiObjectValueMap(CollectionFormat.MULTI, "grant_type", "token-exchange"));       
        formParams.putAll(apiClient.parameterToMultiObjectValueMap(CollectionFormat.MULTI, "subject_token_type", "access_token")); 
        formParams.putAll(apiClient.parameterToMultiObjectValueMap(CollectionFormat.MULTI, "subject_token", maskinPortenToken.getAccess_token()));
        
        final String[] accepts = { 
            "application/json", "text/json"
        };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        final String[] contentTypes = { 
        	"application/x-www-form-urlencoded"
        };
        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);

        ParameterizedTypeReference<TokenResponseDto> returnType = new ParameterizedTypeReference<TokenResponseDto>() {};
        
        apiClient.setBasePath(this.tollAccessTokenUrl);
        
        return apiClient.invokeAPI(path, HttpMethod.POST, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
    }	
    
    /**
     * Get the access token
     * 
     * Response Message has StatusCode Created if POST operation succeed
     * <p><b>201</b> - Created
     * @return TokenResponseDto the response, including the accesstoken
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TokenResponseDto accessTokenForKurerRequestPost(String uploadUrlImmutable) throws RestClientException {
    	logger.info("accessTokenForKurerRequestPost(TokenRequestDto tokenRequest)");
    	//reset for proxy if needed
    	logger.info(this.proxyIsUsed);
        if(Boolean.parseBoolean(proxyIsUsed)) {
        	apiClient.resetRestTemplateWithProxy(this.proxyHost, this.proxyPort);
        }
        
    	Object postBody = null;  //Not in use
    	
    	TokenRequestDto tokenRequest = new TokenRequestDto();
    	tokenRequest.setAssertion(difiJwtCreator.createRequestKurerJwt());
        
    	String path = UriComponentsBuilder.fromPath("/token").build().toUriString();
    	
        /* without token ?? (different statements of support-people at toll.no
    	String path = "";
        if(uploadUrlImmutable !=null && uploadUrlImmutable.toLowerCase().contains("test")){
        	path = UriComponentsBuilder.fromPath("/token").build().toUriString();
        }*/
        
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
        apiClient.setBasePath(difiTokenKurerAudienceUrl);
        
        return apiClient.invokeAPI(path, HttpMethod.POST, queryParams, postBody, headerParams, formParams, accept, contentType, returnType);
    }
    
    
    
    /**
     * Get the access token
     * 
     * Response Message has StatusCode Created if POST operation succeed
     * <p><b>201</b> - Created
     * @return TokenResponseDto the response, including the accesstoken
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TokenResponseDto accessTokenForDocsRequestPost() throws RestClientException {
    	logger.info("accessTokenRequestPost(TokenRequestDto tokenRequest)");
    	//reset for proxy if needed
    	logger.info(this.proxyIsUsed);
        if(Boolean.parseBoolean(proxyIsUsed)) {
        	apiClient.resetRestTemplateWithProxy(this.proxyHost, this.proxyPort);
        }
        
    	Object postBody = null;  //Not in use
    	
    	TokenRequestDto tokenRequest = new TokenRequestDto();
    	tokenRequest.setAssertion(difiJwtCreator.createRequestDocsJwt());
        
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
