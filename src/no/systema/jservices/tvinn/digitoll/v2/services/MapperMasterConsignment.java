package no.systema.jservices.tvinn.digitoll.v2.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import no.systema.jservices.tvinn.digitoll.v2.dao.*;
import no.systema.jservices.tvinn.digitoll.v2.dto.SadmomfDto;
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
	
	
	public MasterConsignment mapMasterConsignment(SadmomfDto dto) {
		
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
		mc.setConsignmentMasterLevel(this.populateConsignmentMasterLevel(dto));
		
		
		
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
	private ConsignmentMasterLevel populateConsignmentMasterLevel(SadmomfDto dto) {
		
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
		cml.setContainerIndicator(dto.getEmcn());//sourceDto.getEmcn()
		//(Mandatory) Gross mass
		cml.setGrossMass(dto.getEmvkb());//sourceDto.getEmvkb()
		
		//(Mandatory) Carrier
		Carrier carrier = new Carrier();
		carrier.setIdentificationNumber(dto.getEmrgt() );
		cml.setCarrier(carrier);
		
		//(Optional) Consignee
		if(StringUtils.isNotEmpty(dto.getEmnam())) {
			Consignee consignee = new Consignee();
			consignee.setName(dto.getEmnam() );
			consignee.setIdentificationNumber(dto.getEmrgm() );
			consignee.setTypeOfPerson(Integer.valueOf("0"));//sourceDto.getEmemmt()
			if(StringUtils.isNotEmpty(dto.getEmpsm())) {
				consignee.setAddress(this.setAddress(dto.getEmpsm(), dto.getEmlkm(), dto.getEmpnm(), dto.getEmad1m(), dto.getEmnrm() ));
			}
			if(StringUtils.isNotEmpty(dto.getEmemm())) {
				consignee.setCommunication(this.setCommunication(dto.getEmemm(), dto.getEmemmt()));
			}
			cml.setConsignee(consignee);
		}
		//(Optional) Consignor
		if(StringUtils.isNotEmpty(dto.getEmnas())) {
			Consignor consignor = new Consignor();
			consignor.setName(dto.getEmnas());
			consignor.setIdentificationNumber(dto.getEmrgs());
			consignor.setTypeOfPerson(Integer.valueOf(dto.getEmemst()));
			if(StringUtils.isNotEmpty(dto.getEmpss())) {
				consignor.setAddress(this.setAddress(dto.getEmpss(),dto.getEmlks(),dto.getEmpns(),dto.getEmad1s(),dto.getEmnrs()));
			}
			if(StringUtils.isNotEmpty(dto.getEmems())) {
				consignor.setCommunication(this.setCommunication(dto.getEmems(), dto.getEmemst()));
			}
			cml.setConsignor(consignor);
		}
				
		//(Mandatory)TransportDocumentMasterLevel
		TransportDocumentMasterLevel td = new TransportDocumentMasterLevel();
		td.setDocumentNumber(dto.getEmdkm());
		td.setType(dto.getEmdkmt());
		cml.setTransportDocumentMasterLevel(td);
		
		//(Optional) Transp.Equipment
		if(StringUtils.isNotEmpty(dto.getEmc1id())) {
			cml.setTranportEquipment(this.populateTransportEquipment(dto));
		}
		
	
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
	
	private List<TransportEquipment> populateTransportEquipment(SadmomfDto dto) {
		List<TransportEquipment> listTranspEquip = new ArrayList<>();
		if(StringUtils.isNotEmpty(dto.getEmc1id())) {
			TransportEquipment te = new TransportEquipment();
			//all below mandatory
			te.setContainerIdentificationNumber(dto.getEmc1id());
			te.setContainerSizeAndType(dto.getEmc1ty());
			te.setContainerPackedStatus(dto.getEmc1ps());
			te.setContainerSupplierType(dto.getEmc1ss());
			listTranspEquip.add(te);
		}
		//2 or more	
		if(StringUtils.isNotEmpty(dto.getEmc2id())) {
			TransportEquipment te = new TransportEquipment();
			//all below mandatory
			te.setContainerIdentificationNumber(dto.getEmc2id());
			te.setContainerSizeAndType(dto.getEmc2ty());
			te.setContainerPackedStatus(dto.getEmc2ps());
			te.setContainerSupplierType(dto.getEmc2ss());
			listTranspEquip.add(te);
		}
		
			
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
