package no.systema.jservices.tvinn.digitoll.v2.services;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.systema.jservices.tvinn.digitoll.v2.dto.ZadmomlfDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.GenericDtoContainer;

@Service
public class ZadmomlfService  {

	private static final Logger logger = LoggerFactory.getLogger(ZadmomlfService.class);
	
	@Autowired
	RestTemplate restTemplate;
	
	public ZadmomlfDto getZadmomlf(String serverRoot, String user, String emdkm) {
		ZadmomlfDto result = new ZadmomlfDto();
		
		logger.warn("USER:" + user);
	
		URI uri = UriComponentsBuilder
			.fromUriString(serverRoot)
			.path("/syjservicestn/syjsZADMOMLF.do")
			.queryParam("user", user)
			.queryParam("emdkm", emdkm)
			.build()
			.encode()
			.toUri();
	
		try {
			HttpHeaders headerParams = new HttpHeaders();
			headerParams.add("Accept", "*/*");
			HttpEntity<?> entity = new HttpEntity<>(headerParams);
		
			ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
			String json = response.getBody();
			logger.warn(json);
			///Json Mapper (RestTemplate only working with String.class)
			ObjectMapper mapper = new ObjectMapper();
			GenericDtoContainer dtoContainer = mapper.readValue(json, GenericDtoContainer.class);
			
			//at this point the dtoContainer has an error or not
			if( dtoContainer!=null && StringUtils.isNotEmpty(dtoContainer.getErrMsg()) ) {
				logger.error("select-ZADMOMLF-ERROR REST-http-response:" + dtoContainer.getErrMsg());
				result = null;
			}else {
				logger.warn("select-ZADMOMLF-REST-http-response:" + response.getStatusCodeValue());
				
				for(Object o: dtoContainer.getList()){
					ZadmomlfDto pojo = mapper.convertValue(o, ZadmomlfDto.class);

					if(pojo!=null) {
						result = pojo;
					}
				}
				
			}
	
		}catch(Exception e) {
			logger.error(e.toString());
			result = null;
		}
		
		return result; 
	}
}
