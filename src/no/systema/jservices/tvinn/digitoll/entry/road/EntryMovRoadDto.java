package no.systema.jservices.tvinn.digitoll.entry.road;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntryMovRoadDto {
	
	private Boolean validEntry;
	private String customsOfficeOfEntry;
	private String timeOfEntry;
	private String mrn;
	
}
