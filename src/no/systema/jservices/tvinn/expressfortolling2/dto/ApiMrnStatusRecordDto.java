package no.systema.jservices.tvinn.expressfortolling2.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiMrnStatusRecordDto {
	private String documentNumber;
	private String type;
	private Boolean received;
	private String documentStatus;
	
}
