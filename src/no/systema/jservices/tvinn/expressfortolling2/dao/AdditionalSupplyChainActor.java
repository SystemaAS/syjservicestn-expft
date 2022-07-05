package no.systema.jservices.tvinn.expressfortolling2.dao;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdditionalSupplyChainActor {
	
	private String identificationNumber = "";
	private String role = "";
	

}
