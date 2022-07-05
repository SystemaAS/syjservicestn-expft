package no.systema.jservices.tvinn.expressfortolling2.dao;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Representative {

	private String name = "";
	private String identificationNumber = "";
	private String status = "";
	private Address address;
	private List<Communication> communication;
	
}
