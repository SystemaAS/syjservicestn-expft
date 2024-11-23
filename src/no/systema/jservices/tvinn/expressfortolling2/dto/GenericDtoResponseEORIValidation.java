package no.systema.jservices.tvinn.expressfortolling2.dto;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import no.systema.jservices.tvinn.digitoll.entry.road.EntryMovRoadDto;
import no.systema.jservices.tvinn.digitoll.v2.dto.routing.EntryRoutingDto;

@Data
/**
 * Container class for all the DTOs that return a json with a list
 * Since this DtoContainer is generic, there will be a price to pay further on when we marshall the JSON-Paylod
 * Refer to the specific mapper to see the extra implementation 
 * 
 * @author oscardelatorre
 * @date Nov 2024
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericDtoResponseEORIValidation {
	
	private String user = null;
	private String eori = null;
	private String errMsg = "";
	//
	private List<Object> list = new ArrayList<Object>();
	
	
}
