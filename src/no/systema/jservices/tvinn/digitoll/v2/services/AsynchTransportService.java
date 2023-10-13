package no.systema.jservices.tvinn.digitoll.v2.services;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.systema.jservices.common.dao.services.BridfDaoService;
import no.systema.jservices.tvinn.digitoll.v2.dao.Transport;
import no.systema.jservices.tvinn.digitoll.v2.dto.ApiRequestIdDto;
import no.systema.jservices.tvinn.digitoll.v2.dto.SadmotfDto;
import no.systema.jservices.tvinn.digitoll.v2.enums.EnumSadmotfStatus2;
import no.systema.jservices.tvinn.digitoll.v2.util.ApiAirRecognizer;
import no.systema.jservices.tvinn.digitoll.v2.util.PrettyLoggerOutputer;
import no.systema.jservices.tvinn.digitoll.v2.util.SadDigitollConstants;
import no.systema.jservices.tvinn.digitoll.v2.util.SadmologLogger;
import no.systema.jservices.tvinn.expressfortolling.api.ApiServices;
import no.systema.jservices.tvinn.expressfortolling.api.ApiServicesAir;
import no.systema.jservices.tvinn.expressfortolling2.dto.ApiMrnDto;
import no.systema.jservices.tvinn.expressfortolling2.dto.GenericDtoContainer;
import no.systema.jservices.tvinn.expressfortolling2.dto.GenericDtoResponse;
import no.systema.jservices.tvinn.expressfortolling2.util.GenericJsonStringPrinter;
import no.systema.jservices.tvinn.expressfortolling2.util.ServerRoot;


@Service
@EnableAsync
public class AsynchTransportService {
	private static final Logger logger = LoggerFactory.getLogger(AsynchTransportService.class);

	
	@Autowired
	private BridfDaoService bridfDaoService;	
	
	@Autowired
	private SadmotfService sadmotfService;
	@Autowired
	private SadmomfService sadmomfService;
	
	@Autowired
	private ApiServices apiServices; 
	
	@Autowired
	private ApiServicesAir apiServicesAir; 
	
	@Autowired
	private SadmologLogger sadmologLogger;	
	
	/**
	 * 
	 * @param serverRoot
	 * @param user
	 * @param etlnrt
	 * @param mrn
	 */
	@Async
	public void putTransportDigitollV2Test (String serverRoot, String user, String etlnrt, String mrn ) {
		
		GenericDtoResponse dtoResponse = new GenericDtoResponse();
		dtoResponse.setUser(user);
		dtoResponse.setEtlnrt(etlnrt);
		dtoResponse.setEllnrt(Integer.valueOf(etlnrt));//for log purposes only
		dtoResponse.setTdn("0"); //dummy (needed for db-log on table SADEXLOG)
		dtoResponse.setMrn(mrn);
		dtoResponse.setRequestMethodApi("PUT");
		boolean apiStatusAlreadyUpdated = false;
		boolean isApiAir = false;
		
		StringBuilder errMsg = new StringBuilder("ERROR ");
		String methodName = new Object() {}
	      .getClass()
	      .getEnclosingMethod()
	      .getName();
		
		logger.warn("Inside " + methodName + "- MRNnr: " + mrn );
		
		try {
			if(checkUser(user)) {
				logger.warn("user OK:" + user);
				List<SadmotfDto> list = sadmotfService.getSadmotfForUpdate(serverRoot, user, etlnrt, mrn);
				
				if(list != null && list.size()>0) {
					logger.warn("list size:" + list.size());
					
					for (SadmotfDto dto: list) {
						logger.warn(dto.toString());
						//get list of master for the mapping of some attributes
						dto.setMasterList(this.sadmomfService.getSadmomf(serverRoot, user, etlnrt)) ;
						//DEBUG
						//logger.info(dto.toString());
						
						//if(StringUtils.isNotEmpty(dto.getEtmid()) && StringUtils.isNotEmpty(dto.getEtuuid() )) {
						if( StringUtils.isNotEmpty(dto.getEtmid()) ) {
							Transport transport =  new MapperTransport().mapTransport(dto);
							logger.warn("Carrier name:" + transport.getCarrier().getName());
							//Debug
							logger.info(GenericJsonStringPrinter.debug(transport));
							//init response in case en ERROR occurs after apiSerivices...
							dtoResponse.setRequestId(dto.getEtuuid());
							
							//API - PROD
							Map tollTokenMap = new HashMap(); //will be populated within the put-method
							//API
							if(ApiAirRecognizer.isAir(dto.getEtktyp())) { isApiAir = true; }
							String json = "";
							if(isApiAir) {
								json = apiServicesAir.putTransportDigitollV2(transport, mrn, tollTokenMap);
							}else {
								json = apiServices.putTransportDigitollV2(transport, mrn, tollTokenMap);
							}
							//At this point we now have a valid tollToken to use
							
							ApiRequestIdDto obj = new ObjectMapper().readValue(json, ApiRequestIdDto.class);
							logger.warn("JSON = " + json);
							logger.warn("requestId = " + obj.getRequestId());
							
							//put in response
							dtoResponse.setRequestId(obj.getRequestId()); //update
							dtoResponse.setEtlnrt(String.valueOf(dto.getEtlnrt()));
							dtoResponse.setAvd(String.valueOf(dto.getEtavd()));
							dtoResponse.setPro(String.valueOf(dto.getEtpro()));
							
							//In case there was an error at end-point and the LRN was not returned
							if(StringUtils.isEmpty(obj.getRequestId())){
								errMsg.append("requestId empty ?? <json raw>: " + json);
								dtoResponse.setErrMsg(errMsg.toString());
								break;
								
							}else {
								//(1) we have the lrn at this point. We must go an API-round trip again to get the MRN
								String requestId = obj.getRequestId();
								dtoResponse.setRequestId(requestId);
								
								//(2)now we have the new lrn for the updated mrn so we proceed with the SADMOTF-update-lrn at Transport
								if(StringUtils.isNotEmpty(requestId) && StringUtils.isNotEmpty(mrn)) {
									String mode = "UL";
									dtoResponse.setMrn(mrn);
									//we must update the send date as well. Only 8-numbers
									String sendDate = transport.getDocumentIssueDate().replaceAll("-", "").substring(0,8);
									dtoResponse.setDocumentIssueDate(sendDate);//as aux for logging purposes
									
									List<SadmotfDto> xx = sadmotfService.updateLrnMrnSadmotf(serverRoot, user, dtoResponse, sendDate, mode);
									if(xx!=null && xx.size()>0) {
										for (SadmotfDto rec: xx) {
											//logger.warn(rec.toString());
											if(StringUtils.isNotEmpty(rec.getEtmid()) ){
												//OK
												apiStatusAlreadyUpdated = true;
											}else {
												errMsg.append("MRN empty after SADMOTF-update:" + mrn);
												dtoResponse.setErrMsg(errMsg.toString());
											}
										}
									}
									//(3) now we make a final check for LRN-status since there might have being some validation errors with the newly acquired LRN that did not appear when we 
									//first received the LRN in the first PUT Master
									
									//Delay 6-10 seconds (as in POST) needed to avoid ERROR 404 on client ...
									logger.warn(PrettyLoggerOutputer.FRAME);
									logger.warn("START of delay: "+ new Date());
									Thread.sleep(SadDigitollConstants.THREAD_DELAY_FOR_GET_MRN_MILLICSECONDS); 
									logger.warn("END of delay: "+ new Date());
									logger.warn(PrettyLoggerOutputer.FRAME);
									
									this.checkLrnValidationStatusTransportDigitollV2FromApi(dtoResponse, requestId, tollTokenMap, isApiAir);
									if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())){
										logger.warn("ERROR: " + dtoResponse.getErrMsg()  + methodName);
										//Update ehst2(SADMOTF) with ERROR = M
										dtoResponse.setDb_st2(EnumSadmotfStatus2.M.toString());
										List<SadmotfDto> tmp = sadmotfService.updateLrnMrnSadmotf(serverRoot, user, dtoResponse, sendDate, mode);
									}else {
										//OK
										logger.warn("RequestId status is OK ... (no errors)");
										dtoResponse.setDb_st2(EnumSadmotfStatus2.S.toString());
										List<SadmotfDto> tmp = sadmotfService.updateLrnMrnSadmotf(serverRoot, user, dtoResponse, sendDate, mode);
									}
									
								}else {
									errMsg.append("RequestId empty after PUT ??: " + "-->RequestId:" + requestId + " -->MRN from db(SADMOTF): " + mrn);
									dtoResponse.setErrMsg(errMsg.toString());
									break;
								}
							 
							  
							}
							break; //only first in list
							
							
						}else {
							errMsg.append(" LRN/MRN are empty. This operation is invalid. Make sure this fields have values before any PUT ");
							dtoResponse.setErrMsg(errMsg.toString());
						}
						
					}
				}else {
					errMsg.append(" no records to fetch from SADMOTF ");
					dtoResponse.setErrMsg(errMsg.toString());
				}
				
			}else {
				errMsg.append(" invalid user " + user + " " + methodName);
				dtoResponse.setErrMsg(errMsg.toString());
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			//Get out stackTrace to the response (errMsg)
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			dtoResponse.setErrMsg(sw.toString());
			dtoResponse.setDb_st2(EnumSadmotfStatus2.M.toString());
		
		}finally {
			
			if(!apiStatusAlreadyUpdated) {
				//check on status
				if(StringUtils.isNotEmpty(dtoResponse.getErrMsg())) {
					logger.info(SadDigitollConstants.LOG_PREFIX_LEGEND + dtoResponse.getErrMsg());
					dtoResponse.setDb_st2(EnumSadmotfStatus2.M.toString());
					logger.info("INSIDE setStatus:" + dtoResponse.getDb_st2());
					//
					List<SadmotfDto> xx = sadmotfService.updateLrnMrnSadmotf(serverRoot, user, dtoResponse, dtoResponse.getDocumentIssueDate(), "UL");
					logger.info("After update on status 2 (finally-clause)");
				}
			}
			//log in log file
			sadmologLogger.doLog(serverRoot, user, dtoResponse);
		}
		
	}
	
	
	
	private boolean checkUser(String user) {
		boolean retval = true;
		if (!bridfDaoService.userNameExist(user)) {
			retval = false;
			//throw new RuntimeException("ERROR: parameter, user, is not valid!");
		}
		return retval;
	}
	
	
	private String checkLrnValidationStatusTransportDigitollV2FromApi(GenericDtoResponse dtoResponse, String lrn, Map tollTokenMap, boolean isApiAir) {
		
		String retval = "";
		
		try{
			String json = "";	
			if(isApiAir) {
				json = apiServicesAir.getValidationStatusTransportDigitollV2(lrn, tollTokenMap );
			}else {
				json = apiServices.getValidationStatusTransportDigitollV2(lrn, tollTokenMap );
			}
				
			ApiMrnDto obj = new ObjectMapper().readValue(json, ApiMrnDto.class);
			logger.warn("JSON = " + json);
			logger.warn("Status = " + obj.getStatus());
			logger.warn("requestID = " + obj.getRequestId());
			logger.warn("notificationDate = " + obj.getNotificationDate());
			if(obj.getValidationErrorList()!=null) {
				logger.warn("validationErrorList = " + obj.getValidationErrorList().toString());
				logger.warn("validationErrorList.length = " + obj.getValidationErrorList().length);
			}
			//
			dtoResponse.setStatusApi(obj.getStatus());
			dtoResponse.setTimestamp(obj.getNotificationDate());
			
			//check if any error to deserialize
			if(obj.getValidationErrorList()!=null && obj.getValidationErrorList().length > 0) {
				//logger.warn("AA");
				StringBuilder sbError = new StringBuilder();
				for( int i = 0; i < obj.getValidationErrorList().length; i++) {
					//logger.warn("BB");
					Map map = (Map)obj.getValidationErrorList()[i];
					logger.warn("Description:" + (String)map.get("description"));
					//error txt
					String errorTxt = obj.getValidationErrorList()[i].toString();
					logger.warn("###:" + errorTxt);
					sbError.append(errorTxt);
					dtoResponse.setErrMsg(sbError.toString());
				}
			}
			
		}catch(Exception e) {
			//e.printStackTrace();
			//Get out stackTrace to the response (errMsg)
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			dtoResponse.setErrMsg(sw.toString());
			
		}
		
		return retval;
	}
	

}
