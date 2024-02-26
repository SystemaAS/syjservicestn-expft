package no.systema.jservices.tvinn.digitoll.external.house;

import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import no.systema.jservices.tvinn.digitoll.external.house.dao.MessageOutbound;
import no.systema.jservices.tvinn.digitoll.v2.enums.EnumPeppolID;
import no.systema.jservices.tvinn.expressfortolling2.util.DateUtils;

@Service
public class PeppolXmlWriterService {
	private static Logger logger = LoggerFactory.getLogger(PeppolXmlWriterService.class.getName());
	
	@Autowired
	private FilenameService filenameService;
	
	/**
	 * 
	 * @param msg
	 */
	public void writeFileOnDisk(MessageOutbound msg, String jsonPayload) {
		try {
		
		  //logger.info(( msg.toString());	
		  DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		  docFactory.setNamespaceAware(true);
		  DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		
		  // root elements
		  Document doc = docBuilder.newDocument();
		  doc.setXmlStandalone(true);
		  Element rootElement = doc.createElement("StandardBusinessDocument");
		  //OK rootElement.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xs:type", "ns0:UserRequest");
		  rootElement.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xs:type", "http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader");
		  doc.appendChild(rootElement);
		
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
		  standard.setTextContent("urn:oasis:names:specification:ubl:schema:xsd:TransportationStatus-2");
		  documentIdentification.appendChild(standard);
		  //<TypeVersion>2.3</TypeVersion>
		  Element typeVersion = doc.createElement("TypeVersion");
		  typeVersion.setTextContent("2.3");
		  documentIdentification.appendChild(typeVersion);
		  //<InstanceIdentifier>UNIQUE ID xxxx</TypeVersion>
		  Element instanceIdentifier = doc.createElement("InstanceIdentifier");
		  instanceIdentifier.setTextContent(msg.getUuid());
		  documentIdentification.appendChild(instanceIdentifier);
		  //<Type>TransportationStatus</Type>
		  Element type = doc.createElement("Type");
		  type.setTextContent("TransportationStatus");
		  documentIdentification.appendChild(type);
		  //<CreationDateAndTime>2023-12-04T15:42:10Z</CreationDateAndTime>  
		  Element creationDateAndTime = doc.createElement("CreationDateAndTime");
		  creationDateAndTime.setTextContent(msg.getMessageIssueDate());
		  documentIdentification.appendChild(creationDateAndTime);
		  //add Doc.Ident. to header
		  header.appendChild(documentIdentification);
		  
		  
		  //BusinessScope
		  Element businessScope = doc.createElement("BusinessScope");
		  this.addScopeElement(doc, businessScope, "DOCUMENTID", "urn:oasis:names:specification:ubl:schema:xsd:TransportationStatus-2::TransportationStatus##urn:fdc:peppol.eu:logistics:trns:transportation_status:1::2.3");
		  this.addScopeElement(doc, businessScope, "PROCESSID", "urn:fdc:peppol.eu:logistics:bis:transportation_status_only:1");
		  //add BusinessScope to header
		  header.appendChild(businessScope);
		  
		  //add header to root
		  rootElement.appendChild(header);
		  
		  //=====================================
		  //add to root - json payload as Base64
		  //=====================================
		  byte[] bytesEncoded = Base64.encodeBase64(jsonPayload.getBytes());
		  logger.trace("Encoded value is " + new String(bytesEncoded));
		  /* to test decode
		  // Decode data on other side, by processing encoded data
		  byte[] valueDecoded = Base64.decodeBase64(bytesEncoded);
		  logger.info("Decoded value is " + new String(valueDecoded));
		  */
		  Element binaryContent = doc.createElement("BinaryContent");
		  binaryContent.setAttribute("xmlns", "http://peppol.eu/xsd/ticc/envelope/1.0");
		  binaryContent.setAttribute("mimeType", "application/json");
		  binaryContent.setAttribute("encoding", "Base64Binary");
		  binaryContent.setTextContent(new String (bytesEncoded));
		  //add base64 to root
		  rootElement.appendChild(binaryContent);
		  
		  // write dom document to a file
		  FileOutputStream output = new FileOutputStream(this.filenameService.getFileNameXml(msg)); 
		  writeXml(doc, output);
		  
		}catch (Exception e) {
			logger.error(e.toString());
		}
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
