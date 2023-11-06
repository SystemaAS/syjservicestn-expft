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

import no.systema.jservices.tvinn.digitoll.v2.dto.SadmotfDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.GenericDtoContainer;
import no.systema.jservices.tvinn.expressfortolling2.dto.GenericDtoResponse;


@Service
public class SadmotfService {
	private static final Logger logger = LoggerFactory.getLogger(SadmotfService.class);
	
	
	@Autowired
	RestTemplate restTemplate;
	
	//@Autowired
	//SadexhfService sadexhfService;
	
	
	public List<SadmotfDto> getSadmotf(String serverRoot, String user, String etlnrt) {
		List<SadmotfDto> result = new ArrayList<SadmotfDto>();
		
		logger.warn("USER:" + user);
		
		URI uri = UriComponentsBuilder
				.fromUriString(serverRoot)
				.path("/syjservicestn/syjsSADMOTF.do")
				.queryParam("user", user)
				.queryParam("etlnrt", etlnrt)
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
				logger.error("select-SADMOTF-ERROR REST-http-response:" + dtoContainer.getErrMsg());
				result = null;
			}else {
				logger.warn("select-SADMOTF-REST-http-response:" + response.getStatusCodeValue());
				
				for(Object o: dtoContainer.getList()){
					SadmotfDto pojo = mapper.convertValue(o, SadmotfDto.class);
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
	public SadmotfDto getSadmotfDto(String serverRoot, String user, String etlnrt) {
		SadmotfDto result = new SadmotfDto();
		
		logger.warn("USER:" + user);
		
		URI uri = UriComponentsBuilder
				.fromUriString(serverRoot)
				.path("/syjservicestn/syjsSADMOTF.do")
				.queryParam("user", user)
				.queryParam("etlnrt", etlnrt)
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
				logger.error("select-SADMOTF-ERROR REST-http-response:" + dtoContainer.getErrMsg());
				result = null;
			}else {
				logger.warn("select-SADMOTF-REST-http-response:" + response.getStatusCodeValue());
				
				for(Object o: dtoContainer.getList()){
					SadmotfDto pojo = mapper.convertValue(o, SadmotfDto.class);
					//get houses' dto for documentNumbers later on (with avd in order to include external Houses outside SYSPED registered manually)
					//pojo.setHouseDtoList(sadexhfService.getDocumentNumberListFromHouses(serverRoot, user, pro));
					//logger.warn(pojo.getHouseDtoList().toString());
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
	
	
	public List<SadmotfDto> getSadmotfForUpdate(String serverRoot, String user, String etlnrt, String mrn) {
		List<SadmotfDto> result = new ArrayList<SadmotfDto>();
		
		logger.warn("USER:" + user);
		
		URI uri = UriComponentsBuilder
				.fromUriString(serverRoot)
				.path("/syjservicestn/syjsSADMOTF.do")
				.queryParam("user", user)
				.queryParam("etmid", mrn)
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
				logger.error("select-SADMOTF-ERROR REST-http-response:" + dtoContainer.getErrMsg());
				result = null;
			}else {
				logger.warn("select-SADMOTF-REST-http-response:" + response.getStatusCodeValue());
				
				for(Object o: dtoContainer.getList()){
					SadmotfDto pojo = mapper.convertValue(o, SadmotfDto.class);
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
	
	public List<SadmotfDto> getSadmotfForUpdate(String serverRoot, String user, String lrn) {
		List<SadmotfDto> result = new ArrayList<SadmotfDto>();
		
		logger.warn("USER:" + user);
		
		URI uri = UriComponentsBuilder
				.fromUriString(serverRoot)
				.path("/syjservicestn/syjsSADMOTF.do")
				.queryParam("user", user)
				.queryParam("etuuid", lrn)
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
					SadmotfDto pojo = mapper.convertValue(o, SadmotfDto.class);
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
	
	/**
	 * 
	 * @param serverRoot
	 * @param user
	 * @param dtoResponse
	 * @param sendDate
	 * @param mode
	 * @return
	 */
	public List<SadmotfDto> updateLrnMrnSadmotf(String serverRoot, String user, GenericDtoResponse dtoResponse, String sendDate, String mode) {
		List<SadmotfDto> result = new ArrayList<SadmotfDto>();
		
		logger.info("user:" + user);
		logger.info("mode:" + mode);
		logger.info("etlnrt:" + dtoResponse.getEtlnrt());
		//logger.info("etavd:" + dtoResponse.getAvd());
		//logger.info("etpro:" + dtoResponse.getPro());
		logger.info("etuuid:" + dtoResponse.getRequestId());
		logger.info("etmid:" + dtoResponse.getMrn());
		logger.info("etdtin:" + sendDate);
		logger.info("etst:" + dtoResponse.getDb_st());
		logger.info("etst2:" + dtoResponse.getDb_st2());
		logger.info("etst3:" + dtoResponse.getDb_st3());
		
		logger.warn("Start --> update of Lrn and Mrn at SADMOTF_U...");
		//example
		//http://localhost:8080/syjservicestn/syjsSADEXMF_U.do?user=NN&emavd=1&empro=501941&mode=UL&emmid=XX&emuuid=uuid
		URI uri = UriComponentsBuilder
				.fromUriString(serverRoot)
				.path("/syjservicestn/syjsSADMOTF_U.do")
				.queryParam("user", user)
				.queryParam("mode", mode)
				.queryParam("etlnrt", dtoResponse.getEtlnrt())
				.queryParam("etuuid", dtoResponse.getRequestId())
				.queryParam("etmid", dtoResponse.getMrn())
				.queryParam("etdtin", Integer.parseInt(sendDate))
				.queryParam("etst", dtoResponse.getDb_st())
				.queryParam("etst2", dtoResponse.getDb_st2())
				.queryParam("etst3", dtoResponse.getDb_st3())
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
				logger.error("select-SADMOTF-ERROR REST-http-response:" + dtoContainer.getErrMsg());
				result = null;
			}else {
				logger.warn("select-SADMOTF-REST-http-response:" + response.getStatusCodeValue());
				SadmotfDto pojo = new SadmotfDto();
				if(mode.startsWith("D")){
					//nothing since it is DELETE;
				}else {
					//set it in order to have a valid response
					pojo.setEtmid(dtoResponse.getMrn());
				}
				result.add(pojo);
				
			}
			logger.warn("End --> update of Lrn and Mrn at SADMOTF_U...");
			
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
	public List<SadmotfDto> setMrnBupSadmotf(String serverRoot, String user, GenericDtoResponse dtoResponse) {
		List<SadmotfDto> result = new ArrayList<SadmotfDto>();
		
		logger.info("user:" + user);
		logger.info("etlnrt:" + dtoResponse.getEtlnrt());
		logger.info("etmid:" + dtoResponse.getMrn());
		
		logger.warn("Start --> update of Mrn-Bup at SADMOTF_U_BUP...");
		//example
		//http://localhost:8080/syjservicestn/syjsSADEXMF_U.do?user=NN&emavd=1&empro=501941&mode=UL&emmid=XX&emuuid=uuid
		URI uri = UriComponentsBuilder
				.fromUriString(serverRoot)
				.path("/syjservicestn/syjsSADMOTF_U_BUP.do")
				.queryParam("user", user)
				.queryParam("etlnrt", dtoResponse.getEtlnrt())
				.queryParam("etmid", dtoResponse.getMrn())
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
				logger.error("SADMOTF-MRN-BUP-ERROR REST-http-response:" + dtoContainer.getErrMsg());
				result = null;
			}else {
				logger.warn("SADMOTF-MRN-BUP-REST-http-response:" + response.getStatusCodeValue());
				SadmotfDto pojo = new SadmotfDto();
				pojo.setEtmid(dtoResponse.getMrn());
				result.add(pojo);
				
			}
			logger.warn("End --> update of Mrn-Bup at SADMOTF_U_BUP...");
			
		}catch(Exception e) {
			logger.error(e.toString());
			result = null;
		}
		
		return result; 
	}
	
	
	public List<SadmotfDto> setRequestIdBupSadmotf(String serverRoot, String user, GenericDtoResponse dtoResponse) {
		List<SadmotfDto> result = new ArrayList<SadmotfDto>();
		
		logger.info("user:" + user);
		logger.info("etlnrt:" + dtoResponse.getEtlnrt());
		logger.info("etuuid:" + dtoResponse.getRequestId());
		
		logger.warn("Start --> update of Request-Bup at SADMOTF_U_BUP...");
		//example
		//http://localhost:8080/syjservicestn/syjsSADEXMF_U.do?user=NN&emavd=1&empro=501941&mode=UL&emmid=XX&emuuid=uuid
		URI uri = UriComponentsBuilder
				.fromUriString(serverRoot)
				.path("/syjservicestn/syjsSADMOTF_U_BUP.do")
				.queryParam("user", user)
				.queryParam("etlnrt", dtoResponse.getEtlnrt())
				.queryParam("etuuid", dtoResponse.getRequestId())
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
				logger.error("SADMOTF-MRN-BUP-ERROR REST-http-response:" + dtoContainer.getErrMsg());
				result = null;
			}else {
				logger.warn("SADMOTF-MRN-BUP-REST-http-response:" + response.getStatusCodeValue());
				SadmotfDto pojo = new SadmotfDto();
				pojo.setEtmid(dtoResponse.getMrn());
				result.add(pojo);
				
			}
			logger.warn("End --> update of Mrn-Bup at SADMOTF_U_BUP...");
			
		}catch(Exception e) {
			logger.error(e.toString());
			result = null;
		}
		
		return result; 
	}
	
	
	
	

}
