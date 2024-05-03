package no.systema.jservices.tvinn.digitoll.v2.services;

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

import no.systema.jservices.tvinn.digitoll.v2.dto.SadmohfDto;
import no.systema.jservices.tvinn.digitoll.v2.dto.SadmomfDto;
import no.systema.jservices.tvinn.digitoll.v2.dto.ZadmohfDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.GenericDtoContainer;
import no.systema.jservices.tvinn.expressfortolling2.dto.GenericDtoResponse;


@Service
public class ZadmohfService {
	private static final Logger logger = LoggerFactory.getLogger(ZadmohfService.class);
	
	
	@Autowired
	RestTemplate restTemplate;
	
	
	
	public List<ZadmohfDto> getZadmohf(String serverRoot, String user, String ehlnrt, String ehlnrm, String ehlnrh) {
		List<ZadmohfDto> result = new ArrayList<ZadmohfDto>();
		
		logger.warn("USER:" + user);
		
		URI uri = UriComponentsBuilder
				.fromUriString(serverRoot)
				.path("/syjservicestn/syjsZADMOHF.do")
				.queryParam("user", user)
				.queryParam("ehlnrt", ehlnrt)
				.queryParam("ehlnrm", ehlnrm)
				.queryParam("ehlnrh", ehlnrh)
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
				logger.error("select-ZADMOHF-ERROR REST-http-response:" + dtoContainer.getErrMsg());
				result = null;
			}else {
				logger.warn("select-ZADMOHF-REST-http-response:" + response.getStatusCodeValue());
				
				for(Object o: dtoContainer.getList()){
					ZadmohfDto pojo = mapper.convertValue(o, ZadmohfDto.class);
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
	
	public List<SadmohfDto> getSadmohfForUpdate(String serverRoot, String user, String ehlnrt, String ehlnrm, String ehlnrh, String mrn) {
		List<SadmohfDto> result = new ArrayList<SadmohfDto>();
		
		logger.warn("USER:" + user);
		logger.warn("MRN:" + mrn);
		
		
		URI uri = UriComponentsBuilder
				.fromUriString(serverRoot)
				.path("/syjservicestn/syjsSADMOHF.do")
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
				logger.error("select-SADMOHF-ERROR REST-http-response:" + dtoContainer.getErrMsg());
				result = null;
			}else {
				logger.warn("select-SADMOHF-REST-http-response:" + response.getStatusCodeValue());
				
				for(Object o: dtoContainer.getList()){
					SadmohfDto pojo = mapper.convertValue(o, SadmohfDto.class);
					//get houses' dto for documentNumbers later on (with avd in order to include external Houses outside SYSPED registered manually)
					//pojo.setHouseDtoList(sadmomfService.getDocumentNumberListFromHouses(serverRoot, user, pro));
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
	/*
	public List<SadexmfDto> getSadexmfForUpdate(String serverRoot, String user, String lrn) {
		List<SadexmfDto> result = new ArrayList<SadexmfDto>();
		
		logger.warn("USER:" + user);
		
		URI uri = UriComponentsBuilder
				.fromUriString(serverRoot)
				.path("/syjservicestn/syjsSADEXMF.do")
				.queryParam("user", user)
				.queryParam("emuuid", lrn)
				.build()
				.encode()
				.toUri();
		
		try {
			HttpHeaders headerParams = new HttpHeaders();
			headerParams.add("Accept", "*");
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
					//get houses' dto for documentNumbers later on (with avd in order to include external Houses outside SYSPED registered manually)
					//N/A -->pojo.setHouseDtoList(sadexhfService.getDocumentNumberListFromHouses(serverRoot, user, pro));
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
	*/
	
	
	/**
	 * 
	 * @param serverRoot
	 * @param user
	 * @param dtoResponse
	 * @param sendDate
	 * @param mode
	 * @return
	 */
	public List<SadmohfDto> updateLrnMrnSadmohf(String serverRoot, String user, GenericDtoResponse dtoResponse, String sendDate, String mode) {
		List<SadmohfDto> result = new ArrayList<SadmohfDto>();
		
		logger.warn("user:" + user);
		logger.warn("mode:" + mode);
		logger.warn("ehlnrt:" + dtoResponse.getEhlnrt());
		logger.warn("ehlnrm:" + dtoResponse.getEhlnrm());
		logger.warn("ehlnrh:" + dtoResponse.getEhlnrh());
		
		logger.warn("ehuuid:" + dtoResponse.getRequestId());
		logger.warn("ehmid:" + dtoResponse.getMrn());
		logger.warn("ehdts:" + sendDate);
		logger.warn("ehst:" + dtoResponse.getDb_st());
		logger.warn("ehst2:" + dtoResponse.getDb_st2());
		logger.warn("ehst3:" + dtoResponse.getDb_st3());
		
		logger.warn("Start --> update of Lrn and Mrn at SADMOHF_U...");
		//example
		//http://localhost:8080/syjservicestn/syjsSADMOHF_U.do?user=NN&ehlnrt=1&ehlnrm=2&ehlnrh=3&mode=UL&ehmid=XX&ehuuid=uuid
		URI uri = UriComponentsBuilder
				.fromUriString(serverRoot)
				.path("/syjservicestn/syjsSADMOHF_U.do")
				.queryParam("user", user)
				.queryParam("mode", mode)
				.queryParam("ehlnrt", dtoResponse.getEhlnrt())
				.queryParam("ehlnrm", dtoResponse.getEhlnrm())
				.queryParam("ehlnrh", dtoResponse.getEhlnrh())
				.queryParam("ehuuid", dtoResponse.getRequestId())
				.queryParam("ehmid", dtoResponse.getMrn())
				.queryParam("ehdts", Integer.parseInt(sendDate))
				.queryParam("ehst", dtoResponse.getDb_st())
				.queryParam("ehst2", dtoResponse.getDb_st2())
				.queryParam("ehst3", dtoResponse.getDb_st3())
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
				logger.error("select-SADMOHF-ERROR REST-http-response:" + dtoContainer.getErrMsg());
				result = null;
			}else {
				logger.warn("select-SADMOHF-REST-http-response:" + response.getStatusCodeValue());
				SadmohfDto pojo = new SadmohfDto();
				if(mode.startsWith("D")){
					//nothing since it is DELETE;
				}else {
					//set it in order to have a valid response
					pojo.setEhmid(dtoResponse.getMrn());
				}
				result.add(pojo);
				
			}
			logger.warn("End --> update of Lrn and Mrn at SADMOHF_U...");
			
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
	 * @return
	 */
	public List<SadmohfDto> setMrnBupSadmohf(String serverRoot, String user, GenericDtoResponse dtoResponse) {
		List<SadmohfDto> result = new ArrayList<SadmohfDto>();
		
		logger.info("user:" + user);
		logger.info("ehlnrt:" + dtoResponse.getEhlnrt());
		logger.info("ehlnrm:" + dtoResponse.getEhlnrm());
		logger.info("ehlnrh:" + dtoResponse.getEhlnrh());
		logger.info("ehmid:" + dtoResponse.getMrn());
		
		logger.warn("Start --> update of Mrn-Bup at SADMOHF_U_BUP...");
		//example
		//http://localhost:8080/syjservicestn/syjsSADEXMF_U.do?user=NN&emavd=1&empro=501941&mode=UL&emmid=XX&emuuid=uuid
		URI uri = UriComponentsBuilder
				.fromUriString(serverRoot)
				.path("/syjservicestn/syjsSADMOHF_U_BUP.do")
				.queryParam("user", user)
				.queryParam("ehlnrt", dtoResponse.getEhlnrt())
				.queryParam("ehlnrm", dtoResponse.getEhlnrm())
				.queryParam("ehlnrh", dtoResponse.getEhlnrh())
				.queryParam("ehmid", dtoResponse.getMrn())
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
				logger.error("SADMOHF-MRN-BUP-ERROR REST-http-response:" + dtoContainer.getErrMsg());
				result = null;
			}else {
				logger.warn("SADMOHF-MRN-BUP-REST-http-response:" + response.getStatusCodeValue());
				SadmohfDto pojo = new SadmohfDto();
				pojo.setEhmid(dtoResponse.getMrn());
				result.add(pojo);
				
			}
			logger.warn("End --> update of Mrn-Bup at SADMOHF_U_BUP...");
			
		}catch(Exception e) {
			logger.error(e.toString());
			result = null;
		}
		
		return result; 
	}
	
	public List<SadmohfDto> setRequestIdBupSadmohf(String serverRoot, String user, GenericDtoResponse dtoResponse) {
		List<SadmohfDto> result = new ArrayList<SadmohfDto>();
		
		logger.info("user:" + user);
		logger.info("ehlnrt:" + dtoResponse.getEhlnrt());
		logger.info("ehlnrm:" + dtoResponse.getEhlnrm());
		logger.info("ehlnrh:" + dtoResponse.getEhlnrh());
		logger.info("ehuuid:" + dtoResponse.getRequestId());
		
		logger.warn("Start --> update of Mrn-Bup at SADMOHF_U_BUP...");
		//example
		//http://localhost:8080/syjservicestn/syjsSADEXMF_U.do?user=NN&emavd=1&empro=501941&mode=UL&emmid=XX&emuuid=uuid
		URI uri = UriComponentsBuilder
				.fromUriString(serverRoot)
				.path("/syjservicestn/syjsSADMOHF_U_BUP.do")
				.queryParam("user", user)
				.queryParam("ehlnrt", dtoResponse.getEhlnrt())
				.queryParam("ehlnrm", dtoResponse.getEhlnrm())
				.queryParam("ehlnrh", dtoResponse.getEhlnrh())
				.queryParam("ehuuid", dtoResponse.getRequestId())
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
				logger.error("SADMOHF-MRN-BUP-ERROR REST-http-response:" + dtoContainer.getErrMsg());
				result = null;
			}else {
				logger.warn("SADMOHF-MRN-BUP-REST-http-response:" + response.getStatusCodeValue());
				SadmohfDto pojo = new SadmohfDto();
				pojo.setEhmid(dtoResponse.getMrn());
				result.add(pojo);
				
			}
			logger.warn("End --> update of Mrn-Bup at SADMOHF_U_BUP...");
			
		}catch(Exception e) {
			logger.error(e.toString());
			result = null;
		}
		
		return result; 
	}
	
	

}
