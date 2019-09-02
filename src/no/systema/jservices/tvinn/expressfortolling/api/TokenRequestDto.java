package no.systema.jservices.tvinn.expressfortolling.api;

import lombok.Data;

/**
* grant_type, MANDATORY, "urn:ietf:params:oauth:grant-type:jwt-bearer <br>
* assertion, MANDATORY, Den genererte JWT’en for token-requesten <br>
*/
@Data
public class TokenRequestDto {
	private String grantType = "urn:ietf:params:oauth:grant-type:jwt-bearer";
	private String assertion;
}
