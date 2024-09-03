package no.systema.jservices.tvinn.digitoll.v2.dto;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

//log data to FTP for främmande houses: 
@Data
@JsonIgnoreProperties(ignoreUnknown = true)

public class ZadmomlfDto{

	private String emdkm = ""; //TEGN 50 master doc id
	private String emdkmt = ""; //TEGN 4 master doc type
	private String empro  = "" ;  //TEGN 20 (including AVD-TUR)
	
	private String avsna = ""; //TEGN 50 sender name
	private String avsid = ""; //TEGN 20 sender Orgnr
	private String motna = ""; //TEGN 50 receiver name
	private String motid = ""; //TEGN 20 receiver Orgnr		
	
	private String trreforg = ""; //TEGN 50 carrier Orgnr	
	private String trrefreg = ""; //TEGN 50 carrier regnr	
	private Integer date = 0; //TEGN 8 
	private Integer time = 0; //TEGN 6 
	private String custoff = ""; //TEGN 35
	
}
