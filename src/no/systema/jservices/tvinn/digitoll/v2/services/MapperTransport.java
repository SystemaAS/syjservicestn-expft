package no.systema.jservices.tvinn.digitoll.v2.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import no.systema.jservices.tvinn.digitoll.v2.dao.*;
import no.systema.jservices.tvinn.expressfortolling2.dao.ConsignmentHouseLevel;
import no.systema.jservices.tvinn.expressfortolling2.dao.TransportDocumentHouseLevel;
import no.systema.jservices.tvinn.expressfortolling2.util.DateUtils;

public class MapperTransport {
	private static final Logger logger = LoggerFactory.getLogger(MapperTransport.class);
	
	//JSON spec: https://api-test.toll.no/api/movement/road/v1/swagger-ui/index.html
	public Transport mapTransport(Object todoDto) {
		
		Transport transport = new Transport();
		//(Mandatory) IssueDate
		//mc.setDocumentIssueDate("2022-08-04T07:49:52Z");
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
		transport.setActiveBorderTransportMeansTransport(this.populateActiveBorderTransportMeans(todoDto));
		
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

		//Mandatory ETA
		//PROD->transport.setEstimatedDateAndTimeOfArrival(new DateUtils().getZuluTimeWithoutMillisecondsUTC(sourceDto.getEmetad(), sourceDto.getEmetat()));
		//	  ->transport.setScheduledDateAndTimeOfArrival(new DateUtils().getZuluTimeWithoutMillisecondsUTC(sourceDto.getEmetad(), sourceDto.getEmetat()));
		//TEST
		transport.setEstimatedDateAndTimeOfArrival(new DateUtils().getZuluTimeWithoutMillisecondsUTC());
		transport.setScheduledDateAndTimeOfArrival(new DateUtils().getZuluTimeWithoutMillisecondsUTC());
		
		/*
		List list1 = new ArrayList();
		//Ref. to the Master Fraktbrev
		list1.add(this.populateTransportDocumentMasterLevel("fraktbrev to Master", "N730"));
		list1.add(this.populateTransportDocumentMasterLevel("second fraktbrev to Master", "N730"));
		//
		ConsignmentMasterLevelTransport ml = new ConsignmentMasterLevelTransport();
		ml.setTransportDocumentMasterLevel(list1);
		//We must map to a list of consignments (0-9999)
		//More consignTra in the list ... TODO
		//TODO
		
		transport.setConsignmentMasterLevelTransport(ml);
		*/
		
		List list = new ArrayList();
		/*ConsignmentMasterLevelTransport ml = new ConsignmentMasterLevelTransport();
		TransportDocumentMasterLevel trDocMasterLevel = new TransportDocumentMasterLevel();
		trDocMasterLevel.setDocumentNumber("one");
		trDocMasterLevel.setType("N730");
		ml.setTransportDocumentMasterLevel(trDocMasterLevel);
		*/
		list.add(this.populateConsignmentMasterLevelTransport("one", "N730"));
		list.add(this.populateConsignmentMasterLevelTransport("two", "N730"));
		/*ConsignmentMasterLevelTransport ml_2 = new ConsignmentMasterLevelTransport();
		TransportDocumentMasterLevel trDocMasterLevel2 = new TransportDocumentMasterLevel();
		trDocMasterLevel2.setDocumentNumber("two");
		trDocMasterLevel2.setType("N730");
		ml_2.setTransportDocumentMasterLevel(trDocMasterLevel);
		list.add(ml_2);
		*/
		//
		transport.setConsignmentMasterLevelTransport(list);
		
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
	
	private String formatDateOfBirth(String value) {
		
		String year = value.substring(0,4);
		String month = value.substring(4,6);
		String day = value.substring(6,8);
		
		return year + "-" + month + "-" + day;
		
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
