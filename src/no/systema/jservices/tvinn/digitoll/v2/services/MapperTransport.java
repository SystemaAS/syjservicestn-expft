package no.systema.jservices.tvinn.digitoll.v2.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import no.systema.jservices.tvinn.digitoll.v2.dao.*;
import no.systema.jservices.tvinn.expressfortolling2.util.DateUtils;

/**
 * SPEC. SWAGGER
 * https://api-test.toll.no/api/movement/road/v2/swagger-ui/index.html
 * 
 * @author oscardelatorre
 * Jun 2023
 */
public class MapperTransport {
	private static final Logger logger = LoggerFactory.getLogger(MapperTransport.class);
	
	
	public Transport mapTransport(Object sourceDto) {
		
		Transport transport = new Transport();
		//(Mandatory) IssueDate
		transport.setDocumentIssueDate(new DateUtils().getZuluTimeWithoutMillisecondsUTC());
		logger.warn(transport.getDocumentIssueDate());
		
		//(Optional) Representative
		//if(StringUtils.isNotEmpty(sourceDto.getEmnar())){
			Representative rep = new Representative();
			rep.setName("todo");
			rep.setIdentificationNumber("todo");
			
			//(Mandatory) this.setAddress("Oslo", "NO", "0010", "Hausemanns gate", "52");
			rep.setAddress(this.setAddress("city", "land code", "postnr", "address", "house nr"));
			//
			List rcommList = new ArrayList();
			rcommList.add(this.populateCommunication("id", "type"));
			rep.setCommunication(rcommList);
			transport.setRepresentative(rep);

		//}
		
		//(Mandatory) ActiveBorderTransMeans
		transport.setActiveBorderTransportMeansTransport(this.populateActiveBorderTransportMeans(sourceDto));
		
		//(Mandatory) Carrier
		Carrier carrier = new Carrier();
		carrier.setName("name");
		carrier.setIdentificationNumber("todo");
		carrier.setAddress(this.setAddress("city", "landcode", "postnr", "address", "housenr"));
		//if(StringUtils.isNotEmpty(sourceDto.getEmemt())) {
			carrier.setCommunication(this.setCommunication("id", "type"));
		//}
		transport.setCarrier(carrier);
		
		//(Mandatory) CustomsOffice
		CustomsOfficeOfFirstEntry cOffice = new CustomsOfficeOfFirstEntry();
		cOffice.setReferenceNumber("refNr");
		transport.setCustomsOfficeOfFirstEntry(cOffice);

		
		//Optional
		//if(sourceDto.getEmetad()>0) {
			//PROD transport.setEstimatedDateAndTimeOfArrival(new DateUtils().getZuluTimeWithoutMillisecondsUTC(sourceDto.getEmetad(), sourceDto.getEmetat()));
			transport.setEstimatedDateAndTimeOfArrival(new DateUtils().getZuluTimeWithoutMillisecondsUTC());
		//}
		//if(sourceDto.getEmetad()>0) {
			//PROD transport.setScheduledDateAndTimeOfArrival(new DateUtils().getZuluTimeWithoutMillisecondsUTC(sourceDto.getEmetad??(), sourceDto.getEmetat??()));
			transport.setScheduledDateAndTimeOfArrival(new DateUtils().getZuluTimeWithoutMillisecondsUTC());
		//}
		
		//Optional
		//if(refsExists) {
			List list = new ArrayList();
			list.add(this.populateConsignmentMasterLevelTransport("one", "N730"));
			list.add(this.populateConsignmentMasterLevelTransport("two", "N730"));
			//
			transport.setConsignmentMasterLevelTransport(list);
		//}
		
		return transport;
	}
	
	private ConsignmentMasterLevelTransport populateConsignmentMasterLevelTransport(String id, String type) {
			
		ConsignmentMasterLevelTransport ml = new ConsignmentMasterLevelTransport();
		TransportDocumentMasterLevel trDocMasterLevel = new TransportDocumentMasterLevel();
		trDocMasterLevel.setDocumentNumber(id);
		trDocMasterLevel.setType(type);
		ml.setTransportDocumentMasterLevel(trDocMasterLevel);
		return ml;
		   
	}
	
	private Communication populateCommunication(String id, String type) {
		
		Communication communication = new Communication();
		communication.setIdentifier(id);
		communication.setType(type);
		return communication;
	}
	
	
	private List<Communication> setCommunication(String id, String type) {
		Communication communication = new Communication();
		communication.setIdentifier(id);
		communication.setType(type);
		List tmp = new ArrayList();
		tmp.add(communication);
		return tmp;
	}
	
	private ActiveBorderTransportMeansTransport populateActiveBorderTransportMeans(Object obj) {
		ActiveBorderTransportMeansTransport ab = new ActiveBorderTransportMeansTransport();
		
		ab.setIdentificationNumber("todo");
		ab.setTypeOfIdentification("todo");
		ab.setTypeOfMeansOfTransport("todo");
		ab.setConveyanceReferenceNumber("todo");
		ab.setCountryCode("todo");
		ab.setModeOfTransportCode("todo");
		
		
		//Mandatory name and communication
		Operator operator = new Operator();
		operator.setName("name");
		List rcommList = new ArrayList();
		rcommList.add(this.populateCommunication("id", "type"));
		operator.setCommunication(rcommList);
		ab.setOperator(operator);

		
		
		return ab;
		
	}
	
	
	private Address setAddress(String city, String country, String postCode, String street, String number) {
		Address address = new Address();
		address.setCity(city);
		address.setCountry(country);
		if(StringUtils.isNotEmpty(postCode)) { address.setPostcode(postCode); }
		if(StringUtils.isNotEmpty(street)) { address.setStreetLine(street); }
		if(StringUtils.isNotEmpty(number)) { address.setNumber(number); }
		
		return address;
	}
	
}
