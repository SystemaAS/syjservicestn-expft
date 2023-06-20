package no.systema.jservices.tvinn.digitoll.v2.dao;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import no.systema.jservices.tvinn.digitoll.v2.dao.ReleasedConfirmation;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Representative {

	private String name;
	private String identificationNumber;
	private Address address;
	private List<Communication> communication;
	private List<ReleasedConfirmation> releasedConfirmation;
	
}
