package no.systema.jservices.tvinn.expressfortolling2.dao;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransportCharges {
	
	private String methodOfPayment = "";
	private String currency = "";
	private Double value = 0.00;
	

}
