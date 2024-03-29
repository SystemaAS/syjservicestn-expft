package no.systema.jservices.tvinn.expressfortolling.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import no.systema.jservices.common.dao.services.BridfDaoService;
import no.systema.jservices.common.util.Log4jUtils;
import no.systema.jservices.tvinn.expressfortolling.api.Authorization;
import no.systema.jservices.tvinn.expressfortolling.api.TokenResponseDto;
import no.systema.jservices.tvinn.expressfortolling.jwt.DifiJwtCreator;
/**
 * Internal use only!
 * 
 * For troubleshooting
 * 
 * @author fredrikmoller
 * @date 2019-09
 *
 */
@RestController
public class TroubleShootingController {
	private static Logger logger = LoggerFactory.getLogger(TroubleShootingController.class.getName());
	
	@Autowired
	private BridfDaoService bridfDaoService;	
	
	@Autowired
	DifiJwtCreator difiJwtCreator;
	
	@Autowired
	Authorization authorization;
	
	/**
	 * @Example: http://localhost:8080/syjservicestn-expft/testAccessToken.do
	 * 
	 * @param session
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "testAccessToken.do", method = { RequestMethod.GET })
	public String testAccessToken(HttpSession session) throws Exception {
		TokenResponseDto maskinPortenResponseDto = authorization.accessTokenRequestPostMovementRoad();
		logger.warn("maskinport-token:" + maskinPortenResponseDto.getAccess_token());
		StringBuilder sb = new StringBuilder("OK...maskinportToken expires in:" + maskinPortenResponseDto.getExpires_in() + " scope:" + maskinPortenResponseDto.getScope());
		
		session.invalidate();
		return sb.toString();
	}
	
	@RequestMapping(value = "testAccessTokenV2.do", method = { RequestMethod.GET })
	public String testAccessTokenV2(HttpSession session) throws Exception {
		TokenResponseDto maskinPortenResponseDto = authorization.accessTokenRequestPostMovementRoad();
		logger.warn("maskinport-token:" + maskinPortenResponseDto.getAccess_token());
		TokenResponseDto tollResponseDto = authorization.accessTokenRequestPostToll(maskinPortenResponseDto);
		logger.warn("toll-token:" + tollResponseDto.getAccess_token());
		StringBuilder sb = new StringBuilder("OK...maskinportToken expires in:" + maskinPortenResponseDto.getExpires_in() + " tollToken expires in:" + tollResponseDto.getExpires_in());
		sb.append(" maskinporten-scope:" + maskinPortenResponseDto.getScope());
		session.invalidate();
		return sb.toString();
	}
	/**
	 * @Example: http://localhost:8080/syjservicestn-expft/verifyJWT.do?user=SYSTEMA
	 * 
	 * @param session
	 * @param request
	 * @param user
	 * @param fileName
	 * @return the payload
	 * @throws Exception
	 */
	@RequestMapping(value = "verifyJWT.do", method = { RequestMethod.GET })
	public String verifyJWT(HttpSession session, @RequestParam(value = "user", required = true) String user) throws Exception {
		StringBuilder sb = new StringBuilder("Payload:").append(System.lineSeparator());
		
		checkUser(user);
		
		sb.append(difiJwtCreator.decodeJWT().toString());
		
		session.invalidate();
		return sb.toString();
		
	}
	
	/**
	 * 
	 * @Example: http://localhost:8080/syjservicestn-expft/showHistory.do?user=SYSTEMA&filename=log4j_syjservicestn-expft.log
	 * 
	 */	
	@RequestMapping(value = "showHistory.do", method = { RequestMethod.GET })
	public String showHistory(HttpSession session, HttpServletRequest request, @RequestParam(value = "user", required = true) String user, 
			 				@RequestParam(value = "filename", required = true) String fileName) {
		StringBuilder sb = new StringBuilder();

		logger.info("showHistory.do...");

		checkUser(user);

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
