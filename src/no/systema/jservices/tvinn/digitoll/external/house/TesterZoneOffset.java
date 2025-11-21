package no.systema.jservices.tvinn.digitoll.external.house;

import java.time.OffsetTime;
import java.time.ZoneOffset;

public class TesterZoneOffset {

	public static void main(String[] args) {
		//TEST
		  String clock = "14:23:29Z";
		  
	      //
	      OffsetTime currentOffsetDateTime = OffsetTime.now();
	        System.out.println("Current OffsetDateTime: " + currentOffsetDateTime);
	        // Get the ZoneOffset from the OffsetDateTime
	        ZoneOffset currentZoneOffset = currentOffsetDateTime.getOffset();
	        System.out.println("Current ZoneOffset: " + currentZoneOffset);
	        clock = clock.replace("Z", currentZoneOffset.toString());
	        
	        System.out.println(clock);
	        
	      //END TEST
	      

	}

}
