package no.systema.jservices.tvinn.digitoll.v2.util;

import org.apache.commons.lang3.StringUtils;

import no.systema.jservices.tvinn.digitoll.v2.enums.EnumPeppolID;

public class PeppolSchemaIdRecognizer {

	
	public static String getPeppolIdPrefix (String value) {
		String retval = "";
		
		if("NO".equals(value)){
			  retval = EnumPeppolID.Norway_Orgnr.toString();
		  }else if ("DK".equals(value)){
			  retval = EnumPeppolID.Denmark_Orgnr.toString();
		  }else if ("LV".equals(value)){
			  retval = EnumPeppolID.Latvia_Vatnr.toString();
		  }else if ("LT".equals(value)){
			  retval = EnumPeppolID.Lithuania_Vatnr.toString();
		  }else if ("SE".equals(value)){
			  retval = EnumPeppolID.Sweden_Orgnr.toString();
		  }else if ("PL".equals(value)){
			  retval = EnumPeppolID.Poland_Vatnr.toString();
		  }else if ("EE".equals(value)){
			  retval = EnumPeppolID.Estonia_Vatnr.toString();
		  }else if ("DE".equals(value)){
			  retval = EnumPeppolID.Germany_Vatnr.toString();
		  }else if ("BG".equals(value)){
			  retval = EnumPeppolID.Bulgaria_Vatnr.toString();
		  }
		
		return  retval;
	}
}
