package no.systema.jservices.tvinn.expressfortolling.api;

import lombok.Data;

@Data
public class TokenResponseDto {
	private String access_token;
	/*In millis*/
	private int expires_in;
	private String scope;
}
