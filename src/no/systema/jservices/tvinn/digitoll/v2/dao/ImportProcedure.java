package no.systema.jservices.tvinn.digitoll.v2.dao;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImportProcedure {
	
	private String importProcedure;
	private String outgoingProcedure;
	private String hasOutgoingProcedure;

}
