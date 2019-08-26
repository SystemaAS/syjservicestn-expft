package no.systema.jservices.tvinn.expressfortolling;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApiServices {

	@Autowired
    private ApiClient apiClient;


	/**
	 * Get all transportation companies for which the logged in user is a customs representative.
	 * GET /api/exf/ekspressfortolling/transportation-company
	 * Accept: application/json
	 * Host: ekspressfortolling.toll.no
	 * 
	 * @return TODO
	 */
	public String getTransportationCompany() {
		String path = "/api/exf/ekspressfortolling/transportation-company";
		//TODO replace with uribuilder
		
//		return apiClient.invokeAPI(path, method, queryParams, body, headerParams, formParams, accept, contentType, returnType)
		
		return null;
		
		
	}
	
	
}
