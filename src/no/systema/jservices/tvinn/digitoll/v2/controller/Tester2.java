package no.systema.jservices.tvinn.digitoll.v2.controller;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import no.systema.jservices.common.util.DateTimeManager;


public class Tester2  {
	static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(15); // no
    static ScheduledFuture<?> t;

	
	public static void main(String[] args) {
		t = executor.scheduleAtFixedRate(new MyTask(), 1000, 3000, TimeUnit.MILLISECONDS);
	    
	}
	
	
    static class MyTask implements Runnable {
        private int attempt = 1;

        public void run() {
            System.out.print(attempt + " ");
            if (++attempt > 5) {
            	t.cancel(true);
            	executor.shutdown();
            }
        }
    }
	
	
	
	
	
	

}

