package no.systema.jservices.tvinn.digitoll.v2.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiRequestIdDto {
	private String requestId;
}
