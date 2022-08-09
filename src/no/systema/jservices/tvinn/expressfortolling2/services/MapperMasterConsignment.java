package no.systema.jservices.tvinn.expressfortolling2.services;

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

public class MapperMasterConsignment {

	public MasterConsignment mapMasterConsignment(SadexmfDto sourceDto) {
		
		MasterConsignment mc = new MasterConsignment();
		//IssueDate
		mc.setDocumentIssueDate("2022-08-04T07:49:52Z");
		//Representative
		Representative rep = new Representative();
		rep.setName(sourceDto.getEmnar());
		rep.setIdentificationNumber(sourceDto.getEmrgr());
		rep.setStatus(sourceDto.getEmstr());
		Address raddress = new Address();
		raddress.setCity(sourceDto.getEmpsr());
		raddress.setCountry(sourceDto.getEmlkr());
		raddress.setStreetLine(sourceDto.getEmnrr());
		raddress.setNumber(sourceDto.getEmnrr());
		rep.setAddress(raddress);
		//
		List rcommList = new ArrayList();
		rcommList.add(this.populateCommunication(sourceDto.getEmemr(), "ME"));
		//rcommList.add(this.populateCommunication("0733794599", "TE"));
		rep.setCommunication(rcommList);
		mc.setRepresentative(rep);
		
		
		//ActiveBorderTransMeans
		mc.setActiveBorderTransportMeans(this.populateActiveBorderTransportMeans(sourceDto));
		
		//Consig.MasterLevel - documentNumber IMPORTANT (parent to houseConsignment documentNumber)
		mc.setConsignmentMasterLevel(this.populateConsignmentMasterLevel(sourceDto, "123123", "N750"));
		//CustomsOffice
		CustomsOfficeOfFirstEntry cOffice = new CustomsOfficeOfFirstEntry();
		cOffice.setReferenceNumber("NO344001");
		mc.setCustomsOfficeOfFirstEntry(cOffice);
		
		
		//Declarant
		Declarant dec = new Declarant();
		dec.setName(sourceDto.getEmnad());
		Address address = new Address();
		address.setCity(sourceDto.getEmpsd());
		address.setCountry(sourceDto.getEmlkd());
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
	
	
	private ConsignmentMasterLevel populateConsignmentMasterLevel(SadexmfDto sourceDto,String docNumber, String type) {
		
		TransportDocumentHouseLevel tdh = new TransportDocumentHouseLevel();
		tdh.setDocumentNumber(docNumber);
		tdh.setType(type);
		//
		
		ConsignmentHouseLevel chl = new ConsignmentHouseLevel();
		chl.setTransportDocumentHouseLevel(tdh);
		
		List tmp = new ArrayList();
		tmp.add(chl);
		ConsignmentMasterLevel cml = new ConsignmentMasterLevel();
		cml.setContainerIndicator(String.valueOf(sourceDto.getEmcn()));
		cml.setGrossMass(String.valueOf(sourceDto.getEmvkb()));
		//
		Carrier carrier = new Carrier();
		carrier.setName(sourceDto.getEmnat());
		carrier.setIdentificationNumber(sourceDto.getEmrgt());
		Address cAddress = this.setAddress(sourceDto.getEmpst(), sourceDto.getEmlkt(), sourceDto.getEmpnt(), sourceDto.getEmad1t(), sourceDto.getEmnrt());
		carrier.setAddress(cAddress);
		carrier.setCommunication(this.setCommunication(sourceDto.getEmemt(), "ME"));
		
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
		te.setContainerIdentificationNumber(sourceDto.getEmcnr());
		List _l1 = new ArrayList();
		_l1.add(te);
		cml.setTranportEquipment(_l1);
		
		TransportDocumentMasterLevel td = new TransportDocumentMasterLevel();
		td.setDocumentNumber("1111112");
		td.setType("N750");
		cml.setTransportDocumentMasterLevel(td);
		cml.setConsignmentHouseLevel(tmp);
		
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
	
	private ActiveBorderTransportMeans populateActiveBorderTransportMeans(SadexmfDto sourceDto) {
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
		operator.setName(sourceDto.getEmsjaf());
		operator.setCitizenship(sourceDto.getEmsjalk());
		//operator.setDateOfBirth("1982-06-22");
		operator.setDateOfBirth(formatDateOfBirth(String.valueOf(sourceDto.getEmsjadt()) ));
		//
		ab.setOperator(operator);
		//Crew
		Crew crew = new Crew();
		crew.setName(sourceDto.getEmsj2f());
		crew.setCitizenship(sourceDto.getEmsj2lk());
		//crew.setDateOfBirth("1982-06-22");
		crew.setDateOfBirth(formatDateOfBirth(String.valueOf(sourceDto.getEmsj2dt()) ));
		List tmp = new ArrayList();
		tmp.add(crew);
		ab.setCrew(tmp);
		
		return ab;
		
	}
	
	private String formatDateOfBirth(String value) {
		
		String year = value.substring(0,4);
		String month = value.substring(4,6);
		String day = value.substring(6,8);
		
		return year + "-" + month + "-" + day;
		
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
