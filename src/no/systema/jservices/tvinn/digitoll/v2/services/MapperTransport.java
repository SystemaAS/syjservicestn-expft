package no.systema.jservices.tvinn.digitoll.v2.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import no.systema.jservices.tvinn.digitoll.v2.dao.*;
import no.systema.jservices.tvinn.digitoll.v2.dto.SadmomfDto;
import no.systema.jservices.tvinn.digitoll.v2.dto.SadmotfDto;
import no.systema.jservices.tvinn.expressfortolling2.util.DateUtils;

/**
 * SPEC. SWAGGER
 * https://api-test.toll.no/api/movement/road/v2/swagger-ui/index.html
 * 
 * @author oscardelatorre
 * Jun 2023
 */
public class MapperTransport {
	private static final Logger logger = LoggerFactory.getLogger(MapperTransport.class);
	
	
	public Transport mapTransport(SadmotfDto sourceDto) {
		
		Transport transport = new Transport();
		//(Mandatory) IssueDate
		transport.setDocumentIssueDate(new DateUtils().getZuluTimeWithoutMillisecondsUTC());
		logger.warn(transport.getDocumentIssueDate());
		
		//(Optional) Representative
		//if(StringUtils.isNotEmpty(sourceDto.getEmnar())){
			Representative rep = new Representative();
			rep.setName(sourceDto.getEtnar());
			rep.setIdentificationNumber(sourceDto.getEtrgr());
			
			//(Mandatory) this.setAddress("Oslo", "NO", "0010", "Hausemanns gate", "52");
			rep.setAddress(this.setAddress(sourceDto.getEtpsr(), sourceDto.getEtlkr(), sourceDto.getEtpnr(), sourceDto.getEtad1r(), sourceDto.getEtnrr()));
			//
			List rcommList = new ArrayList();
			rcommList.add(this.populateCommunication(sourceDto.getEtemr(), sourceDto.getEtemrt()));
			rep.setCommunication(rcommList);
			transport.setRepresentative(rep);

		//}
		
		//(Mandatory) ActiveBorderTransMeans
		transport.setActiveBorderTransportMeansTransport(this.populateActiveBorderTransportMeans(sourceDto));
		
		//(Mandatory) Carrier
		Carrier carrier = new Carrier();
		carrier.setName(sourceDto.getEtnat());
		carrier.setIdentificationNumber(sourceDto.getEtrgt());
		carrier.setAddress(this.setAddress(sourceDto.getEtpst(), sourceDto.getEtlkt(), sourceDto.getEtpnt(), sourceDto.getEtad1t(), sourceDto.getEtnrt()));
		if(StringUtils.isNotEmpty(sourceDto.getEtemt())) {
			carrier.setCommunication(this.setCommunication(sourceDto.getEtemt() , sourceDto.getEtemtt()));
		}
		transport.setCarrier(carrier);
		
		//(Mandatory) CustomsOffice
		CustomsOfficeOfFirstEntry cOffice = new CustomsOfficeOfFirstEntry();
		cOffice.setReferenceNumber(sourceDto.getEttsd());
		transport.setCustomsOfficeOfFirstEntry(cOffice);

		
		//Optional
		if(sourceDto.getEtetad()>0) {
			transport.setEstimatedDateAndTimeOfArrival(new DateUtils().getZuluTimeWithoutMillisecondsUTC(sourceDto.getEtetad(), sourceDto.getEtetat()));
			//TEST transport.setEstimatedDateAndTimeOfArrival(new DateUtils().getZuluTimeWithoutMillisecondsUTC());
		}
		if(sourceDto.getEtshed()>0) {
			transport.setScheduledDateAndTimeOfArrival(new DateUtils().getZuluTimeWithoutMillisecondsUTC(sourceDto.getEtshed(), sourceDto.getEtshet()));
			//TEST transport.setScheduledDateAndTimeOfArrival(new DateUtils().getZuluTimeWithoutMillisecondsUTC());
		}
		
		//Mandatory ref to MasterConsignment max 9999
		//Liste over hovedforsendelser som skal transporteres til grensen med denne transporten
		transport.setConsignmentMasterLevelTransport(this.populateConsignmentMasterLevelTransport(sourceDto.getMasterList()));
		
		
		return transport;
	}
	/**
	 * 
	 * @param list
	 * @return
	 */
	private List<ConsignmentMasterLevelTransport> populateConsignmentMasterLevelTransport(List<SadmomfDto> list) {
		List<ConsignmentMasterLevelTransport> targetList = new ArrayList();
		for(SadmomfDto record : list) {
			ConsignmentMasterLevelTransport ml = new ConsignmentMasterLevelTransport();
			TransportDocumentMasterLevel trDocMasterLevel = new TransportDocumentMasterLevel();
			trDocMasterLevel.setDocumentNumber(record.getEmdkm());
			trDocMasterLevel.setType(record.getEmdkmt());
			ml.setTransportDocumentMasterLevel(trDocMasterLevel);
			targetList.add(ml);
		}
		
		return targetList;
		  
	}
	
	public Transport mapTransportForDelete() {
		
		Transport transport = new Transport();
		//IssueDate
		transport.setDocumentIssueDate(new DateUtils().getZuluTimeWithoutMillisecondsUTC());
		logger.warn(transport.getDocumentIssueDate());
		
		try {
			//System.out.println(obj.writerWithDefaultPrettyPrinter().writeValueAsString(mc));
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return transport;
	}
	
	
	private Communication populateCommunication(String id, String type) {
		
		Communication communication = new Communication();
		communication.setIdentifier(id);
		communication.setType(type);
		return communication;
	}
	
	
	private List<Communication> setCommunication(String id, String type) {
		Communication communication = new Communication();
		communication.setIdentifier(id);
		communication.setType(type);
		List tmp = new ArrayList();
		tmp.add(communication);
		return tmp;
	}
	
	/**
	 * 
	 * CL751 Type of Means of Transport
	 * 
	 * Expected codes for setTypeOfMeansOfTransport = are: 
	 * one of [230, 2305, 2304, 2303, 2302, 2301, 12, 1594, 1593, 1592, 1591, 360, 362, 363, 1, 364, 365, 366, 367, 
	 * 368, 369, 802, 803, 804, 370, 371, 372, 373, 374, 375, 376, 377, 2203, 378, T, 2202, 379, 2201, 810, 811, 812, 813, 814, 815, 816, 31, 817, 32, 818, 33, 8442, 
	 * 34, 8443, 35, 36, 8441, 37, 8446, 38, 8447, 39, 8444, 8445, 380, 381, 382, 383, 384, 385, 386, 387, 3304, 388, 3303, 389, 3302, 3301, 821, 822, 
	 * 823, 824, 825, 826, 827, 828, 829, 8453, 8454, 8451, 8452, 390, 391, 150, 392, 151, 393, 152, 394, 153, 395, 154, 396, 155, 397, 398, 157, 399, 159, 831, 
	 * 832, 833, 834, 835, 8448, 836, 837, 838, 839, 160, 840, 841, 842, 3201, 843, 844, 845, 60, 846, 847, 848, 849, 3101, 3100, 170, 172, 173, 174, 175, 1712, 176, 
	 * 1711, 177, 178, 850, 851, 70, 71, 72, 3112, 180, 3111, 181, 3110, 182, 183, 184, 185, 1602, 1723, 3109, 1601, 3108, 1721, 189, 3107, 3106, 3105, 3104, 3103, 3102, 
	 * 80, 81, 1729, 1607, 1728, 1606, 1727, 1605, 1726, 85, 1604, 1725, 86, 1603, 1724, 87, 88, 89, 190, 3123, 191, 3122, 192, 3121, 3120, 3119, 3118, 3117, 3116, 3115, 
	 * 3114, 3113, 8022, 8023, 3134, 3133, 3132, 3131, 3130, 8021, 1503, 1502, 1501, 3129, 3128, 3127, 3126, 3125, 3124, 1506, 1505, 1504, 1514, 1513, 1512, 1511, 1753, 1752, 
	 * 3138, 1751, 3137, 3136, 3135, 1519, 1518, 1517, 1516, 1515, 4000, 8163, 8161, 8162, 1525, 1524, 1766, 1523, 1765, 1522, 1764, 1521, 1763, 1762, 1761, 1781, 1534, 310, 
	 * 1533, 311, 1532, 312, 1531, 313, 314, 315, 320, 1543, 1542, 1541, 1782, 5000, 330, 210, 1553, 1552, 1551, 220, 341, 342, 343]","pointer":{"messageElementPath":"activeBorderTransportMeans.typeOfMeansOfTransport"}}]
	 * @param dto
	 * @return
	 */
	private ActiveBorderTransportMeansTransport populateActiveBorderTransportMeans(SadmotfDto dto) {
		ActiveBorderTransportMeansTransport ab = new ActiveBorderTransportMeansTransport();
		
		ab.setIdentificationNumber(dto.getEtkmrk());
		ab.setTypeOfIdentification(dto.getEtktyp());
		ab.setTypeOfMeansOfTransport(dto.getEtktm()); 
		if(StringUtils.isNotEmpty(dto.getEtcref())){
			ab.setConveyanceReferenceNumber(dto.getEtcref());
		}
		ab.setCountryCode(dto.getEtklk());
		ab.setModeOfTransportCode(dto.getEtktkd());
		
		
		//Mandatory name and communication
		Operator operator = new Operator();
		operator.setName(dto.getEtsjaf());
		List rcommList = new ArrayList();
		rcommList.add(this.populateCommunication(dto.getEtems(), dto.getEtemst()));
		operator.setCommunication(rcommList);
		ab.setOperator(operator);

		
		
		return ab;
		
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
