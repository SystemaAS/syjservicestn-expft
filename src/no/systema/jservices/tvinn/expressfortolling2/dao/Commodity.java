package no.systema.jservices.tvinn.expressfortolling2.dao;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Commodity {
	
	private String descriptionOfGoods = "";
	private String cusCode = "";
	private CommodityCode commodityCode;
	private List<DangerousGoods> dangerousGoods;
	private GoodsMeasure goodsMeasure;
		
}
