package no.systema.jservices.tvinn.expressfortolling.api;

import java.util.ArrayList;
import java.util.List;

import no.systema.jservices.tvinn.expressfortolling2.dao.ActiveBorderTransportMeans;
import no.systema.jservices.tvinn.expressfortolling2.dao.Address;
import no.systema.jservices.tvinn.expressfortolling2.dao.Carrier;
import no.systema.jservices.tvinn.expressfortolling2.dao.Communication;
import no.systema.jservices.tvinn.expressfortolling2.dao.Consignee;
import no.systema.jservices.tvinn.expressfortolling2.dao.ConsignmentHouseLevel;
import no.systema.jservices.tvinn.expressfortolling2.dao.ConsignmentMasterLevel;
import no.systema.jservices.tvinn.expressfortolling2.dao.Consignor;
import no.systema.jservices.tvinn.expressfortolling2.dao.Crew;
import no.systema.jservices.tvinn.expressfortolling2.dao.CustomsOfficeOfFirstEntry;
import no.systema.jservices.tvinn.expressfortolling2.dao.Declarant;
import no.systema.jservices.tvinn.expressfortolling2.dao.HouseConsignment;
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

public class TestHouseConsignmentDao {

	//JSON spec: https://api-test.toll.no/api/movement/road/v1/swagger-ui/index.html
		public HouseConsignment setHouseConsignment() {
			
			HouseConsignment hc = new HouseConsignment();
			//IssueDate
			hc.setDocumentIssueDate("2022-08-04T07:49:52Z");
			
			//Declarant
			Declarant dec = new Declarant();
			dec.setName("Posten Norge AS");
			Address address = new Address();
			address.setCity("Oslo");
			address.setCountry("NO");
			address.setStreetLine("");
			address.setPostcode("0001");
			address.setNumber("10B");
			address.setPoBox("P.B 194");
			dec.setAddress(address);
			//
			List commList = new ArrayList();
			commList.add(this.populateCommunication("xxx@gmail.com", "EM"));
			commList.add(this.populateCommunication("0733794505", "TE"));
			dec.setCommunication(commList);
			hc.setDeclarant(dec);
			
			
			//Representative
			Representative rep = new Representative();
			rep.setName("Bring AS");
			rep.setIdentificationNumber("951357482");
			
			//rep.setStatus(sourceDto.getEmstr());
			rep.setStatus("2");
			
			Address raddress = new Address();
			raddress.setCity("Oslo");
			raddress.setCountry("NO");
			//PROD-->raddress.setStreetLine(sourceDto.getEmnrr());
			raddress.setStreetLine("Hausemanns gate");
			//PROD-->raddress.setNumber(sourceDto.getEmnrr());
			raddress.setNumber("52F");
			rep.setAddress(raddress);
			//
			List rcommList = new ArrayList();
			rcommList.add(this.populateCommunication("en-epost@mail.no", "ME"));
			//rcommList.add(this.populateCommunication("0733794599", "TE"));
			rep.setCommunication(rcommList);
			hc.setRepresentative(rep);
			
			
			/*
			//ActiveBorderTransMeans
			mc.setActiveBorderTransportMeans(this.populateActiveBorderTransportMeans(sourceDto));
			
			//Consig.MasterLevel - documentNumber IMPORTANT (parent to houseConsignment documentNumber)
			mc.setConsignmentMasterLevel(this.populateConsignmentMasterLevel(sourceDto, "123123", "N750"));
			//CustomsOffice
			CustomsOfficeOfFirstEntry cOffice = new CustomsOfficeOfFirstEntry();
			cOffice.setReferenceNumber("NO344001");
			mc.setCustomsOfficeOfFirstEntry(cOffice);
			
			
			
			
			//ReleasedConfirmation
			List relList = new ArrayList();
			relList.add(this.populateReleasedConfirmation("yyy@doe.com"));
			mc.setReleasedConfirmation(relList);
			
			
			try {
				//System.out.println(obj.writerWithDefaultPrettyPrinter().writeValueAsString(mc));
			}catch(Exception e) {
				e.printStackTrace();
			}
			*/
			return hc;
		}
		
		
		
		
		private Communication populateCommunication(String id, String type) {
			
			Communication communication = new Communication();
			communication.setIdentifier(id);
			communication.setType(type);
			return communication;
		}
		
		
	
	
	
}
