package no.systema.jservices.tvinn.digitoll.v2.controller;

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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import no.systema.jservices.common.dao.services.BridfDaoService;
import no.systema.jservices.tvinn.digitoll.external.house.FilenameService;
import no.systema.jservices.tvinn.digitoll.external.house.MapperMessageOutbound;
import no.systema.jservices.tvinn.digitoll.external.house.dao.MessageOutbound;
import no.systema.jservices.tvinn.digitoll.v2.dto.SadmocfDto;
import no.systema.jservices.tvinn.digitoll.v2.dto.SadmomfDto;
import no.systema.jservices.tvinn.digitoll.v2.enums.EnumSadmocfCommtype;
import no.systema.jservices.tvinn.digitoll.v2.services.SadmocfService;
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
	private SadmocfService sadmocfService;
	
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
		  StringBuilder result = new StringBuilder();
		  boolean isFtpChannel = false;
		  boolean isEmailChannel = false;
		  boolean isWebServiceChannel = false;
		  
		  logger.info("Inside sendMasterIdToPart");
		  logger.info("emlnrt:" + emlnrt);
		  logger.info("emlnrm:" + emlnrm);
		  logger.info("file-receiver name:" + receiverName);
		  logger.info("file-receiver orgNr:" + receiverOrgnr);
		  
		  try {
			  if(StringUtils.isNotEmpty(receiverName) && StringUtils.isNotEmpty(receiverOrgnr) && StringUtils.isNotEmpty(emlnrt) && StringUtils.isNotEmpty(emlnrm)) {
				  //(0) check if this party exists in the SADMOCF-db-table (in order to know the communication type)
				  SadmocfDto dto = new SadmocfDto();
				  if(partyExists(serverRoot, user, receiverOrgnr, receiverName, dto)) {
					  if(dto!=null) { 
						  if(dto.getCommtype().equalsIgnoreCase((EnumSadmocfCommtype.ftp.toString())) ){
							  isFtpChannel = true;
						  }else if (dto.getCommtype().equalsIgnoreCase((EnumSadmocfCommtype.ws.toString())) ){
							  isWebServiceChannel = true;
						  }else if (dto.getCommtype().equalsIgnoreCase((EnumSadmocfCommtype.email.toString())) ){
							  isEmailChannel = true;
						  }
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
							    
							  //(3) check what type of communication channel (FTP or email) that requires serialization
							  if(isFtpChannel || isEmailChannel) {
								  //(3.1) write to file (if needed)
								  filenameService.writeToDisk(msg);
								  result.append("OK " + dto.getCommtype());
								  
								  /*
								  //(3.2) wrap it in PEPPOL XML (when applicable)
								  byte[] bytesEncoded = Base64.encodeBase64(json.getBytes());
								  logger.info("Encoded value is " + new String(bytesEncoded));
								  */
								  /*
								  // Decode data on other side, by processing encoded data
								  byte[] valueDecoded = Base64.decodeBase64(bytesEncoded);
								  logger.info("Decoded value is " + new String(valueDecoded));
								  */
								  
								  DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
								  docFactory.setNamespaceAware(true);
							        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

							        // root elements
							        Document doc = docBuilder.newDocument();
							        doc.setXmlStandalone(true);
							        Element rootElement = doc.createElement("company");
							        //OK rootElement.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xs:type", "ns0:UserRequest");
							        rootElement.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xs:type", "http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader");
							        doc.appendChild(rootElement);

							        Element staff = doc.createElement("staff");
							        staff.setTextContent("hello");
							        rootElement.appendChild(staff);

							        //...create XML elements, and others...

							        // write dom document to a file
							        try (FileOutputStream output = new FileOutputStream("/Users/oscardelatorre/staff-dom.xml")) {
							            writeXml(doc, output);
							        } catch (Exception e) {
							            e.printStackTrace();
							        }

								  
								  
								    
								  /*
								  final byte[] data = xmlMapper.writeValueAsBytes(xmlDao);
								  String payload = new String(data, "UTF-8");
								  	
								  //DEBUG
								  logger.info("### XML payload A:" + payload);
								  */
								  
								  
							  }else {
								 if(isWebServiceChannel) { 
									 //(4) no serialization is required 
									 //TODO web-services implementation per party, email, other
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
	
	// write doc to output stream
    private  void writeXml(Document doc, OutputStream output) throws TransformerException {

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(output);

        transformer.transform(source, result);

    }
	
	/**
	 * 
	 * @param orgnr
	 * @param commMap
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
				retval = true;
			}
		}
		
		
		return retval;
	}
	
	
}
