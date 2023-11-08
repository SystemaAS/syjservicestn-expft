package no.systema.jservices.tvinn.digitoll.v2.controller;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;


public class Tester  {

	
	public static void main(String[] args) {
		Tester tester = new Tester();
		
		String mrn = "";
		int counter = 1;
		ScheduledExecutorService pool = Executors.newSingleThreadScheduledExecutor();
		
			while (StringUtils.isEmpty(mrn)) {
				if(counter>6) {
					pool.shutdown();
					break;

				}
				try {
					if(counter==3) {
						mrn="xxx";
						
					}
					System.out.println("EMPTY");
					//Thread.sleep(10000);
					tester.fake();
					ScheduledFuture<String> schedule = pool.schedule(()-> "own sleep", 5, TimeUnit.SECONDS);
					System.out.println(schedule.get());
				
					
				}catch(Exception e) {
					System.out.println(e.toString());
					
				}finally {
					counter++;
				}
				
			}
			pool.shutdown();
			
		System.out.println("OUT");
	}
	
	
	private void fake() {
		System.out.println("fake");
	}

}

