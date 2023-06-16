package no.systema.jservices.tvinn.digitoll.v2.dao;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Consignor {

	//Mandatory
		private String name;
		//Mandatory
		private String identificationNumber;
		//Mandatory
		//1 → En fysisk person 2 → En juridisk person, det vil si en bedrift 3 → En samling personer
		private Integer typeOfPerson = 2; 
		
		//Optional
		private Address address;
		//Optional
		private List<Communication> communication;
	
}
