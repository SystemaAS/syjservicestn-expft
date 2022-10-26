package no.systema.jservices.tvinn.expressfortolling2.util;


import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import no.systema.jservices.common.util.DateTimeManager;
import no.systema.jservices.tvinn.expressfortolling2.dto.GenericDtoResponse;
import no.systema.jservices.tvinn.expressfortolling2.dto.SadexlogDto;
import no.systema.jservices.tvinn.expressfortolling2.services.SadexlogService;

@Component
public class SadexlogLogger {
	private Logger logger = LoggerFactory.getLogger(SadexlogLogger.class.getName());
	private String MODE_ADD = "A";
	
	
	@Autowired
	SadexlogService sadexlogService;
	
	public void doLog(String serverRoot, String user, GenericDtoResponse dtoResponse){
		
		try {
			logger.warn("Inside doLog ...");
			SadexlogDto dto = new SadexlogDto();
			dto.setElavd(Integer.valueOf(dtoResponse.getAvd()));
			dto.setElpro(Integer.valueOf(dtoResponse.getPro()));
			dto.setEltdn(Integer.valueOf(dtoResponse.getTdn()));
			dto.setEldate(getLogDate(dtoResponse.getTimestamp()));
			dto.setEltime(getLogTime(dtoResponse.getTimestamp()));
			dto.setEltyp(getLogTyp(dtoResponse.getErrMsg()));
			if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())) {
				dto.setElltxt(getLogText(dtoResponse.getErrMsg()));
			}else {
				dto.setElltxt(dtoResponse.getStatusApi() + " " + dtoResponse.getRequestMethodApi());
			}
			//create error record
			logger.warn("Record to log:" + dto.toString());
			if(StringUtils.isNotEmpty(serverRoot) && StringUtils.isNotEmpty(user)) {
				sadexlogService.insertLogRecord(serverRoot, user, dto, MODE_ADD);
			}else {
				logger.error("Mandatory fields for SADEXLOG record missing ... ?");
			}
			
		}catch(Exception e) {
			//Get out stackTrace to the response (errMsg)
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			dtoResponse.setErrMsg(sw.toString());
			//log to std output and db
			logger.error(dtoResponse.getErrMsg());
			
		}
	}
	/**
	 * 
	 * @param value
	 * @return
	 */
	private String getLogTyp(String errMsg) throws Exception {
		String retval = "INFO";
		if(StringUtils.isNotEmpty(errMsg)) {
			retval = "ERROR";
		}
		return retval;
	}
	/**
	 * 
	 * @param value
	 * @return
	 */
	private Integer getLogDate(String value) throws Exception {
		Integer retval = 0;
		//valid: 2022-10-25T07:34:49Z
		if(StringUtils.isNotEmpty(value) && value.contains("T")) {
			String tmpDate = value.substring(0,10);
			tmpDate = tmpDate.replaceAll("-", "");
			retval = Integer.valueOf(tmpDate);
		}else {
			//make it yourself
			retval = Integer.valueOf(new DateTimeManager().getCurrentDate_ISO("yyyyMMdd"));
		}
		return retval;
	}
	/**
	 * 
	 * @param value
	 * @return
	 */
	private Integer getLogTime(String value) throws Exception {
		Integer retval = 0;
		//valid: 2022-10-25T07:34:49Z
		if(StringUtils.isNotEmpty(value) && value.contains("T") && value.length()>=19) {
			String tmpDate = value.substring(11, 19);
			tmpDate = tmpDate.replaceAll(":", "");
			
			retval = Integer.valueOf(tmpDate);
			
		}else {
			//make it yourself
			retval = Integer.valueOf(new DateTimeManager().getCurrentDate_ISO("HHmmss"));
		}
		return retval;
	}
	/**
	 * 
	 * @param value
	 * @return
	 */
	private String getLogText(String value) throws Exception {
		final Integer MAX_CHARS = 1020;
		String retval = value;
		if(StringUtils.isNotEmpty(value)) {
			if(value.length() > MAX_CHARS) {
				retval = value.substring(0,MAX_CHARS);
			}
		}
		return retval;
	}
	
}
