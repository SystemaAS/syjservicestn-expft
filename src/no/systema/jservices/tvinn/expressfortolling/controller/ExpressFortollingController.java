package no.systema.jservices.tvinn.expressfortolling.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import no.systema.jservices.common.dao.services.BridfDaoService;
import no.systema.jservices.common.util.Log4jUtils;
import no.systema.jservices.tvinn.expressfortolling.api.ApiServices;
import no.systema.jservices.tvinn.expressfortolling.entities.TransportationCompanyDto;
/**
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
	public List<TransportationCompanyDto> getTransportationCompany(HttpSession session, 
																@RequestParam(value = "user", required = true) String user,
																@RequestParam(value = "id", required = false) String id) {
		logger.info("transportationCompany.do, id="+id);
		
		checkUser(user);
		
		List<TransportationCompanyDto> payload = apiServices.getTransportationCompany();
		
		session.invalidate();
		return payload;

	}

	/**
	 * 
	 * @Example: http://localhost:8080/syjservicestn-expft/showHistory.do?user=SYSTEMA&filename=log4j_syjservicestn-expft.log
	 * 
	 */	
	@RequestMapping(value = "showHistory.do", method = { RequestMethod.GET })
	@ResponseBody
	public String showHistory(HttpSession session, HttpServletRequest request, @RequestParam("user") String user) {
		StringBuilder sb = new StringBuilder();

		logger.info("showHistory.do...");

		checkUser(user);

		String fileName = request.getParameter("filename");
		Assert.notNull(fileName, "fileName must be delivered.");

		try {
			sb.append(Log4jUtils.getLog4jData(fileName));
		} catch (IOException e) {
			logger.error("Kilroy was here");
			return e.getLocalizedMessage();
		}

		session.invalidate();
		return sb.toString();

	}

	private void checkUser(String user) {
		if (!bridfDaoService.userNameExist(user)) {
			throw new RuntimeException("ERROR: parameter, user, is not valid!");
		}		
	}	
	
}
