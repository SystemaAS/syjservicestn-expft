package no.systema.main.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.*;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.systema.jservices.tvinn.expressfortolling.controller.ExpressFortollingController;

/**
 * The class is used for HAl-json unmarshalling since there are some problems with RestTemplate and Spring HateOAS in Spring 4.
 * It acts upon a String (hal-json-payload as String)
 * @author oscardelatorre
 * @date Apr 2020
 */
public class ObjectMapperHalJson {
	private static Logger logger = LoggerFactory.getLogger(ObjectMapperHalJson.class.getName());
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
	/**
	 * 
	 * @return
	 */
	public boolean isValidTargetNode(){
		boolean retval = true;
		try{
			ObjectMapper om = new ObjectMapper();
	        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
	        om.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true);
	        
	
	        JsonNode jsNode = om.readTree(this.halJsonPayload);
	        JsonNode tmp = jsNode.at(this.targetNode);
	        logger.warn(tmp.toString());
	        if(StringUtils.isEmpty(tmp.toString())){
	        	retval = false;
	        }else{
	        	logger.warn(tmp.toString());
	        }
	        
		}catch(Exception e){
			e.printStackTrace();
			retval = false;
		}
		return retval;
	}
}
