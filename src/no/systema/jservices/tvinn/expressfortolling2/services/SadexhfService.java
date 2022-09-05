package no.systema.jservices.tvinn.expressfortolling2.services;

import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

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
import no.systema.jservices.tvinn.expressfortolling2.dto.GenericDtoResponse;
import no.systema.jservices.tvinn.expressfortolling2.dto.SadexhfDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.SadexifDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.SadexmfDto;


@Service
public class SadexhfService {
	private static final Logger logger = LoggerFactory.getLogger(SadexhfService.class);
	
	
	@Autowired
	RestTemplate restTemplate;
	

	
	public List<SadexhfDto> getSadexhf(String serverRoot, String user, String avd, String pro, String tdn) {
		List<SadexhfDto> result = new ArrayList<SadexhfDto>();
		
		
		logger.warn("USER:" + user);
		logger.warn("AVD:" + avd);
		logger.warn("PRO:" + pro);
		logger.warn("TDN:" + tdn);
		
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
					pojo.setGoodsItemList(this.getItemLines(serverRoot, user, avd, pro, tdn));
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
	
	
	public List<SadexhfDto> getSadexhfForUpdate(String serverRoot, String user, String mrn, String avd, String pro, String tdn) {
		List<SadexhfDto> result = new ArrayList<SadexhfDto>();
		
		logger.warn("USER:" + user);
		
		URI uri = UriComponentsBuilder
				.fromUriString(serverRoot)
				.path("/syjservicestn/syjsSADEXHF.do")
				.queryParam("user", user)
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
				logger.error("select-SADEXMF-ERROR REST-http-response:" + dtoContainer.getErrMsg());
				result = null;
			}else {
				logger.warn("select-SADEXMF-REST-http-response:" + response.getStatusCodeValue());
				
				for(Object o: dtoContainer.getList()){
					SadexhfDto pojo = mapper.convertValue(o, SadexhfDto.class);
					//get item lines
					pojo.setGoodsItemList(this.getItemLines(serverRoot, user, avd, pro, tdn));
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
	
	
	
	/**
	 * The method is called from a Master service. It is use to fill the master documentNumber-house-list from this list
	 * @param user
	 * @param avd
	 * @param pro
	 * @return
	 */
	public List<SadexhfDto> getDocumentNumberListFromHouses(String serverRoot, String user, String avd, String pro) {
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

	private List<SadexifDto> getItemLines(String serverRoot, String user, String avd, String pro, String tdn) {
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
	
	/**
	 * 
	 * @param serverRoot
	 * @param user
	 * @param dtoResponse
	 * @param sendDate
	 * @param mode
	 * @return
	 */
	public List<SadexhfDto> updateLrnMrnSadexhf(String serverRoot, String user, GenericDtoResponse dtoResponse, String sendDate, String mode) {
		List<SadexhfDto> result = new ArrayList<SadexhfDto>();
		
		logger.warn("USER:" + user);
		logger.warn("AVD:" + dtoResponse.getAvd());
		logger.warn("PRO:" + dtoResponse.getPro());
		logger.warn("TDN:" + dtoResponse.getTdn());
		
		
		//example
		//http://localhost:8080/syjservicestn/syjsSADEXMF_U.do?user=NN&emavd=1&empro=501941&mode=UL&emmid=XX&emuuid=uuid
		URI uri = UriComponentsBuilder
				.fromUriString(serverRoot)
				.path("/syjservicestn/syjsSADEXHF_U.do")
				.queryParam("user", user)
				.queryParam("mode", mode)
				.queryParam("ehavd", Integer.valueOf(dtoResponse.getAvd()))
				.queryParam("ehpro", Integer.valueOf(dtoResponse.getPro()))
				.queryParam("ehtdn", Integer.valueOf(dtoResponse.getTdn()))
				.queryParam("ehuuid", dtoResponse.getLrn())
				.queryParam("ehmid", dtoResponse.getMrn())
				.queryParam("ehst", dtoResponse.getDb_st())
				.queryParam("ehst2", dtoResponse.getDb_st2())
				.queryParam("ehst3", dtoResponse.getDb_st3())
				
				//sendDate ?? field
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
				
				SadexhfDto pojo = new SadexhfDto();
				if(mode.startsWith("D")){
					//nothing since it is DELETE;
				}else {
					//set it in order to have a valid response
					pojo.setEhmid(dtoResponse.getMrn());
				}
				result.add(pojo);
				
			}

		}catch(Exception e) {
			logger.error(e.toString());
			result = null;
		}
		logger.warn(result.toString());
		return result; 
	}

}
