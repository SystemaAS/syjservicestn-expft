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
@JsonPropertyOrder({"receptacleIdentificationNumber", "containerIndicator", "totalGrossMass", "numberOfPackages", "goodsDescription", "transportDocumentHouseLevel", "consignmentMasterLevel"})
public class HouseConsignmentConsignmentHouseLevel {
	
	//Optional (Post)
	private String receptacleIdentificationNumber;
	//Mandatory*
	private Integer containerIndicator = 0;
	//Mandatory*
	private Double totalGrossMass = 0.00;
	//Mandatory (v2)
	private Integer numberOfPackages = 0;
	//Mandatory (v2)
	private String goodsDescription;
	//Mandatory
	private TransportDocumentHouseLevel transportDocumentHouseLevel;
	
	//Optional
	@JsonProperty("consignmentMasterLevel")
	private HouseConsignmentMasterLevel consignmentMasterLevel;
	
	//Mandatory*
	private ImportProcedure importProcedure;
	
	//Optional
	private List<PreviousDocuments> previousDocuments;		
	//Optional
	private List<ExportFromEU> exportFromEU;
	
	//Mandatory*
	private Consignor consignor;
	//Mandatory*
	private Consignee consignee;
			
	//Optional
	private PlaceOfAcceptance placeOfAcceptance;
	//Optional
	private PlaceOfDelivery placeOfDelivery;
	
	//Only VOEC
	private List<GoodsItem> goodsItem;
	
	//Optional
	private List<TransportEquipment> transportEquipment;
		
}
