package no.systema.jservices.tvinn.expressfortolling2.dto;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

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
	private String status = "";
	private String db_st = "";
	private String db_st2 = "";
	private String db_st3 = "";
	
	private String errMsg = "";
	
	private List<Object> list = new ArrayList<Object>();
	
}
