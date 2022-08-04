package no.systema.jservices.tvinn.expressfortolling2.dao;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Commodity {
	
	private String descriptionOfGoods;
	private String cusCode;
	private CommodityCode commodityCode;
	private List<DangerousGoods> dangerousGoods;
	private GoodsMeasure goodsMeasure;
		
}
