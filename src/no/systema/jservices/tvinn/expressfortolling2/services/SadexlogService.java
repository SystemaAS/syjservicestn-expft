package no.systema.jservices.tvinn.expressfortolling2.services;

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
import no.systema.jservices.tvinn.expressfortolling2.dto.GenericDtoResponse;
import no.systema.jservices.tvinn.expressfortolling2.dto.SadexhfDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.SadexlogDto;

@Service
public class SadexlogService {
private static final Logger logger = LoggerFactory.getLogger(SadexlogService.class);
	
	@Autowired
	RestTemplate restTemplate;
	
	public List<SadexlogDto> insertLogRecord(String serverRoot, String user, SadexlogDto dto, String mode) {
		List<SadexlogDto> result = new ArrayList<SadexlogDto>();
		
		logger.warn("USER:" + user);
		logger.warn("AVD:" + dto.getElavd());
		logger.warn("PRO:" + dto.getElpro());
		logger.warn("TDN:" + dto.getEltdn());
		
		
		//example
		//http://localhost:8080/syjservicestn/syjsSADEXLOG_U.do?user=NN&emavd=1&empro=501941&mode=A&etc
		URI uri = UriComponentsBuilder
				.fromUriString(serverRoot)
				.path("/syjservicestn/syjsSADEXLOG_U.do")
				.queryParam("user", user)
				.queryParam("mode", mode)
				.queryParam("elavd", dto.getElavd())
				.queryParam("elpro", dto.getElpro())
				.queryParam("eltdn", dto.getEltdn())
				.queryParam("eldate", dto.getEldate())
				.queryParam("eltime", dto.getEltime())
				.queryParam("eltyp", dto.getEltyp())
				.queryParam("elltxt", dto.getElltxt())
				
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
				logger.error("select-SADEXLOG-ERROR REST-http-response:" + dtoContainer.getErrMsg());
				result = null;
			}else {
				logger.warn("select-SADEXLOG-REST-http-response:" + response.getStatusCodeValue());
				result.add(dto);
				
			}

		}catch(Exception e) {
			logger.error(e.toString());
			result = null;
		}
		logger.warn(result.toString());
		return result; 
	}
	
	
	public List<SadexlogDto> insertLogRecordFake(String serverRoot, String user, SadexlogDto dto, String mode) {
		logger.warn("Arlekin!");
		return null;
	}
}
