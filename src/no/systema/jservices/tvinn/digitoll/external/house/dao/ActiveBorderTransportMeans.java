package no.systema.jservices.tvinn.digitoll.external.house.dao;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActiveBorderTransportMeans {
	private String identificationNumber;
	private String typeOfIdentification;
	private String countryCode;
	private String modeOfTransportCode;
}
