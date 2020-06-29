package no.systema.jservices.tvinn.expressfortolling.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import no.systema.jservices.common.dao.services.BridfDaoService;
import no.systema.jservices.common.dto.TypesOfExportDto;
import no.systema.jservices.common.dto.TransportationCompanyDto;
import no.systema.jservices.tvinn.expressfortolling.api.ApiServices;
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
	 * @Example: http://localhost:8080/syjservicestn-expft/transportationCompany.do?user=SYSTEMA&id=5
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
	
}
