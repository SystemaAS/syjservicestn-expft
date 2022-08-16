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
import no.systema.jservices.tvinn.expressfortolling2.dto.SadexhfDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.SadexifDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.SadexmfDto;


@Service
public class SadexhfService {
	private static final Logger logger = LoggerFactory.getLogger(SadexhfService.class);
	
	
	@Autowired
	RestTemplate restTemplate;
	
	@Value("${http.syjservices.root.servlet}")
	String serverRoot; 
	
	
	public List<SadexhfDto> getSadexhf(String user, String avd, String pro, String tdn) {
		List<SadexhfDto> result = new ArrayList<SadexhfDto>();
		
		logger.warn("USER:" + user);
		
		URI uri = UriComponentsBuilder
				.fromUriString(serverRoot)
				.path("/syjservicestn/syjsSADEXHF.do")
				.queryParam("user", user)
				.queryParam("ehavd", avd)
				.queryParam("ehpro", pro) 
				.queryParam("ehtdn", tdn) 
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
				logger.error("select-SADEXHF-ERROR REST-http-response:" + dtoContainer.getErrMsg());
				result = null;
			}else {
				logger.warn("select-SADEXHF-REST-http-response:" + response.getStatusCodeValue());
				
				for(Object o: dtoContainer.getList()){
					SadexhfDto pojo = mapper.convertValue(o, SadexhfDto.class);
					//get item lines
					pojo.setGoodsItemList(this.getItemLines(user, avd, pro, tdn));
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
	
	public List<SadexhfDto> getDocumentNumberListFromHouses(String user, String avd, String pro) {
		List<SadexhfDto> result = new ArrayList<SadexhfDto>();
		
		logger.warn("USER:" + user);
		
		URI uri = UriComponentsBuilder
				.fromUriString(serverRoot)
				.path("/syjservicestn/syjsSADEXHF.do")
				.queryParam("user", user)
				.queryParam("ehavd", avd)
				.queryParam("ehpro", pro) 
				.build()
				.encode()
				.toUri();
		
		try {
			HttpHeaders headerParams = new HttpHeaders();
			headerParams.add("Accept", "*/*");
			HttpEntity<?> entity = new HttpEntity<>(headerParams);
		
			ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
			String json = response.getBody();
			logger.debug(json);
			///Json Mapper (RestTemplate only working with String.class)
			ObjectMapper mapper = new ObjectMapper();
			GenericDtoContainer dtoContainer = mapper.readValue(json, GenericDtoContainer.class);
			
			//at this point the dtoContainer has an error or not
			if( dtoContainer!=null && StringUtils.isNotEmpty(dtoContainer.getErrMsg()) ) {
				logger.error("select-SADEXHF-ERROR REST-http-response:" + dtoContainer.getErrMsg());
				result = null;
			}else {
				logger.warn("select-SADEXHF-REST-http-response:" + response.getStatusCodeValue());
				
				for(Object o: dtoContainer.getList()){
					SadexhfDto pojo = mapper.convertValue(o, SadexhfDto.class);
					
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

	private List<SadexifDto> getItemLines(String user, String avd, String pro, String tdn) {
		List<SadexifDto> result = new ArrayList<SadexifDto>();
		
		logger.warn("USER:" + user);
		
		URI uri = UriComponentsBuilder
				.fromUriString(serverRoot)
				.path("/syjservicestn/syjsSADEXIF.do")
				.queryParam("user", user)
				.queryParam("eiavd", avd)
				.queryParam("eipro", pro) 
				.queryParam("eitdn", tdn) 
				.build()
				.encode()
				.toUri();
		
		try {
			HttpHeaders headerParams = new HttpHeaders();
			headerParams.add("Accept", "*/*");
			HttpEntity<?> entity = new HttpEntity<>(headerParams);
		
			ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
			String json = response.getBody();
			logger.info(json);
			///Json Mapper (RestTemplate only working with String.class)
			ObjectMapper mapper = new ObjectMapper();
			GenericDtoContainer dtoContainer = mapper.readValue(json, GenericDtoContainer.class);
			
			//at this point the dtoContainer has an error or not
			if( dtoContainer!=null && StringUtils.isNotEmpty(dtoContainer.getErrMsg()) ) {
				logger.error("select-SADEXIF-ERROR REST-http-response:" + dtoContainer.getErrMsg());
				result = null;
			}else {
				logger.warn("select-SADEXIF-REST-http-response:" + response.getStatusCodeValue());
				
				for(Object o: dtoContainer.getList()){
					SadexifDto pojo = mapper.convertValue(o, SadexifDto.class);
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
	
	
	public List<SadexhfDto> updateLrnMrnSadexhf(String user, Integer avd, Integer pro, String lrn, String mrn) {
		List<SadexhfDto> result = new ArrayList<SadexhfDto>();
		
		logger.warn("USER:" + user);
		//example
		//http://localhost:8080/syjservicestn/syjsSADEXMF_U.do?user=NN&emavd=1&empro=501941&mode=UL&emmid=XX&emuuid=uuid
		URI uri = UriComponentsBuilder
				.fromUriString(serverRoot)
				.path("/syjservicestn/syjsSADEXHF_U.do")
				.queryParam("user", user)
				.queryParam("mode", "UL")
				.queryParam("ehavd", avd)
				.queryParam("ehpro", pro)
				.queryParam("ehuuid", lrn)
				.queryParam("ehmid", mrn)
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
				logger.error("select-SADEXHF-ERROR REST-http-response:" + dtoContainer.getErrMsg());
				result = null;
			}else {
				logger.warn("select-SADEXHF-REST-http-response:" + response.getStatusCodeValue());
				
				for(Object o: dtoContainer.getList()){
					SadexhfDto pojo = mapper.convertValue(o, SadexhfDto.class);
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
