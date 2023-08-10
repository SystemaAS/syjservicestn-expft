package no.systema.jservices.tvinn.digitoll.v2.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class PrettyLoggerOutputer {
	public static String FRAME = "========================================";
	public static String STARS = " **************** ";
	public static String TAGS_START = " <<<<<<<<<<<<<<<< ";
	public static String TAGS_END = " >>>>>>>>>>>>>>>> ";
	
	
	
	public static String setStarsBeforeAndAfter (String legend) {
		StringBuilder strBuilder = new StringBuilder();
		
		strBuilder.append(STARS + legend + STARS);		
		
		return strBuilder.toString();
	}
	
	public static String setTagsBeforeAndAfter (String legend) {
		StringBuilder strBuilder = new StringBuilder();
		
		strBuilder.append(TAGS_START + legend + TAGS_END);		
		
		return strBuilder.toString();
	}
}
