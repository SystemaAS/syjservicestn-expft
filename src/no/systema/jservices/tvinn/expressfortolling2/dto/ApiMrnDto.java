package no.systema.jservices.tvinn.expressfortolling2.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiMrnDto {
	private String status;
	private String localReferenceNumber;
	private String masterReferenceNumber;
	private String notificationDate;
	private Object[] validationErrorList;
	private String mrn;
	private String requestId;
	private Object[] incompleteDocumentationReasonList;
	
	private ApiMrnEoriValidationDto eoriValidation;
	
}
