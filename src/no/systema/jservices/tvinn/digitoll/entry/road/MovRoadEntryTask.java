package no.systema.jservices.tvinn.digitoll.entry.road;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MovRoadEntryTask implements Runnable {
	private static Logger logger = LoggerFactory.getLogger("roadentry");
	
private String message;
    
    public MovRoadEntryTask(String message){
        this.message = message;
    }
    
    
	@Override
	public void run() {
		
		logger.info(message);
		
	}
	
}
