package no.systema.jservices.tvinn.digitoll.v2.util;

import org.apache.commons.lang3.StringUtils;

public class ApiAirRecognizer {

	public static Boolean isAir (String value) {
		Boolean retval = false;
		if(StringUtils.isNotEmpty(value)) {
			if(value.startsWith("4")) {
				retval = true;
			}
		}
		
		return  retval;
	}
}
