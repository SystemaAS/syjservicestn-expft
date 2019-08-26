package no.systema.jservices.tvinn.expressfortolling;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import no.systema.jservices.common.dao.services.BridfDaoService;
import no.systema.jservices.common.util.Log4jUtils;
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
	 * 
	 */	
	@RequestMapping(value="transportationCompany.do", method={RequestMethod.GET})
	@ResponseBody
	public String download(HttpSession session, HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();
			
		
		String payload = apiServices.getTransportationCompany();
		

		return sb.toString();

	}


	/**
	 * 
	 * @Example: http://gw.systema.no:8080/syjservicestn-expft/showHistory.do?user=SYSTEMA&filename=log4j_syjservicestn-expft.log
	 * 
	 */	
	@RequestMapping(value="showHistory.do", method={RequestMethod.GET, RequestMethod.POST})
	@ResponseBody
	public String showHistory(HttpSession session, HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();

		logger.info("showHistory.do...");
		try {
			String user = request.getParameter("user");
			Assert.notNull(user, "user must be delivered."); 

			String userName = bridfDaoService.getUserName(user);
			Assert.notNull(userName, "userName not found in Bridf."); 

			String fileName = request.getParameter("filename");
			Assert.notNull(fileName, "fileName must be delivered."); 			
			
			
			sb.append(Log4jUtils.getLog4jData(fileName));
			
			
		} catch (Exception e) {
			// write std.output error output
			e.printStackTrace();
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			e.printStackTrace(printWriter);
			return "ERROR [JsonResponseOutputterController]" + writer.toString();
		}

		session.invalidate();
		return sb.toString();

	}
	
	
}
