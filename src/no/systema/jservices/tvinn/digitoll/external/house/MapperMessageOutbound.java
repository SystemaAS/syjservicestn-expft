package no.systema.jservices.tvinn.digitoll.external.house;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.systema.jservices.tvinn.digitoll.external.house.dao.Communication;
import no.systema.jservices.tvinn.digitoll.external.house.dao.ConsignmentMasterLevel;
import no.systema.jservices.tvinn.digitoll.external.house.dao.CustomsOfficeOfFirstEntry;
import no.systema.jservices.tvinn.digitoll.external.house.dao.MessageOutbound;
import no.systema.jservices.tvinn.digitoll.external.house.dao.Receiver;
import no.systema.jservices.tvinn.digitoll.external.house.dao.Sender;
import no.systema.jservices.tvinn.digitoll.v2.controller.DigitollV2ExternalHouseController;
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
		msg.setMessageNumber(masterRecord.getEmlnrt() + "-" + masterRecord.getTransportDto().getEtrgt());
		msg.setMessageIssueDate(new DateUtils().getZuluTimeWithoutMillisecondsUTC());
		msg.setDocumentID(masterRecord.getEmdkm());
		//Sender
		Sender sender = new Sender();
		sender.setName(masterRecord.getTransportDto().getEtnar());
		sender.setIdentificationNumber(masterRecord.getTransportDto().getEtrgr());
		//Communication
		if(masterRecord.getTransportDto().getEtemrt()!=null ){
			if("EM".equals(masterRecord.getTransportDto().getEtemrt()) ){
				if(StringUtils.isNotEmpty(masterRecord.getTransportDto().getEtemr())) {
					Communication comm = new Communication();
					comm.setEmailAddress(masterRecord.getTransportDto().getEtemr());
					sender.setCommunication(comm);
				}
			}else if ("TE".equals(masterRecord.getTransportDto().getEtemrt()) ){
				if(StringUtils.isNotEmpty(masterRecord.getTransportDto().getEtemr())) {
					Communication comm = new Communication();
					comm.setTelephoneNumber(masterRecord.getTransportDto().getEtemr());
					sender.setCommunication(comm);
				}
			}		
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
		custOffice.setEstimatedDateAndTimeOfArrival(new DateUtils().getZuluTimeWithoutMilliseconds(masterRecord.getTransportDto().getEtetad(), etaTime));
		//msg.setEstimatedDateAndTimeOfArrival(new DateUtils().getZuluTimeWithoutMilliseconds(masterRecord.getTransportDto().getEtetad(), etaTime));
		msg.setCustomsOfficeOfFirstEntry(custOffice);
		
		//Carrier Id (Orgnr)
		ConsignmentMasterLevel consignmentMasterLevel = new ConsignmentMasterLevel();
		consignmentMasterLevel.setCarrierIdentificationNumber(masterRecord.getTransportDto().getEtrgt());
		msg.setConsignmentMasterLevel(consignmentMasterLevel);
		
		return msg;  
	}
}
