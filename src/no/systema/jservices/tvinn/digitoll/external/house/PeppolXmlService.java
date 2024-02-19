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

@Service
public class PeppolXmlService {
	private static Logger logger = LoggerFactory.getLogger(PeppolXmlService.class.getName());
	
	@Autowired
	private FilenameService filenameService;
	
	/**
	 * 
	 * @param msg
	 */
	public void writeFileOnDisk(MessageOutbound msg, byte[] bytesBase64Encoded) {
		try {
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
		  //add to header
		  rootElement.appendChild(header);
		  
		  //add to root - json payload as binary
		  Element binaryContent = doc.createElement("BinaryContent");
		  binaryContent.setTextContent(new String (bytesBase64Encoded));
		  rootElement.appendChild(binaryContent);
		  
		  
		  //...create XML elements, and others...
		
		  // write dom document to a file
		  FileOutputStream output = new FileOutputStream(this.filenameService.getFileNameXml(msg)); 
		  writeXml(doc, output);
		  
		}catch (Exception e) {
			logger.error(e.toString());
		}
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
