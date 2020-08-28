package no.systema.jservices.tvinn.expressfortolling.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.systema.jservices.common.dao.services.BridfDaoService;
import no.systema.jservices.common.dto.TypesOfExportDto;
import no.systema.jservices.common.dto.TransportationCompanyDto;
import no.systema.jservices.common.dto.ManifestDto;

import no.systema.jservices.tvinn.expressfortolling.api.ApiServices;
import no.systema.main.util.ObjectMapperHalJson;
/**
 * Main entrance for accessing Express fortolling API.
 * 
 * @author fredrikmoller
 * @date 2019-09
 *
 */
@RestController
public class ExpressFortollingController {
	private static Logger logger = Logger.getLogger(ExpressFortollingController.class.getName());
	
	@Autowired
	private BridfDaoService bridfDaoService;	
	
	@Autowired
	private ApiServices apiServices; 
	
	
	/**
	 * @Example: http://localhost:8080/syjservicestn-expft/transportationCompany.do?user=SYSTEMA&id=936809219
	 */	
	@RequestMapping(value="transportationCompany.do", method={RequestMethod.GET})
	public TransportationCompanyDto getTransportationCompany(HttpSession session, 
																@RequestParam(value = "user", required = true) String user,
																@RequestParam(value = "id", required = false) String id) {
		logger.info("transportationCompany.do, id="+id);
		
		checkUser(user);
		
		TransportationCompanyDto payload = apiServices.getTransportationCompany(id);
		
		session.invalidate();
		return payload;

	}

	private void checkUser(String user) {
		if (!bridfDaoService.userNameExist(user)) {
			throw new RuntimeException("ERROR: parameter, user, is not valid!");
		}		
	}	
	/**
	 * Get the manifest by id
	 * @Example http://localhost:8080/syjservicestn-expft/getManifest.do?user=SYSTEMA&id=f2bfbb94-afae-4af3-a4ff-437f787d322f
	 * @param session
	 * @param user
	 * @param id
	 * @return
	 */
	@RequestMapping(value="getManifest.do", method={RequestMethod.GET}) 
	public ManifestDto getManifest(HttpSession session, @RequestParam(value = "user", required = true) String user, 
														@RequestParam(value = "id", required = true) String id) throws Exception {
		logger.info("getManifest.do, id="+id);
		
		checkUser(user);
		
		String payload = apiServices.getManifest(id);
		//convert to Dto (we do not do this in the service since we must see the JSON string in case of errors. It is easier to follow...
		ObjectMapperHalJson objMapper = new ObjectMapperHalJson(payload, "");
		StringBuffer jsonToConvert = new StringBuffer();
		//get list of DTOs
		ManifestDto manifestDto = objMapper.getObjectMapper(jsonToConvert).readValue(jsonToConvert.toString(), new TypeReference<ManifestDto>() {
		        });
		session.invalidate();
		return manifestDto;

	}
	/**
	 * Get the manifest by id as raw as Toll.no returns it (HalJson)
	 * @Example http://localhost:8080/syjservicestn-expft/getManifest.do?user=SYSTEMA&id=f2bfbb94-afae-4af3-a4ff-437f787d322f
	 * @param session
	 * @param user
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="getManifestRaw.do", method={RequestMethod.GET}) 
	public String getManifestRaw(HttpSession session, @RequestParam(value = "user", required = true) String user, 
														@RequestParam(value = "id", required = true) String id) throws Exception {
		logger.info("getManifest.do, id="+id);
		
		checkUser(user);
		
		String payload = apiServices.getManifest(id);
		session.invalidate();
		return payload;

	}
}
