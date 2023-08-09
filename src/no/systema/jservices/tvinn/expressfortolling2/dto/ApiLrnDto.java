package no.systema.jservices.tvinn.expressfortolling2.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiLrnDto {
	private String lrn;
	private String requestId;
}
