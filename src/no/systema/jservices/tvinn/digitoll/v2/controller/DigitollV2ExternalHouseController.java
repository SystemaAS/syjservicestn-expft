package no.systema.jservices.tvinn.digitoll.v2.controller;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import no.systema.jservices.common.dao.services.BridfDaoService;
import no.systema.jservices.tvinn.digitoll.external.house.FilenameService;
import no.systema.jservices.tvinn.digitoll.external.house.MapperMessageOutbound;
import no.systema.jservices.tvinn.digitoll.external.house.dao.MessageOutbound;
import no.systema.jservices.tvinn.digitoll.v2.dto.SadmomfDto;
import no.systema.jservices.tvinn.digitoll.v2.services.SadmomfService;
import no.systema.jservices.tvinn.digitoll.v2.services.SadmotfService;
import no.systema.jservices.tvinn.expressfortolling2.util.ServerRoot;


/**
 * This handles all outbound communication to an external partner when the UseCase is = "send master id to an external party"
 * (The external party will then send its own houses with the carrier's master id)
 * 
 * @author oscardelatorre
 * @date Dec 2023
 */

@RestController
public class DigitollV2ExternalHouseController {
	private static Logger logger = LoggerFactory.getLogger(DigitollV2ExternalHouseController.class.getName());
	

	@Autowired
	private BridfDaoService bridfDaoService;	
	
	@Autowired
	private SadmomfService sadmomfService;
	@Autowired
	private SadmotfService sadmotfService;
	
	@Autowired
	private FilenameService filenameService;
	
	/**
	 * 
	 * @param request
	 * @param applicationUser
	 * @param emlnrt
	 * @param emlnrm
	 * @param orgNr
	 * @return
	 */
	@RequestMapping(value = "/digitollv2/send_masterId_toExternalParty.do", method = {RequestMethod.GET, RequestMethod.POST})
	  public @ResponseBody String sendMasterIdToPart(HttpServletRequest request, @RequestParam String user, @RequestParam String emlnrt,
			  						@RequestParam String emlnrm, @RequestParam String receiverName, @RequestParam String receiverOrgnr ) {
		
		  String serverRoot = ServerRoot.getServerRoot(request);
		  String result = "";
		  logger.info("Inside sendMasterIdToPart");
		  logger.info("emlnrt:" + emlnrt);
		  logger.info("emlnrm:" + emlnrm);
		  logger.info("file-receiver name:" + receiverName);
		  logger.info("file-receiver orgNr:" + receiverOrgnr);
		  
		  try {
			  if(StringUtils.isNotEmpty(receiverName) && StringUtils.isNotEmpty(receiverOrgnr) && StringUtils.isNotEmpty(emlnrt) && StringUtils.isNotEmpty(emlnrm)) {
				  //(1) get the master Dao record from Db
				  List<SadmomfDto> list = sadmomfService.getSadmomf(serverRoot, user, emlnrt, emlnrm);
				  for (SadmomfDto masterDto: list) {
					  masterDto.setTransportDto(sadmotfService.getSadmotfDto(serverRoot, user, emlnrt));
					  logger.trace(masterDto.toString());
					  //(2) Map to MessageOutbound
					  MessageOutbound msg = new MapperMessageOutbound().mapMessageOutbound(masterDto, receiverName, receiverOrgnr);
					  ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
					  String json = ow.writeValueAsString(msg);
					  logger.info(json);
					  
					  //
					  filenameService.writeToDisk(msg);
					  
					  
					  /*
					  //(3) check what type of communication channel (FTP or email)
					  if(true) {
						  //(3.1) write to file (if needed)
						  filenameService.writeToDisk(msg);
					  }else {
						  //TODO web-services
					  }
					  */
					  break; //Only first record in the list 
				  }
			  }
		  }catch(Exception e) {
			  logger.error(e.toString());
			  result = "ERROR";
		  }
	      //
		  if(result.isEmpty()) {
			  result = "OK";
		  }
		  
		  return result;
		  
	  }
	
	
	
}
