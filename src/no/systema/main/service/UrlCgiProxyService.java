/**
 * 
 */
package no.systema.main.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
//java net
import java.net.URL;
import java.net.URLConnection;

import org.apache.logging.log4j.*;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import no.systema.main.util.EncodingTransformer;
/**
 * 
 * @author oscardelatorre
 * @date 2020, Mars
 * 
 */
@Service
public class UrlCgiProxyService{
	private static Logger logger = LogManager.getLogger(UrlCgiProxyService.class.getName());
	private static final String ENCODING_JSON_UTF8 = "UTF8";
	private static final String ENCODING_STREAMS_UTF8 = "UTF-8";

	
	
	/**
	 * Returns a content JSON-payload from a http request via POST
	 * 
	 * @param urlStr
	 * @param urlParameters
	 * @return
	 */
	public String getJsonContent(String urlStr, String urlParameters) throws Exception{
		StringBuffer buffer = new StringBuffer();
		String utfPayload = null;
		
		try{
			
			URL url = new URL(urlStr);
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			//Open writer
			OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), ENCODING_STREAMS_UTF8);
			writer.write(urlParameters);
			writer.flush();
			//Open reader
			String line = "";
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), ENCODING_STREAMS_UTF8));
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
				//logger.info(line);
			}
			writer.close();
			reader.close(); 
			
			EncodingTransformer transformer = new EncodingTransformer();
			utfPayload = transformer.transformToJSONTargetEncoding(buffer.toString(), UrlCgiProxyService.ENCODING_JSON_UTF8);
			//logger.info(utfPayload);
			
		}catch(Exception e){
    		//e.printStackTrace();
    		//logger.info("ERROR: " +  e.toString());
			throw e;
    		
    	}
		return utfPayload;
	}
	
	/**
	 * This method converts any JSON string to a VALID JSON UTF-8 String...
	 * 
	 * @param jsonPayloadOriginal
	 * @return
	 */
	public String getJsonContentFromJsonRawString(String jsonPayloadOriginal){
		String utfPayload = null;
		
		try{
			//JSON automatic converstion requires UTF8 in order to work. We must convert all responses to UTF8
			EncodingTransformer transformer = new EncodingTransformer();
			utfPayload = transformer.transformToJSONTargetEncoding(jsonPayloadOriginal, UrlCgiProxyService.ENCODING_JSON_UTF8);
			
    	}catch(Exception e){
    		e.printStackTrace();
    		logger.info("Error:", e);
    	}
		
		return utfPayload;
	}
	
	
}
