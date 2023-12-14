package no.systema.jservices.tvinn.digitoll.v2.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SadmocfDto   {

	private String orgnr  = "";//      tegn      30     
	private String name  = "";//      tegn      30  
	private String commtype  = ""; //  tegn      10 (ftp, email, webserv)   
	

}
