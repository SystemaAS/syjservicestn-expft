package no.systema.jservices.tvinn.digitoll.external.house.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.util.NullableUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import no.systema.jservices.common.dao.services.BridfDaoService;
import no.systema.jservices.tvinn.digitoll.external.house.EvryXmlWriterService;
import no.systema.jservices.tvinn.digitoll.external.house.FilenameService;
import no.systema.jservices.tvinn.digitoll.external.house.JsonWriterService;
import no.systema.jservices.tvinn.digitoll.external.house.MapperMessageOutbound;
import no.systema.jservices.tvinn.digitoll.external.house.PeppolXmlWriterService;
import no.systema.jservices.tvinn.digitoll.external.house.PeppolXmlWriterService_TransportExecutionPlanRequest;
import no.systema.jservices.tvinn.digitoll.external.house.dao.MessageOutbound;
import no.systema.jservices.tvinn.digitoll.v2.dto.SadmocfDto;
import no.systema.jservices.tvinn.digitoll.v2.dto.SadmohfDto;
import no.systema.jservices.tvinn.digitoll.v2.dto.SadmolffDto;
import no.systema.jservices.tvinn.digitoll.v2.dto.SadmolhffDto;
import no.systema.jservices.tvinn.digitoll.v2.dto.SadmomfDto;
import no.systema.jservices.tvinn.digitoll.v2.enums.EnumSadmocfCommtype;
import no.systema.jservices.tvinn.digitoll.v2.enums.EnumSadmocfFormat;
import no.systema.jservices.tvinn.digitoll.v2.enums.EnumSadmolffStatus;
import no.systema.jservices.tvinn.digitoll.v2.services.SadmocfService;
import no.systema.jservices.tvinn.digitoll.v2.services.SadmohfService;
import no.systema.jservices.tvinn.digitoll.v2.services.SadmolffService;
import no.systema.jservices.tvinn.digitoll.v2.services.SadmolhffService;
import no.systema.jservices.tvinn.digitoll.v2.services.SadmomfService;
import no.systema.jservices.tvinn.digitoll.v2.services.SadmotfService;
import no.systema.jservices.tvinn.digitoll.v2.services.ZadmomlfService;
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
	private static String CHANNEL_PEPPOL = "peppol";
	private static String CHANNEL_EVRY = "evry";

	@Autowired
	private BridfDaoService bridfDaoService;	
	
	@Autowired
	private SadmohfService sadmohfService;
	@Autowired
	private SadmomfService sadmomfService;
	@Autowired
	private SadmotfService sadmotfService;
	
	@Autowired
	private SadmocfService sadmocfService;
	@Autowired
	private SadmolffService sadmolffService;
	@Autowired
	private SadmolhffService sadmolhffService;
	@Autowired
	private ZadmomlfService zadmomlfService;
	
	@Autowired
	private FilenameService filenameService;
	
	@Autowired
	private PeppolXmlWriterService peppolXmlWriterService;
	@Autowired
	private PeppolXmlWriterService_TransportExecutionPlanRequest peppolXmlWriterService_TransExecPlanRequest;
	
	@Autowired
	private EvryXmlWriterService evryXmlWriterService;
	
	@Autowired
	private JsonWriterService jsonWriterService;
	
	
	@Value("${expft.external.house.spec.version}")
	private String specVersion;
	
	@Value("${expft.dir.exthouse.attachm.upload}")
	private String attachmentsPath;
	
	/**
	 * This method delivers a serialize file.
	 * The method deals with the construction of the correct file to deliver to an external system.
	 * The actual sending does not takes place here (either FTP, WS or EMAIL are the responsibility of another system)
	 * 
	 * The db-table with configuration parameters is: SADMOCF
	 * 
	 * The carrier sends the masterId to an external product owner.
	 * @param request
	 * @param user
	 * @param emlnrt
	 * @param emlnrm
	 * @param receiverName
	 * @param receiverOrgnr
	 * @param attachmentsExist
	 * 
	 * @return
	 * 
	 * 
	 */
	@RequestMapping(value = "/digitollv2/send_masterId_toExternalParty.do", method = {RequestMethod.GET, RequestMethod.POST})
	  public @ResponseBody String sendMasterIdToPart(HttpServletRequest request, @RequestParam String user, @RequestParam String emlnrt,
			  						@RequestParam String emlnrm, @RequestParam String receiverName, @RequestParam String receiverOrgnr, 
			  						@RequestParam Boolean attachmentsExist ) {
		
		  String serverRoot = ServerRoot.getServerRoot(request);
		  StringBuilder result = new StringBuilder();
		  
		  logger.info("Inside sendMasterIdToPart");
		  logger.info("emlnrt:" + emlnrt);
		  logger.info("emlnrm:" + emlnrm);
		  logger.info("file-receiver name:" + receiverName);
		  logger.info("file-receiver orgNr:" + receiverOrgnr);
		  logger.info("attachmentsExist:" + attachmentsExist); //in case there are pdf:s (ZH or other) to send to the receiver
		  
		  try {
			  if(StringUtils.isNotEmpty(receiverName) && StringUtils.isNotEmpty(receiverOrgnr) && StringUtils.isNotEmpty(emlnrt) && StringUtils.isNotEmpty(emlnrm)) {
				  //(0) check if this party exists in the SADMOCF-db-table (in order to know the format type (json or xml-peppol)
				  SadmocfDto dtoConfig = new SadmocfDto();
				  if(partyExists(serverRoot, user, receiverOrgnr, receiverName, dtoConfig)) {
					  if(dtoConfig!=null) {
						  //(1) get the master Dao record from Db (ALWAYS no matter what format)
						  List<SadmomfDto> list = sadmomfService.getSadmomf(serverRoot, user, emlnrt, emlnrm);
						  for (SadmomfDto masterDto: list) {
							  masterDto.setTransportDto(sadmotfService.getSadmotfDto(serverRoot, user, emlnrt));
							  logger.trace(masterDto.toString());
							  //(2) Map to MessageOutbound including attachments (if applicable)
							  MessageOutbound msg = new MapperMessageOutbound(this.specVersion).mapMessageOutbound(masterDto, receiverName, receiverOrgnr, attachmentsExist, attachmentsPath);
							  ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
							  String json = ow.writeValueAsString(msg);
							  if(json!=null && json.length()>=2000) {
								  logger.info(json.substring(0,2000));
							  }else {
								  logger.info(json);
							  }
							 
							  logger.info(dtoConfig.toString());  
							  //(3) check what format to serialize (xml or json)
							  if(dtoConfig.getFormat().equalsIgnoreCase(EnumSadmocfFormat.xml.toString())) {
								  //(3.1) wrap json in correct xml format (peppol will act as an envelope...)
								  if(dtoConfig.getXmlxsd().toLowerCase().contains(CHANNEL_PEPPOL)) {
									  //get the real JSON-payload to wrap within the peppol-xml-wrapper format;
									  String jsonPayload = filenameService.writeToString(msg);
									  logger.trace(jsonPayload);
									  logger.info(result.toString());
									  try {
										  //OBSOLETE -->(3.2) wrap it in PEPPOL XML (when applicable)
										  //if(this.peppolXmlWriterService.writeFileOnDisk(msg, jsonPayload) == 0) {
										  
										  //New Peppol - Transport Execution Plan - Family - no json at all ...
										  if(this.peppolXmlWriterService_TransExecPlanRequest.writeFileOnDisk(msg) == 0) {
											  //TEST with new TranportExecutionPlanRequest
											  //TODO
											  //END TEST
											  List tmp = sadmolffService.insertLogRecord(serverRoot, user, this.getSadmolffDto(masterDto, msg), "A");
											  if(tmp!=null && !tmp.isEmpty()) {
												  result.append("OK " + dtoConfig.getCommtype() + " " + dtoConfig.getFormat());
											  }else {
												  result.append("ERROR. peppolXmlWriterService logRecordSadmolff ???? ");
											  }
									  	  }else {
									  		result.append("ERROR. peppolXmlWriterService writeFileOnDisk ???? ");
									  	  }
									  }catch(Exception e) {
										  result.append("ERROR. peppolXmlWriterService " + e.toString());
									  }
									  
								  }else if(dtoConfig.getXmlxsd().toLowerCase().contains(CHANNEL_EVRY)) {
									  //get the real JSON-payload to wrap within the peppol-xml-wrapper format;
									  String jsonPayload = filenameService.writeToString(msg);
									  logger.trace(jsonPayload);
									  logger.info(result.toString());
									  try {
										  //(3.2) wrap it in EVRY XML (when applicable)
										  if(this.evryXmlWriterService.writeFileOnDisk(msg, jsonPayload) == 0) {
											  List tmp = sadmolffService.insertLogRecord(serverRoot, user, this.getSadmolffDto(masterDto, msg), "A");
											  if(tmp!=null && !tmp.isEmpty()) {
												  result.append("OK " + dtoConfig.getCommtype() + " " + dtoConfig.getFormat());
											  }else {
												  result.append("ERROR. evryXmlWriterService logRecordSadmolff ???? ");
											  }
									  	  }else {
									  		result.append("ERROR. evryXmlWriterService writeFileOnDisk ???? ");
									  	  }
									  }catch(Exception e) {
										  result.append("ERROR. evryXmlWriterService " + e.toString());
									  }
								
								  
								  }else {
									  //when a particular xml has not been implemented
									  result.append("ERROR. xmlxsd-error: " + dtoConfig.getXmlxsd() + " not implemented... "); 
								  }
								  
							  }else {
								  if(dtoConfig.getFormat().equalsIgnoreCase(EnumSadmocfFormat.json.toString())) {
									  if(jsonWriterService.writeFileOnDisk(msg) == 0) {
										  List tmp = sadmolffService.insertLogRecord(serverRoot, user, this.getSadmolffDto(masterDto, msg), "A");
										  if(tmp!=null && !tmp.isEmpty()) {
											  result.append("OK " + dtoConfig.getCommtype() + " " + dtoConfig.getFormat());
										  }else {
											  result.append("ERROR. jsonWriterService logRecordSadmolff ???? ");
										  }
										  
									  }else {
										  result.append("ERROR. jsonWriterService ..." );
									  }
								  }else {
									  result.append("ERROR. format-error: " + dtoConfig.getFormat() + " not implemented... ");
								  }
								 
							  }
							  
							  break; //Only first record in the list 
						  }
					  }
				  }else {
					  result.append("ERROR. Setup-error: Partneren-orgnr er ikke registrert (sadmocf) for send av: masterId ");
				  }
			  }
		  }catch(Exception e) {
			  logger.error(e.toString());
			  result.append("ERROR" + e.getMessage());
		  }
	      //
		  if(result.toString().isEmpty()) {
			  result.append("OK");
		  }
		  
		  return result.toString();
		  
	  }
	
	@RequestMapping(value = "/digitollv2/send_masterId_toExternalPartyXXX.do", method = {RequestMethod.GET, RequestMethod.POST})
	  public @ResponseBody String testWithMultipartXXX(HttpServletRequest request, @RequestParam String user, @RequestParam String files ) {
		
		logger.info("Inside: testWithMultipartXXX");
		logger.info("filesRaw:" + files);
		//logger.info("file-base64Str:" + base64Str);
		//byte[] valueDecoded = Base64.decodeBase64(base64Str);
		//byte[] valueDecoded = java.util.Base64.getDecoder().decode(base64Str);
		try {
			logger.info("A");	
			/*File serverFile = new File(filePath);
			BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
	        stream.write(FileUtils.readFileToByteArray(serverFile));
	        stream.close();
	        logger.info("Server File Location=" + serverFile.getCanonicalPath());
			*/
			//OK -->String content = new String(valueDecoded);
			//OK -->logger.info("file-content:" + content);
		}catch(Exception e) {
			logger.error(e.toString());
		}
		
		return "Hej";
	}
	
	/**
	 * Sends the external house in return to the external party (usually the carrier/representative) responsible for the transport
	 * @param request
	 * @param user
	 * @param ehlnrt
	 * @param ehlnrm
	 * @param ehlnrh
	 * @param receiverName
	 * @param receiverOrgnr
	 * @param attachmentsExist
	 * @return
	 */
	@RequestMapping(value = "/digitollv2/send_externalHouse_toExternalParty.do", method = {RequestMethod.GET, RequestMethod.POST})
	  public @ResponseBody String sendExternalHouseToParty(HttpServletRequest request, @RequestParam String user, @RequestParam String ehlnrt,
			  						@RequestParam String ehlnrm, @RequestParam String ehlnrh, @RequestParam String receiverName, @RequestParam String receiverOrgnr,
			  						@RequestParam Boolean attachmentsExist ) {
		
		  String serverRoot = ServerRoot.getServerRoot(request);
		  StringBuilder result = new StringBuilder();
		  
		  logger.info("Inside sendExternalHouseToParty");
		  logger.info("ehlnrt:" + ehlnrt);
		  logger.info("ehlnrm:" + ehlnrm);
		  logger.info("ehlnrh:" + ehlnrh);
		  logger.info("file-receiver name:" + receiverName);
		  logger.info("file-receiver orgNr:" + receiverOrgnr);
		  logger.info("attachmentsExist:" + attachmentsExist); //in case there are pdf:s (ZH or other) to send to the receiver
		  
		  try {
			  if(StringUtils.isNotEmpty(receiverName) && StringUtils.isNotEmpty(receiverOrgnr) && StringUtils.isNotEmpty(ehlnrt) 
				  && StringUtils.isNotEmpty(ehlnrm) && StringUtils.isNotEmpty(ehlnrh) ) {
				  //(0) check if this party exists in the SADMOCF-db-table (in order to know the format type (json or xml-peppol)
				  SadmocfDto dtoConfig = new SadmocfDto();
				  if(partyExists(serverRoot, user, receiverOrgnr, receiverName, dtoConfig)) {
					  if(dtoConfig!=null) {
						  //(1) get the master Dao record from Db (ALWAYS no matter what format)
						  List<SadmohfDto> list = sadmohfService.getSadmohf(serverRoot, user, ehlnrt, ehlnrm, ehlnrh);
						  //List<SadmomfDto> list = sadmomfService.getSadmomf(serverRoot, user, emlnrt, emlnrm);
						  for (SadmohfDto houseDto: list) {
							  houseDto.setTransportDto(sadmotfService.getSadmotfDto(serverRoot, user, ehlnrt));
							  houseDto.setMasterDto(sadmomfService.getSadmomfDto(serverRoot, user, ehlnrt, ehlnrm));
							  String emdkm_ff = houseDto.getMasterDto().getEmdkm_ff();
							  houseDto.setCarrierMasterIdDto(zadmomlfService.getZadmomlf(serverRoot, user, emdkm_ff));
							  logger.trace(houseDto.toString());
							  //(2) Map to MessageOutbound including attachments (if applicable)
							  MessageOutbound msg = new MapperMessageOutbound(specVersion).mapMessageOutboundExternalHouse(dtoConfig, houseDto, receiverName, receiverOrgnr, attachmentsExist, attachmentsPath);
							  ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
							  String json = ow.writeValueAsString(msg);
							  if(json!=null && json.length()>=2000) {
								  logger.info(json.substring(0,2000));
							  }else {
								  logger.info(json);
							  }
							  
							  logger.info(dtoConfig.toString());  
							  //(3) check what format to serialize (xml or json)
							  if(dtoConfig.getFormat().equalsIgnoreCase(EnumSadmocfFormat.xml.toString())) {
								  //(3.1) wrap json in correct xml format (peppol will act as an envelope...)
								  if(dtoConfig.getXmlxsd().toLowerCase().contains(CHANNEL_PEPPOL)) {
									  //get the real JSON-payload to wrap within the peppol-xml-wrapper format;
									  String jsonPayload = filenameService.writeToString(msg);
									  logger.trace(jsonPayload);
									  logger.info(result.toString());
									  try {
										  //(3.2) wrap it in PEPPOL XML (when applicable)
										  if(this.peppolXmlWriterService.writeFileOnDisk(msg, jsonPayload) == 0) {
											  List tmp = sadmolhffService.insertLogRecord(serverRoot, user, this.getSadmolhffDto(houseDto, msg), "A");
											  if(tmp!=null && !tmp.isEmpty()) {
												  result.append("OK " + dtoConfig.getCommtype() + " " + dtoConfig.getFormat());
											  }else {
												  result.append("ERROR. peppolXmlWriterService logRecordSadmolhff ???? ");
											  }
									  	  }else {
									  		result.append("ERROR. peppolXmlWriterService writeFileOnDisk ???? ");
									  	  }
									  }catch(Exception e) {
										  result.append("ERROR. peppolXmlWriterService " + e.toString());
									  }
									  
								  }else if(dtoConfig.getXmlxsd().toLowerCase().contains(CHANNEL_EVRY)) {
									  //get the real JSON-payload to wrap within the evry-xml-wrapper format;
									  String jsonPayload = filenameService.writeToString(msg);
									  logger.info(jsonPayload);
									  logger.info(result.toString());
									  try {
										  //(3.2) wrap it in EVRY XML (when applicable)
										  if(this.evryXmlWriterService.writeFileOnDisk(msg, jsonPayload) == 0) {
											  List tmp = sadmolhffService.insertLogRecord(serverRoot, user, this.getSadmolhffDto(houseDto, msg), "A");
											  if(tmp!=null && !tmp.isEmpty()) {
												  result.append("OK " + dtoConfig.getCommtype() + " " + dtoConfig.getFormat());
											  }else {
												  result.append("ERROR. evryXmlWriterService logRecordSadmolhff ???? ");
											  }
									  	  }else {
									  		result.append("ERROR. evryXmlWriterService writeFileOnDisk ???? ");
									  	  }
									  }catch(Exception e) {
										  result.append("ERROR. evryXmlWriterService " + e.toString());
									  }
									  
								    
								  }else {
									  //when a particular xml has not been implemented
									  result.append("ERROR. xmlxsd-error: " + dtoConfig.getXmlxsd() + " not implemented... "); 
								  }
								  
							  }else {
								  if(dtoConfig.getFormat().equalsIgnoreCase(EnumSadmocfFormat.json.toString())) {
									  if(jsonWriterService.writeFileOnDisk(msg) == 0) {
										  List tmp = sadmolhffService.insertLogRecord(serverRoot, user, this.getSadmolhffDto(houseDto, msg), "A");
										  if(tmp!=null && !tmp.isEmpty()) {
											  result.append("OK " + dtoConfig.getCommtype() + " " + dtoConfig.getFormat());
										  }else {
											  result.append("ERROR. jsonWriterService logRecordSadmolhff ???? ");
										  }
										  
									  }else {
										  result.append("ERROR. jsonWriterService ..." );
									  }
								  }else {
									  result.append("ERROR. format-error: " + dtoConfig.getFormat() + " not implemented... ");
								  }
								 
							  }
							  
							  break; //Only first record in the list 
						  }
					  }
				  }else {
					  result.append("ERROR. Setup-error: Partneren-orgnr er ikke registrert (sadmocf) for send av: masterId ");
				  }
			  }
		  }catch(Exception e) {
			  e.printStackTrace();
			  logger.error(e.toString());
			  result.append("ERROR" + e.getMessage());
		  }
	      //
		  if(result.toString().isEmpty()) {
			  result.append("OK");
		  }
		  
		  return result.toString();
		  
	  }
	
	/**
	 * 
	 * @param masterDto
	 * @param msg
	 * @return
	 */
	private SadmolffDto getSadmolffDto(SadmomfDto masterDto, MessageOutbound msg) {
		
		SadmolffDto dto = new SadmolffDto();
		dto.setEmdkm(msg.getDocumentID());
		dto.setUuid(msg.getUuid());
		dto.setEmlnrt(String.valueOf(masterDto.getEmlnrt()));
		dto.setStatus(EnumSadmolffStatus.C.toString());
		//
		dto.setAvsid(msg.getSender().getIdentificationNumber());
		dto.setMotid(msg.getReceiver().getIdentificationNumber());
		
		
		return dto;
	}
	/**
	 * 
	 * @param houseDto
	 * @param msg
	 * @return
	 */
	private SadmolhffDto getSadmolhffDto(SadmohfDto houseDto, MessageOutbound msg) {
		
		SadmolhffDto dto = new SadmolhffDto();
		dto.setEhdkh(msg.getDocumentID());
		dto.setUuid(msg.getUuid());
		dto.setEhlnrt(String.valueOf(houseDto.getEhlnrt()));
		dto.setStatus(EnumSadmolffStatus.C.toString());
		//
		dto.setAvsid(msg.getSender().getIdentificationNumber());
		dto.setMotid(msg.getReceiver().getIdentificationNumber());
		
		
		return dto;
	}
	
	/**
	 * 
	 * @param serverRoot
	 * @param user
	 * @param orgnr
	 * @param name
	 * @param dto
	 * @return
	 */
	private boolean partyExists (String serverRoot, String user, String orgnr, String name, SadmocfDto dto) {
		boolean retval = false;
		List<SadmocfDto> list = this.sadmocfService.getSadmocf(serverRoot, user, orgnr, name);
		if(list!=null && !list.isEmpty()) {
			for (SadmocfDto tmpDto : list) {
				dto.setOrgnr(tmpDto.getOrgnr());
				dto.setName(tmpDto.getName());
				dto.setCommtype(tmpDto.getCommtype());
				dto.setFormat(tmpDto.getFormat());
				dto.setXmlxsd(tmpDto.getXmlxsd());
				dto.setAvsname(tmpDto.getAvsname());
				dto.setAvsorgnr(tmpDto.getAvsorgnr());
				retval = true;
			}
		}
		
		
		return retval;
	}
	
	
}
