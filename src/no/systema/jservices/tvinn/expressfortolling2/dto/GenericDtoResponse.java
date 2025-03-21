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
 * @date aug 2022
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericDtoResponse {
	
	private String user = null;
	private String avd = null;
	private String pro = null;
	private String tdn = "";
	private String lrn = "";
	private String mrn = "";
	private String statusApi = "";
	private String requestMethodApi = "";
	private String requestId = "";
	//
	private String db_st = "";
	private String db_st2 = "";
	private String db_st3 = "";
	
	private String timestamp = "";
	private String errMsg = "";
	//transport
	private String etlnrt = "";
	//master
	private String emlnrt = "";
	private String emlnrm = "";
	//house
	private String ehlnrt = "";
	private String ehlnrm = "";
	private String ehlnrh = "";
	//log
	private Integer ellnrt = 0;
	private Integer ellnrm = 0;
	private Integer ellnrh = 0;
	//aux
	private String documentIssueDate = "0";
	//incomplete documentation
	private String incompleteDocumentationString = "";
	//
	//
	private List<Object> list = new ArrayList<Object>();
	//for entry movement road
	private EntryMovRoadDto entryMovementRoad = new EntryMovRoadDto();
	//for routing - entry
	private List<EntryRoutingDto> entryList = new ArrayList<EntryRoutingDto>();
	
	//for eoriValidation
	private ApiMrnEoriValidationDto eoriValidation;
}
