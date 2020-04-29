package no.systema.main.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The class is used for HAl-json unmarshalling since there are some problems with RestTemplate and Spring HateOAS in Spring 4.
 * It acts upon a String (hal-json-payload as String)
 * @author oscardelatorre
 * @date Apr 2020
 */
public class ObjectMapperHalJson {
	private String halJsonPayload;
	private String targetNode;
	
	public ObjectMapperHalJson(String halJsonPayload, String targetNode){
		this.halJsonPayload = halJsonPayload;
		this.targetNode = targetNode;
	}
	/**
	 * This method is used for deseralization
	 *  
	 * @param jsonToConvert
	 * @param halNode
	 * @return
	 * @throws Exception
	 */
	public ObjectMapper getObjectMapper (StringBuffer jsonToConvert) throws Exception{
		ObjectMapper om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        JsonNode jsNode = om.readTree(this.halJsonPayload);
        String targetStr = jsNode.at(this.targetNode).toString();
        jsonToConvert.append(targetStr);
        
        return om;
	}
}
