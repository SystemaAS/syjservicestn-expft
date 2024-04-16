package no.systema.jservices.tvinn.digitoll.v2.dto;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

//log data to FTP for främmande houses: 
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SadmolffDto {

	private String status = ""; //TEGN 20 status (send, confirmed, dialog, receipt, ok) 
	private String uuid = ""; //TEGN 36 messageId
	private String emdkm = ""; //TEGN 50 master doc id
	private String emlnrt  = "" ;  //TEGN   7 
	private String avsid = ""; //TEGN 20 sender Orgnr
	private String motid = ""; //TEGN 20 receiver Orgnr		
	
	private Integer date = 0; //numeric 8 (SONET 8,0)
	private Integer time = 0; //numeric 6 (SONET 6,0)
	
}
