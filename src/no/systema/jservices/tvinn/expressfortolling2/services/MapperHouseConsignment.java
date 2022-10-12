package no.systema.jservices.tvinn.expressfortolling2.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
import no.systema.jservices.tvinn.expressfortolling2.dto.SadexifDto;
import no.systema.jservices.tvinn.expressfortolling2.util.DateUtils;

public class MapperHouseConsignment {
	private static final Logger logger = LoggerFactory.getLogger(MapperMasterConsignment.class);
	
	//JSON spec: https://api-test.toll.no/api/movement/road/v1/swagger-ui/index.html
	public HouseConsignment mapHouseConsignment(SadexhfDto sourceDto) {
		
		HouseConsignment hc = new HouseConsignment();
		//(Mandatory) IssueDate
		//hc.setDocumentIssueDate("2022-08-16T11:49:52Z");
		hc.setDocumentIssueDate(new DateUtils().getZuluTimeWithoutMilliseconds());
		
		//(Mandatory) Declarant
		Declarant dec = new Declarant();
		
		dec.setName(sourceDto.getEhnad());
		Address address = new Address();
		address.setCity(sourceDto.getEhpsd());
		address.setCountry(sourceDto.getEhlkd());
		if(StringUtils.isNotEmpty(sourceDto.getEhad1d())) { address.setStreetLine(sourceDto.getEhad1d()); }
		if(StringUtils.isNotEmpty(sourceDto.getEhpnd())) { address.setPostcode(sourceDto.getEhpnd()); }
		if(StringUtils.isNotEmpty(sourceDto.getEhnrd())) { address.setNumber(sourceDto.getEhnrd()); }
		if(StringUtils.isNotEmpty(sourceDto.getEhpbd())) { address.setPoBox(sourceDto.getEhpbd()); }
		dec.setAddress(address);
		//
		List commList = new ArrayList();
		commList.add(this.populateCommunication(sourceDto.getEhemd(), sourceDto.getEhemdt()));
		//commList.add(this.populateCommunication("0733794505", "TE"));
		dec.setCommunication(commList);
		
		hc.setDeclarant(dec);
		
		
		//(Mandatory) Representative
		Representative rep = new Representative();
		rep.setName(sourceDto.getEhnar());
		rep.setIdentificationNumber(sourceDto.getEhrgr());
		//status
		if(StringUtils.isNotEmpty(sourceDto.getEhstr())){
			rep.setStatus(sourceDto.getEhstr());
		}else {
			rep.setStatus("2");
		}
		
		Address raddress = new Address();
		raddress.setCity(sourceDto.getEhpsr());
		raddress.setCountry(sourceDto.getEhlkr());
		if(StringUtils.isNotEmpty(sourceDto.getEhad1r())) { raddress.setStreetLine(sourceDto.getEhad1r()); }
		if(StringUtils.isNotEmpty(sourceDto.getEhpnr())) { raddress.setPostcode(sourceDto.getEhpnr()); }
		if(StringUtils.isNotEmpty(sourceDto.getEhnrr())) { raddress.setNumber(sourceDto.getEhnrr()); }
		if(StringUtils.isNotEmpty(sourceDto.getEhpbr())) { raddress.setPoBox(sourceDto.getEhpbr()); }
		rep.setAddress(raddress);
		
		//
		List rcommList = new ArrayList();
		rcommList.add(this.populateCommunication(sourceDto.getEhemr(), sourceDto.getEhemrt()));
		//rcommList.add(this.populateCommunication("0733794599", "TE"));
		rep.setCommunication(rcommList);
		hc.setRepresentative(rep);
		
		//(Mandatory) 
		hc.setHouseConsignmentConsignmentHouseLevel(this.populateHouseConsignmentConsignmentHouseLevel(sourceDto));
		
		
		return hc;
	}
	
	
	
	
	private Communication populateCommunication(String id, String type) {
		
		Communication communication = new Communication();
		communication.setIdentifier(id);
		communication.setType(type);
		return communication;
	}
	
	
	
	private HouseConsignmentConsignmentHouseLevel populateHouseConsignmentConsignmentHouseLevel(SadexhfDto sourceDto) {
		DateUtils dateUtils = new DateUtils("yyyyMMdd", "yyyy-MM-dd");
		
		HouseConsignmentConsignmentHouseLevel chl = new HouseConsignmentConsignmentHouseLevel();
		chl.setContainerIndicator(sourceDto.getEhcnin());
		chl.setTotalGrossMass(sourceDto.getEhvkb());
		chl.setReferenceNumberUCR(sourceDto.getEhucr());
		
		List exportFromEUList = new ArrayList();
		if(StringUtils.isNotEmpty(sourceDto.getEheid())) {
			ExportFromEU exportFromEU = new ExportFromEU();
			exportFromEU.setExportId(sourceDto.getEheid());
			//exportFromEU.setExportId("22SEE1452362514521");
			exportFromEU.setTypeOfExport(sourceDto.getEhetypt());
			exportFromEUList.add(exportFromEU);
			chl.setExportFromEU(exportFromEUList);
		}
		
		ImportProcedure importProcedure = new ImportProcedure();
		importProcedure.setImportProcedure(sourceDto.getEhprt());
		//TRA/EXP/TRE
		importProcedure.setOutgoingProcedure(sourceDto.getEhupr());
		chl.setImportProcedure(importProcedure);
		
		List prevDocsList = new ArrayList();
		if(StringUtils.isNotEmpty(sourceDto.getEhtrnr())) {
			PreviousDocuments prevDocs = new PreviousDocuments();
			prevDocs.setReferenceNumber(sourceDto.getEhtrnr());
			prevDocs.setTypeOfReference(sourceDto.getEhtrty());
			prevDocs.setDeclarantNumber(sourceDto.getEhrg());
			prevDocs.setDeclarationDate(dateUtils.getDate(String.valueOf(sourceDto.getEh0068a())));
			prevDocs.setSequenceNumber(String.valueOf(sourceDto.getEh0068b()));
			prevDocsList.add(prevDocs);
			chl.setPreviousDocuments(prevDocsList);
		}else {
			/*PreviousDocuments prevDocs = new PreviousDocuments();
			prevDocs.setReferenceNumber("22NO12345678987654");
			prevDocs.setTypeOfReference("CUDE");
			prevDocs.setDeclarantNumber("123456789");
			prevDocs.setDeclarationDate("2022-08-10");
			prevDocs.setSequenceNumber("1");
			prevDocsList.add(prevDocs);
			chl.setPreviousDocuments(prevDocsList);
			*/
		}
		
		PlaceOfLoading ploading = new PlaceOfLoading();
		ploading.setLocation(sourceDto.getEhsdft());
		//ploading.setUnloCode("NO SVD");
		AddressCountry ploadAddress = new AddressCountry();
		ploadAddress.setCountry(sourceDto.getEhlkf());
		ploading.setAddress(ploadAddress);
 		chl.setPlaceOfLoading(ploading);
 		
		PlaceOfUnloading punloading = new PlaceOfUnloading();
		punloading.setLocation(sourceDto.getEhsdtt());
		//punloading.setUnloCode("NO SVD");
		AddressCountry punloadAddress = new AddressCountry();
		ploadAddress.setCountry(sourceDto.getEhlkt());
		punloading.setAddress(ploadAddress);
		chl.setPlaceOfUnloading(punloading);
		
		Consignee consignee = new Consignee();
		consignee.setName(sourceDto.getEhnam());
		consignee.setIdentificationNumber(sourceDto.getEhrgm());
		if(this.isPrivatePerson(sourceDto)) {
			consignee.setTypeOfPerson(1); //personnumer = 11 siffror
		}else {
			consignee.setTypeOfPerson(2); //orgnr = 9 siffror
		}
		//PROD-->
		Address cAddress = this.setAddress(sourceDto.getEhpsm(), sourceDto.getEhlkm(), sourceDto.getEhpnm(), sourceDto.getEhad1m(), sourceDto.getEhnrm());
		//Address cAddress = this.setAddress("Oslo", "NO", "0010", "Hausemanns gate", "52");
		consignee.setAddress(cAddress);
		consignee.setCommunication(this.setCommunication(sourceDto.getEhemm(), sourceDto.getEhemmt()));
		chl.setConsignee(consignee);
		
		Consignor consignor = new Consignor();
		consignor.setName(sourceDto.getEhnas());
		consignor.setIdentificationNumber(sourceDto.getEhrgs());
		if(this.isPrivatePerson(sourceDto)) {
			consignor.setTypeOfPerson(1);
		}else {
			consignor.setTypeOfPerson(2);
		}
		Address cgorAddress = this.setAddress(sourceDto.getEhpss(), sourceDto.getEhlks(), sourceDto.getEhpns(), sourceDto.getEhad1s(), sourceDto.getEhnrs());
		//Address cgorAddress = this.setAddress("Oslo", "NO", "0010", "Hausemanns gate", "52");
		consignor.setAddress(cgorAddress);
		consignor.setCommunication(this.setCommunication(sourceDto.getEhems(), sourceDto.getEhemst()));
		chl.setConsignor(consignor);
		
		TransportDocumentHouseLevel transpDocHouseLevel = new TransportDocumentHouseLevel();
		transpDocHouseLevel.setDocumentNumber(sourceDto.getEhdkh());
		transpDocHouseLevel.setType(sourceDto.getEhdkht());
		//(Mandatory) DocumentNumber
		transpDocHouseLevel.setDocumentNumber(sourceDto.getEhdkh());
		transpDocHouseLevel.setType(sourceDto.getEhdkht());
		chl.setTransportDocumentHouseLevel(transpDocHouseLevel);
		
		//TEST
		//List goodsItem = this.getGoodsItemList();
		//chl.setGoodsItem(goodsItem);
		
		logger.warn("GOODS-ITEM-LIST size:" + String.valueOf(sourceDto.getGoodsItemList().size()));
		List goodsItem = this.getGoodsItemList(sourceDto.getGoodsItemList());
		chl.setGoodsItem(goodsItem);
		
		//(Optional)Transport Equipment
		if(StringUtils.isNotEmpty(sourceDto.getEhcnr())) {
			List transpEquipmentList = new ArrayList();
			TransportEquipment transportEquipment = new TransportEquipment();
			transportEquipment.setContainerIdentificationNumber(sourceDto.getEhcnr());
	
			//Expected codes are one of [A, B, C, D, T, t, H, Y, Z] .. KANSKE ??? OBSOLETE
			/*transportEquipment.setContainerPackedStatus("A");
			transportEquipment.setContainerSizeAndType("22");
			transportEquipment.setContainerSupplierType("1");
			*/
			transpEquipmentList.add(transportEquipment);
			chl.setTransportEquipment(transpEquipmentList);
		}
		
		//(Optional)Passive Transport Means
		List ptmList = new ArrayList();
		if(StringUtils.isNotEmpty(sourceDto.getEhpmrk())) {
			PassiveTransportMeans passiveTransportMeans = new PassiveTransportMeans();
			passiveTransportMeans.setCountryCode(sourceDto.getEhplk());
			passiveTransportMeans.setIdentificationNumber(sourceDto.getEhpmrk());
			passiveTransportMeans.setTypeOfIdentification(Integer.valueOf(sourceDto.getEhptyp())); //30 t.ex
			passiveTransportMeans.setTypeOfMeansOfTransport(sourceDto.getEhptm());
			ptmList.add(passiveTransportMeans);
			chl.setPassiveTransportMeans(ptmList);
		}
		
		//(Mandatory) TransportCharges
		TransportCharges transpCharges = new TransportCharges();
		//Expected codes are one of [A=Kontant, B=Kredikort, C, D=Annet, H=Elektronisk pengeöverf., Y, Z=not pre-paid]"
		//Optional
		/*if(sourceDto.get??) {
			transpCharges.setMethodOfPayment(sourceDto.get??);
		}*/
		transpCharges.setCurrency(sourceDto.getEhtcva());
		transpCharges.setValue(sourceDto.getEhtcbl());
		chl.setTransportCharges(transpCharges);
		
		//(Mandatory Total Amount
		TotalAmountInvoiced totalAmount = new TotalAmountInvoiced();
		totalAmount.setValue(sourceDto.getEhtcbl());
		totalAmount.setCurrency(sourceDto.getEhtcva());
		chl.setTotalAmountInvoiced(totalAmount);
		
		return chl;
		
	}
	private boolean isPrivatePerson(SadexhfDto sourceDto) {
		boolean retval = true;
		//Orgnr = 9-siffror
		//Personnr = 11-siffor
		
		//1 - Natural person, 2 - Legal person, 3 - Association of persons
		if(StringUtils.isNotEmpty(sourceDto.getEhrgm()) && sourceDto.getEhrgm().length()==11) {
			retval = false;
		}
		
		return retval;
	}
	/**
	 * 
	 * @return
	 */
	private List<GoodsItem> getGoodsItemListTest() {
		List goodsItemList = new ArrayList();
		
		GoodsItem item = new GoodsItem();
		item.setDeclarationGoodsItemNumber("2");
		item.setTransitGoodsItemNumber("3");
		item.setTypeOfGoods("11");
		item.setReferenceNumberUCR("1234565");
		
		ItemAmountInvoiced itemAmountInvoiced = new ItemAmountInvoiced();
		itemAmountInvoiced.setCurrency("NOK");
		itemAmountInvoiced.setValue(0.0);
		item.setItemAmountInvoiced(itemAmountInvoiced);
		
		Commodity commodity = new Commodity();
		commodity.setDescriptionOfGoods("string");
		
		//Expected codes are one of [A, B, C, D, T, t, H, Y, Z]"
		//commodity.setCusCode("A");
		
		CommodityCode commodityCode = new CommodityCode();
		commodityCode.setCombinedNomenclatureCode("00");
		commodityCode.setHarmonizedSystemSubheadingCode("551100");
		commodity.setCommodityCode(commodityCode);
		//(Optional)Dangerous goods
		/*List dangGoodsList = new ArrayList();
		DangerousGoods dangerousGoods = new DangerousGoods();
		dangerousGoods.setUnNumber("1055");
		dangGoodsList.add(dangerousGoods);
		commodity.setDangerousGoods(dangGoodsList);
		*/
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
		
		//(Optional)Transport Equipment
		/*List transpEquipmentList = new ArrayList();
		TransportEquipment transportEquipment = new TransportEquipment();
		transportEquipment.setContainerIdentificationNumber("12345678901234567");
		transportEquipment.setContainerPackedStatus("0");
		transpEquipmentList.add(transportEquipment);
		item.setTransportEquipment(transpEquipmentList);
		*/
		
		//add to goods item list
		goodsItemList.add(item);
		
		
		
		return goodsItemList;
		
	}
	
	private List<GoodsItem> getGoodsItemList(List<SadexifDto> list) {
		List returnList = new ArrayList();
		
		for (SadexifDto dto: list) {
			GoodsItem item = new GoodsItem();
			if(dto.getEili()>0) {
				item.setDeclarationGoodsItemNumber(String.valueOf(dto.getEili()));
			}
			if(dto.getEilit()>0) {
				item.setTransitGoodsItemNumber(String.valueOf(dto.getEilit()));
			}
			if(StringUtils.isNotEmpty(dto.getEigty())) {
				item.setTypeOfGoods(dto.getEigty());
			}
			//Mandatory UCR
			item.setReferenceNumberUCR(dto.getEiucr());
			
			ItemAmountInvoiced itemAmountInvoiced = new ItemAmountInvoiced();
			itemAmountInvoiced.setCurrency(dto.getEival());
			itemAmountInvoiced.setValue(dto.getEibl());
			item.setItemAmountInvoiced(itemAmountInvoiced);
			
			Commodity commodity = new Commodity();
			commodity.setDescriptionOfGoods(dto.getEivt());
			
			//Expected codes are one of [A, B, C, D, T, t, H, Y, Z]"
			//commodity.setCusCode("A");
			
			CommodityCode commodityCode = new CommodityCode();
			String tariff = String.valueOf(dto.getEivnt());
			logger.warn("TARIFFNR:" + tariff);
			commodityCode.setHarmonizedSystemSubheadingCode(tariff.substring(0, 6));
			commodityCode.setCombinedNomenclatureCode(tariff.substring(6));
			commodity.setCommodityCode(commodityCode);
			//(Optional)Dangerous goods
			/*List dangGoodsList = new ArrayList();
			DangerousGoods dangerousGoods = new DangerousGoods();
			dangerousGoods.setUnNumber("1055");
			dangGoodsList.add(dangerousGoods);
			commodity.setDangerousGoods(dangGoodsList);
			*/
			//Goods measure
			GoodsMeasure goodsMeasure = new GoodsMeasure();
			logger.warn("BRUTTO:" + dto.getEicvkb());
			logger.warn("NETTO:" + dto.getEicvkn());
			goodsMeasure.setGrossMass(dto.getEicvkb());
			goodsMeasure.setNetMass(dto.getEicvkn());
			if(StringUtils.isNotEmpty(dto.getEiunit())) {
				goodsMeasure.setSupplementaryUnits(dto.getEiunit());
			}
			commodity.setGoodsMeasure(goodsMeasure);
			//
			item.setCommodity(commodity);
			
			//(Mandatory) Country of Origin
			CountryOfOrigin countryOfOrigin = new CountryOfOrigin();
			countryOfOrigin.setCountry(dto.getEilk());
			item.setCountryOfOrigin(countryOfOrigin);
			
			//(Mandatory) Packaging
			List packagingList = new ArrayList();
			Packaging packaging = new Packaging();
			packaging.setNumberOfPackages(dto.getEint());
			packaging.setTypeOfPackages(dto.getEinteh());
			if(StringUtils.isNotEmpty(dto.getEipmrk())) {
				packaging.setShippingMarks(dto.getEipmrk());
			}
			packagingList.add(packaging);
			item.setPackaging(packagingList);
			
			//(Optional)Passive Transport Means
			if(StringUtils.isNotEmpty(dto.getEiplk())) {
				List ptmList = new ArrayList();
				/*PassiveTransportMeans passiveTransportMeans = new PassiveTransportMeans();
				passiveTransportMeans.setCountryCode(dto.getEiplk());
				passiveTransportMeans.setIdentificationNumber(); //TODO (db?)
				passiveTransportMeans.setTypeOfIdentification(30); //TODO (db?)
				passiveTransportMeans.setTypeOfMeansOfTransport("150"); //TODO (db?)
				*/
				PassiveTransportMeans passiveTransportMeans = new PassiveTransportMeans();
				passiveTransportMeans.setCountryCode(dto.getEiplk());
				passiveTransportMeans.setIdentificationNumber(dto.getEipmrk());
				passiveTransportMeans.setTypeOfIdentification(Integer.valueOf(dto.getEiptyp())); //30 t.ex
				passiveTransportMeans.setTypeOfMeansOfTransport(dto.getEiptm());
				
				ptmList.add(passiveTransportMeans);
				item.setPassiveTransportMeans(ptmList);
			}
			
			//(Optional)Transport Equipment
			/*List transpEquipmentList = new ArrayList();
			TransportEquipment transportEquipment = new TransportEquipment();
			transportEquipment.setContainerIdentificationNumber("12345678901234567");
			transportEquipment.setContainerPackedStatus("0");
			transpEquipmentList.add(transportEquipment);
			item.setTransportEquipment(transpEquipmentList);
			*/
			
			//add to goods item list
			returnList.add(item);
		}
		
		
		return returnList;
		
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
		if(StringUtils.isNotEmpty(city)) {
			address.setCity(city);
		}
		if(StringUtils.isNotEmpty(country)) {
			address.setCountry(country);
		}
		if(StringUtils.isNotEmpty(postCode)) {
			address.setPostcode(postCode);
		}
		if(StringUtils.isNotEmpty(street)) {
			address.setStreetLine(street);
		}
		if(StringUtils.isNotEmpty(number)) {
			address.setNumber(number);
		}
		
		return address;
	}
	
}
