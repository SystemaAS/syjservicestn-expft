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

import no.systema.jservices.tvinn.digitoll.v2.dto.SadmomfDto;
import no.systema.jservices.tvinn.digitoll.v2.dto.SadmotfDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.GenericDtoContainer;
import no.systema.jservices.tvinn.expressfortolling2.dto.GenericDtoResponse;


@Service
public class SadmomfService {
	private static final Logger logger = LoggerFactory.getLogger(SadmomfService.class);
	
	
	@Autowired
	RestTemplate restTemplate;
	
	
	public List<SadmomfDto> getSadmomf(String serverRoot, String user, String emlnrt) {
		List<SadmomfDto> result = new ArrayList<SadmomfDto>();
		
		logger.warn("USER:" + user);
		
		URI uri = UriComponentsBuilder
				.fromUriString(serverRoot)
				.path("/syjservicestn/syjsSADMOMF.do")
				.queryParam("user", user)
				.queryParam("emlnrt", emlnrt)
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
				logger.error("select-SADMOMF-ERROR REST-http-response:" + dtoContainer.getErrMsg());
				result = null;
			}else {
				logger.warn("select-SADMOMF-REST-http-response:" + response.getStatusCodeValue());
				
				for(Object o: dtoContainer.getList()){
					SadmomfDto pojo = mapper.convertValue(o, SadmomfDto.class);
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
	
	public List<SadmomfDto> getSadmomf(String serverRoot, String user, String emlnrt, String emlnrm) {
		List<SadmomfDto> result = new ArrayList<SadmomfDto>();
		
		logger.warn("USER:" + user);
		
		URI uri = UriComponentsBuilder
				.fromUriString(serverRoot)
				.path("/syjservicestn/syjsSADMOMF.do")
				.queryParam("user", user)
				.queryParam("emlnrt", emlnrt)
				.queryParam("emlnrm", emlnrm) 
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
				logger.error("select-SADMOMF-ERROR REST-http-response:" + dtoContainer.getErrMsg());
				result = null;
			}else {
				logger.warn("select-SADMOMF-REST-http-response:" + response.getStatusCodeValue());
				
				for(Object o: dtoContainer.getList()){
					SadmomfDto pojo = mapper.convertValue(o, SadmomfDto.class);
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
	
	public SadmomfDto getSadmomfDto(String serverRoot, String user, String emlnrt, String emlnrm) {
		SadmomfDto result = new SadmomfDto();
		
		logger.warn("USER:" + user);
		
		URI uri = UriComponentsBuilder
				.fromUriString(serverRoot)
				.path("/syjservicestn/syjsSADMOMF.do")
				.queryParam("user", user)
				.queryParam("emlnrt", emlnrt)
				.queryParam("emlnrm", emlnrm) 
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
				logger.error("select-SADMOMF-ERROR REST-http-response:" + dtoContainer.getErrMsg());
				result = null;
			}else {
				logger.warn("select-SADMOMF-REST-http-response:" + response.getStatusCodeValue());
				
				for(Object o: dtoContainer.getList()){
					SadmomfDto pojo = mapper.convertValue(o, SadmomfDto.class);
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
	/**
	 * 
	 * @param serverRoot
	 * @param user
	 * @param emlnrt
	 * @param emlnrm
	 * @param mrn
	 * @return
	 */
	public List<SadmomfDto> getSadmomfForUpdate(String serverRoot, String user, String emlnrt, String emlnrm, String mrn) {
		List<SadmomfDto> result = new ArrayList<SadmomfDto>();
		
		logger.warn("USER:" + user);
		
		URI uri = UriComponentsBuilder
				.fromUriString(serverRoot)
				.path("/syjservicestn/syjsSADMOMF.do")
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
				logger.error("select-SADMOMF-ERROR REST-http-response:" + dtoContainer.getErrMsg());
				result = null;
			}else {
				logger.warn("select-SADMOMF-REST-http-response:" + response.getStatusCodeValue());
				
				for(Object o: dtoContainer.getList()){
					SadmomfDto pojo = mapper.convertValue(o, SadmomfDto.class);
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
	
	/**
	 * 
	 * @param serverRoot
	 * @param user
	 * @param lrn
	 * @return
	 */
	/*
	public List<SadmomfDto> getSadmomfForUpdate(String serverRoot, String user, String lrn) {
		List<SadmomfDto> result = new ArrayList<SadmomfDto>();
		
		logger.warn("USER:" + user);
		
		URI uri = UriComponentsBuilder
				.fromUriString(serverRoot)
				.path("/syjservicestn/syjsSADMOMF.do")
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
				logger.error("select-SADMOMF-ERROR REST-http-response:" + dtoContainer.getErrMsg());
				result = null;
			}else {
				logger.warn("select-SADMOMF-REST-http-response:" + response.getStatusCodeValue());
				
				for(Object o: dtoContainer.getList()){
					SadmomfDto pojo = mapper.convertValue(o, SadmomfDto.class);
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
	public List<SadmomfDto> updateLrnMrnSadmomf(String serverRoot, String user, GenericDtoResponse dtoResponse, String sendDate, String mode) {
		List<SadmomfDto> result = new ArrayList<SadmomfDto>();
		
		logger.warn("user:" + user);
		logger.warn("mode:" + mode);
		logger.warn("emlnrt:" + dtoResponse.getEmlnrt());
		logger.warn("emlnrm:" + dtoResponse.getEmlnrm());
		logger.warn("emuuid:" + dtoResponse.getRequestId());
		logger.warn("emmid:" + dtoResponse.getMrn());
		logger.warn("emdtin:" + sendDate);
		logger.warn("emst:" + dtoResponse.getDb_st());
		logger.warn("emst2:" + dtoResponse.getDb_st2());
		logger.warn("emst3:" + dtoResponse.getDb_st3());
		
		logger.warn("Start --> update of Lrn and Mrn at SADMOTF_U...");
		//example
		//http://localhost:8080/syjservicestn/syjsSADMOMF_U.do?user=NN&emlnrt=1&emlnrm=2&mode=UL&emmid=XX&emuuid=uuid
		URI uri = UriComponentsBuilder
				.fromUriString(serverRoot)
				.path("/syjservicestn/syjsSADMOMF_U.do")
				.queryParam("user", user)
				.queryParam("mode", mode)
				.queryParam("emlnrt", dtoResponse.getEmlnrt())
				.queryParam("emlnrm", dtoResponse.getEmlnrm())
				.queryParam("emuuid", dtoResponse.getRequestId())
				.queryParam("emmid", dtoResponse.getMrn())
				.queryParam("emdtin", Integer.parseInt(sendDate))
				.queryParam("emst", dtoResponse.getDb_st())
				.queryParam("emst2", dtoResponse.getDb_st2())
				.queryParam("emst3", dtoResponse.getDb_st3())
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
				logger.error("select-SADMOMF-ERROR REST-http-response:" + dtoContainer.getErrMsg());
				result = null;
			}else {
				logger.warn("select-SADMOMF-REST-http-response:" + response.getStatusCodeValue());
				SadmomfDto pojo = new SadmomfDto();
				if(mode.startsWith("D")){
					//nothing since it is DELETE;
				}else {
					//set it in order to have a valid response
					pojo.setEmmid(dtoResponse.getMrn());
				}
				result.add(pojo);
				
			}
			logger.warn("End --> update of Lrn and Mrn at SADMOMF_U...");
			
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
	public List<SadmomfDto> setMrnBupSadmomf(String serverRoot, String user, GenericDtoResponse dtoResponse) {
		List<SadmomfDto> result = new ArrayList<SadmomfDto>();
		
		logger.info("user:" + user);
		logger.info("emlnrt:" + dtoResponse.getEmlnrt());
		logger.info("emlnrm:" + dtoResponse.getEmlnrm());
		logger.info("emmid:" + dtoResponse.getMrn());
		
		logger.warn("Start --> update of Mrn-Bup at SADMOMF_U_BUP...");
		//example
		//http://localhost:8080/syjservicestn/syjsSADEXMF_U.do?user=NN&emavd=1&empro=501941&mode=UL&emmid=XX&emuuid=uuid
		URI uri = UriComponentsBuilder
				.fromUriString(serverRoot)
				.path("/syjservicestn/syjsSADMOMF_U_BUP.do")
				.queryParam("user", user)
				.queryParam("emlnrt", dtoResponse.getEmlnrt())
				.queryParam("emlnrm", dtoResponse.getEmlnrm())
				.queryParam("emmid", dtoResponse.getMrn())
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
				logger.error("SADMOMF-MRN-BUP-ERROR REST-http-response:" + dtoContainer.getErrMsg());
				result = null;
			}else {
				logger.warn("SADMOMF-MRN-BUP-REST-http-response:" + response.getStatusCodeValue());
				SadmomfDto pojo = new SadmomfDto();
				pojo.setEmmid(dtoResponse.getMrn());
				result.add(pojo);
				
			}
			logger.warn("End --> update of Mrn-Bup at SADMOMF_U_BUP...");
			
		}catch(Exception e) {
			logger.error(e.toString());
			result = null;
		}
		
		return result; 
	}
	
	
	public List<SadmomfDto> setRequestIdBupSadmomf(String serverRoot, String user, GenericDtoResponse dtoResponse) {
		List<SadmomfDto> result = new ArrayList<SadmomfDto>();
		
		logger.info("user:" + user);
		logger.info("emlnrt:" + dtoResponse.getEmlnrt());
		logger.info("emlnrm:" + dtoResponse.getEmlnrm());
		logger.info("emuuid:" + dtoResponse.getRequestId());
		
		logger.warn("Start --> update of Mrn-Bup at SADMOMF_U_BUP...");
		//example
		//http://localhost:8080/syjservicestn/syjsSADEXMF_U.do?user=NN&emavd=1&empro=501941&mode=UL&emmid=XX&emuuid=uuid
		URI uri = UriComponentsBuilder
				.fromUriString(serverRoot)
				.path("/syjservicestn/syjsSADMOMF_U_BUP.do")
				.queryParam("user", user)
				.queryParam("emlnrt", dtoResponse.getEmlnrt())
				.queryParam("emlnrm", dtoResponse.getEmlnrm())
				.queryParam("emuuid", dtoResponse.getRequestId())
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
				logger.error("SADMOMF-MRN-BUP-ERROR REST-http-response:" + dtoContainer.getErrMsg());
				result = null;
			}else {
				logger.warn("SADMOMF-MRN-BUP-REST-http-response:" + response.getStatusCodeValue());
				SadmomfDto pojo = new SadmomfDto();
				pojo.setEmmid(dtoResponse.getMrn());
				result.add(pojo);
				
			}
			logger.warn("End --> update of Mrn-Bup at SADMOMF_U_BUP...");
			
		}catch(Exception e) {
			logger.error(e.toString());
			result = null;
		}
		
		return result; 
	}
	
	
	

}
