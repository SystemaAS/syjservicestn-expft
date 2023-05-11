package no.systema.jservices.tvinn.expressfortolling2.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import no.systema.jservices.tvinn.expressfortolling2.dao.ActiveBorderTransportMeans;
import no.systema.jservices.tvinn.expressfortolling2.dao.Address;
import no.systema.jservices.tvinn.expressfortolling2.dao.AddressCountry;
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
import no.systema.jservices.tvinn.expressfortolling2.dto.SadexmfDto;
import no.systema.jservices.tvinn.expressfortolling2.util.DateUtils;

public class MapperMasterConsignment {
	private static final Logger logger = LoggerFactory.getLogger(MapperMasterConsignment.class);
	
	//JSON spec: https://api-test.toll.no/api/movement/road/v1/swagger-ui/index.html
	public MasterConsignment mapMasterConsignment(SadexmfDto sourceDto) {
		
		MasterConsignment mc = new MasterConsignment();
		//(Mandatory) IssueDate
		//mc.setDocumentIssueDate("2022-08-04T07:49:52Z");
		mc.setDocumentIssueDate(new DateUtils().getZuluTimeWithoutMillisecondsUTC());
		logger.warn(mc.getDocumentIssueDate());
		
		//(Optional) Representative
		if(StringUtils.isNotEmpty(sourceDto.getEmnar())){
			Representative rep = new Representative();
			rep.setName(sourceDto.getEmnar());
			rep.setIdentificationNumber(sourceDto.getEmrgr());
			//Status
			if(StringUtils.isNotEmpty(sourceDto.getEmstr())){
				rep.setStatus(sourceDto.getEmstr());
			}else {
				rep.setStatus("2");
			}
			//(Mandatory) this.setAddress("Oslo", "NO", "0010", "Hausemanns gate", "52");
			rep.setAddress(this.setAddress(sourceDto.getEmpsr(), sourceDto.getEmlkr(), sourceDto.getEmpnr(), sourceDto.getEmad1r(), sourceDto.getEmnrr()));
			//
			List rcommList = new ArrayList();
			rcommList.add(this.populateCommunication(sourceDto.getEmemr(), sourceDto.getEmemrt()));
			rep.setCommunication(rcommList);
			mc.setRepresentative(rep);

		}
		
		
		//(Mandatory) ActiveBorderTransMeans
		mc.setActiveBorderTransportMeans(this.populateActiveBorderTransportMeans(sourceDto));
		
		//(Mandatory) Consig.MasterLevel - documentNumber IMPORTANT (parent to houseConsignment documentNumber)
		mc.setConsignmentMasterLevel(this.populateConsignmentMasterLevel(sourceDto));
		
		//(Mandatory) CustomsOffice
		CustomsOfficeOfFirstEntry cOffice = new CustomsOfficeOfFirstEntry();
		cOffice.setReferenceNumber(sourceDto.getEmtsd());
		mc.setCustomsOfficeOfFirstEntry(cOffice);
		
		//(Mandatory) Declarant
		Declarant dec = new Declarant();
		dec.setName(sourceDto.getEmnad());
		//this.setAddress("Oslo", "NO", "0010", "Hausemanns gate", "52");
		dec.setAddress(this.setAddress(sourceDto.getEmpsd(), sourceDto.getEmlkd(), sourceDto.getEmpnd(), sourceDto.getEmad1d(), sourceDto.getEmnrd()));
		//
		List commList = new ArrayList();
		commList.add(this.populateCommunication(sourceDto.getEmemd(), sourceDto.getEmemdt()));
		//commList.add(this.populateCommunication("0733794505", "TE"));
		dec.setCommunication(commList);
		mc.setDeclarant(dec);
		
		//(Optional) ReleasedConfirmation 
		if(StringUtils.isNotEmpty(sourceDto.getEmrcem1())) {
			List relList = new ArrayList();
			relList.add(this.populateReleasedConfirmation( sourceDto.getEmrcem1() ));
			if(StringUtils.isNotEmpty(sourceDto.getEmrcem2())) {
				relList.add(this.populateReleasedConfirmation( sourceDto.getEmrcem2() ));
				if(StringUtils.isNotEmpty(sourceDto.getEmrcem3())) {
					relList.add(this.populateReleasedConfirmation( sourceDto.getEmrcem3() ));
				}	
			}
			mc.setReleasedConfirmation(relList);
		}
		
		try {
			//System.out.println(obj.writerWithDefaultPrettyPrinter().writeValueAsString(mc));
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return mc;
	}
	
	
	//JSON spec: https://api-test.toll.no/api/movement/road/v1/swagger-ui/index.html
	/**
	 * Only issueDate and declarant for delete
	 * @param sourceDto
	 * @return
	 */
	public MasterConsignment mapMasterConsignmentForDelete(SadexmfDto sourceDto) {
			
			MasterConsignment mc = new MasterConsignment();
			//IssueDate
			//mc.setDocumentIssueDate("2022-08-04T07:49:52Z");
			mc.setDocumentIssueDate(new DateUtils().getZuluTimeWithoutMillisecondsUTC());
			logger.warn(mc.getDocumentIssueDate());
			//Declarant
			Declarant dec = new Declarant();
			dec.setName(sourceDto.getEmnad());
			dec.setAddress(this.setAddress(sourceDto.getEmpsd(), sourceDto.getEmlkd(), sourceDto.getEmpnd(), sourceDto.getEmad1d(), sourceDto.getEmnrd()));
			
			//Mandatory (communication)
			List commList = new ArrayList();
			commList.add(this.populateCommunication(sourceDto.getEmemd(), sourceDto.getEmemdt()));
			dec.setCommunication(commList);
			//
			mc.setDeclarant(dec);
			
			
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
	/**
	 * Most important child in master: all house documentNumbers of the house consignments
	 * @param id
	 * @param type
	 * @return
	 */
	private ConsignmentHouseLevel populateConsignmentHouseLevel(String id, String type) {
		
	   ConsignmentHouseLevel houseLevel = new ConsignmentHouseLevel();
	   TransportDocumentHouseLevel tdh = new TransportDocumentHouseLevel();
	   tdh.setDocumentNumber(id);
	   tdh.setType(type);
	   houseLevel.setTransportDocumentHouseLevel(tdh);
	   return houseLevel;
	}
	
	private ConsignmentMasterLevel populateConsignmentMasterLevel(SadexmfDto sourceDto) {
		//documentNumbers for all house consignments of this master
		List list = new ArrayList();
		for (SadexhfDto houseDto : sourceDto.getHouseDtoList()) {
			if(StringUtils.isNotEmpty(houseDto.getEhdkh())) {
				// TODO Maybe filter with -->only those with a certain status in ehst,ehst2 or ehst3 ???
				list.add(this.populateConsignmentHouseLevel(houseDto.getEhdkh(), houseDto.getEhdkht()));
			}
		}
		ConsignmentMasterLevel cml = new ConsignmentMasterLevel();
		cml.setConsignmentHouseLevel(list);
		
		//(Mandatory) Container
		cml.setContainerIndicator(String.valueOf(sourceDto.getEmcn()));
		//(Mandatory) Gross mass
		cml.setGrossMass(String.valueOf(sourceDto.getEmvkb()));
		
		//(Mandatory) Carrier
		Carrier carrier = new Carrier();
		carrier.setName(sourceDto.getEmnat());
		carrier.setIdentificationNumber(sourceDto.getEmrgt());
		carrier.setAddress(this.setAddress(sourceDto.getEmpst(), sourceDto.getEmlkt(), sourceDto.getEmpnt(), sourceDto.getEmad1t(), sourceDto.getEmnrt()));
		if(StringUtils.isNotEmpty(sourceDto.getEmemt())) {
			carrier.setCommunication(this.setCommunication(sourceDto.getEmemt(), sourceDto.getEmemtt()));
		}
		cml.setCarrier(carrier);
		
		
		//(Mandatory)TransportDocumentMasterLevel
		TransportDocumentMasterLevel td = new TransportDocumentMasterLevel();
		td.setDocumentNumber(sourceDto.getEmdkm());
		td.setType(sourceDto.getEmdkmt());
		cml.setTransportDocumentMasterLevel(td);
		
		//(Optional) ReferenceNumberUCR
		if(StringUtils.isNotEmpty(sourceDto.getEmucr())) {
			cml.setReferenceNumberUCR(sourceDto.getEmucr());
		}
		
		
		//(Optional) Consignee
		if(StringUtils.isNotEmpty(sourceDto.getEmnam())) {
			Consignee consignee = new Consignee();
			consignee.setName(sourceDto.getEmnam());
			consignee.setIdentificationNumber(sourceDto.getEmrgm());
			consignee.setTypeOfPerson(Integer.valueOf(sourceDto.getEmemmt()));
			if(StringUtils.isNotEmpty(sourceDto.getEmpsm())) {
				consignee.setAddress(this.setAddress(sourceDto.getEmpsm(), sourceDto.getEmlkm(), sourceDto.getEmpnm(), sourceDto.getEmad1m(), sourceDto.getEmnrm()));
			}
			cml.setConsignee(consignee);
		}
		//(Optional) Consignee
		if(StringUtils.isNotEmpty(sourceDto.getEmnas())) {
			Consignor consignor = new Consignor();
			consignor.setName(sourceDto.getEmnas());
			consignor.setIdentificationNumber(sourceDto.getEmrgs());
			consignor.setTypeOfPerson(Integer.valueOf(sourceDto.getEmemst()));
			if(StringUtils.isNotEmpty(sourceDto.getEmpss())) {
				consignor.setAddress(this.setAddress(sourceDto.getEmpss(), sourceDto.getEmlks(), sourceDto.getEmpns(), sourceDto.getEmad1s(), sourceDto.getEmnrs()));
			}
			cml.setConsignor(consignor);
		}
		
		//(Optional) PlaceOfLoading
		if(StringUtils.isNotEmpty(sourceDto.getEmsdl())) {
			PlaceOfLoading pl = new PlaceOfLoading();
			if(StringUtils.isNotEmpty(sourceDto.getEmsdlt())) { pl.setLocation(sourceDto.getEmsdlt()); }
			if(StringUtils.isNotEmpty(sourceDto.getEmsdl())) { pl.setUnloCode(sourceDto.getEmsdl()); }
			if(StringUtils.isNotEmpty(sourceDto.getEmlkl())) {
				AddressCountry addressCountry = new AddressCountry();
				addressCountry.setCountry(sourceDto.getEmlkl());
				pl.setAddress(addressCountry);
			}
			cml.setPlaceOfLoading(pl);
		}
		
		//(Optional) PlaceOfUnloading
		if(StringUtils.isNotEmpty(sourceDto.getEmsdl())) {
			PlaceOfUnloading pul = new PlaceOfUnloading();
			if(StringUtils.isNotEmpty(sourceDto.getEmsdut())) { pul.setLocation(sourceDto.getEmsdut()); }
			if(StringUtils.isNotEmpty(sourceDto.getEmsdu())) { pul.setUnloCode(sourceDto.getEmsdu()); }
			if(StringUtils.isNotEmpty(sourceDto.getEmlku())) {
				AddressCountry addressCountry = new AddressCountry();
				addressCountry.setCountry(sourceDto.getEmlku());
				pul.setAddress(addressCountry);
			}
			cml.setPlaceOfUnloading(pul);
		}
		
		//TODO db-field ??? (Optional) PassiveBorderTransportMeans
		/*PassiveBorderTransportMeans pbtm = new PassiveBorderTransportMeans();
		cml.setPassiveBorderTransportMeans(pbtm);
		*/
		
		//(Optional) Transp.Equipment
		if(StringUtils.isNotEmpty(sourceDto.getEmcnr())) {
			TransportEquipment te = new TransportEquipment();
			te.setContainerIdentificationNumber(sourceDto.getEmcnr());
			List _l1 = new ArrayList();
			_l1.add(te);
			cml.setTranportEquipment(_l1);
		}
		
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
		
		ab.setIdentificationNumber(sourceDto.getEmkmrk());
		ab.setTypeOfIdentification(sourceDto.getEmktyp());
		ab.setTypeOfMeansOfTransport(sourceDto.getEmktm());
		ab.setNationalityCode(sourceDto.getEmklk());
		ab.setModeOfTransportCode(sourceDto.getEmktkd());
		
		DateUtils dateUtils = new DateUtils();
		//PROD 
		if(sourceDto.getEmatdd()>0) {
			ab.setActualDateAndTimeOfDeparture( dateUtils.getZuluTimeWithoutMillisecondsUTC(sourceDto.getEmatdd(), sourceDto.getEmatdt()) );
		}
		if(sourceDto.getEmetdd()>0) {
			ab.setEstimatedDateAndTimeOfDeparture(dateUtils.getZuluTimeWithoutMillisecondsUTC(sourceDto.getEmetdd(), sourceDto.getEmetdt()));
		}
		//Mandatory ETA
		ab.setEstimatedDateAndTimeOfArrival(dateUtils.getZuluTimeWithoutMillisecondsUTC(sourceDto.getEmetad(), sourceDto.getEmetat()));
		
		
		//Mandatory only name
		Operator operator = new Operator();
		operator.setName(sourceDto.getEmsjaf());
		//(OBSOLETE)
		/*
		if(StringUtils.isNotEmpty(sourceDto.getEmsjalk())){
			operator.setCitizenship(sourceDto.getEmsjalk());
		}
		if(sourceDto.getEmsjadt()>0){
			operator.setDateOfBirth(formatDateOfBirth(String.valueOf(sourceDto.getEmsjadt()) ));
		}*/
		//
		ab.setOperator(operator);

		//(Optional) Crew
		if(StringUtils.isNotEmpty(sourceDto.getEmsj2f())) {
			Crew crew = new Crew();
			crew.setName(sourceDto.getEmsj2f());
			crew.setCitizenship(sourceDto.getEmsj2lk());
			//crew.setDateOfBirth("1982-06-22");
			crew.setDateOfBirth(formatDateOfBirth(String.valueOf(sourceDto.getEmsj2dt()) ));
			List tmp = new ArrayList();
			tmp.add(crew);
			ab.setCrew(tmp);
		}
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
		if(StringUtils.isNotEmpty(postCode)) { address.setPostcode(postCode); }
		if(StringUtils.isNotEmpty(street)) { address.setStreetLine(street); }
		if(StringUtils.isNotEmpty(number)) { address.setNumber(number); }
		
		return address;
	}
	
}
