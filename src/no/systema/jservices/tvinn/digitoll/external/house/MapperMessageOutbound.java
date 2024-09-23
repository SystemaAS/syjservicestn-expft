package no.systema.jservices.tvinn.digitoll.external.house;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.systema.jservices.tvinn.digitoll.external.house.dao.ActiveBorderTransportMeans;
import no.systema.jservices.tvinn.digitoll.external.house.dao.Communication;
import no.systema.jservices.tvinn.digitoll.external.house.dao.Consignee;
import no.systema.jservices.tvinn.digitoll.external.house.dao.ConsignmentHouseLevel;
import no.systema.jservices.tvinn.digitoll.external.house.dao.ConsignmentMasterLevel;
import no.systema.jservices.tvinn.digitoll.external.house.dao.Consignor;
import no.systema.jservices.tvinn.digitoll.external.house.dao.CustomsOfficeOfFirstEntry;
import no.systema.jservices.tvinn.digitoll.external.house.dao.DeclarantId;
import no.systema.jservices.tvinn.digitoll.external.house.dao.ExportFromEU;
import no.systema.jservices.tvinn.digitoll.external.house.dao.MessageOutbound;
import no.systema.jservices.tvinn.digitoll.external.house.dao.PreviousDocuments;
import no.systema.jservices.tvinn.digitoll.external.house.dao.Receiver;
import no.systema.jservices.tvinn.digitoll.external.house.dao.Sender;
import no.systema.jservices.tvinn.digitoll.external.house.dao.TransportDocumentHouseLevel;
import no.systema.jservices.tvinn.digitoll.external.house.dao.TransportDocumentMasterLevel;
import no.systema.jservices.tvinn.digitoll.v2.dto.SadmocfDto;
import no.systema.jservices.tvinn.digitoll.v2.dto.SadmohfDto;
import no.systema.jservices.tvinn.digitoll.v2.dto.SadmomfDto;
import no.systema.jservices.tvinn.expressfortolling2.util.DateUtils;

/**
 * Maps the transport-master DAOs to the DigitollMO-Advise (for sending the outbound Master Id to an external part)
 * 
 * @author oscardelatorre
 * @date Dec-2023
 * 
 */
public class MapperMessageOutbound {
	private static Logger logger = LoggerFactory.getLogger(MapperMessageOutbound.class.getName());
	private String version = "";
	
	public MapperMessageOutbound(String version) {
		this.version = version;
	}
	public MessageOutbound mapMessageOutbound(SadmomfDto masterRecord, String receiverName, String receiverOrgnr) {
		MessageOutbound msg = new MessageOutbound();
		msg.setMessageType("DigitalMOMaster");
		msg.setVersion(this.version);
		String uuid = UUID.randomUUID().toString();
		msg.setMessageNumber(uuid);
		msg.setUuid(uuid); //in order to use it in Peppol-XML-Wrapper (if applicable)
		msg.setMessageIssueDate(new DateUtils().getZuluTimeWithoutMillisecondsUTC());
		msg.setDocumentID(masterRecord.getEmdkm());
		msg.setNote(masterRecord.getTransportDto().getEtavd() + "-" + masterRecord.getTransportDto().getEtpro());
		//Sender
		Sender sender = new Sender();
		sender.setName(masterRecord.getTransportDto().getEtnar());
		sender.setIdentificationNumber(masterRecord.getTransportDto().getEtrgr());
		//Communication
		List commList = new ArrayList();
		if(masterRecord.getTransportDto().getEtemrt()!=null ){
			if("EM".equals(masterRecord.getTransportDto().getEtemrt()) ){
				if(StringUtils.isNotEmpty(masterRecord.getTransportDto().getEtemr())) {
					Communication comm = new Communication();
					comm.setEmailAddress(masterRecord.getTransportDto().getEtemr());
					//sender.setCommunication(comm);
					commList.add(comm);
				}
			}else if ("TE".equals(masterRecord.getTransportDto().getEtemrt()) ){
				if(StringUtils.isNotEmpty(masterRecord.getTransportDto().getEtemr())) {
					Communication comm = new Communication();
					comm.setTelephoneNumber(masterRecord.getTransportDto().getEtemr());
					//sender.setCommunication(comm);
					commList.add(comm);
				}
			}
			sender.setCommunication(commList);
		}
		msg.setSender(sender);
		
		//Receiver
		Receiver receiver = new Receiver();
		receiver.setName(receiverName);
		receiver.setIdentificationNumber(receiverOrgnr);
		msg.setReceiver(receiver);
		
		//Consignor
		Consignor consignor = new Consignor();
		consignor.setName(masterRecord.getEmnas());
		msg.setConsignor(consignor);
		//Consignee
		Consignee consignee = new Consignee();
		consignee.setName(masterRecord.getEmnam());
		msg.setConsignee(consignee);
		
		
		//Customs Office
		CustomsOfficeOfFirstEntry custOffice = new CustomsOfficeOfFirstEntry();
		custOffice.setReferenceNumber(masterRecord.getTransportDto().getEttsd());
		//Since we do not have seconds in time we must add this to the integer: 1000(HHmm) will be 100000(HHmmss)
		Integer etaTime = masterRecord.getTransportDto().getEtetat() * 100;
		msg.setEstimatedDateAndTimeOfArrival(new DateUtils().getZuluTimeWithoutMilliseconds(masterRecord.getTransportDto().getEtetad(), etaTime));
		msg.setCustomsOfficeOfFirstEntry(custOffice);
		ActiveBorderTransportMeans abtranspMeans = new ActiveBorderTransportMeans();
		abtranspMeans.setIdentificationNumber(masterRecord.getTransportDto().getEtkmrk());
		abtranspMeans.setTypeOfIdentification(masterRecord.getTransportDto().getEtktyp());
		abtranspMeans.setCountryCode(masterRecord.getTransportDto().getEtklk());
		abtranspMeans.setModeOfTransportCode(masterRecord.getTransportDto().getEtktkd());
		msg.setActiveBorderTransportMeans(abtranspMeans);
		
		//Carrier Id (Orgnr)
		ConsignmentMasterLevel consignmentMasterLevel = new ConsignmentMasterLevel();
		consignmentMasterLevel.setTotalGrossMass(masterRecord.getEmvkb());
		consignmentMasterLevel.setCarrierIdentificationNumber(masterRecord.getTransportDto().getEtrgt());
		TransportDocumentMasterLevel transportDocumentMasterLevel = new TransportDocumentMasterLevel();
		transportDocumentMasterLevel.setDocumentNumber(masterRecord.getEmdkm());
		transportDocumentMasterLevel.setType(masterRecord.getEmdkmt());
		consignmentMasterLevel.setTransportDocumentMasterLevel(transportDocumentMasterLevel);
		
		msg.setConsignmentMasterLevel(consignmentMasterLevel);
		
		return msg;  
	}
	
	/**
	 * Map the external house to be sent to the external party owner of the transport and masterID
	 * @param houseRecord
	 * @param receiverName
	 * @param receiverOrgnr
	 * @return
	 */
	public MessageOutbound mapMessageOutboundExternalHouse(SadmocfDto dtoConfig, SadmohfDto houseDto, String receiverName, String receiverOrgnr) {
		MessageOutbound msg = new MessageOutbound();
		msg.setMessageType("DigitalMOHouse");
		msg.setVersion(this.version);
		String uuid = UUID.randomUUID().toString();
		msg.setMessageNumber(uuid);
		msg.setUuid(uuid); //in order to use it in Peppol-XML-Wrapper (if applicable)
		msg.setMessageIssueDate(new DateUtils().getZuluTimeWithoutMillisecondsUTC());
	
		msg.setDocumentID(houseDto.getEhdkh());
		/*
		msg.setNote(masterRecord.getTransportDto().getEtavd() + "-" + masterRecord.getTransportDto().getEtpro());
		*/
		//=======
		//Sender
		//=======
		Sender sender = new Sender();
		//default
		if(StringUtils.isNotEmpty(dtoConfig.getAvsname()) && StringUtils.isNotEmpty(dtoConfig.getAvsorgnr())) {
			sender.setName(dtoConfig.getAvsname());
			sender.setIdentificationNumber(dtoConfig.getAvsorgnr());
		}else {
			//product owner's orgnr (sender of the external house to toll.no)
			sender.setName("AVSNAME empty ?");
			sender.setIdentificationNumber("AVSORGNR empty ?");
			
		}
		
		//Communication
		List commList = new ArrayList();
		Communication comm = new Communication();
		if(houseDto.getTransportDto().getEtemrt()!=null ){
			if("EM".equals(houseDto.getTransportDto().getEtemrt()) ){
				if(StringUtils.isNotEmpty(houseDto.getTransportDto().getEtemr())) {
					//Communication comm = new Communication();
					comm.setEmailAddress(houseDto.getTransportDto().getEtemr());
					//sender.setCommunication(comm);
					commList.add(comm);
				}
			}else if ("TE".equals(houseDto.getTransportDto().getEtemrt()) ){
				if(StringUtils.isNotEmpty(houseDto.getTransportDto().getEtemr())) {
					//Communication comm = new Communication();
					comm.setTelephoneNumber(houseDto.getTransportDto().getEtemr());
					//sender.setCommunication(comm);
					commList.add(comm);
				}
			}
			sender.setCommunication(commList);
		}
		msg.setSender(sender);
		//=========
		//Receiver
		//=========
		Receiver receiver = new Receiver();
		receiver.setName(receiverName);
		receiver.setIdentificationNumber(receiverOrgnr);
		msg.setReceiver(receiver);
		
		//Consignor-Avsändare
		Consignor consignor = new Consignor();
		consignor.setName(houseDto.getEhnas());
		msg.setConsignor(consignor);
		//Consignee-Mottagare
		Consignee consignee = new Consignee();
		consignee.setName(houseDto.getEhnam());
		msg.setConsignee(consignee);
		
		//========================
		//Consignment house level
		//========================
		ConsignmentHouseLevel consignmentHouseLevel = new ConsignmentHouseLevel();
		consignmentHouseLevel.setTotalGrossMass(houseDto.getEhvkb());
		consignmentHouseLevel.setNumberOfPackages(houseDto.getEhntk());
		consignmentHouseLevel.setGoodsDescription(houseDto.getEhvt());
		//TransportDocumentHouseLevel on consignment house level
		TransportDocumentHouseLevel transportDocumentHouseLevel = new TransportDocumentHouseLevel();
		transportDocumentHouseLevel.setDocumentNumber(houseDto.getEhdkh());
		transportDocumentHouseLevel.setType(houseDto.getEhdkht());
		consignmentHouseLevel.setTransportDocumentHouseLevel(transportDocumentHouseLevel);
		//Master level
		if(StringUtils.isNotEmpty(houseDto.getMasterDto().getEmdkm_ff())) {
			ConsignmentMasterLevel consignmentMasterLevel = new ConsignmentMasterLevel();
			consignmentMasterLevel.setCarrierIdentificationNumber(houseDto.getMasterDto().getEmrgt_ff());
			TransportDocumentMasterLevel transportDocumentMasterLevel = new TransportDocumentMasterLevel();
			transportDocumentMasterLevel.setDocumentNumber(houseDto.getMasterDto().getEmdkm_ff());
			transportDocumentMasterLevel.setType(houseDto.getMasterDto().getEmdkmt_ff());
			consignmentMasterLevel.setTransportDocumentMasterLevel(transportDocumentMasterLevel);
			//add
			consignmentHouseLevel.setConsignmentMasterLevel(consignmentMasterLevel);
		}
		
		//Previous documents
		if(StringUtils.isNotEmpty(houseDto.getEhtrnr())) {
			//We should include the other possible 9 transits. This is only the first one
			List previousDocumentsList = new ArrayList();
			if(StringUtils.isNotEmpty(houseDto.getEhtrnr())) {
				PreviousDocuments previousDocuments = new PreviousDocuments();
				previousDocuments.setReferenceNumber(houseDto.getEhtrnr());
				previousDocuments.setTypeOfReference("N820");
				previousDocumentsList.add(previousDocuments);
			}
			//add
			consignmentHouseLevel.setPreviousDocuments(previousDocumentsList);
		}
		//When CUDE and sequence are present
		if(StringUtils.isNotEmpty(houseDto.getEhrg()) && houseDto.getEh0068a()>0 && houseDto.getEh0068b()>0) {
			DeclarantId declarantId = new DeclarantId();
			declarantId.setDeclarantNumber(houseDto.getEhrg());
			declarantId.setDeclarationDate(this.getDeclarationDateFormattedISO( houseDto.getEh0068a()) );
			declarantId.setSequenceNumber(houseDto.getEh0068b());
			//add
			consignmentHouseLevel.setDeclarantId(declarantId);
		}
		//Export from EU
		if(StringUtils.isNotEmpty(houseDto.getEheid())) {
			List exportFromEUList = new ArrayList();
			ExportFromEU exportFromEU = new ExportFromEU();
			exportFromEU.setTypeOfExport(houseDto.getEhetypt());
			exportFromEU.setExportId(houseDto.getEheid());
			exportFromEUList.add(exportFromEU);
			//add
			consignmentHouseLevel.setExportFromEU(exportFromEUList);
		}
		
		
		//Prosedyr (non existent in the implementation guide but it is mandatory
		consignmentHouseLevel.setProcedure(houseDto.getEhprt());
		
		//add to message
		msg.setConsignmentHouseLevel(consignmentHouseLevel);
		//==========================
		//END ConsignmentHouseLevel
		//==========================
		
		//Customs Office
		CustomsOfficeOfFirstEntry custOffice = new CustomsOfficeOfFirstEntry();
		custOffice.setReferenceNumber(houseDto.getCarrierMasterIdDto().getCustoff());
		//Since we do not have seconds in time we must add this to the integer: 1000(HHmm) will be 100000(HHmmss)
		Integer etaTime = houseDto.getCarrierMasterIdDto().getTime();
		msg.setEstimatedDateAndTimeOfArrival(new DateUtils().getZuluTimeWithoutMilliseconds(houseDto.getCarrierMasterIdDto().getDate(), etaTime));
		msg.setCustomsOfficeOfFirstEntry(custOffice);
		
		/*
		ActiveBorderTransportMeans abtranspMeans = new ActiveBorderTransportMeans();
		abtranspMeans.setIdentificationNumber(masterRecord.getTransportDto().getEtkmrk());
		abtranspMeans.setTypeOfIdentification(masterRecord.getTransportDto().getEtktyp());
		abtranspMeans.setCountryCode(masterRecord.getTransportDto().getEtklk());
		msg.setActiveBorderTransportMeans(abtranspMeans);
		
		//Carrier Id (Orgnr)
		ConsignmentMasterLevel consignmentMasterLevel = new ConsignmentMasterLevel();
		consignmentMasterLevel.setCarrierIdentificationNumber(masterRecord.getTransportDto().getEtrgt());
		consignmentMasterLevel.setDocumentNumber(masterRecord.getEmdkm());
		consignmentMasterLevel.setType(masterRecord.getEmdkmt());
		
		msg.setConsignmentMasterLevel(consignmentMasterLevel);
		
		*/
		
		return msg;  
	}
	
	private String getDeclarationDateFormattedISO(Integer value) {
		String retval = null;
		if(value>0) {
			String tmp = String.valueOf(value);
			if(tmp!=null && tmp.length()==8) {
				String year = tmp.substring(0,4);
				String month = tmp.substring(4,6);
				String day = tmp.substring(6,8);
				retval = year + "-" + month + "-" + day;
			}
		}
		
		return retval;
	}
}
