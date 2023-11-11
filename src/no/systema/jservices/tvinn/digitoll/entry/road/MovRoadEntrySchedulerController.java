package no.systema.jservices.tvinn.digitoll.entry.road;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import no.systema.jservices.tvinn.digitoll.entry.road.scheduler.MovRoadEntryScheduler;
import no.systema.jservices.tvinn.expressfortolling2.dto.GenericDtoResponse;
import no.systema.jservices.tvinn.expressfortolling2.util.ServerRoot;


/**
 * http://localhost:8080/syjservicestn-expft/digitollv2/scheduler.do?action=stop
 * @author oscardelatorre
 * @date Nov 2023
 * 
 */
@RestController
public class MovRoadEntrySchedulerController {
	private static Logger logger = LoggerFactory.getLogger("roadentry");
	
	@Autowired
	MovRoadEntryScheduler movRoadEntryScheduler;
	
	@RequestMapping(value="/digitollv2/scheduler.do", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public String jsonTest(HttpServletRequest request ) throws Exception {
		String retval = "init";
		String action = request.getParameter("action");
		
		if(StringUtils.isNotEmpty(action)) {
			if(action.equals("start")) {
				retval = "START";
				logger.info(retval);
				movRoadEntryScheduler.run(retval);
			}else if (action.equals("stop")) {
				retval = "STOP";
				movRoadEntryScheduler.shutdown(retval);
			}
		}
	
		
		return retval;
	}
	
	
}
