package no.systema.jservices.tvinn.digitoll.external.house.dao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConsignmentMasterLevel {
	private Integer totalGrossMass;
	private String carrierIdentificationNumber;
	//private String documentNumber;
	//private String type;
	
	private TransportDocumentMasterLevel transportDocumentMasterLevel = new TransportDocumentMasterLevel();
	
}
