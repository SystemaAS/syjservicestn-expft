package no.systema.jservices.tvinn.digitoll.v2.services;

import java.net.InetAddress;
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

import no.systema.jservices.tvinn.digitoll.v2.dto.SadmoifDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.GenericDtoContainer;


@Service
public class SadmoifService {
	private static final Logger logger = LoggerFactory.getLogger(SadmoifService.class);
	
	
	@Autowired
	RestTemplate restTemplate;
	
	
	
	public List<SadmoifDto> getSadmoif(String serverRoot, String user, String eilnrt, String eilnrm, String eilnrh) {
		List<SadmoifDto> result = new ArrayList<SadmoifDto>();
		
		logger.warn("USER:" + user);
		
		URI uri = UriComponentsBuilder
				.fromUriString(serverRoot)
				.path("/syjservicestn/syjsSADMOIF.do")
				.queryParam("user", user)
				.queryParam("eilnrt", eilnrt)
				.queryParam("eilnrm", eilnrm)
				.queryParam("eilnrh", eilnrh)
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
				logger.error("select-SADMOIF-ERROR REST-http-response:" + dtoContainer.getErrMsg());
				result = null;
			}else {
				logger.warn("select-SADMOIF-REST-http-response:" + response.getStatusCodeValue());
				
				for(Object o: dtoContainer.getList()){
					SadmoifDto pojo = mapper.convertValue(o, SadmoifDto.class);
					//get houses' dto for documentNumbers later on (with avd in order to include external Houses outside SYSPED registered manually)
					//pojo.setHouseDtoList(sadexhfService.getDocumentNumberListFromHouses(serverRoot, user, pro));
					//logger.warn(pojo.getHouseDtoList().toString());
					if(pojo!=null) {
						result.add(pojo);
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
