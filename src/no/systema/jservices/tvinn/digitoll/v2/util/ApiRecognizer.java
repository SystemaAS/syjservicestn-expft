package no.systema.jservices.tvinn.digitoll.v2.util;

import org.apache.commons.lang3.StringUtils;

public class ApiRecognizer {

	public static Boolean isAir (String value) {
		Boolean retval = false;
		if(StringUtils.isNotEmpty(value)) {
			if(value.startsWith("4")) {
				retval = true;
			}
		}
		
		return  retval;
	}
	
	public static Boolean isRail (String value) {
		Boolean retval = false;
		if(StringUtils.isNotEmpty(value)) {
			if(value.startsWith("2")) {
				retval = true;
			}
		}
		
		return  retval;
	}
}
