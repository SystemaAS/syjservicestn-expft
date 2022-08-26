package no.systema.jservices.tvinn.expressfortolling2.services;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.systema.jservices.tvinn.expressfortolling2.dao.ActiveBorderTransportMeans;
import no.systema.jservices.tvinn.expressfortolling2.dao.Address;
import no.systema.jservices.tvinn.expressfortolling2.dao.AddressCountry;
import no.systema.jservices.tvinn.expressfortolling2.dao.Carrier;
import no.systema.jservices.tvinn.expressfortolling2.dao.Commodity;
import no.systema.jservices.tvinn.expressfortolling2.dao.CommodityCode;
import no.systema.jservices.tvinn.expressfortolling2.dao.Communication;
import no.systema.jservices.tvinn.expressfortolling2.dao.Consignee;
import no.systema.jservices.tvinn.expressfortolling2.dao.ConsignmentHouseLevel;
import no.systema.jservices.tvinn.expressfortolling2.dao.ConsignmentMasterLevel;
import no.systema.jservices.tvinn.expressfortolling2.dao.Consignor;
import no.systema.jservices.tvinn.expressfortolling2.dao.CountryOfOrigin;
import no.systema.jservices.tvinn.expressfortolling2.dao.Crew;
import no.systema.jservices.tvinn.expressfortolling2.dao.CustomsOfficeOfFirstEntry;
import no.systema.jservices.tvinn.expressfortolling2.dao.DangerousGoods;
import no.systema.jservices.tvinn.expressfortolling2.dao.Declarant;
import no.systema.jservices.tvinn.expressfortolling2.dao.ExportFromEU;
import no.systema.jservices.tvinn.expressfortolling2.dao.GoodsItem;
import no.systema.jservices.tvinn.expressfortolling2.dao.GoodsMeasure;
import no.systema.jservices.tvinn.expressfortolling2.dao.HouseConsignment;
import no.systema.jservices.tvinn.expressfortolling2.dao.HouseConsignmentConsignmentHouseLevel;
import no.systema.jservices.tvinn.expressfortolling2.dao.ImportProcedure;
import no.systema.jservices.tvinn.expressfortolling2.dao.ItemAmountInvoiced;
import no.systema.jservices.tvinn.expressfortolling2.dao.MasterConsignment;
import no.systema.jservices.tvinn.expressfortolling2.dao.Operator;
import no.systema.jservices.tvinn.expressfortolling2.dao.Packaging;
import no.systema.jservices.tvinn.expressfortolling2.dao.PassiveBorderTransportMeans;
import no.systema.jservices.tvinn.expressfortolling2.dao.PassiveTransportMeans;
import no.systema.jservices.tvinn.expressfortolling2.dao.PlaceOfLoading;
import no.systema.jservices.tvinn.expressfortolling2.dao.PlaceOfUnloading;
import no.systema.jservices.tvinn.expressfortolling2.dao.PreviousDocuments;
import no.systema.jservices.tvinn.expressfortolling2.dao.ReleasedConfirmation;
import no.systema.jservices.tvinn.expressfortolling2.dao.Representative;
import no.systema.jservices.tvinn.expressfortolling2.dao.TotalAmountInvoiced;
import no.systema.jservices.tvinn.expressfortolling2.dao.TransportCharges;
import no.systema.jservices.tvinn.expressfortolling2.dao.TransportDocumentHouseLevel;
import no.systema.jservices.tvinn.expressfortolling2.dao.TransportDocumentMasterLevel;
import no.systema.jservices.tvinn.expressfortolling2.dao.TransportEquipment;
import no.systema.jservices.tvinn.expressfortolling2.dto.SadexhfDto;
import no.systema.jservices.tvinn.expressfortolling2.util.DateUtils;

public class MapperHouseConsignment {
	private static final Logger logger = LoggerFactory.getLogger(MapperMasterConsignment.class);
	
	//JSON spec: https://api-test.toll.no/api/movement/road/v1/swagger-ui/index.html
	public HouseConsignment mapHouseConsignment(SadexhfDto sourceDto) {
		
		HouseConsignment hc = new HouseConsignment();
		//IssueDate
		//hc.setDocumentIssueDate("2022-08-16T11:49:52Z");
		hc.setDocumentIssueDate(new DateUtils().getZuluTimeWithoutMilliseconds());
		
		//Declarant
		Declarant dec = new Declarant();
		
		dec.setName("Posten Norge AS");
		Address address = new Address();
		address.setCity("Oslo");
		address.setCountry("NO");
		address.setStreetLine("Hausemanns gate");
		address.setPostcode("0001");
		address.setNumber("10B");
		address.setPoBox("P.B 194");
		dec.setAddress(address);
		//
		List commList = new ArrayList();
		commList.add(this.populateCommunication("xxx@gmail.com", "ME"));
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
		
		
		hc.setHouseConsignmentConsignmentHouseLevel(this.populateHouseConsignmentConsignmentHouseLevel(sourceDto));
		
		/*
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
	
	
	
	private HouseConsignmentConsignmentHouseLevel populateHouseConsignmentConsignmentHouseLevel(SadexhfDto sourceDto) {
		
		HouseConsignmentConsignmentHouseLevel chl = new HouseConsignmentConsignmentHouseLevel();
		chl.setContainerIndicator(1);
		chl.setTotalGrossMass(103.23);
		chl.setReferenceNumberUCR("string");
		
		List _exportFromEUList = new ArrayList();
		ExportFromEU exportFromEU = new ExportFromEU();
		exportFromEU.setExportId("22SEE1452362514521");
		exportFromEU.setTypeOfExport("UGE_EXPORT");
		_exportFromEUList.add(exportFromEU);
		chl.setExportFromEU(_exportFromEUList);
		
		ImportProcedure importProcedure = new ImportProcedure();
		importProcedure.setImportProcedure("IMMEDIATE_RELEASE_IMPORT");
		importProcedure.setOutgoingProcedure("EXP");
		chl.setImportProcedure(importProcedure);
		
		List prevDocsList = new ArrayList();
		PreviousDocuments prevDocs = new PreviousDocuments();
		prevDocs.setReferenceNumber("22NO12345678987654");
		prevDocs.setTypeOfReference("CUDE");
		prevDocs.setDeclarantNumber("123456789");
		prevDocs.setDeclarationDate("2022-08-10");
		prevDocs.setSequenceNumber("1");
		prevDocsList.add(prevDocs);
		chl.setPreviousDocuments(prevDocsList);
		
		PlaceOfLoading ploading = new PlaceOfLoading();
		ploading.setLocation("string");
		ploading.setUnloCode("NO SVD");
		AddressCountry ploadAddress = new AddressCountry();
		ploadAddress.setCountry("NO");
		ploading.setAddress(ploadAddress);
 		chl.setPlaceOfLoading(ploading);
 		
		PlaceOfUnloading punloading = new PlaceOfUnloading();
		punloading.setLocation("string");
		punloading.setUnloCode("NO SVD");
		AddressCountry punloadAddress = new AddressCountry();
		ploadAddress.setCountry("NO");
		punloading.setAddress(ploadAddress);
		chl.setPlaceOfUnloading(punloading);
		
		Consignee consignee = new Consignee();
		consignee.setName("Consignee Jonsson");
		consignee.setIdentificationNumber("951325847");
		consignee.setTypeOfPerson(1);
		//PROD-->Address cAddress = this.setAddress(sourceDto.getEmpst(), sourceDto.getEmlkt(), sourceDto.getEmpnt(), sourceDto.getEmad1t(), sourceDto.getEmnrt());
		Address cAddress = this.setAddress("Oslo", "NO", "0010", "Hausemanns gate", "52");
		consignee.setAddress(cAddress);
		consignee.setCommunication(this.setCommunication("en-epost@mail.no", "ME"));
		chl.setConsignee(consignee);
		
		Consignor consignor = new Consignor();
		consignor.setName("Consignor Svensson");
		consignor.setIdentificationNumber("951325847");
		consignor.setTypeOfPerson(1);
		//PROD-->Address cAddress = this.setAddress(sourceDto.getEmpst(), sourceDto.getEmlkt(), sourceDto.getEmpnt(), sourceDto.getEmad1t(), sourceDto.getEmnrt());
		Address cgorAddress = this.setAddress("Oslo", "NO", "0010", "Hausemanns gate", "52");
		consignor.setAddress(cgorAddress);
		consignor.setCommunication(this.setCommunication("consignor@mail.no", "ME"));
		chl.setConsignor(consignor);
		
		TransportDocumentHouseLevel transpDocHouseLevel = new TransportDocumentHouseLevel();
		transpDocHouseLevel.setDocumentNumber(sourceDto.getEhdkh());
		transpDocHouseLevel.setType(sourceDto.getEhdkht());
		chl.setTransportDocumentHouseLevel(transpDocHouseLevel);
		
		List goodsItem = this.getGoodsItemList();
		chl.setGoodsItem(goodsItem);
		
		//(Optional)Transport Equipment
		List transpEquipmentList = new ArrayList();
		TransportEquipment transportEquipment = new TransportEquipment();
		transportEquipment.setContainerIdentificationNumber("2342342432SAS");
		transpEquipmentList.add(transportEquipment);
		chl.setTransportEquipment(transpEquipmentList);
		
		
		//(Optional)Passive Transport Means
		List ptmList = new ArrayList();
		PassiveTransportMeans passiveTransportMeans = new PassiveTransportMeans();
		passiveTransportMeans.setCountryCode("DK");
		passiveTransportMeans.setIdentificationNumber("DK123456");
		passiveTransportMeans.setTypeOfIdentification(30);
		passiveTransportMeans.setTypeOfMeansOfTransport("150");
		ptmList.add(passiveTransportMeans);
		chl.setPassiveTransportMeans(ptmList);
		
		
		TransportCharges transpCharges = new TransportCharges();
		transpCharges.setMethodOfPayment("s");
		transpCharges.setCurrency("NOK");
		transpCharges.setValue(0.0);
		chl.setTransportCharges(transpCharges);
		
		
		TotalAmountInvoiced totalAmount = new TotalAmountInvoiced();
		totalAmount.setValue(100.0);
		totalAmount.setCurrency("NOK");
		chl.setTotalAmountInvoiced(totalAmount);
		
		return chl;
		
	}
	/**
	 * 
	 * @return
	 */
	private List<GoodsItem> getGoodsItemList() {
		List goodsItemList = new ArrayList();
		
		GoodsItem item = new GoodsItem();
		item.setDeclarationGoodsItemNumber("2");
		item.setTransitGoodsItemNumber("3");
		item.setTypeOfGoods("11");
		
		ItemAmountInvoiced itemAmountInvoiced = new ItemAmountInvoiced();
		itemAmountInvoiced.setCurrency("NOK");
		itemAmountInvoiced.setValue(0.0);
		item.setItemAmountInvoiced(itemAmountInvoiced);
		
		Commodity commodity = new Commodity();
		commodity.setDescriptionOfGoods("string");
		commodity.setCusCode("string");
		CommodityCode commodityCode = new CommodityCode();
		commodityCode.setCombinedNomenclatureCode("00");
		commodityCode.setHarmonizedSystemSubheadingCode("551100");
		commodity.setCommodityCode(commodityCode);
		//Dangerous goods
		List dangGoodsList = new ArrayList();
		DangerousGoods dangerousGoods = new DangerousGoods();
		dangerousGoods.setUnNumber("1055");
		dangGoodsList.add(dangerousGoods);
		commodity.setDangerousGoods(dangGoodsList);
		//Goods measure
		GoodsMeasure goodsMeasure = new GoodsMeasure();
		goodsMeasure.setGrossMass(0.0);
		goodsMeasure.setNetMass(0.0);
		goodsMeasure.setSupplementaryUnits("STK");
		commodity.setGoodsMeasure(goodsMeasure);
		//
		item.setCommodity(commodity);
		
		//Country of Origin
		CountryOfOrigin countryOfOrigin = new CountryOfOrigin();
		countryOfOrigin.setCountry("SE");
		item.setCountryOfOrigin(countryOfOrigin);
		
		//Packaging
		List packagingList = new ArrayList();
		Packaging packaging = new Packaging();
		packaging.setNumberOfPackages(0);
		packaging.setShippingMarks("String");
		packaging.setTypeOfPackages("PX");
		packagingList.add(packaging);
		item.setPackaging(packagingList);
		
		//Passive Transport Means
		List ptmList = new ArrayList();
		PassiveTransportMeans passiveTransportMeans = new PassiveTransportMeans();
		passiveTransportMeans.setCountryCode("DK");
		passiveTransportMeans.setIdentificationNumber("DK 123456");
		passiveTransportMeans.setTypeOfIdentification(30);
		passiveTransportMeans.setTypeOfMeansOfTransport("150");
		ptmList.add(passiveTransportMeans);
		item.setPassiveTransportMeans(ptmList);
		
		//Transport Equipment
		List transpEquipmentList = new ArrayList();
		TransportEquipment transportEquipment = new TransportEquipment();
		transportEquipment.setContainerIdentificationNumber("12345678901234567");
		transpEquipmentList.add(transportEquipment);
		item.setTransportEquipment(transpEquipmentList);
		
		
		//add to goods item list
		goodsItemList.add(item);
		
		
		
		return goodsItemList;
		
	}
	
	private List<Communication> setCommunication(String id, String type) {
		Communication communication = new Communication();
		communication.setIdentifier(id);
		communication.setType(type);
		List tmp = new ArrayList();
		tmp.add(communication);
		return tmp;
	}
	/*
	private ActiveBorderTransportMeans populateActiveBorderTransportMeans(SadexmfDto sourceDto) {
		ActiveBorderTransportMeans ab = new ActiveBorderTransportMeans();
		ab.setIdentificationNumber("DK 123654");
		ab.setTypeOfIdentification("30");
		ab.setTypeOfMeansOfTransport("150");
		ab.setNationalityCode("SE");
		ab.setModeOfTransportCode("3");
		ab.setActualDateAndTimeOfDeparture("2022-09-20T07:49:52Z");
		ab.setEstimatedDateAndTimeOfDeparture("2022-09-20T07:49:52Z");
		ab.setEstimatedDateAndTimeOfArrival("2022-09-20T07:49:52Z");
		//
		Operator operator = new Operator();
		operator.setName(sourceDto.getEmsjaf());
		operator.setCitizenship(sourceDto.getEmsjalk());
		//operator.setDateOfBirth("1982-06-22");
		operator.setDateOfBirth(formatDateOfBirth(String.valueOf(sourceDto.getEmsjadt()) ));
		//
		ab.setOperator(operator);
		//Crew
		Crew crew = new Crew();
		crew.setName(sourceDto.getEmsj2f());
		crew.setCitizenship(sourceDto.getEmsj2lk());
		//crew.setDateOfBirth("1982-06-22");
		crew.setDateOfBirth(formatDateOfBirth(String.valueOf(sourceDto.getEmsj2dt()) ));
		List tmp = new ArrayList();
		tmp.add(crew);
		ab.setCrew(tmp);
		
		return ab;
		
	}
	
	private String formatDateOfBirth(String value) {
		
		String year = value.substring(0,4);
		String month = value.substring(4,6);
		String day = value.substring(6,8);
		
		return year + "-" + month + "-" + day;
		
	}
	
	private ReleasedConfirmation populateReleasedConfirmation(String email) {
		ReleasedConfirmation releasedConfirmation = new ReleasedConfirmation();
		releasedConfirmation.setEmailAddress(email);
		return releasedConfirmation;
	}
	*/
	
	private Address setAddress(String city, String country, String postCode, String street, String number) {
		Address address = new Address();
		address.setCity(city);
		address.setCountry(country);
		address.setPostcode(postCode);
		address.setStreetLine(street);
		address.setNumber(number);
		
		return address;
	}
	
}
