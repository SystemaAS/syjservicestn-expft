package no.systema.jservices.tvinn.expressfortolling2.dao;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Tester {

	public static void main(String[] args) {
		Tester tester = new Tester();
		// TODO Auto-generated method stub
		ObjectMapper obj = new ObjectMapper();
		MasterConsignment mc = new MasterConsignment();
		//IssueDate
		mc.setDocumentIssueDate("20220704");
		//Representative
		Representative rep = new Representative();
		rep.setName("Bring AS");
		rep.setIdentificationNumber("951357482");
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
		rcommList.add(tester.populateCommunication("en-epost@mail.com", "EM"));
		rcommList.add(tester.populateCommunication("0733794599", "TE"));
		rep.setCommunication(rcommList);
		mc.setRepresentative(rep);
		
		
		//ActiveBorderTransMeans
		mc.setActiveBorderTransportMeans(tester.populateActiveBorderTransportMeans());
		//Consig.MasterLevel
		mc.setConsignmentMasterLevel(tester.populateConsignmentMasterLevel("string", "N750"));
		//CustomsOffice
		CustomsOfficeOfFirstEntry cOffice = new CustomsOfficeOfFirstEntry();
		cOffice.setReferenceNumber("NO344001");
		mc.setCustomsOfficeOfFirstEntry(cOffice);
		
		
		//Declarant
		Declarant dec = new Declarant();
		dec.setName("John Doe");
		Address address = new Address();
		address.setCountry("NO");
		dec.setAddress(address);
		//
		List commList = new ArrayList();
		commList.add(tester.populateCommunication("xxx@gmail.com", "EM"));
		commList.add(tester.populateCommunication("0733794505", "TE"));
		dec.setCommunication(commList);
		mc.setDeclarant(dec);
		
		//ReleasedConfirmation
		List relList = new ArrayList();
		relList.add(tester.populateReleasedConfirmation("yyy@doe.com"));
		mc.setReleasedConfirmation(relList);
		
		
		try {
			System.out.println(obj.writerWithDefaultPrettyPrinter().writeValueAsString(mc));
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private Communication populateCommunication(String id, String type) {
		
		Communication communication = new Communication();
		communication.setIdentifier(id);
		communication.setType(type);
		return communication;
	}
	
	private ConsignmentMasterLevel populateConsignmentMasterLevel(String docNumber, String type) {
		
		TransportDocumentHouseLevel tdh = new TransportDocumentHouseLevel();
		tdh.setDocumentNumber(docNumber);
		tdh.setType(type);
		//
		
		ConsignmentHouseLevel chl = new ConsignmentHouseLevel();
		//chl.setTransportDocumentHouseLevel(tdh);
		
		List tmp = new ArrayList();
		tmp.add(chl);
		ConsignmentMasterLevel cml = new ConsignmentMasterLevel();
		//
		Carrier carrier = new Carrier();
		cml.setCarrier(carrier);
		//
		Consignee consignee = new Consignee();
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
		//
		TransportEquipment te = new TransportEquipment();
		te.setContainerIdentificationNumber("1234567SAS");
		List _l1 = new ArrayList();
		_l1.add(te);
		cml.setTranportEquipment(_l1);
		
		TransportDocumentMasterLevel td = new TransportDocumentMasterLevel();
		cml.setTransportDocumentMasterLevel(td);
		//cml.setConsignmentHouseLevel(tmp);
		
		return cml;
	}
	
	private ActiveBorderTransportMeans populateActiveBorderTransportMeans() {
		ActiveBorderTransportMeans ab = new ActiveBorderTransportMeans();
		ab.setIdentificationNumber("DK 123654");
		ab.setTypeOfIdentification("30");
		ab.setTypeOfMeansOfTransport("150");
		ab.setNationalityCode("SE");
		ab.setModeOfTransportCode("3");
		ab.setActualDateAndTimeOfDeparture("2022-11-20T07:49:52Z");
		ab.setEstimatedDateAndTimeOfDeparture("2022-11-20T07:49:52Z");
		ab.setEstimatedDateAndTimeOfArrival("2022-11-20T07:49:52Z");
		//
		Operator operator = new Operator();
		operator.setName("Kari Nordman");
		//operator.setCitizenship("UK");
		//operator.setDateOfBirth("1982-06-22");
		//
		ab.setOperator(operator);
		//Crew
		Crew crew = new Crew();
		crew.setName(null);
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

}
