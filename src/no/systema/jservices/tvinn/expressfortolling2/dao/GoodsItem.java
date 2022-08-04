package no.systema.jservices.tvinn.expressfortolling2.dao;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GoodsItem {
	//Optional
	private String declarationGoodsItemNumber;
	//Optional
	private String transitGoodsItemNumber;
	//Optional
	private String typeOfGoods;
	//Mandatory*
	private ItemAmountInvoiced itemAmountInvoiced;
	//Mandatory*
	private String referenceNumberUCR;
	//Optional
	private List<AdditionalSupplyChainActor> additionalSupplyChainActor;
	//Mandatory*
	private Commodity commodity;
	//Mandatory*
	private CountryOfOrigin countryOfOrigin;
	//Mandatory*
	private List<Packaging> packaging;
	//Optional
	private List<PassiveTransportMeans> passiveTransportMeans;
	//Optional
	private List<TransportEquipment> transportEquipment;
	
}
