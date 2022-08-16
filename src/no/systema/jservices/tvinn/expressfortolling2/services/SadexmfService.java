package no.systema.jservices.tvinn.expressfortolling2.services;

import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.systema.jservices.tvinn.expressfortolling2.dto.GenericDtoContainer;
import no.systema.jservices.tvinn.expressfortolling2.dto.SadexmfDto;


@Service
public class SadexmfService {
	private static final Logger logger = LoggerFactory.getLogger(SadexmfService.class);
	
	
	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	SadexhfService sadexhfService;
	
	@Value("${http.syjservices.root.servlet}")
	String serverRoot; 
	
	public List<SadexmfDto> getSadexmf(String user, String avd, String pro) {
		List<SadexmfDto> result = new ArrayList<SadexmfDto>();
		
		logger.warn("USER:" + user);
		
		URI uri = UriComponentsBuilder
				.fromUriString(serverRoot)
				.path("/syjservicestn/syjsSADEXMF.do")
				.queryParam("user", user)
				.queryParam("emavd", avd)
				.queryParam("empro", pro) 
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
				logger.error("select-SADEXMF-ERROR REST-http-response:" + dtoContainer.getErrMsg());
				result = null;
			}else {
				logger.warn("select-SADEXMF-REST-http-response:" + response.getStatusCodeValue());
				
				for(Object o: dtoContainer.getList()){
					SadexmfDto pojo = mapper.convertValue(o, SadexmfDto.class);
					//get houses' dto for documentNumbers later on
					pojo.setHouseDtoList(sadexhfService.getDocumentNumberListFromHouses(user, avd, pro));
					logger.info(pojo.getHouseDtoList().toString());
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
	
	public List<SadexmfDto> getSadexmfForUpdate(String user, String mrn) {
		List<SadexmfDto> result = new ArrayList<SadexmfDto>();
		
		logger.warn("USER:" + user);
		
		URI uri = UriComponentsBuilder
				.fromUriString(serverRoot)
				.path("/syjservicestn/syjsSADEXMF.do")
				.queryParam("user", user)
				.queryParam("emmid", mrn)
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
				logger.error("select-SADEXMF-ERROR REST-http-response:" + dtoContainer.getErrMsg());
				result = null;
			}else {
				logger.warn("select-SADEXMF-REST-http-response:" + response.getStatusCodeValue());
				
				for(Object o: dtoContainer.getList()){
					SadexmfDto pojo = mapper.convertValue(o, SadexmfDto.class);
					//get item lines
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
	
	
	public List<SadexmfDto> updateLrnMrnSadexmf(String user, Integer avd, Integer pro, String lrn, String mrn) {
		List<SadexmfDto> result = new ArrayList<SadexmfDto>();
		
		logger.warn("USER:" + user);
		//example
		//http://localhost:8080/syjservicestn/syjsSADEXMF_U.do?user=NN&emavd=1&empro=501941&mode=UL&emmid=XX&emuuid=uuid
		URI uri = UriComponentsBuilder
				.fromUriString(serverRoot)
				.path("/syjservicestn/syjsSADEXMF_U.do")
				.queryParam("user", user)
				.queryParam("mode", "UL")
				.queryParam("emavd", avd)
				.queryParam("empro", pro)
				.queryParam("emuuid", lrn)
				.queryParam("emmid", mrn)
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
				logger.error("select-SADEXMF-ERROR REST-http-response:" + dtoContainer.getErrMsg());
				result = null;
			}else {
				logger.warn("select-SADEXMF-REST-http-response:" + response.getStatusCodeValue());
				
				for(Object o: dtoContainer.getList()){
					SadexmfDto pojo = mapper.convertValue(o, SadexmfDto.class);
					//get item lines
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
