package no.systema.jservices.tvinn.digitoll.v2.services;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.systema.jservices.common.util.FileManager;
import no.systema.jservices.tvinn.digitoll.external.house.dao.MessageOutbound;
import no.systema.jservices.tvinn.digitoll.v2.dto.SadmocfDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.ApiMrnStatusRecordDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.GenericDtoResponseLight;
import no.systema.jservices.tvinn.kurermanifest.util.Utils;
import no.systema.main.service.UrlCgiProxyService;

@Service
public class RestSmtpService {
	private static final Logger logger = LoggerFactory.getLogger(RestSmtpService.class);
	
	@Value("${expft.file.log.service.root}")
    private String HTTP_ROOT_CGI;
	
	@Value("${expft.file.log.service.user}")
    private String USER_CGI;
	
	@Autowired
	UrlCgiProxyService urlCgiProxyService;
	
	/**
	 * 
	 * @param msg
	 * @param dtoConfig
	 * @param errMsg
	 * @return
	 */
	public boolean sendEmail(MessageOutbound msg, SadmocfDto dtoConfig, String errMsg){
		boolean retval = true;
		try{
			//https://gw.systema.no:65209/sycgip/TDIG005R.pgm?user=OSCAR&rcp=oscar.delatorre@wisetechglobal.com&subject=Tarzan&note=Jane&attach=/home/cb/tdig000r.txt
			
			if(this.USER_CGI!=null && StringUtils.isNotEmpty( dtoConfig.getEmail_adr()) ){
				String EMAIL_URL = this.HTTP_ROOT_CGI + "/sycgip/TDIG005R.pgm";
				//add URL-parameters
				StringBuffer urlRequestParams = new StringBuffer();
				urlRequestParams.append("user=" + this.USER_CGI);
				urlRequestParams.append("&rcp=" + dtoConfig.getEmail_adr());
				//Subject
				String subject = this.getSubject(msg);
				String urlEncodedSubject = this.encodeValue(subject);
				urlRequestParams.append("&subject=" + urlEncodedSubject);
				//Content
				String content = this.getNoteContent(msg);
				String urlEncodedContent = this.encodeValue(content);
				urlRequestParams.append("&note=" + urlEncodedContent);
				
				//session.setAttribute(TransportDispConstants.ACTIVE_URL_RPG_TRANSPORT_DISP, BASE_URL + "==>params: " + urlRequestParams.toString()); 
		    	logger.info(Calendar.getInstance().getTime() + " CGI-start timestamp");
		    	logger.warn("URL: " + EMAIL_URL);
		    	logger.warn("URL PARAMS: " + urlRequestParams);
		    	String jsonPayload = this.urlCgiProxyService.getJsonContent(EMAIL_URL, urlRequestParams.toString());
		    	//Debug --> 
		    	logger.info(Calendar.getInstance().getTime() +  " CGI-end timestamp");
		    	if(jsonPayload!=null){
		    		logger.info(jsonPayload);
		    		//to catch the errMsg, if any
		    		GenericDtoResponseLight obj = new ObjectMapper().readValue(jsonPayload, GenericDtoResponseLight.class);
		    		logger.debug(obj.toString());
		    		if(StringUtils.isNotEmpty(obj.getErrMsg())){
		    			logger.error("errMsg not empty ? ...");
		    			retval = false;
		    		}
		    	}
			}else{
				logger.error("rcp-param (email) or user-param to URL-service missing ? ...");
				retval = false;
			}
		}catch(Exception e){
			/*
			logger.error("ERROR on TRANSMISSION log on RPG-service: " + e.toString());
			String errorFileRenamed= "errorDbLog_" + Paths.get(fileName).getFileName().toString();
			try{
				//move the file and tag it as log-db-error. The file might have been deliver or not. This is just to tag the db-log function
				this.fileMgr.moveCopyFiles(fileName, errorDir, FileManager.COPY_FLAG, errorFileRenamed, FileManager.TIME_STAMP_SUFFIX_FLAG);
			}catch(Exception e2){
				e2.toString();
			}*/
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
	
	private String getNoteContent(MessageOutbound msg) throws Exception {
		String LINE_FEED = "\r\n";
		String retval = "";
		StringBuilder sb = new StringBuilder();
		//Transport MRN
		sb.append(msg.getNote() + LINE_FEED+ LINE_FEED);
		//Sender name and orgnr
		sb.append("SENDER-name:" + msg.getSender().getName() + LINE_FEED);
		sb.append("SENDER-orgnr:" + msg.getSender().getIdentificationNumber() + LINE_FEED);
		//Sender's Master Id
		sb.append("SENDER-MASTER-ID:" + msg.getConsignmentMasterLevel().getTransportDocumentMasterLevel().getDocumentNumber() + LINE_FEED);
		sb.append("SENDER-doc.Type:" + msg.getConsignmentMasterLevel().getTransportDocumentMasterLevel().getType() + LINE_FEED + LINE_FEED);
		//Transport data
		sb.append("CARRIER-orgnr:" + msg.getConsignmentMasterLevel().getCarrierIdentificationNumber() + LINE_FEED);
		sb.append("CARRIER-lorry reg.nr:" + msg.getActiveBorderTransportMeans().getIdentificationNumber() + LINE_FEED); 
		sb.append("CARRIER-lorry-country-code:" + msg.getActiveBorderTransportMeans().getCountryCode() + LINE_FEED);
		//ETA
		sb.append("CARRIER-ETA:" + msg.getEstimatedDateAndTimeOfArrival() + LINE_FEED);
		
		retval = sb.toString();
		
		return retval;
	}
	private String getSubject(MessageOutbound msg) throws Exception {
		String SEPARATOR = "-";
		String retval = "";
		StringBuilder sb = new StringBuilder();
		sb.append("DIGITOLL external-Ids " + msg.getSender().getName());
		//Transport MRN
		//sb.append(msg.getNote() );
		
		retval = sb.toString();
		
		return retval;
	}
}
