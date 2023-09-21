package no.systema.jservices.tvinn.digitoll.v2.services;

import no.systema.jservices.tvinn.expressfortolling2.util.DateUtils;

public class Tester {

	public static void main(String[] args) {
		Integer etetad = 20230921;
		Integer etetat = 800;
		
		String out1_1 = new DateUtils().getZuluTimeWithoutMillisecondsUTC_HHmm(etetad, etetat);
		System.out.println (out1_1);
		
	}

}
