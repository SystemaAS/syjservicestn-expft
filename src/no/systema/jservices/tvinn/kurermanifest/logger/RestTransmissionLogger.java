package no.systema.jservices.tvinn.kurermanifest.logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Calendar;

import org.apache.logging.log4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import no.systema.jservices.common.util.FileManager;
import no.systema.jservices.tvinn.kurermanifest.util.Utils;
import no.systema.main.service.UrlCgiProxyService;

@Service
public class RestTransmissionLogger {
	private static final Logger logger = LogManager.getLogger(RestTransmissionLogger.class);
	private FileManager fileMgr = new FileManager();
	
	@Value("${kurer.file.log.service.root}")
    private String HTTP_ROOT_CGI;
	
	@Value("${kurer.file.log.service.user}")
    private String USER_CGI;
	
	@Autowired
	UrlCgiProxyService urlCgiProxyService;
	
	/**
	 * log on database through some service. Send a copy to the errorDir if the log fails
	 * 
	 * @param fileName
	 * @param errorDir
	 * @param errorCode
	 * @param errMsg
	 * 
	 * @return
	 */
	public boolean logTransmission(String fileName, String errorDir, String errorCode, String errMsg){
		boolean retval = true;
		try{
			String uuid = new Utils().getUUID(fileName);
			
			if(uuid!=null && this.USER_CGI!=null){
				//http://10.13.3.22/sycgip/sad115r.pgm?user=YBC&uuid=0d2010a8-a777-4eeb-b653-e174f63b7f62(&error=4xx)(&errortxt={json}
				String LOG_URL = this.HTTP_ROOT_CGI + "/sycgip/sad115r.pgm";
				
				//add URL-parameters
				StringBuffer urlRequestParams = new StringBuffer();
				urlRequestParams.append("user=" + this.USER_CGI);
				urlRequestParams.append("&uuid=" + uuid);
				if(errorCode!=null){
					urlRequestParams.append("&error=" + errorCode);
					urlRequestParams.append("&errortxt=");
					if(errMsg!=null){
						urlRequestParams.append(this.encodeValue(errMsg));
					}
				}
				
				//session.setAttribute(TransportDispConstants.ACTIVE_URL_RPG_TRANSPORT_DISP, BASE_URL + "==>params: " + urlRequestParams.toString()); 
		    	logger.info(Calendar.getInstance().getTime() + " CGI-start timestamp");
		    	logger.info("URL: " + LOG_URL);
		    	logger.info("URL PARAMS: " + urlRequestParams);
		    	String jsonPayload = this.urlCgiProxyService.getJsonContent(LOG_URL, urlRequestParams.toString());
		    	//Debug --> 
		    	logger.info(Calendar.getInstance().getTime() +  " CGI-end timestamp");
		    	if(jsonPayload!=null){
		    		logger.info(jsonPayload);
		    	}
			}else{
				logger.fatal("uuid-param or user-param to URL-service missing ? ...");
			}
		}catch(Exception e){
			logger.error("ERROR on TRANSMISSION log on RPG-service: " + e.toString());
			String errorFileRenamed= "errorDbLog_" + Paths.get(fileName).getFileName().toString();
			try{
				//move the file and tag it as log-db-error. The file might have been deliver or not. This is just to tag the db-log function
				this.fileMgr.moveCopyFiles(fileName, errorDir, FileManager.COPY_FLAG, errorFileRenamed, FileManager.TIME_STAMP_SUFFIX_FLAG);
			}catch(Exception e2){
				e2.toString();
			}
			retval = false;
		}
		return retval;
	}
	
	/**
	 * URL-encoding
	 * @param value
	 * @return
	 */
	private  String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }
}
