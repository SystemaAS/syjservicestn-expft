package no.systema.jservices.tvinn.expressfortolling2.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiMrnStatusHouseRecordDto {
	private String documentNumber;
	private String type;
	private String documentStatus;
	private List incompleteDocumentationReasonList;
	private String errMsg = "";
	
}
