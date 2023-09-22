package no.systema.jservices.tvinn.expressfortolling2.dao;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HouseConsignmentConsignmentHouseLevel {
	
	//Mandatory*
	private Integer containerIndicator = 0;
	//Mandatory*
	private Double totalGrossMass = 0.00;
	//Mandatory (v2)
	private Integer numberOfPackages = 0;
	//Mandatory (v2)
	private String goodsDescription;
		
	//Mandatory*
	private String referenceNumberUCR;
	//Optional
	private List<PreviousDocuments> previousDocuments;
	//Optional
	private List<ExportFromEU> exportFromEU;
	//Mandatory*
	private ImportProcedure importProcedure;
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
