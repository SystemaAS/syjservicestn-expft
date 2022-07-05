package no.systema.jservices.tvinn.expressfortolling2.dao;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HouseConsignmentConsignmentHouseLevel {
	
	//Mandatory*
	private Integer containerIndicator = 0;
	//Mandatory*
	private Double totalGrossMass = 0.00;
	//Mandatory*
	private String referenceNumberUCR = "";
	//Optional
	private List<ExportFromEU> exportFromEU;
	//Mandatory*
	private ImportProcedure importProcedure;
	//Optional
	private List<PreviousDocuments> previousDocuments;
	//Optional
	private AdditionalFiscalReferences additionalFiscalReferences;
	//Optional
	private PlaceOfLoading placeOfLoading;
	//Mandatory*
	private PlaceOfUnloading placeOfUnloading;
	//Optional
	private List<AdditionalSupplyChainActor> additionalSupplyChainActor;
	//Mandatory*
	private Consignee consignee;
	//Mandatory*
	private Consignor consignor;
	//Mandatory*
	private TransportDocumentHouseLevel transportDocumentHouseLevel;
	//Mandatory*
	private List<GoodsItem> goodsItem;
	//Mandatory*
	private TransportCharges transportCharges;
	//Optional
	private List<CountriesOfRoutingOfConsignments> countriesOfRoutingOfConsignments;
	//Optional
	private List<TransportEquipment> transportEquipment;
	//Optional
	private List<PassiveTransportMeans> passiveTransportMeans;
	//Mandatory
	private TotalAmountInvoiced totalAmountInvoiced;
	
	
}
