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

import no.systema.jservices.tvinn.digitoll.external.house.dao.Attachments;
import no.systema.jservices.tvinn.digitoll.external.house.dao.Communication;
import no.systema.jservices.tvinn.digitoll.external.house.dao.DocumentReferences;
import no.systema.jservices.tvinn.digitoll.external.house.dao.MessageOutbound;
import no.systema.jservices.tvinn.digitoll.v2.dto.SadmomfDto;
import no.systema.jservices.tvinn.digitoll.v2.enums.EnumPeppolID;
import no.systema.jservices.tvinn.digitoll.v2.util.PeppolSchemaIdRecognizer;
import no.systema.jservices.tvinn.expressfortolling2.enums.EnumPeppolTransportServiceCodes;
import no.systema.jservices.tvinn.expressfortolling2.util.DateUtils;

@Service
public class PeppolXmlWriterService_TransportExecutionPlanRequest {
	private static Logger logger = LoggerFactory.getLogger(PeppolXmlWriterService_TransportExecutionPlanRequest.class.getName());
	
	@Autowired
	private FilenameService filenameService;
	
	/**
	 * 
	 * @param msg
	 */
	public int writeFileOnDisk (MessageOutbound msg, SadmomfDto masterDto ) {
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
		  
		  //======================================
		  //START HEADER for SBDH-Peppol Envelope
		  //======================================
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
		  //standard.setTextContent("urn:fdc:norstella.no:toll:trns:adviseringsmelding:1");
		  standard.setTextContent("urn:fdc:peppol.eu:logistics:trns:transport_execution_plan_request:1::2.4");
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
		  //type.setTextContent("adviseringsmelding");
		  type.setTextContent("TransportExecutionPlanRequest-2");
		  documentIdentification.appendChild(type);
		  //<CreationDateAndTime>2023-12-04T15:42:10Z</CreationDateAndTime>  
		  Element creationDateAndTime = doc.createElement("CreationDateAndTime");
		  creationDateAndTime.setTextContent(msg.getMessageIssueDate());
		  documentIdentification.appendChild(creationDateAndTime);
		  //add Doc.Ident. to header
		  header.appendChild(documentIdentification);
		  
		  
		  //BusinessScope
		  Element businessScope = doc.createElement("BusinessScope");
		  this.addScopeElement(doc, businessScope, "DOCUMENTID", "urn:oasis:names:specification:ubl:schema:xsd:TransportExecutionPlanRequest-2::TransportExecutionPlanRequest##urn:fdc:peppol.eu:logistics:trns:transport_execution_plan_request:1::2.4");
		  this.addScopeElement(doc, businessScope, "PROCESSID", "urn:fdc:peppol.eu:logistics:bis:advanced_transport_execution_plan:1");
		  
		  //this.addScopeElement(doc, businessScope, "DOCUMENTID", "urn:fdc:norstella.no:toll:trns:adviseringsmelding:1");
		  //this.addScopeElement(doc, businessScope, "PROCESSID", "urn:fdc:norstella.no:toll:bis:advisering:1");
		  
		  this.addScopeElement(doc, businessScope, "COUNTRY_C1", "NO");
		  
		  //add BusinessScope to header
		  header.appendChild(businessScope);
		  //add header to root
		  rootElement.appendChild(header);
		  //======================================
		  //END HEADER for SBDH-Peppol Envelope
		  //======================================
		  
		  
		  //=========================================================
		  //START Transport document - TransportExecutionPlanRequest
		  //=========================================================
		  Element transportExecutionPlanRequest = doc.createElement("ubl:TransportExecutionPlanRequest");
		  transportExecutionPlanRequest.setAttribute("xmlns:ubl","urn:oasis:names:specification:ubl:schema:xsd:TransportExecutionPlanRequest-2" );
		  transportExecutionPlanRequest.setAttribute("xmlns:cac","urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2" );
		  transportExecutionPlanRequest.setAttribute("xmlns:cbc","urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2" );
		  //NOT REQUIRED ? transportExecutionPlanRequest.setAttribute("xmlns:ext","urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2" );
		  //OBSOLETE transportExecutionPlanRequest.setAttribute("xmlns:no","www.norstella.no" );
		  ////map with java object
		  Element customizationId = doc.createElement("cbc:CustomizationID");
		  this.setCompleteElement(customizationId, transportExecutionPlanRequest, "urn:fdc:peppol.eu:logistics:trns:transport_execution_plan_request:1" );
		  Element profileId = doc.createElement("cbc:ProfileID");
		  this.setCompleteElement(profileId, transportExecutionPlanRequest, "urn:fdc:peppol.eu:logistics:bis:advanced_transport_execution_plan:1" );
		  Element documentId = doc.createElement("cbc:ProfileExecutionID"); //UUID
		  this.setCompleteElement(documentId, transportExecutionPlanRequest, msg.getDocumentID());
		  Element messageNumber = doc.createElement("cbc:ID"); //UUID
		  this.setCompleteElement(messageNumber, transportExecutionPlanRequest, msg.getMessageNumber());
		  
		  //Obsolete
		  //Element version = doc.createElement("cbc:VersionID");
		  //this.setCompleteElement(version, transportExecutionPlanRequest, msg.getVersion());
		  
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
		  //Check XSD and Norstella PDF
		  
		  //SENDER-Mandatory is called TransportUserParty in this document type
		  Element senderParty_TransportUserParty = doc.createElement("cac:TransportUserParty");
		  this.setSenderParty(senderParty_TransportUserParty, doc, msg);
		  //add party
		  transportExecutionPlanRequest.appendChild(senderParty_TransportUserParty);
		  
		  //RECEIVER-Mandatory is called TransportServiceProviderParty in this document type
		  Element receiverParty_TransportServiceProviderParty = doc.createElement("cac:TransportServiceProviderParty");
		  this.setReceiverParty(receiverParty_TransportServiceProviderParty, doc, msg);
		  //add party
		  transportExecutionPlanRequest.appendChild(receiverParty_TransportServiceProviderParty);
		  
 		  
 		  /*
		  //SENDER - Ref. above TransportUserParty
		  Element senderParty = doc.createElement("cac:SenderParty");
		  this.setSenderParty(senderParty, doc, msg);
		  //add party
		  transportExecutionPlanRequest.appendChild(senderParty);
		  
		  //RECEIVER - Ref above = TransportServiceProviderParty
		  Element receiverParty = doc.createElement("cac:ReceiverParty");
		  this.setReceiverParty(receiverParty, doc, msg);
		  //add party
		  transportExecutionPlanRequest.appendChild(receiverParty);
		  */
 		  
		  //Document Refs with Attachments (when applicable)
		  if(msg.getDocumentReferences()!=null && msg.getDocumentReferences().size()>0) {
			  int docRefCounter = 1;
			  int attachmentCounter = 1;
			  
			  for(DocumentReferences documentReferences : msg.getDocumentReferences()) {
				  Element additionalDocumentReference = doc.createElement("cac:AdditionalDocumentReference");
				  Element docRefId = doc.createElement("cbc:ID");
				  this.setCompleteElement(docRefId, additionalDocumentReference, documentReferences.getReferenceId());
				  Element documentType = doc.createElement("cbc:DocumentType");
				  this.setCompleteElement(documentType, additionalDocumentReference, documentReferences.getTypeOfReference());
				  
				  //Attachments
				  if(msg.getAttachments()!=null && msg.getAttachments().size()>0) {
					  for(Attachments attachment : msg.getAttachments()) {
						  if (docRefCounter == attachmentCounter) { //in order to get the correct attachment on the list of document references...
							  Element cacAttachment = doc.createElement("cac:Attachment");
							  Element embeddedDocBinaryObject = doc.createElement("cbc:EmbeddedDocumentBinaryObject");
							  embeddedDocBinaryObject.setAttribute("filename", attachment.getDocumentName());
							  embeddedDocBinaryObject.setAttribute("mimeCode", "application/pdf");
							  this.setCompleteElement(embeddedDocBinaryObject, cacAttachment, attachment.getContent());
							  additionalDocumentReference.appendChild(cacAttachment);							  
						  }
						  attachmentCounter++; 
					  }
				  }
				  transportExecutionPlanRequest.appendChild(additionalDocumentReference);
				  docRefCounter++;
			
			  }
		  }
		  
		  Element mainTransportService = doc.createElement("cac:MainTransportationService");
		  Element transportServiceCode = doc.createElement("cbc:TransportServiceCode");
		  Element transportationServiceDescription = doc.createElement("cbc:TransportationServiceDescription");
		  this.setCompleteElement(transportServiceCode, mainTransportService, EnumPeppolTransportServiceCodes._CustomsDeclaration.toString()); //
		  this.setCompleteElement(transportationServiceDescription, mainTransportService, msg.getMessageType());
		  transportExecutionPlanRequest.appendChild(mainTransportService);
		  
		  //-------------------
		  //START Consignment
		  //-------------------
		  Element consignment = doc.createElement("cac:Consignment");
		  // mandatory in peppol. May contain a dummy value
		  Element dummyId = doc.createElement("cbc:ID");
		  String DUMMY_VALUE = "12535678901234567";
		  this.setCompleteElement(dummyId, consignment, DUMMY_VALUE);
		  
		  //gross weight
		  Element grossWeight = doc.createElement("cbc:GrossWeightMeasure");
		  grossWeight.setAttribute("unitCode", "KGM"); //standard ??
		  this.setCompleteElement(grossWeight, consignment, String.valueOf(masterDto.getEmvkb()));
		  //Consignee/Consignor
		  Element consignee = doc.createElement("cac:ConsigneeParty");
		  this.setConsigneeConsignorParty(consignee, doc, msg, true);
		  Element consignor = doc.createElement("cac:ConsignorParty");
		  this.setConsigneeConsignorParty(consignor, doc, msg, false);
		  consignment.appendChild(consignee);
		  consignment.appendChild(consignor);
		  
		  //carrier identification number
		  Element carrierParty = doc.createElement("cac:CarrierParty");
		  Element partyLegalEntity = doc.createElement("cac:PartyLegalEntity");
		  Element carrierName = doc.createElement("cbc:RegistrationName");
		  this.setCompleteElement(carrierName, partyLegalEntity, masterDto.getTransportDto().getEtnat());
		  Element carrierIdentificationNumber = doc.createElement("cbc:CompanyID");
		  //send the country code and get the schemeID for Carrier (0192 (NO), 0007 (SE), 0198 (DK), etc)
		  carrierIdentificationNumber.setAttribute("schemeID", PeppolSchemaIdRecognizer.getPeppolIdPrefix(masterDto.getTransportDto().getEtlkt()));
		  //wash the orgnr in case this is with the land code prefix since Peppol does not accept EORI-numbers 
		  String tmpIdNr = msg.getConsignmentMasterLevel().getCarrierIdentificationNumber();
		  if(masterDto.getTransportDto().getEtlkt().equals(msg.getConsignmentMasterLevel().getCarrierIdentificationNumber().substring(0,2))) {
			  tmpIdNr = tmpIdNr.substring(2);
		  }
		  this.setCompleteElement(carrierIdentificationNumber, partyLegalEntity, tmpIdNr);
		  carrierParty.appendChild(partyLegalEntity);
		  consignment.appendChild(carrierParty);
		  
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
		  
		  //TransportMeans (Road, Rail, Maritime, Air)
		  this.setTransportMeans(doc, consignment, msg);
		  
		  //Office of Entry
		  Element offOfEntry = doc.createElement("cac:OfficeOfEntryLocation");
		  Element referenceNumber = doc.createElement("cac:ID");
		  this.setCompleteElement(referenceNumber, offOfEntry, msg.getCustomsOfficeOfFirstEntry().getReferenceNumber());
		  consignment.appendChild(offOfEntry);
		  
		  //Master level - Document Reference
		  Element documentReferenceMasterLevel = doc.createElement("cac:DocumentReference");
		  Element docRefId = doc.createElement("cbc:ID");
		  this.setCompleteElement(docRefId, documentReferenceMasterLevel, msg.getConsignmentMasterLevel().getTransportDocumentMasterLevel().getDocumentNumber());
		  Element docRefType = doc.createElement("cbc:Type");
		  this.setCompleteElement(docRefType, documentReferenceMasterLevel, msg.getConsignmentMasterLevel().getTransportDocumentMasterLevel().getType());
		  consignment.appendChild(documentReferenceMasterLevel);
		  //----------------
		  //END Consignment
		  //----------------
		  
		  //add consignment
		  transportExecutionPlanRequest.appendChild(consignment);
		  //=========================================================
		  //END Transport document - TransportExecutionPlanRequest
		  //=========================================================
		
		  
		  //add to root finally
		  rootElement.appendChild(transportExecutionPlanRequest);
		  
		  
		  
		  
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
	 * TransportMeans
	 * @param doc
	 * @param consignment
	 * @param msg
	 */
	public void setTransportMeans(Document doc, Element consignment,  MessageOutbound msg) {
		  Element mainCarriageShipmentStage = doc.createElement("cac:MainCarriageShipmentStage");
		  Element modeOfTransportCode = doc.createElement("cbc:TransportModeCode");
		  this.setCompleteElement(modeOfTransportCode, mainCarriageShipmentStage, msg.getActiveBorderTransportMeans().getModeOfTransportCode());
		  mainCarriageShipmentStage.appendChild(modeOfTransportCode);
		  //
		  Element transportMeans = doc.createElement("cac:TransportMeans");
		  Element activeBorderCountryCode = doc.createElement("cbc:RegistrationNationalityID");
		  this.setCompleteElement(activeBorderCountryCode, transportMeans, msg.getActiveBorderTransportMeans().getCountryCode());
		  
		  //Rail
		  if(msg.getActiveBorderTransportMeans().getModeOfTransportCode().equals("2")) {
			  Element railTransport = doc.createElement("cac:RailTransport");
			  Element activeBorderIdentificationNumber = doc.createElement("cbc:TrainID");
			  this.setCompleteElement(activeBorderIdentificationNumber, railTransport, msg.getActiveBorderTransportMeans().getIdentificationNumber());
			  Element railCarId = doc.createElement("cbc:RailCarID");
 			  this.setCompleteElement(railCarId, railTransport, "turref..."); //saknas ref i mappade object msg (det är sadmotf.etcref)
			  transportMeans.appendChild(railTransport);
			  
		  //Road	  
		  }else if (msg.getActiveBorderTransportMeans().getModeOfTransportCode().equals("3")) { 
			  Element roadTransport = doc.createElement("cac:RoadTransport");
			  Element activeBorderIdentificationNumber = doc.createElement("cbc:LicensePlateID");
			  this.setCompleteElement(activeBorderIdentificationNumber, roadTransport, msg.getActiveBorderTransportMeans().getIdentificationNumber());
			  transportMeans.appendChild(roadTransport);
			  
		  //Sjö	  
		  }else if (msg.getActiveBorderTransportMeans().getModeOfTransportCode().equals("1")) {
			  Element roadTransport = doc.createElement("cac:MaritimeTransport");
			  Element activeBorderIdentificationNumber = doc.createElement("cbc:VesselID");
			  this.setCompleteElement(activeBorderIdentificationNumber, roadTransport, msg.getActiveBorderTransportMeans().getIdentificationNumber());
			  transportMeans.appendChild(roadTransport);
			  
		  //Flyg	  
		  }else if (msg.getActiveBorderTransportMeans().getModeOfTransportCode().equals("4")) {
			  Element airTransport = doc.createElement("cac:AirTransport");
			  Element activeBorderIdentificationNumber = doc.createElement("cbc:AircraftID");
			  this.setCompleteElement(activeBorderIdentificationNumber, airTransport, msg.getActiveBorderTransportMeans().getIdentificationNumber());
			  transportMeans.appendChild(airTransport);
		  }
		  //append to parent
		  mainCarriageShipmentStage.appendChild(transportMeans);
		  consignment.appendChild(mainCarriageShipmentStage);
		  
		
	}
	/**
	 * 
	 * @param party
	 * @param doc
	 * @param msg
	 */
	private void setSenderParty(Element party, Document doc, MessageOutbound msg) {
		Element identificationNumber = doc.createElement("cbc:EndpointID");
		identificationNumber.setAttribute("schemeID", EnumPeppolID.Norway_Orgnr.toString());
		this.setCompleteElement(identificationNumber, party, msg.getSender().getIdentificationNumber());
		
		//Party Identification - mandatory in Peppol
		Element partyIdentification = doc.createElement("cac:PartyIdentification");
		Element peppolId = doc.createElement("cbc:ID");
		this.setCompleteElement(peppolId, partyIdentification, msg.getSender().getIdentificationNumber());
		party.appendChild(partyIdentification);
				
		//Sender - legalEntity
		Element partyLegalEntity = doc.createElement("cac:PartyLegalEntity");
		Element name = doc.createElement("cbc:RegistrationName");
		this.setCompleteElement(name, partyLegalEntity, msg.getSender().getName());
		party.appendChild(partyLegalEntity);
		//Sender - contact
		Element contact = doc.createElement("cac:Contact");
		Element telephone = doc.createElement("cbc:Telephone");
		Element email = doc.createElement("cbc:ElectronicMail");
		for(Communication comm : msg.getSender().getCommunication()){
		  if(comm.getTelephoneNumber()!=null && StringUtils.isNotEmpty(comm.getTelephoneNumber())){
			  this.setCompleteElement(telephone, contact, comm.getTelephoneNumber());
			  
		  }else if(comm.getEmailAddress()!=null && StringUtils.isNotEmpty(comm.getEmailAddress())){
			  this.setCompleteElement(email, contact, comm.getEmailAddress());
		  }
		}
		party.appendChild(contact);
		  
	}
	/*
	private void setTransportUserParty(Element party, Document doc, MessageOutbound msg) {
		Element identificationNumber = doc.createElement("cbc:EndpointID");
		identificationNumber.setAttribute("schemeID", "0192");
		this.setCompleteElement(identificationNumber, party, msg.getSender().getIdentificationNumber());
		
		//Party Identification - mandatory in Peppol
		Element partyIdentification = doc.createElement("cac:PartyIdentification");
		Element peppolId = doc.createElement("cbc:ID");
		this.setCompleteElement(peppolId, partyIdentification, msg.getSender().getIdentificationNumber());
		party.appendChild(partyIdentification);
		
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
		  
	}*/
	private void setReceiverParty(Element party, Document doc, MessageOutbound msg) {
		Element identificationNumber = doc.createElement("cbc:EndpointID");
		identificationNumber.setAttribute("schemeID", EnumPeppolID.Norway_Orgnr.toString());
		this.setCompleteElement(identificationNumber, party, msg.getReceiver().getIdentificationNumber());
		
		//Party Identification - mandatory in Peppol
		Element partyIdentification = doc.createElement("cac:PartyIdentification");
		Element peppolId = doc.createElement("cbc:ID");
		this.setCompleteElement(peppolId, partyIdentification, msg.getReceiver().getIdentificationNumber());
		party.appendChild(partyIdentification);
				
		//Receiver - legalEntity
		Element partyLegalEntity = doc.createElement("cac:PartyLegalEntity");
		Element name = doc.createElement("cbc:RegistrationName");
		this.setCompleteElement(name, partyLegalEntity, msg.getReceiver().getName());
		party.appendChild(partyLegalEntity);
		  
	}
	/*
	private void setTransportServiceProviderParty(Element party, Document doc, MessageOutbound msg) {
		Element identificationNumber = doc.createElement("cbc:EndpointID");
		identificationNumber.setAttribute("schemeID", "0192");
		this.setCompleteElement(identificationNumber, party, msg.getReceiver().getIdentificationNumber());
		
		//Party Identification - mandatory in Peppol
		Element partyIdentification = doc.createElement("cac:PartyIdentification");
		Element peppolId = doc.createElement("cbc:ID");
		this.setCompleteElement(peppolId, partyIdentification, msg.getReceiver().getIdentificationNumber());
		party.appendChild(partyIdentification);
		
		//Sender - legalEntity
		Element partyLegalEntity = doc.createElement("cac:PartyLegalEntity");
		Element name = doc.createElement("cbc:RegistrationName");
		this.setCompleteElement(name, partyLegalEntity, msg.getReceiver().getName());
		party.appendChild(partyLegalEntity);
		  
	}
	*/
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
		
		//only for DOCUMENTID
		if(typeValue.equals("DOCUMENTID")) {
			Element identifier = doc.createElement("Identifier");
			identifier.setTextContent("busdox-docid-qns");
			scope.appendChild(identifier);
		}
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
