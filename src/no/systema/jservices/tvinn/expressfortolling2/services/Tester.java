package no.systema.jservices.tvinn.expressfortolling2.services;

import org.apache.commons.lang3.StringUtils;

import no.systema.jservices.tvinn.expressfortolling2.util.DateUtils;

public class Tester {

	public static void main(String[] args) {
		System.out.println(new DateUtils().getZuluTimeWithoutMillisecondsUTC());
		System.out.println(new DateUtils().getZuluTimeWithoutMillisecondsUTC(20221107,154500));
		
		

	}

}
