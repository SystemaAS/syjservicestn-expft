package no.systema.jservices.tvinn.digitoll.external.house;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import no.systema.jservices.tvinn.digitoll.external.house.dao.MessageOutbound;

@Service
public class FilenameService {
	private static Logger logger = LoggerFactory.getLogger(FilenameService.class.getName());
	
	@Value("${expft.external.house.file.outbound.prefix}")
	private String filePrefix;
	
	@Value("${expft.external.house.file.outbound.suffix.timestamp.mask}")
	private String fileTimestampMask;
	
	@Value("${expft.external.house.file.outbound.type}")
	private String fileType;
	
	@Value("${expft.external.house.file.outbound.output.dir.edi}")
	private String fileOutboundDir;
	
	
	
	/**
	 * 
	 * @param msg
	 * @return
	 */
	public String writeToString(MessageOutbound msg) {
		String retval = "";
		ObjectWriter ow = new ObjectMapper().writer();
		
		try {
			retval = ow.writeValueAsString(msg);
			
		}catch(Exception e) {
			logger.error(e.toString());
		}
		return retval;
	}
	/**
	 * 
	 * @param msg
	 * @return
	 */
	public String getFileNameXml(MessageOutbound msg) {
		String fileTypeXml = ".xml";
		String retval = "";
		try {
			  
			Calendar now = Calendar.getInstance();
			SimpleDateFormat formatter = new SimpleDateFormat(this.fileTimestampMask);
			String suffix = formatter.format(now.getTime()) ;
			  
			//String sFile = this.fileOutboundDir + this.filePrefix + suffix + fileTypeXml;
			String sFile = this.fileOutboundDir + msg.getSender().getIdentificationNumber() + "_" + suffix + fileTypeXml;
			logger.info(sFile);
			
			retval = sFile;
			
		}catch(Exception e) {
			logger.error(e.toString());
		}
		return retval;
	}
}
