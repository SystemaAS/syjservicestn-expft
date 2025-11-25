package no.systema.jservices.tvinn.digitoll.external.house;


import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.systema.jservices.tvinn.digitoll.external.house.dao.ActiveBorderTransportMeans;
import no.systema.jservices.tvinn.digitoll.external.house.dao.Attachments;
import no.systema.jservices.tvinn.digitoll.external.house.dao.Communication;
import no.systema.jservices.tvinn.digitoll.external.house.dao.Consignee;
import no.systema.jservices.tvinn.digitoll.external.house.dao.ConsignmentHouseLevel;
import no.systema.jservices.tvinn.digitoll.external.house.dao.ConsignmentMasterLevel;
import no.systema.jservices.tvinn.digitoll.external.house.dao.Consignor;
import no.systema.jservices.tvinn.digitoll.external.house.dao.CustomsOfficeOfFirstEntry;
import no.systema.jservices.tvinn.digitoll.external.house.dao.DeclarantId;
import no.systema.jservices.tvinn.digitoll.external.house.dao.DocumentReferences;
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
import no.systema.jservices.tvinn.digitoll.v2.dto.SadmotfDto;
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
	/**
	 * 
	 * @param masterDto
	 * @param receiverName
	 * @param receiverOrgnr
	 * @param attachmentsExist
	 * @param attachmentsPath
	 * @return
	 */
	public MessageOutbound mapMessageOutbound(SadmomfDto masterDto, String receiverName, String receiverOrgnr, Boolean attachmentsExist, String attachmentsPath) {
		MessageOutbound msg = new MessageOutbound();
		msg.setMessageType("DigitalMOMaster");
		msg.setVersion(this.version);
		String uuid = UUID.randomUUID().toString();
		msg.setUuid(uuid); //in order to use it in Peppol-XML-Wrapper (if applicable) - One per message sending
		msg.setMessageNumber(uuid);
		msg.setMessageIssueDate(new DateUtils().getZuluTimeWithoutMillisecondsUTC());
		
		//msg.setDocumentID(masterDto.getEmdkm()); not applicable here
		String uuidDocId = UUID.randomUUID().toString();
		msg.setDocumentID(uuidDocId); //Common DocumentID for all messages in a notification exchange. Must be UUID
		
		//msg.setNote(masterDto.getTransportDto().getEtavd() + "-" + masterDto.getTransportDto().getEtpro());
		msg.setNote("TRANSPORT MRN:" + masterDto.getTransportDto().getEtmid());
		
		//Sender
		Sender sender = new Sender();
		sender.setName(masterDto.getTransportDto().getEtnar());
		sender.setIdentificationNumber(masterDto.getTransportDto().getEtrgr());
		//Communication
		List commList = new ArrayList();
		if(masterDto.getTransportDto().getEtemrt()!=null ){
			if("EM".equals(masterDto.getTransportDto().getEtemrt()) ){
				if(StringUtils.isNotEmpty(masterDto.getTransportDto().getEtemr())) {
					Communication comm = new Communication();
					comm.setEmailAddress(masterDto.getTransportDto().getEtemr());
					//sender.setCommunication(comm);
					commList.add(comm);
				}
			}else if ("TE".equals(masterDto.getTransportDto().getEtemrt()) ){
				if(StringUtils.isNotEmpty(masterDto.getTransportDto().getEtemr())) {
					Communication comm = new Communication();
					comm.setTelephoneNumber(masterDto.getTransportDto().getEtemr());
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
		
		if(attachmentsExist) {
			List docReferencesList = new ArrayList();
			List attachmentList = new ArrayList();
			//get files previously serialized in the GUI (upload UC)
			FileAttachmentService fileAttachmentService = new FileAttachmentService(attachmentsPath);
			List <Path> x = fileAttachmentService.getFileAttachments(masterDto.getEmdkm(), receiverOrgnr);
			for (Path file : x) {
				//System.out.println(file.getFileName().toString());
				DocumentReferences docReferences = new DocumentReferences();
				docReferences.setReferenceId(file.getFileName().toString());
				docReferences.setTypeOfReference("docReference"); //vet inte om det är faktura eller annat...
				docReferencesList.add(docReferences);
				try {
		            String payloadPath = fileAttachmentService.getAttachmentsPath() + File.separator + file.getFileName().toString();
		            String base64String = fileAttachmentService.convertImageToBase64(payloadPath);
		            Attachments attachments = new Attachments();
					attachments.setDocumentName(file.getFileName().toString());
					attachments.setContent(base64String);
					attachmentList.add(attachments);
		        } catch (IOException e) {
		        	logger.error(e.toString());
		            e.printStackTrace();
		        }
			}
			if(!docReferencesList.isEmpty()) {
				msg.setDocumentReferences(docReferencesList);
			}
			if(!attachmentList.isEmpty()) {
				msg.setAttachments(attachmentList);
			}
			//clean up and delete files from file system
			fileAttachmentService.deleteFileAttachments(masterDto.getEmdkm(), receiverOrgnr);
		}
		
		//Consignor
		Consignor consignor = new Consignor();
		consignor.setName(masterDto.getEmnas());
		msg.setConsignor(consignor);
		//Consignee
		Consignee consignee = new Consignee();
		consignee.setName(masterDto.getEmnam());
		msg.setConsignee(consignee);
		
		//Customs Office
		CustomsOfficeOfFirstEntry custOffice = new CustomsOfficeOfFirstEntry();
		custOffice.setReferenceNumber(masterDto.getTransportDto().getEttsd());
		//Since we do not have seconds in time we must add this to the integer: 1000(HHmm) will be 100000(HHmmss)
		Integer etaTime = masterDto.getTransportDto().getEtetat() * 100;
		msg.setEstimatedDateAndTimeOfArrival(new DateUtils().getZuluTimeWithoutMilliseconds(masterDto.getTransportDto().getEtetad(), etaTime));
		msg.setCustomsOfficeOfFirstEntry(custOffice);
		ActiveBorderTransportMeans abtranspMeans = new ActiveBorderTransportMeans();
		abtranspMeans.setIdentificationNumber(masterDto.getTransportDto().getEtkmrk());
		abtranspMeans.setTypeOfIdentification(masterDto.getTransportDto().getEtktyp());
		abtranspMeans.setCountryCode(masterDto.getTransportDto().getEtklk());
		abtranspMeans.setModeOfTransportCode(masterDto.getTransportDto().getEtktkd());
		msg.setActiveBorderTransportMeans(abtranspMeans);
		
		//Carrier Id (Orgnr)
		ConsignmentMasterLevel consignmentMasterLevel = new ConsignmentMasterLevel();
		consignmentMasterLevel.setTotalGrossMass(masterDto.getEmvkb());
		consignmentMasterLevel.setCarrierIdentificationNumber(masterDto.getTransportDto().getEtrgt());
		TransportDocumentMasterLevel transportDocumentMasterLevel = new TransportDocumentMasterLevel();
		transportDocumentMasterLevel.setDocumentNumber(masterDto.getEmdkm());
		transportDocumentMasterLevel.setType(masterDto.getEmdkmt());
		consignmentMasterLevel.setTransportDocumentMasterLevel(transportDocumentMasterLevel);
		
		msg.setConsignmentMasterLevel(consignmentMasterLevel);
		
		return msg;  
	}
	
	/**
	 * Map the external house to be sent to the external party owner of the transport and masterID
	 * @param houseRecord
	 * @param receiverName
	 * @param receiverOrgnr
	 * @param attachmentsExist
	 * @param attachmentsPath
	 * @return
	 */
	public MessageOutbound mapMessageOutboundExternalHouse(SadmocfDto dtoConfig, SadmohfDto houseDto, String receiverName, String receiverOrgnr, Boolean attachmentsExist, String attachmentsPath) {
		MessageOutbound msg = new MessageOutbound();
		msg.setMessageType("DigitalMOHouse");
		msg.setVersion(this.version);
		String uuid = UUID.randomUUID().toString();
		msg.setMessageNumber(uuid);
		msg.setUuid(uuid); //in order to use it in Peppol-XML-Wrapper (if applicable)
		msg.setMessageIssueDate(new DateUtils().getZuluTimeWithoutMillisecondsUTC());
	
		//msg.setDocumentID(houseDto.getEhdkh());
		msg.setDocumentID(UUID.randomUUID().toString());
		/*
		msg.setNote(masterRecord.getTransportDto().getEtavd() + "-" + masterRecord.getTransportDto().getEtpro());
		*/
		//=======
		//Sender
		//=======
		Sender sender = new Sender();
		//default
		
		if( (StringUtils.isNotEmpty(dtoConfig.getAvsname()) &&  !dtoConfig.getAvsname().equals("null")) 
				&& (StringUtils.isNotEmpty(dtoConfig.getAvsorgnr()) && !dtoConfig.getAvsorgnr().equals("null")) ) {
			sender.setName(dtoConfig.getAvsname());
			sender.setIdentificationNumber(dtoConfig.getAvsorgnr());
		}else {
			//product owner's orgnr (sender of the external house to toll.no)
			sender.setName("SADMOCF.AVSNAME empty ?");
			sender.setIdentificationNumber("SADMOCF.AVSORGNR empty ?");
			
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
		
		
		if(attachmentsExist) {
			List docReferencesList = new ArrayList();
			List attachmentList = new ArrayList();
			//get files previously serialized in the GUI (upload UC)
			FileAttachmentService fileAttachmentService = new FileAttachmentService(attachmentsPath);
			List <Path> x = fileAttachmentService.getFileAttachments(houseDto.getEhdkh(), receiverOrgnr);
			for (Path file : x) {
				//System.out.println(file.getFileName().toString());
				DocumentReferences docReferences = new DocumentReferences();
				docReferences.setReferenceId(file.getFileName().toString());
				docReferences.setTypeOfReference("docReference"); //vet inte om det är faktura eller annat...
				docReferencesList.add(docReferences);
				try {
		            String payloadPath = fileAttachmentService.getAttachmentsPath() + File.separator + file.getFileName().toString();
		            String base64String = fileAttachmentService.convertImageToBase64(payloadPath);
		            Attachments attachments = new Attachments();
					attachments.setDocumentName(file.getFileName().toString());
					attachments.setContent(base64String);
					attachmentList.add(attachments);
		        } catch (IOException e) {
		        	logger.error(e.toString());
		            e.printStackTrace();
		        }
			}
			if(!docReferencesList.isEmpty()) {
				msg.setDocumentReferences(docReferencesList);
			}
			if(!attachmentList.isEmpty()) {
				msg.setAttachments(attachmentList);
			}
			//clean up and delete files from file system
			fileAttachmentService.deleteFileAttachments(houseDto.getEhdkh(), receiverOrgnr);
		}
		
		
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
