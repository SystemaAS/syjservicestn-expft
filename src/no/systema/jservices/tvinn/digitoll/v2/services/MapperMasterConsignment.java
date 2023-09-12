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
		
		//(Optional) Representative (do not exist at db level, fetch from Transport if applicable)
		//The criteria to send the Representative at a Master level is that there is a confirmation-email wanted at the user-level
		//It will be the copy of the representative at a Tranport level
		if(StringUtils.isNotEmpty(dto.getEmrcem1())){
			if(StringUtils.isNotEmpty(dto.getTransportDto().getEtnar())) {
				Representative rep = new Representative();
				rep.setName(dto.getTransportDto().getEtnar());
				rep.setIdentificationNumber(dto.getTransportDto().getEtrgr());
				//(Mandatory) this.setAddress("Oslo", "NO", "0010", "Hausemanns gate", "52");
				rep.setAddress(this.setAddress(dto.getTransportDto().getEtpsr(), dto.getTransportDto().getEtlkr(), dto.getTransportDto().getEtpnr(), dto.getTransportDto().getEtad1r(), dto.getTransportDto().getEtnrr()));
				//
				List rcommList = new ArrayList();
				rcommList.add(this.populateCommunication(dto.getTransportDto().getEtemr(), dto.getTransportDto().getEtemrt()));
				rep.setCommunication(rcommList);
				//confirmastion emails
				List rconfirmationList = new ArrayList();
				rconfirmationList.add(this.populateConfirmation(dto.getEmrcem1()));
				if(StringUtils.isNotEmpty(dto.getEmrcem2())) {
					rconfirmationList.add(this.populateConfirmation(dto.getEmrcem2()));
				}
				if(StringUtils.isNotEmpty(dto.getEmrcem3())) {
					rconfirmationList.add(this.populateConfirmation(dto.getEmrcem3()));
				}
				rep.setReleasedConfirmation(rconfirmationList);
				
				mc.setRepresentative(rep);
			}

		}
		
		
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
	private ReleasedConfirmation populateConfirmation(String email ) {
		ReleasedConfirmation confirmation = new ReleasedConfirmation();
		confirmation.setEmailAddress(email);
		return confirmation;
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
			consignee.setTypeOfPerson(dto.getEmtppm()); //1- Fysisk person , 2-Juridisk person (företag). Add field in db
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
			consignor.setTypeOfPerson(dto.getEmtppm()); //1- Fysisk person , 2-Juridisk person (företag). Add field in db
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
		if(StringUtils.isNotEmpty(dto.getEmlkl())) {
			PlaceOfLoading pl = new PlaceOfLoading();
			if(StringUtils.isNotEmpty(dto.getEmsdlt())) { pl.setLocation(dto.getEmsdlt()); }
			if(StringUtils.isNotEmpty(dto.getEmsdl())) { pl.setUnloCode(dto.getEmsdl()); }
			if(StringUtils.isNotEmpty(dto.getEmlkl())) {
				AddressCountry addressCountry = new AddressCountry();
				addressCountry.setCountry(dto.getEmlkl());
				pl.setAddress(addressCountry);
			}
			cml.setPlaceOfLoading(pl);
		}
		
		//(Optional) PlaceOfUnloading
		if(StringUtils.isNotEmpty(dto.getEmlku())) {
			PlaceOfUnloading pul = new PlaceOfUnloading();
			if(StringUtils.isNotEmpty(dto.getEmsdut())) { pul.setLocation(dto.getEmsdut()); }
			if(StringUtils.isNotEmpty(dto.getEmsdu())) { pul.setUnloCode(dto.getEmsdu()); }
			if(StringUtils.isNotEmpty(dto.getEmlku())) {
				AddressCountry addressCountry = new AddressCountry();
				addressCountry.setCountry(dto.getEmlku());
				pul.setAddress(addressCountry);
			}
			cml.setPlaceOfUnloading(pul);
		}
			
		//(Optional) PlaceOfDelivery
		if(StringUtils.isNotEmpty(dto.getEmlkd())) {
			PlaceOfDelivery pdel = new PlaceOfDelivery();
			if(StringUtils.isNotEmpty(dto.getEmsddt())) { pdel.setLocation(dto.getEmsddt()); }
			if(StringUtils.isNotEmpty(dto.getEmsdd())) { pdel.setUnloCode(dto.getEmsdd()); }
			if(StringUtils.isNotEmpty(dto.getEmlkd())) {
				AddressCountry addressCountry = new AddressCountry();
				addressCountry.setCountry(dto.getEmlkd());
				pdel.setAddress(addressCountry);
			}
			cml.setPlaceOfDelivery(pdel);
		}
		
		
		
		return cml;
		
	}
	//Containers
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
			//3:rd
			if(StringUtils.isNotEmpty(dto.getEmc3id())) {
				TransportEquipment te3 = new TransportEquipment();
				//all below mandatory
				te3.setContainerIdentificationNumber(dto.getEmc3id());
				te3.setContainerSizeAndType(dto.getEmc3ty());
				te3.setContainerPackedStatus(dto.getEmc3ps());
				te3.setContainerSupplierType(dto.getEmc3ss());
				listTranspEquip.add(te3);
				
			}
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
