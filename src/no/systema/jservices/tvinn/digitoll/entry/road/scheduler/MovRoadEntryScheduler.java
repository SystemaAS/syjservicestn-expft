package no.systema.jservices.tvinn.digitoll.entry.road.scheduler;

import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import no.systema.jservices.tvinn.digitoll.entry.road.MovRoadEntryTask;
import no.systema.jservices.tvinn.digitoll.v2.controller.DigitollV2TransportController;

@Service
public class MovRoadEntryScheduler {
	private static Logger logger = LoggerFactory.getLogger("roadentry");
	
	ScheduledFuture scheduledFuture;
	
	@Autowired
	ThreadPoolTaskScheduler threadPoolTaskScheduler;
		
	
	public void run(String action) {
		
		//taskScheduler.scheduleWithFixedDelay( new MovRoadEntryTask("Fixed 1 second Delay"), 1000);
		scheduledFuture = threadPoolTaskScheduler.scheduleWithFixedDelay( new MovRoadEntryTask(action), 60000);
		
	}
	
	public void shutdown(String action) {
		logger.info(action);
		scheduledFuture.cancel(true);
	}
	
	
	

	
}
