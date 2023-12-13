package no.systema.jservices.tvinn.digitoll.external.house;


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

	
	public MessageOutbound mapMessageOutbound(SadmomfDto masterRecord, String receiverName, String receiverOrgnr) {
		MessageOutbound msg = new MessageOutbound();
		msg.setMessageType("DigitalMO");
		msg.setMessageNumber(masterRecord.getEmlnrt() + "-" + masterRecord.getTransportDto().getEtrgt());
		msg.setMessageIssueDate(new DateUtils().getZuluTimeWithoutMillisecondsUTC());
		msg.setDocumentID(masterRecord.getEmdkm());
		//Sender
		Sender sender = new Sender();
		sender.setName(masterRecord.getTransportDto().getEtnar());
		sender.setIdentificationNumber(masterRecord.getTransportDto().getEtrgr());
		msg.setSender(sender);
		//Receiver
		Receiver receiver = new Receiver();
		receiver.setName(receiverName);
		receiver.setIdentificationNumber(receiverOrgnr);
		msg.setReceiver(receiver);
		
		  
		return msg;  
	}
}
