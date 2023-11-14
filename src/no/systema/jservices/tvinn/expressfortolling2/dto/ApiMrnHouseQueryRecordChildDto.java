package no.systema.jservices.tvinn.expressfortolling2.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiMrnHouseQueryRecordChildDto {
	private String description;
	private ApiMrnHouseQueryRecordChildPointerDto pointer;
	
	
}
