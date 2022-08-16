package no.systema.jservices.tvinn.expressfortolling2.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class GenericJsonStringPrinter {

	public static String debug (Object obj) {
		String json = "";
		try {
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			json = ow.writeValueAsString(obj);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return json;
	}
}
