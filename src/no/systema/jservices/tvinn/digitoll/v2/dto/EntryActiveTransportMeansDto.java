package no.systema.jservices.tvinn.digitoll.v2.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntryActiveTransportMeansDto {
	private String identificationNumber;
	private String typeOfIdentification;
	
}
