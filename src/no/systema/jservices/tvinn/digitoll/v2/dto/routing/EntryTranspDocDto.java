package no.systema.jservices.tvinn.digitoll.v2.dto.routing;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntryTranspDocDto {
	private String referenceNumber;
	private String type;
	
}
