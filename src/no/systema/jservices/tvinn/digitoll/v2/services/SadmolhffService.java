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

import no.systema.jservices.tvinn.expressfortolling2.dto.GenericDtoContainer;
import no.systema.jservices.tvinn.digitoll.v2.dto.SadmolffDto;
import no.systema.jservices.tvinn.digitoll.v2.dto.SadmolhffDto;

@Service
public class SadmolhffService {
private static final Logger logger = LoggerFactory.getLogger(SadmolhffService.class);
	
	@Autowired
	RestTemplate restTemplate;
	
	public List<SadmolhffDto> insertLogRecord(String serverRoot, String user, SadmolhffDto dto, String mode) {
		List<SadmolhffDto> result = new ArrayList<SadmolhffDto>();
		
		logger.warn("USER:" + user);
		logger.warn("ehdkh:" + dto.getEhdkh());
		logger.warn("ehlnrt:" + dto.getEhlnrt());
		logger.warn("uuid:" + dto.getUuid());
		logger.warn("status:" + dto.getStatus());
		
		//example
		//http://localhost:8080/syjservicestn/syjsSADEXLOG_U.do?user=NN&emavd=1&empro=501941&mode=A&etc
		URI uri = UriComponentsBuilder
				.fromUriString(serverRoot)
				.path("/syjservicestn/syjsSADMOLHFF_U.do")
				.queryParam("user", user)
				.queryParam("mode", mode)
				.queryParam("ehdkh", dto.getEhdkh())
				.queryParam("ehlnrt", dto.getEhlnrt())
				.queryParam("uuid", dto.getUuid())
				.queryParam("status", dto.getStatus())
				.queryParam("avsid", dto.getAvsid())
				.queryParam("motid", dto.getMotid())
				
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
				logger.error("select-SADMOLHFF-ERROR REST-http-response:" + dtoContainer.getErrMsg());
				result = null;
			}else {
				logger.warn("select-SADMOLHFF-REST-http-response:" + response.getStatusCodeValue());
				result.add(dto);
				
			}

		}catch(Exception e) {
			logger.error(e.toString());
			result = null;
		}
		logger.debug(result.toString());
		return result; 
	}
	
}
