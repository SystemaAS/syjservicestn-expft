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
 * 
 */
public class MapperMasterConsignment {
	private static final Logger logger = LoggerFactory.getLogger(MapperMasterConsignment.class);
	
	
	public MasterConsignment mapMasterConsignment(Object sourceDto) {
		
		MasterConsignment mc = new MasterConsignment();
		//(Mandatory) IssueDate
		//mc.setDocumentIssueDate("2022-08-04T07:49:52Z");
		mc.setDocumentIssueDate(new DateUtils().getZuluTimeWithoutMillisecondsUTC());
		logger.warn(mc.getDocumentIssueDate());
		
		//(Optional) Representative
		//if(StringUtils.isNotEmpty(dto.getEmnar())){
			Representative rep = new Representative();
			rep.setName("sourceDto.getEmnar()");
			rep.setIdentificationNumber("sourceDto.getEmrgr()");
			//(Mandatory) this.setAddress("Oslo", "NO", "0010", "Hausemanns gate", "52");
			rep.setAddress(this.setAddress("sourceDto.getEmpsr()", "sourceDto.getEmlkr()", "sourceDto.getEmpnr()", "sourceDto.getEmad1r()", "sourceDto.getEmnrr()"));
			//
			List rcommList = new ArrayList();
			rcommList.add(this.populateCommunication("sourceDto.getEmemr()", "sourceDto.getEmemrt()"));
			rep.setCommunication(rcommList);
			
			//(Optional) ReleasedConfirmation 
			//if(StringUtils.isNotEmpty("sourceDto.getEmrcem1()")) {
				List relList = new ArrayList();
				relList.add(this.populateReleasedConfirmation( "sourceDto.getEmrcem1()" ));
				//if(StringUtils.isNotEmpty(sourceDto.getEmrcem2())) {
					relList.add(this.populateReleasedConfirmation( "sourceDto.getEmrcem2()" ));
					//if(StringUtils.isNotEmpty(sourceDto.getEmrcem3())) {
						relList.add(this.populateReleasedConfirmation( "sourceDto.getEmrcem3()" ));
					//}	
				//}
				rep.setReleasedConfirmation(relList);
			//}
			
			mc.setRepresentative(rep);

		//}
		
		
		//(Mandatory) Consig.MasterLevel - documentNumber IMPORTANT (parent to houseConsignment documentNumber)
		mc.setConsignmentMasterLevel(this.populateConsignmentMasterLevel(sourceDto));
		
		
		
		try {
			//System.out.println(obj.writerWithDefaultPrettyPrinter().writeValueAsString(mc));
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return mc;
	}
	
	
	
	/**
	 * Only issueDate for delete
	 * @param sourceDto
	 * @return
	 */
	
	public MasterConsignment mapMasterConsignmentForDelete() {
			
			MasterConsignment mc = new MasterConsignment();
			//IssueDate
			mc.setDocumentIssueDate(new DateUtils().getZuluTimeWithoutMillisecondsUTC());
			logger.warn(mc.getDocumentIssueDate());
			
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
	/*TODO
	private ConsignmentHouseLevel populateConsignmentHouseLevel(String id, String type) {
		
	   ConsignmentHouseLevel houseLevel = new ConsignmentHouseLevel();
	   TransportDocumentHouseLevel tdh = new TransportDocumentHouseLevel();
	   tdh.setDocumentNumber(id);
	   tdh.setType(type);
	   houseLevel.setTransportDocumentHouseLevel(tdh);
	   return houseLevel;
	}
	*/
	
	/**
	 * 
	 * @param sourceDto
	 * @return
	 */
	private ConsignmentMasterLevel populateConsignmentMasterLevel(Object sourceDto) {
		
		ConsignmentMasterLevel cml = new ConsignmentMasterLevel();
		
		//documentNumbers for all house consignments of this master
		/* BARA för POSTSÄCKAR ? -->List list = new ArrayList();
		for (SadexhfDto houseDto : sourceDto.getHouseDtoList()) {
			if(StringUtils.isNotEmpty(houseDto.getEhdkh())) {
				// TODO Maybe filter with -->only those with a certain status in ehst,ehst2 or ehst3 ???
				list.add(this.populateConsignmentHouseLevel(houseDto.getEhdkh(), houseDto.getEhdkht()));
			}
		}
		cml.setConsignmentHouseLevel(list);
		*/
		
		//(Mandatory) Container
		cml.setContainerIndicator(Integer.valueOf("0"));//sourceDto.getEmcn()
		//(Mandatory) Gross mass
		cml.setGrossMass(Double.valueOf("0.0"));//sourceDto.getEmvkb()
		
		//(Mandatory) Carrier
		Carrier carrier = new Carrier();
		carrier.setIdentificationNumber("sourceDto.getEmrgt()");
		cml.setCarrier(carrier);
		
		//(Optional) Consignee
		//if(StringUtils.isNotEmpty(sourceDto.getEmnam())) {
			Consignee consignee = new Consignee();
			consignee.setName("sourceDto.getEmnam()");
			consignee.setIdentificationNumber("sourceDto.getEmrgm()");
			consignee.setTypeOfPerson(Integer.valueOf("0"));//sourceDto.getEmemmt()
			if(StringUtils.isNotEmpty("sourceDto.getEmpsm()")) {
				consignee.setAddress(this.setAddress("sourceDto.getEmpsm()", "sourceDto.getEmlkm()", "sourceDto.getEmpnm()", "sourceDto.getEmad1m()", "sourceDto.getEmnrm()"));
			}
			//if(StringUtils.isNotEmpty(sourceDto.getEmemt())) {
				consignee.setCommunication(this.setCommunication("id", "type"));
			//}
			cml.setConsignee(consignee);
		//}
		//(Optional) Consignor
		//if(StringUtils.isNotEmpty(sourceDto.getEmnas())) {
			Consignor consignor = new Consignor();
			consignor.setName("sourceDto.getEmnas()");
			consignor.setIdentificationNumber("sourceDto.getEmrgs()");
			consignor.setTypeOfPerson(Integer.valueOf("0"));//sourceDto.getEmemst()
			if(StringUtils.isNotEmpty("sourceDto.getEmpss()")) {
				consignor.setAddress(this.setAddress("sourceDto.getEmpss()", "sourceDto.getEmlks()", "sourceDto.getEmpns()", "sourceDto.getEmad1s()", "sourceDto.getEmnrs()"));
			}
			//if(StringUtils.isNotEmpty(sourceDto.getEmemt())) {
				consignor.setCommunication(this.setCommunication("id", "type"));
			//}
			cml.setConsignor(consignor);
		//}
				
		//(Mandatory)TransportDocumentMasterLevel
		TransportDocumentMasterLevel td = new TransportDocumentMasterLevel();
		td.setDocumentNumber("sourceDto.getEmdkm()");
		td.setType("sourceDto.getEmdkmt()");
		cml.setTransportDocumentMasterLevel(td);
		
		//(Optional) Transp.Equipment
		//if(!this.populateTransportEquipment(sourceDto).isEmpty()) {
			cml.setTranportEquipment(this.populateTransportEquipment(sourceDto));
		//}
		
	
		//TODO (Optional)consignmentHouseLevel
		
		//(Optional) PlaceOfLoading
		//if(StringUtils.isNotEmpty(sourceDto.getEmsdl())) {
			PlaceOfLoading pl = new PlaceOfLoading();
			if(StringUtils.isNotEmpty("sourceDto.getEmsdlt()")) { pl.setLocation("sourceDto.getEmsdlt()"); }
			if(StringUtils.isNotEmpty("sourceDto.getEmsdl()")) { pl.setUnloCode("sourceDto.getEmsdl()"); }
			if(StringUtils.isNotEmpty("sourceDto.getEmlkl()")) {
				AddressCountry addressCountry = new AddressCountry();
				addressCountry.setCountry("sourceDto.getEmlkl()");
				pl.setAddress(addressCountry);
			}
			cml.setPlaceOfLoading(pl);
		//}
		
		//(Optional) PlaceOfUnloading
		//if(StringUtils.isNotEmpty(sourceDto.getEmsdl())) {
			PlaceOfUnloading pul = new PlaceOfUnloading();
			if(StringUtils.isNotEmpty("sourceDto.getEmsdut()")) { pul.setLocation("sourceDto.getEmsdut()"); }
			if(StringUtils.isNotEmpty("sourceDto.getEmsdu()")) { pul.setUnloCode("sourceDto.getEmsdu()"); }
			if(StringUtils.isNotEmpty("sourceDto.getEmlku()")) {
				AddressCountry addressCountry = new AddressCountry();
				addressCountry.setCountry("sourceDto.getEmlku()");
				pul.setAddress(addressCountry);
			}
			cml.setPlaceOfUnloading(pul);
		//}
			
		//(Optional) PlaceOfDelivery
		//if(StringUtils.isNotEmpty(sourceDto.getEmsdl())) {
			PlaceOfDelivery pdel = new PlaceOfDelivery();
			if(StringUtils.isNotEmpty("sourceDto.getEmsdut()")) { pdel.setLocation("sourceDto.getEmsdut()"); }
			if(StringUtils.isNotEmpty("sourceDto.getEmsdu()")) { pdel.setUnloCode("sourceDto.getEmsdu()"); }
			if(StringUtils.isNotEmpty("sourceDto.getEmlku()")) {
				AddressCountry addressCountry = new AddressCountry();
				addressCountry.setCountry("sourceDto.getEmlku()");
				pdel.setAddress(addressCountry);
			}
			cml.setPlaceOfDelivery(pdel);
		//}
		
		
		
		return cml;
		
	}
	
	private List<TransportEquipment> populateTransportEquipment(Object sourceDto) {
		List<TransportEquipment> listTranspEquip = new ArrayList<>();
		//if(StringUtils.isNotEmpty(sourceDto.getEmcnr())) {
			TransportEquipment te = new TransportEquipment();
			//all below mandatory
			te.setContainerIdentificationNumber("sourceDto.getEmcnr()");
			te.setContainerSizeAndType("todo");
			te.setContainerPackedStatus("todo");
			te.setContainerSupplierType("todo");
			listTranspEquip.add(te);
		//}
		//2 or more	
		/*if(StringUtils.isNotEmpty(sourceDto.getEmcnr2())) {
			TransportEquipment te = new TransportEquipment();
			//all below mandatory
			te.setContainerIdentificationNumber("sourceDto.getEmcnr2()");
			te.setContainerSizeAndType("todo");
			te.setContainerPackedStatus("todo");
			te.setContainerSupplierType("todo");
			listTranspEquip.add(te);
		}*/

			
		return listTranspEquip;
		
	}
	
	private List<Communication> setCommunication(String id, String type) {
		Communication communication = new Communication();
		communication.setIdentifier(id);
		communication.setType(type);
		List tmp = new ArrayList();
		tmp.add(communication);
		return tmp;
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
