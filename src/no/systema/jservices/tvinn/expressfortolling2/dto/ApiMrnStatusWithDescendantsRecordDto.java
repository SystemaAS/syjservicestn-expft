package no.systema.jservices.tvinn.expressfortolling2.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiMrnStatusWithDescendantsRecordDto {
	private String sumOfWeightForMasterConsignments;
	private String sumOfWeightForHouseConsignments;
	
	private List<MasterConsignments> masterConsignments;
	
}
