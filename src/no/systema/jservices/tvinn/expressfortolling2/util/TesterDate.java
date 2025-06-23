package no.systema.jservices.tvinn.expressfortolling2.util;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

public class TesterDate {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//String dateZulu = "2022-08-25T19:43:36Z";
		//String tmp = dateZulu.replaceAll("-", "").substring(0,8);
		//System.out.println(tmp);
		DateUtils dateUtil = new DateUtils();
		
		
		System.out.println(dateUtil.getZuluTimeWithoutMillisecondsUTC());
		System.out.println(dateUtil.getZuluTimeWithoutMillisecondsWithOffset());
		System.out.println(dateUtil.getZuluTimeWithoutMillisecondsWithOffset("2025-06-23T13:52:36Z"));
		
		
	}
	
	

}
