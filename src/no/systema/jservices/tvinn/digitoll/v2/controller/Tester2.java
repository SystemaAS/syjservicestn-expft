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


public class Tester2  {

	
	public static void main(String[] args) {
		StringBuilder x = new StringBuilder();
		test(x);
		System.out.println(x.toString());
	}
	
	
	private static void test(StringBuilder str) {
		str.append("whatever");
	}

}

