package no.systema.jservices.tvinn.digitoll.external.house;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.systema.jservices.tvinn.digitoll.external.house.controller.DigitollV2ExternalHouseController;
import no.systema.jservices.tvinn.digitoll.external.house.dao.ActiveBorderTransportMeans;
import no.systema.jservices.tvinn.digitoll.external.house.dao.Communication;
import no.systema.jservices.tvinn.digitoll.external.house.dao.ConsignmentMasterLevel;
import no.systema.jservices.tvinn.digitoll.external.house.dao.CustomsOfficeOfFirstEntry;
import no.systema.jservices.tvinn.digitoll.external.house.dao.MessageOutbound;
import no.systema.jservices.tvinn.digitoll.external.house.dao.Receiver;
import no.systema.jservices.tvinn.digitoll.external.house.dao.Sender;
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
	
	
	public MessageOutbound mapMessageOutbound(SadmomfDto masterRecord, String receiverName, String receiverOrgnr) {
		MessageOutbound msg = new MessageOutbound();
		msg.setMessageType("DigitalMOMaster");
		msg.setVersion("1.0");
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
		msg.setActiveBorderTransportMeans(abtranspMeans);
		
		//Carrier Id (Orgnr)
		ConsignmentMasterLevel consignmentMasterLevel = new ConsignmentMasterLevel();
		consignmentMasterLevel.setCarrierIdentificationNumber(masterRecord.getTransportDto().getEtrgt());
		consignmentMasterLevel.setDocumentNumber(masterRecord.getEmdkm());
		consignmentMasterLevel.setType(masterRecord.getEmdkmt());
		
		msg.setConsignmentMasterLevel(consignmentMasterLevel);
		
		return msg;  
	}
}
