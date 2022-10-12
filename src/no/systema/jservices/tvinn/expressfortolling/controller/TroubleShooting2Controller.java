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
 * For troubleshooting the new Express fort. (V2) Movement Road
 * 
 * @author oscardelatorre
 * @date 2022
 *
 */
@RestController
public class TroubleShooting2Controller {
	private static Logger logger = LoggerFactory.getLogger(TroubleShooting2Controller.class.getName());
	
	@Autowired
	private BridfDaoService bridfDaoService;	
	
	@Autowired
	DifiJwtCreator difiJwtCreator;
	
	@Autowired
	Authorization authorization;
	
	/**
	 * @Example: http://localhost:8080/syjservicestn-expft/testAccessToken2.do
	 * 
	 * @param session
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "testAccessToken2.do", method = { RequestMethod.GET })
	public String testAccessTokenMovementRoad(HttpSession session) throws Exception {
		//TokenResponseDto responseDto  = authorization.accessTokenRequestPost();
		
		TokenResponseDto maskinPortenResponseDto = authorization.accessTokenRequestPostMovementRoad();
		//System.out.println("difi-token:" + maskinPortenResponseDto.getAccess_token());
		TokenResponseDto tollResponseDto = authorization.accessTokenRequestPostToll(maskinPortenResponseDto);
		logger.warn("toll-token:" + tollResponseDto.getAccess_token());
		logger.warn("toll-token expires_in:" + tollResponseDto.getExpires_in());
		
		
		
		//logger.warn("responseDto = "+responseDto);
		StringBuilder sb = new StringBuilder();
		
		session.invalidate();
		return sb.toString();
	}
	
	
	private void checkUser(String user) {
		if (!bridfDaoService.userNameExist(user)) {
			throw new RuntimeException("ERROR: parameter, user, is not valid!");
		}		
	}	
	
}
