package no.systema.jservices.tvinn.digitoll.external.house;

import java.io.File;
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
	
	@Value("${expft.external.house.file.outbound.output.dir.edi.backup}")
	private String fileOutboundDirBackup;
	
	
	
	/**
	 * Serializes a file in case we must send via FTP
	 * @param msg
	 */
	public void writeToDisk(MessageOutbound msg) {
		ObjectWriter ow = new ObjectMapper().writer();
		
		try {
			  
			Calendar now = Calendar.getInstance();
			SimpleDateFormat formatter = new SimpleDateFormat(this.fileTimestampMask);
			String suffix = formatter.format(now.getTime()) + "_" + msg.getDocumentID() ;
			  
			String sFile = this.fileOutboundDir + this.filePrefix + suffix + this.fileType;
			String sFileBup = this.fileOutboundDir + fileOutboundDirBackup + this.filePrefix + suffix + this.fileType;
			logger.info(sFile);
			ow.writeValue(new File(sFile), msg);
			ow.writeValue(new File(sFileBup), msg);
		}catch(Exception e) {
			logger.error(e.toString());
		}
	}
	
}
