package no.systema.jservices.tvinn.digitoll.v2.dao;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
//we need this line in order to follow the order as in the properties. 
@JsonPropertyOrder({"containerIndicator", "grossMass", "carrier", "consignee", "consignor", "transportDocumentMasterLevel", "tranportEquipment"})
public class ConsignmentMasterLevel {
	
	
	//Mandatory
	private Integer containerIndicator;
	//Mandatory
	private Integer grossMass;
	//Mandatory
	private Carrier carrier;
	private Consignee consignee;
	private Consignor consignor;
	//Mandatory
	private TransportDocumentMasterLevel transportDocumentMasterLevel;
	
	@JsonProperty("tranportEquipment")
	private List<TransportEquipment> tranportEquipment;
	
	//Optional
	//Liste over hvilke postsekker som denne forsendelsene består av. Skal kun benyttes for hovedforsendelser som inneholder post
	private ConsignmentHouseLevel consignmentHouseLevel;
	
	//Optional
	private PlaceOfLoading placeOfLoading;
	private PlaceOfUnloading placeOfUnloading;
	private PlaceOfDelivery placeOfDelivery;
	
	

}
