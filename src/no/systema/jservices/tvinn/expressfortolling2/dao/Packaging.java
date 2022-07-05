package no.systema.jservices.tvinn.expressfortolling2.dao;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Packaging {
	
	private String shippingMarks = "";
	private Integer numberOfPackages = 0;
	private String typeOfPackages = "";
	

}
