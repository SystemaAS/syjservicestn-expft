package no.systema.jservices.tvinn.digitoll.entry.road;

import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
		
	
	
	
	/**
	 * With this method we can stop and restart the scheduler with the task
	 * @param request
	 * @return
	 * @throws Exception
	 */
	
	@RequestMapping(value="/digitollv2/scheduler/start", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public String movRoadEntryEngineStart() throws Exception {
		String action = "start";
		logger.info(action);
		movRoadEntryScheduler.run();
		
		return action;
	}
	
	@RequestMapping(value="/digitollv2/scheduler/stop", method={RequestMethod.GET, RequestMethod.POST}) 
	@ResponseBody
	public String movRoadEntryEngineStop() throws Exception {
		String action = "stop";
		logger.info(action);
		movRoadEntryScheduler.shutdown(action);
		
		return action;
	}
	
	
	
	
}
