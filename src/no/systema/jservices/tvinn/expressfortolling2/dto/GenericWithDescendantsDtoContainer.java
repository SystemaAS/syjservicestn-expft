package no.systema.jservices.tvinn.expressfortolling2.dto;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * Container class for all the DTOs that return a json object
 * Since this DtoContainer is generic, there will be a price to pay further on when we marshall the JSON-Paylod
 * Refer to the specific mapper to see the extra implementation 
 * 
 * @author oscardelatorre
 * @date aug 2024
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericWithDescendantsDtoContainer {
	
	private String user = null;
	private String errMsg = "";
	private Object object;
}
