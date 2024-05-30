package no.systema.jservices.tvinn.expressfortolling2.dao;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConsignmentMasterLevel {
	
	private List<ConsignmentHouseLevel> consignmentHouseLevel;
	
	private String containerIndicator;
	private String grossMass;
	private Carrier carrier;
	private TransportDocumentMasterLevel transportDocumentMasterLevel;
	private String referenceNumberUCR;
	private Consignee consignee;
	private Consignor consignor;
	//
	private PlaceOfLoading placeOfLoading;
	private PlaceOfUnloading placeOfUnloading;
	
	@JsonProperty("passiveBorderTransportMeans")
	private PassiveBorderTransportMeans passiveBorderTransportMeans;
	
	@JsonProperty("transportEquipment")
	private List<TransportEquipment> transportEquipment;

}
