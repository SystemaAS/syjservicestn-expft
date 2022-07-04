package no.systema.jservices.tvinn.expressfortolling2.dao;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsignmentMasterLevel {
	
	private List<ConsignmentHouseLevel> consignmentHouseLevel;
	
	private String containerIndicator = "";
	private String grossMass = "";
	private Carrier carrier;
	private TransportDocumentMasterLevel transportDocumentMasterLevel;
	private String referenceNumberUCR = "";
	private Consignee consignee;
	private Consignor consignor;
	//
	private PlaceOfLoading placeOfLoading;
	private PlaceOfUnloading placeOfUnloading;
	private PassiveBorderTransportMeans passiveBorderTransportMeans;
	private List<TransportEquipment> tranportEquipment;

}
