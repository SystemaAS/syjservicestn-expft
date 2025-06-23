package no.systema.jservices.tvinn.digitoll.external.house;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;

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
import org.springframework.stereotype.Service;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import no.systema.jservices.tvinn.digitoll.external.house.dao.Communication;
import no.systema.jservices.tvinn.digitoll.external.house.dao.MessageOutbound;
import no.systema.jservices.tvinn.digitoll.v2.enums.EnumPeppolID;
import no.systema.jservices.tvinn.expressfortolling2.util.DateUtils;

@Service
public class PeppolXmlWriterService_TransportExecutionPlanGroup {
	private static Logger logger = LoggerFactory.getLogger(PeppolXmlWriterService_TransportExecutionPlanGroup.class.getName());
	
	@Autowired
	private FilenameService filenameService;
	
	/**
	 * 
	 * @param msg
	 */
	public int writeFileOnDisk (MessageOutbound msg) {
		int retval = 0;
		try {
		
		  //logger.info(( msg.toString());	
		  DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		  docFactory.setNamespaceAware(true);
		  DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		
		  // root elements
		  Document doc = docBuilder.newDocument();
		  doc.setXmlStandalone(true);
		  Element rootElement = doc.createElement("StandardBusinessDocument");
		  //OLD...rootElement.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xs:type", "http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader");
		  Attr attr = doc.createAttribute("xmlns");
		  attr.setValue("http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader");
		  rootElement.setAttributeNode(attr);
		  //rootElement.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xs:type", "http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader");
		  rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xs", "http://www.w3.org/2001/XMLSchema");
		  doc.appendChild(rootElement);
		  
		  //=========
		  //Header
		  //=========
		  Element header = doc.createElement("StandardBusinessDocumentHeader");
		  //staff.setTextContent(new String (bytesBase64Encoded));
		  Element headerVersion = doc.createElement("HeaderVersion");
		  headerVersion.setTextContent("1.0");
		  header.appendChild(headerVersion);
		  //Sender
		  Element sender = doc.createElement("Sender");
		  Element senderIdentifier = doc.createElement("Identifier");
		  senderIdentifier.setAttribute("Authority", "iso6523-actorid-upis");
		  senderIdentifier.setTextContent(EnumPeppolID.Norway_Orgnr.toString() + ":" + msg.getSender().getIdentificationNumber()); //from the json-payload
		  sender.appendChild(senderIdentifier);
		  header.appendChild(sender);
		  //Receiver
		  Element receiver = doc.createElement("Receiver");
		  Element receiverIdentifier = doc.createElement("Identifier");
		  receiverIdentifier.setAttribute("Authority", "iso6523-actorid-upis");
		  receiverIdentifier.setTextContent(EnumPeppolID.Norway_Orgnr.toString() + ":" + msg.getReceiver().getIdentificationNumber()); //from the json-payload
		  receiver.appendChild(receiverIdentifier);
		  header.appendChild(receiver);
		  //
		  //DocumentIdentification
		  Element documentIdentification = doc.createElement("DocumentIdentification");
		  //<Standard>urn:oasis:names:specification:ubl:schema:xsd:TransportationStatus-2</Standard>
		  Element standard = doc.createElement("Standard");
		  //standard.setTextContent("urn:oasis:names:specification:ubl:schema:xsd:TransportationStatus-2");
		  standard.setTextContent("urn:fdc:norstella.no:toll:trns:adviseringsmelding:1");
		  documentIdentification.appendChild(standard);
		  //<TypeVersion>2.3</TypeVersion>
		  Element typeVersion = doc.createElement("TypeVersion");
		  //typeVersion.setTextContent("2.3");
		  typeVersion.setTextContent("1.0");
		  documentIdentification.appendChild(typeVersion);
		  //<InstanceIdentifier>UNIQUE ID xxxx</TypeVersion>
		  Element instanceIdentifier = doc.createElement("InstanceIdentifier");
		  instanceIdentifier.setTextContent(msg.getUuid());
		  documentIdentification.appendChild(instanceIdentifier);
		  //<Type>TransportationStatus</Type>
		  Element type = doc.createElement("Type");
		  //type.setTextContent("TransportationStatus");
		  type.setTextContent("adviseringsmelding");
		  documentIdentification.appendChild(type);
		  //<CreationDateAndTime>2023-12-04T15:42:10Z</CreationDateAndTime>  
		  Element creationDateAndTime = doc.createElement("CreationDateAndTime");
		  creationDateAndTime.setTextContent(msg.getMessageIssueDate());
		  documentIdentification.appendChild(creationDateAndTime);
		  //add Doc.Ident. to header
		  header.appendChild(documentIdentification);
		  
		  
		  //BusinessScope
		  Element businessScope = doc.createElement("BusinessScope");
		  //this.addScopeElement(doc, businessScope, "DOCUMENTID", "urn:oasis:names:specification:ubl:schema:xsd:TransportationStatus-2::TransportationStatus##urn:fdc:peppol.eu:logistics:trns:transportation_status:1::2.3");
		  //this.addScopeElement(doc, businessScope, "PROCESSID", "urn:fdc:peppol.eu:logistics:bis:transportation_status_only:1");
		  this.addScopeElement(doc, businessScope, "DOCUMENTID", "urn:fdc:norstella.no:toll:trns:adviseringsmelding:1");
		  this.addScopeElement(doc, businessScope, "PROCESSID", "urn:fdc:norstella.no:toll:bis:advisering:1");
		  this.addScopeElement(doc, businessScope, "COUNTRY_C1", "NO");
		  
		  //add BusinessScope to header
		  header.appendChild(businessScope);
		  //add header to root
		  rootElement.appendChild(header);
		  
		  //==================
		  //Transport document
		  //==================
		  Element transportExecutionPlanRequest = doc.createElement("ubl:TransportExecutionPlanRequest");
		  transportExecutionPlanRequest.setAttribute("xmlns:ubl","urn:oasis:names:specification:ubl:schema:xsd:TransportExecutionPlanRequest-2" );
		  transportExecutionPlanRequest.setAttribute("xmlns:cac","urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2" );
		  transportExecutionPlanRequest.setAttribute("xmlns:cbc","urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2" );
		  transportExecutionPlanRequest.setAttribute("xmlns:ext","urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2" );
		  transportExecutionPlanRequest.setAttribute("xmlns:no","www.norstella.no" );
		  ////map with java object
		  Element customizationId = doc.createElement("cbc:CustomizationID");
		  this.setCompleteElement(customizationId, transportExecutionPlanRequest, "urn:fdc:peppol.eu:logistics:trns:transport_execution_plan_request:1" );
		  Element profileId = doc.createElement("cbc:ProfileID");
		  this.setCompleteElement(profileId, transportExecutionPlanRequest, "urn:fdc:peppol.eu:logistics:bis:transport_notification:1" );
		  Element documentId = doc.createElement("cbc:ProfileExecutionID");
		  this.setCompleteElement(documentId, transportExecutionPlanRequest, msg.getDocumentID());
		  Element messageNumber = doc.createElement("cbc:ID");
		  this.setCompleteElement(messageNumber, transportExecutionPlanRequest, msg.getMessageNumber());
		  Element version = doc.createElement("cbc:VersionID");
		  this.setCompleteElement(version, transportExecutionPlanRequest, msg.getVersion());
		  
		  //==============================
		  //START IssueDate and IssueTime
		  Element messageIssueDate = doc.createElement("cbc:IssueDate");
		  
		  //Convert to Peppol format as in: 2025-06-23T11:57:33+02:00 ... obsolete right now. We use the mapped value in UTC-Z format
		  //String zuluWithOffset = new DateUtils().getZuluTimeWithoutMillisecondsWithOffset(msg.getMessageIssueDate());
		  //System.out.println(zulu);
		  //String[] issueDateTime = zuluWithOffset.split("T");
		  
		  String[] issueDateTime = msg.getMessageIssueDate().split("T");
		  String issueDate = ""; String issueTime = "";
		  if(issueDateTime.length==2) {
			  issueDate = issueDateTime[0];
			  issueTime = issueDateTime[1];
		  }
		  this.setCompleteElement(messageIssueDate, transportExecutionPlanRequest, issueDate);
		  Element messageIssueTime = doc.createElement("cbc:IssueTime");
		  this.setCompleteElement(messageIssueTime, transportExecutionPlanRequest, issueTime);
		  //END IssueDate and IssueTime
		  //==============================
		  
		  Element documentStatusCode = doc.createElement("cbc:DocumentStatusCode");
		  this.setCompleteElement(documentStatusCode, transportExecutionPlanRequest, "9");
		  Element note = doc.createElement("cbc:Notes");
		  this.setCompleteElement(note, transportExecutionPlanRequest, msg.getNote());
		  Element terms = doc.createElement("cbc:TransportUserRemarks");
		  this.setCompleteElement(terms, transportExecutionPlanRequest, "todo");
		  
		  //...todo more
		  //========
		  //Parties
		  //========
		  //SENDER
		  Element senderParty = doc.createElement("cac:SenderParty");
		  this.setSenderParty(senderParty, doc, msg);
		  //add party
		  transportExecutionPlanRequest.appendChild(senderParty);
		  
		  //RECEIVER
		  Element receiverParty = doc.createElement("cac:ReceiverParty");
		  this.setReceiverParty(receiverParty, doc, msg);
		  //add party
		  transportExecutionPlanRequest.appendChild(receiverParty);
		  
		  //MainTransportationService
		  /*
		  <cac:MainTransportationService>
			<cbc:TransportServiceCode>19</cbc:TransportServiceCode>
			<cbc:TransportationServiceDescription><?messageType?>DigitalMOMaster<?messageType?></cbc:TransportationServiceDescription>
		</cac:MainTransportationService>
		  */
		  Element mainTransportService = doc.createElement("cac:MainTransportationService");
		  Element transportServiceCode = doc.createElement("cbc:TransportServiceCode");
		  Element transportationServiceDescription = doc.createElement("cbc:TransportationServiceDescription");
		  //TODO ? this.setCompleteElement(transportServiceCode, mainTransportService, "19?");
		  this.setCompleteElement(transportationServiceDescription, mainTransportService, msg.getMessageType());
		  transportExecutionPlanRequest.appendChild(mainTransportService);
		  //Consignment
		  /*
		   * <cac:Consignment>
		<cbc:ID>12535157654567654</cbc:ID>		
		<cbc:DeclaredCustomsValueAmount currencyID="NOK"><?transport.value?>1000</cbc:DeclaredCustomsValueAmount>
		<cbc:GrossWeightMeasure unitCode="KGM">103</cbc:GrossWeightMeasure>				
		<cbc:TotalTransportHandlingUnitQuantity unitCode="EA"><?consignmentHouseLevel.numberOfPackages?>6</cbc:TotalTransportHandlingUnitQuantity>		
		<cac:ConsigneeParty>			
			<cac:PartyName>
				<cbc:Name><?consignee.name?>Importer AS</cbc:Name>				
			</cac:PartyName>			
		</cac:ConsigneeParty>
		<cac:ConsignorParty>			
			<cac:PartyName>
				<cbc:Name><?consignor.name?>Exporter AS</cbc:Name>				
			</cac:PartyName>			
		</cac:ConsignorParty>  
		   */
		  Element consignment = doc.createElement("cac:Consignment");
		  Element consignee = doc.createElement("cac:ConsigneeParty");
		  this.setConsigneeConsignorParty(consignee, doc, msg, true);
		  Element consignor = doc.createElement("cac:ConsignorParty");
		  this.setConsigneeConsignorParty(consignor, doc, msg, false);
		  consignment.appendChild(consignee);
		  consignment.appendChild(consignor);
		  
		  //Time of arrival
		  Element transportEvent = doc.createElement("cac:TransportEvent");
		  Element period = doc.createElement("cac:Period");
		  Element startDate = doc.createElement("cbc:StartDate");
		  Element startTime = doc.createElement("cbc:StartTime");
		  //date time ETA
		  String[] etaDateTime = msg.getEstimatedDateAndTimeOfArrival().split("T");
		  String etaDate = ""; String etaTime = "";
		  if(etaDateTime.length==2) {
			  etaDate = etaDateTime[0];
			  etaTime = etaDateTime[1];
		  }
		  this.setCompleteElement(startDate, period, etaDate);
		  this.setCompleteElement(startTime, period, etaTime);
		  transportEvent.appendChild(period);
		  consignment.appendChild(transportEvent);
		  
		  //Office of Entry
		  Element offOfEntry = doc.createElement("cac:OfficeOfEntryLocation");
		  Element referenceNumber = doc.createElement("cac:ID");
		  this.setCompleteElement(referenceNumber, offOfEntry, msg.getCustomsOfficeOfFirstEntry().getReferenceNumber());
		  consignment.appendChild(offOfEntry);
		  
		  
		  //add consignment
		  transportExecutionPlanRequest.appendChild(consignment);
		  
		  //add to root
		  rootElement.appendChild(transportExecutionPlanRequest);
		  
		  
		  /*
		  //=====================================
		  //add to BinaryContent - json payload as Base64
		  //=====================================
		  byte[] bytesEncoded = Base64.encodeBase64(jsonPayload.getBytes());
		  logger.trace("Encoded value is " + new String(bytesEncoded));
		  
		  //add the base64-string in BinaryContent-tag
		  Element binaryContent = doc.createElement("BinaryContent");
		  binaryContent.setAttribute("xmlns", "http://peppol.eu/xsd/ticc/envelope/1.0");
		  binaryContent.setAttribute("mimeType", "application/json");
		  binaryContent.setAttribute("encoding", "Base64Binary");
		  binaryContent.setTextContent(new String (bytesEncoded));
		  //add BinaryContent to root
		  rootElement.appendChild(binaryContent);
		  */
		  
		  
		  
		  // write to logger
		  logger.info("About to write XML-payload...");
		  logger.info(this.getStringFromDocument(doc));
		  
		  // write dom document to a file
		  FileOutputStream output = new FileOutputStream(this.filenameService.getFileNameXml(msg)); 
		  writeXml(doc, output);
		  
		 
		  
		}catch (Exception e) {
			retval = -1;
			logger.error(e.toString());
		}
		
		return retval;
	}
	/**
	 * 
	 * @param party
	 * @param doc
	 * @param msg
	 */
	private void setSenderParty(Element party, Document doc, MessageOutbound msg) {
		Element identificationNumber = doc.createElement("cbc:EndpointID");
		identificationNumber.setAttribute("schemeID", "0198");
		this.setCompleteElement(identificationNumber, party, msg.getSender().getIdentificationNumber());
		//Sender - legalEntity
		Element partyLegalEntity = doc.createElement("cac:PartyLegalEntity");
		Element name = doc.createElement("cbc:RegistrationName");
		this.setCompleteElement(name, partyLegalEntity, msg.getSender().getName());
		party.appendChild(partyLegalEntity);
		//Sender - contact
		Element contact = doc.createElement("cac:Contact");
		Element telephone = doc.createElement("cbc:Telephone");
		Element email = doc.createElement("cdc:ElectronicMail");
		for(Communication comm : msg.getSender().getCommunication()){
		  if(comm.getTelephoneNumber()!=null && StringUtils.isNotEmpty(comm.getTelephoneNumber())){
			  this.setCompleteElement(telephone, contact, comm.getTelephoneNumber());
			  
		  }else if(comm.getEmailAddress()!=null && StringUtils.isNotEmpty(comm.getEmailAddress())){
			  this.setCompleteElement(email, contact, comm.getEmailAddress());
		  }
		}
		party.appendChild(contact);
		  
	}
	private void setReceiverParty(Element party, Document doc, MessageOutbound msg) {
		Element identificationNumber = doc.createElement("cbc:EndpointID");
		identificationNumber.setAttribute("schemeID", "0198");
		this.setCompleteElement(identificationNumber, party, msg.getReceiver().getIdentificationNumber());
		//Sender - legalEntity
		Element partyLegalEntity = doc.createElement("cac:PartyLegalEntity");
		Element name = doc.createElement("cbc:RegistrationName");
		this.setCompleteElement(name, partyLegalEntity, msg.getReceiver().getName());
		party.appendChild(partyLegalEntity);
		  
	}
	
	private void setConsigneeConsignorParty(Element party, Document doc, MessageOutbound msg, boolean consignee) {
		Element partyName = doc.createElement("cac:PartyName");
		Element name = doc.createElement("cbc:Name");
		if(consignee) {
			this.setCompleteElement(name, partyName, msg.getConsignee().getName());
		}else {
			this.setCompleteElement(name, partyName, msg.getConsignor().getName());
		}
		party.appendChild(partyName);
		  
	}
	
	private void setCompleteElement(Element element, Element parent, String value) {
		element.setTextContent(value);
		parent.appendChild(element);
	}
	
	
	
	private String getStringFromDocument(Document doc) throws TransformerException {
	    DOMSource domSource = new DOMSource(doc);
	    StringWriter writer = new StringWriter();
	    StreamResult result = new StreamResult(writer);
	    TransformerFactory tf = TransformerFactory.newInstance();
	    Transformer transformer = tf.newTransformer();
	    transformer.transform(domSource, result);
	    String returnStr = writer.toString();
	    if(returnStr!=null && returnStr.length()>2000) {
	    	returnStr = returnStr.substring(0,2000);
	    }
	    return returnStr;
	}
	
	private void addScopeElement(Document doc, Element parent, String typeValue, String idValue) {
		Element scope = doc.createElement("Scope");
		//Type
		Element type = doc.createElement("Type");
		type.setTextContent(typeValue);
		scope.appendChild(type);
		//InstanceIdentifier
		Element instanceIdentifier = doc.createElement("InstanceIdentifier");
		instanceIdentifier.setTextContent(idValue);
		scope.appendChild(instanceIdentifier);
		//add to parent
		parent.appendChild(scope);
		
	}
	// write doc to output stream
    private  void writeXml(Document doc, OutputStream output) throws TransformerException {

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(output);

        transformer.transform(source, result);

    }
    
    
}
