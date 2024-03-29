package no.systema.jservices.tvinn.expressfortolling2.dao;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PreviousDocuments {
	
	private String typeOfReference;
	private String referenceNumber;
	private String declarantNumber;
	private String declarationDate;
	private String sequenceNumber;
	

}
