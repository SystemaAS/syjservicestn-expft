package no.systema.jservices.tvinn.digitoll.external.house.dao;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConsignmentHouseLevel {
	private Double totalGrossMass = 0.00D;
	private Integer numberOfPackages = 0;
	private String goodsDescription;
	private TransportDocumentHouseLevel transportDocumentHouseLevel;
	private ConsignmentMasterLevel consignmentMasterLevel;
	private List<ExportFromEU> exportFromEU; 
	private List<PreviousDocuments> previousDocuments;
	private DeclarantId declarantId;
	private String procedure; //this is own
	
	
}
