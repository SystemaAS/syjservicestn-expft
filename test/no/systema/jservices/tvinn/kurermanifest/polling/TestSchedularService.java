package no.systema.jservices.tvinn.kurermanifest.polling;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.*;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * This class must be externalized in a separate java project and package as a JAR in order to use it as an ENGINE
 * 
 */
public class TestSchedularService {
	 private Logger logger = LoggerFactory.getLogger(TestSchedularService.class);
	 long sleep = 5000;
	 long numberOfTimes = 3;
	 
	 //PRODUCTION
	 //long sleep = 300000; */
	 /*every 5 minutes = 12 times per hour. 24hrs * 12 times = 288 times */
	 //long numberOfTimes = 288; 
	 
	 
	 @Test
	 public void testLoop() throws Exception {
		 ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		 ScheduledFuture future = executor.scheduleWithFixedDelay(new PollingService(), 0, sleep,TimeUnit.MILLISECONDS);
		 Thread.sleep(numberOfTimes * sleep);
		 future.cancel(false);
		 executor.shutdown();
	 }
}
	 
class PollingService implements Runnable {
		private Logger logger = LoggerFactory.getLogger(PollingService.class);
		private String startUrl = "http://localhost:8080/syjservicestn-expft/testUpload";
		 
		private int count = 0;
		URI uri = null;
		 
		public void run() {
			try {
				this.uri = new URI(this.startUrl);
				
				System.out.println("iteration :" + (count++));
				//call rest controller to start polling of directories for file upload
				RestTemplate restTemplate = new RestTemplate();
				ResponseEntity<String> response = new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
				
				response = restTemplate.getForEntity(uri, String.class);
				System.out.println("response="+response);
			} catch (Exception e) {
				System.out.println("There is space for improvements on indata..." + e.toString());
				e.printStackTrace();
			}
	 	}
}
