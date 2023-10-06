package no.systema.jservices.tvinn.digitoll.v2.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntryRoutingResultDto {
	private String id;
	private String routing;
	
}
