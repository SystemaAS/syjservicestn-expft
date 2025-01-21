package no.systema.jservices.tvinn.expressfortolling.controller;

import java.io.IOException;
import java.net.ProxySelector;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

//import org.apache.cxf.endpoint.Client;
//import org.apache.cxf.frontend.ClientProxy;
//import org.apache.cxf.jaxrs.client.WebClient;
//import org.apache.cxf.transport.http.HTTPConduit;
//import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import com.eori.validation.soap.ws.client.generated.EORIValidation;
import com.eori.validation.soap.ws.client.generated.EoriResponse;
import com.eori.validation.soap.ws.client.generated.EoriValidationResult;
import com.eori.validation.soap.ws.client.generated.Validation;

import no.systema.jservices.common.dao.services.BridfDaoService;
import no.systema.jservices.common.util.Log4jUtils;
import no.systema.jservices.tvinn.digitoll.v2.services.SadmohfService;
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
	private static final Logger logger = LoggerFactory.getLogger(TroubleShootingController.class);
	
	@Autowired
	private BridfDaoService bridfDaoService;	
	
	@Autowired
	DifiJwtCreator difiJwtCreator;
	
	@Autowired
	Authorization authorization;
	
	@Value("${digitoll.access.use.proxy}")
	String proxyIsUsed;
	
	@Value("${digitoll.access.proxy.host}")
	String proxyHost;
	
	@Value("${digitoll.access.proxy.port}")
	Integer proxyPort;
	
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
	 * This uses the wsdl generated code from:
	 * 
	 * with Java 8 (command line where JAVA_HOME/bin/wsimport...
	 * >wsimport -s . -keep -p com.eori.validation.soap.ws.client.generated "https://ec.europa.eu/taxation_customs/dds2/eos/validation/services/validation?wsdl"
	 * 
	 * As a test for EORI-Validation
	 * 
	 * Important: open for firewall on DSV:
	 * https://ec.europa.eu/taxation_customs/dds2/eos/validation/services/validation?wsdl
	 * http://eori.ws.eos.dds.s/
	 * 
	 * Works fine from http:localhost:8080/... and https://gw.systema.no.8443...
	 * 
	 * @param session
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "testEORIValidation.do", method = { RequestMethod.GET })
	public String testEORIValidation(HttpSession session) throws Exception {
		logger.info("inside testEORIValidation" );
		
		StringBuilder sb = new StringBuilder();
		Validation validation = new Validation();
		
		logger.info("START testEORIValidation" );
		EORIValidation eoriValidation = validation.getEORIValidationImplPort();
		logger.info("after implPort");
		//TOTEN
		List<String> eoriList = new ArrayList();
		eoriList.add("SE4446864193");
		eoriList.add("YYYYYYYYY");
		eoriList.add("SE4441976109");
		
		logger.info("AAA");
		EoriValidationResult result = eoriValidation.validateEORI(eoriList);
		logger.info("BBB");
		List<EoriResponse> responseList = result.getResult();
		for (EoriResponse response: responseList ) {
			//logger.info(response.getEori() + "XXX" + response.getName() + " Status:" + response.getStatus() + "-" + response.getStatusDescr());
			//logger.info(response.getCity() + " " + response.getCountry() + " " + response.getPostalCode());
			//output on browser
			sb.append("-------------EORI:" + response.getEori() + " Name:" + response.getName() + " Status:" + response.getStatus() + "-" + response.getStatusDescr());
			sb.append(" " + response.getCity() + " " + response.getCountry() + " " + response.getPostalCode());
			logger.info(sb.toString());
		}
		
		
		session.invalidate();
		return sb.toString();
	}
	
	
	/**
	 * 
	 * @param session
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "testEORIValidationCXF.do", method = { RequestMethod.GET })
	public String testEORIValidationCXF(HttpSession session) throws Exception {
		logger.info("inside testEORIValidationCXF" );
		
		
		logger.info("START httpProxy " );
		Validation siri = new Validation();
		logger.info("AAA" );
		EORIValidation port = siri.getEORIValidationImplPort();
		logger.info("BBB" );
		/*
		Client client = ClientProxy.getClient(port);
		logger.info("CCC" );
		HTTPConduit conduit = (HTTPConduit) client.getConduit();
		logger.info("DDD" );
		conduit.getClient().setProxyServer(proxyHost);
		logger.info("EEE" );
		conduit.getClient().setProxyServerPort(proxyPort);
		logger.info("FFF" );
		logger.info("END httpProxy" );
		*/
		
		
		
		StringBuilder sb = new StringBuilder();
		Validation validation = new Validation();
		
		logger.info("START testEORIValidation" );
		EORIValidation eoriValidation = validation.getEORIValidationImplPort();
		logger.info("after implPort");
		//TOTEN
		List<String> eoriList = new ArrayList();
		eoriList.add("SE4446864193");
		eoriList.add("YYYYYYYYY");
		eoriList.add("SE4441976109");
		
		logger.info("AAA");
		EoriValidationResult result = eoriValidation.validateEORI(eoriList);
		logger.info("BBB");
		List<EoriResponse> responseList = result.getResult();
		for (EoriResponse response: responseList ) {
			//logger.info(response.getEori() + "XXX" + response.getName() + " Status:" + response.getStatus() + "-" + response.getStatusDescr());
			//logger.info(response.getCity() + " " + response.getCountry() + " " + response.getPostalCode());
			//output on browser
			sb.append("-------------EORI:" + response.getEori() + " Name:" + response.getName() + " Status:" + response.getStatus() + "-" + response.getStatusDescr());
			sb.append(" " + response.getCity() + " " + response.getCountry() + " " + response.getPostalCode());
			logger.info(sb.toString());
		}
		
		
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
