package no.systema.jservices.tvinn.expressfortolling.api;

import java.util.ArrayList;
import java.util.List;

import no.systema.jservices.tvinn.expressfortolling2.dao.ActiveBorderTransportMeans;
import no.systema.jservices.tvinn.expressfortolling2.dao.Address;
import no.systema.jservices.tvinn.expressfortolling2.dao.Carrier;
import no.systema.jservices.tvinn.expressfortolling2.dao.Communication;
import no.systema.jservices.tvinn.expressfortolling2.dao.Consignee;
import no.systema.jservices.tvinn.expressfortolling2.dao.ConsignmentHouseLevel;
import no.systema.jservices.tvinn.expressfortolling2.dao.ConsignmentMasterLevel;
import no.systema.jservices.tvinn.expressfortolling2.dao.Consignor;
import no.systema.jservices.tvinn.expressfortolling2.dao.Crew;
import no.systema.jservices.tvinn.expressfortolling2.dao.CustomsOfficeOfFirstEntry;
import no.systema.jservices.tvinn.expressfortolling2.dao.Declarant;
import no.systema.jservices.tvinn.expressfortolling2.dao.MasterConsignment;
import no.systema.jservices.tvinn.expressfortolling2.dao.Operator;
import no.systema.jservices.tvinn.expressfortolling2.dao.PassiveBorderTransportMeans;
import no.systema.jservices.tvinn.expressfortolling2.dao.PlaceOfLoading;
import no.systema.jservices.tvinn.expressfortolling2.dao.PlaceOfUnloading;
import no.systema.jservices.tvinn.expressfortolling2.dao.ReleasedConfirmation;
import no.systema.jservices.tvinn.expressfortolling2.dao.Representative;
import no.systema.jservices.tvinn.expressfortolling2.dao.TransportDocumentHouseLevel;
import no.systema.jservices.tvinn.expressfortolling2.dao.TransportDocumentMasterLevel;
import no.systema.jservices.tvinn.expressfortolling2.dao.TransportEquipment;
import no.systema.jservices.tvinn.expressfortolling2.dto.SadexhfDto;

public class TestMasterConsignmentDao {

	public MasterConsignment setMasterConsignment() {
		
		MasterConsignment mc = new MasterConsignment();
		//IssueDate
		mc.setDocumentIssueDate("2022-08-04T07:49:52Z");
		//Representative
		Representative rep = new Representative();
		rep.setName("Systema AS");
		rep.setIdentificationNumber("936809219");
		rep.setStatus("2");
		Address raddress = new Address();
		raddress.setCity("OSLO");
		raddress.setCountry("NO");
		raddress.setStreetLine("Hausemanns gate");
		raddress.setPostcode("0530");
		raddress.setNumber("52F");
		rep.setAddress(raddress);
		//
		List rcommList = new ArrayList();
		rcommList.add(this.populateCommunication("en-epost@mail.com", "ME"));
		rcommList.add(this.populateCommunication("0733794599", "TE"));
		rep.setCommunication(rcommList);
		mc.setRepresentative(rep);
		
		
		//ActiveBorderTransMeans
		mc.setActiveBorderTransportMeans(this.populateActiveBorderTransportMeans());
		//Consig.MasterLevel
		mc.setConsignmentMasterLevel(this.populateConsignmentMasterLevel("123123", "N750"));
		//CustomsOffice
		CustomsOfficeOfFirstEntry cOffice = new CustomsOfficeOfFirstEntry();
		cOffice.setReferenceNumber("NO344001");
		mc.setCustomsOfficeOfFirstEntry(cOffice);
		
		
		//Declarant
		Declarant dec = new Declarant();
		dec.setName("John Doe");
		Address address = new Address();
		address.setCity("Oslo");
		address.setCountry("NO");
		dec.setAddress(address);
		//
		List commList = new ArrayList();
		commList.add(this.populateCommunication("xxx@gmail.com", "EM"));
		commList.add(this.populateCommunication("0733794505", "TE"));
		dec.setCommunication(commList);
		mc.setDeclarant(dec);
		
		//ReleasedConfirmation
		List relList = new ArrayList();
		relList.add(this.populateReleasedConfirmation("yyy@doe.com"));
		mc.setReleasedConfirmation(relList);
		
		
		try {
			//System.out.println(obj.writerWithDefaultPrettyPrinter().writeValueAsString(mc));
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return mc;
	}
	
	
	
	
	private Communication populateCommunication(String id, String type) {
		
		Communication communication = new Communication();
		communication.setIdentifier(id);
		communication.setType(type);
		return communication;
	}
	
   private ConsignmentHouseLevel populateConsignmentHouseLevel(String id, String type) {
		
	   ConsignmentHouseLevel houseLevel = new ConsignmentHouseLevel();
	   TransportDocumentHouseLevel tdh = new TransportDocumentHouseLevel();
	   tdh.setDocumentNumber(id);
	   tdh.setType(type);
	   houseLevel.setTransportDocumentHouseLevel(tdh);
	   return houseLevel;
	}
	
	
	private ConsignmentMasterLevel populateConsignmentMasterLevel(String docNumber, String type) {
		
		List list = new ArrayList();
		list.add(this.populateConsignmentHouseLevel("1233334566", "N740"));
		list.add(this.populateConsignmentHouseLevel("7898798789", "N713"));
		
		ConsignmentMasterLevel cml = new ConsignmentMasterLevel();
		cml.setConsignmentHouseLevel(list);
		
		
		cml.setContainerIndicator("1");
		cml.setGrossMass("100");
		//
		Carrier carrier = new Carrier();
		carrier.setName("Kari Nordman");
		carrier.setIdentificationNumber("951623487");
		Address cAddress = this.setAddress("Oslo", "NO", "0010", "Hausemanns gate", "52");
		carrier.setAddress(cAddress);
		carrier.setCommunication(this.setCommunication("mail@mail.se", "ME"));
		
		cml.setCarrier(carrier);
		//
		/*Consignee consignee = new Consignee();
		cml.setConsignee(consignee);
		//
		Consignor consignor = new Consignor();
		cml.setConsignor(consignor);
		//
		PlaceOfLoading pl = new PlaceOfLoading();
		PlaceOfUnloading punl = new PlaceOfUnloading();
		cml.setPlaceOfLoading(pl);
		cml.setPlaceOfUnloading(punl);
		
		//
		PassiveBorderTransportMeans pbtm = new PassiveBorderTransportMeans();
		cml.setPassiveBorderTransportMeans(pbtm);
		*/
		//
		TransportEquipment te = new TransportEquipment();
		te.setContainerIdentificationNumber("1234567SAS");
		List _l1 = new ArrayList();
		_l1.add(te);
		cml.setTranportEquipment(_l1);
		
		TransportDocumentMasterLevel td = new TransportDocumentMasterLevel();
		td.setDocumentNumber("1111112");
		td.setType("N750");
		cml.setTransportDocumentMasterLevel(td);
		
		
		return cml;
	}
	
	private List<Communication> setCommunication(String id, String type) {
		Communication communication = new Communication();
		communication.setIdentifier(id);
		communication.setType(type);
		List tmp = new ArrayList();
		tmp.add(communication);
		return tmp;
	}
	
	private ActiveBorderTransportMeans populateActiveBorderTransportMeans() {
		ActiveBorderTransportMeans ab = new ActiveBorderTransportMeans();
		ab.setIdentificationNumber("DK 123654");
		ab.setTypeOfIdentification("30");
		ab.setTypeOfMeansOfTransport("150");
		ab.setNationalityCode("SE");
		ab.setModeOfTransportCode("3");
		ab.setActualDateAndTimeOfDeparture("2022-09-20T07:49:52Z");
		ab.setEstimatedDateAndTimeOfDeparture("2022-09-20T07:49:52Z");
		ab.setEstimatedDateAndTimeOfArrival("2022-09-20T07:49:52Z");
		//
		Operator operator = new Operator();
		operator.setName("Kari Nordman");
		operator.setCitizenship("UK");
		operator.setDateOfBirth("1982-06-22");
		//
		ab.setOperator(operator);
		//Crew
		Crew crew = new Crew();
		crew.setName("Per Norman");
		crew.setCitizenship("NO");
		crew.setDateOfBirth("1982-06-22");
		List tmp = new ArrayList();
		tmp.add(crew);
		ab.setCrew(tmp);
		
		return ab;
		
	}
	
	private ReleasedConfirmation populateReleasedConfirmation(String email) {
		ReleasedConfirmation releasedConfirmation = new ReleasedConfirmation();
		releasedConfirmation.setEmailAddress(email);
		return releasedConfirmation;
	}
	
	private Address setAddress(String city, String country, String postCode, String street, String number) {
		Address address = new Address();
		address.setCity(city);
		address.setCountry(country);
		address.setPostcode(postCode);
		address.setStreetLine(street);
		address.setNumber(number);
		
		return address;
	}
	
}
