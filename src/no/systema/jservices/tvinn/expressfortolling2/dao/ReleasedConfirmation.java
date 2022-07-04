package no.systema.jservices.tvinn.expressfortolling2.dao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReleasedConfirmation {
	private String emailAddress = "";
}
