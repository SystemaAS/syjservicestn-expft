package no.systema.jservices.tvinn.expressfortolling2.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiValidationListDto {
	private String description;
	private String pointer;

}
