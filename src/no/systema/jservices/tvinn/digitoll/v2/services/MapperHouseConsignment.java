package no.systema.jservices.tvinn.digitoll.v2.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.systema.jservices.tvinn.digitoll.v2.dao.*;
import no.systema.jservices.tvinn.expressfortolling2.util.BigDecimalFormatter;
import no.systema.jservices.tvinn.expressfortolling2.util.DateUtils;


/**
 * SPEC. SWAGGER
 * https://api-test.toll.no/api/movement/road/v2/swagger-ui/index.html
 * 
 * @author oscardelatorre
 * Jun 2023
 * 
 */
public class MapperHouseConsignment {
	private static final Logger logger = LoggerFactory.getLogger(MapperHouseConsignment.class);
	
	public HouseConsignment mapHouseConsignment(Object sourceDto) {
		
		HouseConsignment hc = new HouseConsignment();
		//(Mandatory) IssueDate
		//hc.setDocumentIssueDate("2022-08-16T11:49:52Z");
		hc.setDocumentIssueDate(new DateUtils().getZuluTimeWithoutMillisecondsUTC());
		
		
		//(Mandatory) Representative
		Representative rep = new Representative();
		rep.setName("sourceDto.getEhnar()");
		rep.setIdentificationNumber("sourceDto.getEhrgr()");
		rep.setAddress(this.setAddress("sourceDto.getEhpsr()", "sourceDto.getEhlkr()", "sourceDto.getEhpnr()", "sourceDto.getEhad1r()", "sourceDto.getEhnrr()"));
		//
		List rcommList = new ArrayList();
		rcommList.add(this.populateCommunication("sourceDto.getEhemr()", "sourceDto.getEhemrt()"));
		rep.setCommunication(rcommList);
		hc.setRepresentative(rep);
		
		//(Mandatory)
		
		hc.setHouseConsignmentConsignmentHouseLevel(this.populateHouseConsignmentConsignmentHouseLevel(sourceDto));
		
		
		return hc;
	}
	
	/**
	 * Only issueDate for delete
	 * @param sourceDto
	 * @return
	 * 
	 */
	public HouseConsignment mapHouseConsignmentForDelete(Object sourceDto) {
		
		HouseConsignment hc = new HouseConsignment();
		//(Mandatory) IssueDate
		hc.setDocumentIssueDate(new DateUtils().getZuluTimeWithoutMillisecondsUTC());
		
		
		return hc;
	}
	
	private Communication populateCommunication(String id, String type) {
		
		Communication communication = new Communication();
		communication.setIdentifier(id);
		communication.setType(type);
		return communication;
	}
	
	
	/**
	 * 
	 * @param sourceDto
	 * @return
	 * refer to https://api-test.toll.no/api/movement/road/v2/swagger-ui/... for all specifics
	 */
	
	private HouseConsignmentConsignmentHouseLevel populateHouseConsignmentConsignmentHouseLevel(Object sourceDto) {
		DateUtils dateUtils = new DateUtils("yyyyMMdd", "yyyy-MM-dd");
		//(Mandatory) HouseConsignment-ConsignmentHouseLevel
		HouseConsignmentConsignmentHouseLevel chl = new HouseConsignmentConsignmentHouseLevel();
		
		//(Optional) Bara för postsäckar
		//if("todo") {
		//	chl.setReceptacleIdentificationNumber("todo");
		//}
		
		//(Mandatory) ContainerIndicator
		chl.setContainerIndicator(0);//sourceDto.getEhcnin()
		//(Mandatory) TotalGrossMass
		chl.setTotalGrossMass(0.00);//sourceDto.getEhvkb()
				
		/* TODO
		if(sourceDto.getGoodsItemList()!=null && sourceDto.getGoodsItemList().size()>0) {
			//(Mandatory) numberOfPackages (sum of those in Item Lines)
			chl.setNumberOfPackages(this.getTotalNumberOfPackages(sourceDto.getGoodsItemList())); 
			//(Mandatory) goodsDescription (concatenated description of all item lines
			chl.setGoodsDescription(this.getGoodsDescription(sourceDto.getGoodsItemList())); 
		}
		*/
		
		//(Mandatory) TransportDocumentHouseLevel
		TransportDocumentHouseLevel transpDocHouseLevel = new TransportDocumentHouseLevel();
		transpDocHouseLevel.setDocumentNumber("sourceDto.getEhdkh()");
		transpDocHouseLevel.setType("sourceDto.getEhdkht()");
		chl.setTransportDocumentHouseLevel(transpDocHouseLevel);
		

		//TODO (Optional) consignmentMasterLevel
		
		
		
		//(Mandatory) ImportProcedure
		ImportProcedure importProcedure = new ImportProcedure();
		importProcedure.setImportProcedure("sourceDto.getEhprt()");
		//(Optional)TRA/EXP/TRE/FALSE
		if(StringUtils.isNotEmpty("sourceDto.getEhupr()")) {
			//if("sourceDto.getEhupr()".equalsIgnoreCase("FALSE")){
				importProcedure.setHasOutgoingProcedure("sourceDto.getEhupr()");
			//}else {
				//TRA/EXP/TRE
				//importProcedure.setOutgoingProcedure(sourceDto.getEhupr());	
			//}	
		}
		chl.setImportProcedure(importProcedure);
		
		
		//(Optional) Previous Documents
		List prevDocsList = new ArrayList();
		//if(StringUtils.isNotEmpty("sourceDto.getEhtrnr()") || 
		//		(StringUtils.isNotEmpty("sourceDto.getEhrg()") && sourceDto.getEh0068b()>0 )) {
			//(1)
			if(StringUtils.isNotEmpty("sourceDto.getEhtrnr()")){
				PreviousDocuments prevDocs = new PreviousDocuments();
				prevDocs.setTypeOfReference("sourceDto.getEhtrty()");
				prevDocs.setReferenceNumber("sourceDto.getEhtrnr()");
				prevDocsList.add(prevDocs);

			}
			//(2)
			//if(StringUtils.isNotEmpty(sourceDto.getEhrg()) && sourceDto.getEh0068b()>0) {
				PreviousDocuments prevDocs = new PreviousDocuments();
				prevDocs.setTypeOfReference("CUDE");
				prevDocs.setDeclarantNumber("sourceDto.getEhrg()");
				prevDocs.setDeclarationDate(dateUtils.getDate(String.valueOf("sourceDto.getEh0068a()")));
				prevDocs.setSequenceNumber(String.valueOf("sourceDto.getEh0068b()"));
				prevDocsList.add(prevDocs);
			//}
			chl.setPreviousDocuments(prevDocsList);
		//}
		
		//(Optional) ExportFromEU
		List exportFromEUList = new ArrayList();
		//if(StringUtils.isNotEmpty(sourceDto.getEheid())) {
			ExportFromEU exportFromEU = new ExportFromEU();
			exportFromEU.setExportId("sourceDto.getEheid()");
			//exportFromEU.setExportId("22SEE1452362514521");
			exportFromEU.setTypeOfExport("sourceDto.getEhetypt()");
			exportFromEUList.add(exportFromEU);
			chl.setExportFromEU(exportFromEUList);
		//}
		
		
		//(Mandatory) Consignor
		Consignor consignor = new Consignor();
		consignor.setName("sourceDto.getEhnas()");
		consignor.setIdentificationNumber("sourceDto.getEhrgs()");
		//if(this.isPrivatePerson(sourceDto)) {
			consignor.setTypeOfPerson(1);
		//}else {
		//	consignor.setTypeOfPerson(2);
		//}
		//(Optional)Address ... this.setAddress("Oslo", "NO", "0010", "Hausemanns gate", "52");
		//if(StringUtils.isNotEmpty(sourceDto.getEhpss())) {
			consignor.setAddress(this.setAddress("sourceDto.getEhpss()", "sourceDto.getEhlks()", "sourceDto.getEhpns()", "sourceDto.getEhad1s()", "sourceDto.getEhnrs()"));
		//}
		//(Optional) Communication
		//if(StringUtils.isNotEmpty(sourceDto.getEhems())) { 
			consignor.setCommunication(this.setCommunication("sourceDto.getEhems()", "sourceDto.getEhemst()"));
		//}
		chl.setConsignor(consignor);
				
		
		//(Mandatory) Consignee
		Consignee consignee = new Consignee();
		consignee.setName("sourceDto.getEhnam()");
		consignee.setIdentificationNumber("sourceDto.getEhrgm()");
		//if(this.isPrivatePerson(sourceDto)) {
			consignee.setTypeOfPerson(1); //personnumer = 11 siffror
		//}else {
		//	consignee.setTypeOfPerson(2); //orgnr = 9 siffror
		//}
		//(Optional) Address
		//if(StringUtils.isNotEmpty(sourceDto.getEhpsm())) { 
			consignee.setAddress(this.setAddress("sourceDto.getEhpsm()", "sourceDto.getEhlkm()", "sourceDto.getEhpnm()", "sourceDto.getEhad1m()", "sourceDto.getEhnrm()"));
		//}	
		//(Optional) Communication
		//if(StringUtils.isNotEmpty(sourceDto.getEhemm())) { 
			consignee.setCommunication(this.setCommunication("sourceDto.getEhemm()", "sourceDto.getEhemmt()")); 
		//}
		chl.setConsignee(consignee);
		
		//(Optional) PlaceOfAcceptance
		//if(StringUtils.isNotEmpty(sourceDto.getEmsdl())) {
			PlaceOfAcceptance placc = new PlaceOfAcceptance();
			if(StringUtils.isNotEmpty("sourceDto.getEhsdlt()")) { placc.setLocation("sourceDto.getEhsdlt()"); }
			if(StringUtils.isNotEmpty("sourceDto.getEhsdl()")) { placc.setUnloCode("sourceDto.getEhsdl()"); }
			if(StringUtils.isNotEmpty("sourceDto.getEhlkl()")) {
				AddressCountry addressCountry = new AddressCountry();
				addressCountry.setCountry("sourceDto.getEmlkl()");
				placc.setAddress(addressCountry);
			}
			chl.setPlaceOfAcceptance(placc);
		//}
		
		//(Optional) PlaceOfDelivery
		//if(StringUtils.isNotEmpty(sourceDto.getExxx())) {
			PlaceOfDelivery pldel = new PlaceOfDelivery();
			if(StringUtils.isNotEmpty("sourceDto.getEhsdlt()")) { pldel.setLocation("sourceDto.getEhsdlt()"); }
			if(StringUtils.isNotEmpty("sourceDto.getEhsdl()")) { pldel.setUnloCode("sourceDto.getEhsdl()"); }
			if(StringUtils.isNotEmpty("sourceDto.getEhlkl()")) {
				AddressCountry addressCountry = new AddressCountry();
				addressCountry.setCountry("sourceDto.getEhlkl()");
				pldel.setAddress(addressCountry);
			}
			chl.setPlaceOfDelivery(pldel);
		//}
		
			
			
		//TODO (Optional) goodsItem
		/* Only for VOEC
		logger.warn("GOODS-ITEM-LIST size:" + String.valueOf(sourceDto.getGoodsItemList().size()));
		Map<String, Double> mapTotalAmountInvoiced = new HashMap<String, Double>(); //must be filled out as the sum of all amounts per item line)
		StringBuilder currencyCodeTotalAmountInvoiced = new StringBuilder(); //this is the currency for the totalAmountInvoiced
		
		if(sourceDto.getGoodsItemList()!=null && sourceDto.getGoodsItemList().size()>0) {
			List goodsItem = this.getGoodsItemList(sourceDto.getGoodsItemList(), mapTotalAmountInvoiced, currencyCodeTotalAmountInvoiced);
			chl.setGoodsItem(goodsItem);
		}else {
			logger.error("###ERROR-ERROR-ERROR --> GOODS-ITEM-LIST on SADEXIF is 0 ??? - not valid for API...");
		}
		*/
		
		//(Optional)Transport Equipment
		List<TransportEquipment> list = this.populateTransportEquipment(sourceDto);
		if(!list.isEmpty()) {
			chl.setTranportEquipment(list);
		}
		
		
		return chl;
		
	}
	
	private List<TransportEquipment> populateTransportEquipment(Object sourceDto) {
		List<TransportEquipment> listTranspEquip = new ArrayList<>();
		//if(StringUtils.isNotEmpty(sourceDto.getEmcnr())) {
			TransportEquipment te = new TransportEquipment();
			//all below mandatory
			te.setContainerIdentificationNumber("sourceDto.getEhcnr()");
			te.setContainerSizeAndType("todo");
			//Expected codes are one of [A, B, C, D, T, t, H, Y, Z] .. KANSKE ??? OBSOLETE
			te.setContainerPackedStatus("todo");
			te.setContainerSupplierType("todo");
			listTranspEquip.add(te);
		//}
		//2 or more	
		/*if(StringUtils.isNotEmpty(sourceDto.getEmcnr2())) {
			TransportEquipment te = new TransportEquipment();
			//all below mandatory
			te.setContainerIdentificationNumber("sourceDto.getEmcnr2()");
			te.setContainerSizeAndType("todo");
			te.setContainerPackedStatus("todo");
			te.setContainerSupplierType("todo");
			listTranspEquip.add(te);
		}*/

			
		return listTranspEquip;
		
	}
	
	/*
	private List<CountriesOfRoutingOfConsignments> getExtraRoutes(SadexhfDto sourceDto, List<CountriesOfRoutingOfConsignments> list) {
		List<CountriesOfRoutingOfConsignments> retval = list;
		//2
		if(StringUtils.isNotEmpty(sourceDto.getEhlkr2())) {
			retval.add(this.addRoute(2, sourceDto.getEhlkr2()));
			//3
			if(StringUtils.isNotEmpty(sourceDto.getEhlkr3())) {
				retval.add(this.addRoute(3, sourceDto.getEhlkr3()));
				//4
				if(StringUtils.isNotEmpty(sourceDto.getEhlkr4())) {
					retval.add(this.addRoute(4, sourceDto.getEhlkr4()));
					//5
					if(StringUtils.isNotEmpty(sourceDto.getEhlkr5())) {
						retval.add(this.addRoute(5, sourceDto.getEhlkr5()));
						//6
						if(StringUtils.isNotEmpty(sourceDto.getEhlkr6())) {
							retval.add(this.addRoute(6, sourceDto.getEhlkr6()));
							//7
							if(StringUtils.isNotEmpty(sourceDto.getEhlkr7())) {
								retval.add(this.addRoute(7, sourceDto.getEhlkr7()));
								//8
								if(StringUtils.isNotEmpty(sourceDto.getEhlkr8())) {
									retval.add(this.addRoute(8, sourceDto.getEhlkr8()));
								}
							}
						}
					}
				}
			}
		}
		
		return retval;
	}
	*/
	
	/*
	private CountriesOfRoutingOfConsignments addRoute(Integer seqNumber, String value) {
		CountriesOfRoutingOfConsignments retval = new CountriesOfRoutingOfConsignments();
		retval.setSequenceNumber(seqNumber);
		retval.setCountry(value);
		return retval;
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
	*/
	
	/**
	 * 
	 * @param list
	 * @param totalAmountInvoiced
	 * @param totalAmountInvoicedCurrencyCode
	 * @return
	 */
	/*
	private List<GoodsItem> getGoodsItemList(List<SadexifDto> list, Map<String, Double> totalAmountInvoiced, StringBuilder totalAmountInvoicedCurrencyCode) {
		List<GoodsItem> returnList = new ArrayList<GoodsItem>();
		Double totalAmount = 0.00D;
		int counter = 0;
		
		for (SadexifDto dto: list) {
			counter++;
			GoodsItem item = new GoodsItem();
			//(Optionals)
			if(dto.getEili()>0) { item.setDeclarationGoodsItemNumber(String.valueOf(dto.getEili())); }
			if(dto.getEilit()>0) { item.setTransitGoodsItemNumber(String.valueOf(dto.getEilit())); }
			if(StringUtils.isNotEmpty(dto.getEigty())) { item.setTypeOfGoods(dto.getEigty()); }
			if(StringUtils.isNotEmpty(dto.getEiucr())) { item.setReferenceNumberUCR(dto.getEiucr()); }
			
			//(Mandatories)
			ItemAmountInvoiced itemAmountInvoiced = new ItemAmountInvoiced();
			itemAmountInvoiced.setCurrency(dto.getEival());
			if(counter==1) {
				//take the currency from the first item line. It will be valid as return currency for totalAmountInvoiced
				totalAmountInvoicedCurrencyCode.append(itemAmountInvoiced.getCurrency());
			}
			itemAmountInvoiced.setValue(dto.getEibl());
			item.setItemAmountInvoiced(itemAmountInvoiced);
			//accumulate totalAmount
			totalAmount += itemAmountInvoiced.getValue();
			
			//(Mandatory) Commodity
			Commodity commodity = new Commodity();
			commodity.setDescriptionOfGoods(dto.getEivt());
			//Expected codes are one of [A, B, C, D, T, t, H, Y, Z]"
			//commodity.setCusCode("A");
			CommodityCode commodityCode = new CommodityCode();
			String tariff = String.valueOf(dto.getEivnt());
			//adjust tariff if necessary in order to always have 8-chars. We must fill with zeros in case the string is < 8
			if(StringUtils.isNotEmpty(tariff) && tariff.length()<8) {
				tariff = new no.systema.jservices.common.util.StringUtils().leadingStringWithNumericFiller(tariff, 8, "0");
			}
			logger.warn("TARIFFNR:" + tariff);
			commodityCode.setHarmonizedSystemSubheadingCode(tariff.substring(0, 6));
			commodityCode.setCombinedNomenclatureCode(tariff.substring(6));
			commodity.setCommodityCode(commodityCode);
			
			//(Optional)Dangerous goods
			//List dangGoodsList = new ArrayList();
			//DangerousGoods dangerousGoods = new DangerousGoods();
			//dangerousGoods.setUnNumber("1055");
			//dangGoodsList.add(dangerousGoods);
			//commodity.setDangerousGoods(dangGoodsList);
			
			
			//Goods measure
			GoodsMeasure goodsMeasure = new GoodsMeasure();
			logger.warn("BRUTTO:" + dto.getEicvkb());
			logger.warn("NETTO:" + dto.getEicvkn());
			//(Mandatory) Gross
			goodsMeasure.setGrossMass(dto.getEicvkb());
			//(Optional) Net
			if(dto.getEicvkn()>0.00){
				goodsMeasure.setNetMass(dto.getEicvkn());
			}
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
			//(Optional) ShippingMarks
			if(StringUtils.isNotEmpty(dto.getEipmrk())) {
				packaging.setShippingMarks(dto.getEipmrk());
			}
			packagingList.add(packaging);
			item.setPackaging(packagingList);
			
			//(Optional)Passive Transport Means
			if(StringUtils.isNotEmpty(dto.getEiplk())) {
				List ptmList = new ArrayList();
				//PassiveTransportMeans passiveTransportMeans = new PassiveTransportMeans();
				//passiveTransportMeans.setCountryCode(dto.getEiplk());
				//passiveTransportMeans.setIdentificationNumber(); //TODO (db?)
				//passiveTransportMeans.setTypeOfIdentification(30); //TODO (db?)
				//passiveTransportMeans.setTypeOfMeansOfTransport("150"); //TODO (db?)
				
				PassiveTransportMeans passiveTransportMeans = new PassiveTransportMeans();
				passiveTransportMeans.setCountryCode(dto.getEiplk());
				passiveTransportMeans.setIdentificationNumber(dto.getEipmrk());
				passiveTransportMeans.setTypeOfIdentification(Integer.valueOf(dto.getEiptyp())); //30 t.ex
				passiveTransportMeans.setTypeOfMeansOfTransport(dto.getEiptm());
				
				ptmList.add(passiveTransportMeans);
				item.setPassiveTransportMeans(ptmList);
			}
			
			//(Optional)Transport Equipment
			if(StringUtils.isNotEmpty(dto.getEicnr())) {
				List transpEquipmentList = new ArrayList();
				TransportEquipment transportEquipment = new TransportEquipment();
				transportEquipment.setContainerIdentificationNumber(dto.getEicnr());
				//transportEquipment.setContainerPackedStatus("0");
				transpEquipmentList.add(transportEquipment);
				item.setTransportEquipment(transpEquipmentList);
			}
			
			//add to goods item list
			returnList.add(item);
		}
		//send totalAmount for further use outside this method...
		BigDecimal bd = new BigDecimal(totalAmount).setScale(2, RoundingMode.HALF_UP);
		totalAmountInvoiced.put("totalAmount", bd.doubleValue());
		
		return returnList;
		
	}
	*/
	
	/*
	private int getTotalNumberOfPackages(List<SadexifDto> list) {
		List<GoodsItem> returnList = new ArrayList<GoodsItem>();
		int totalAmounNumberOfPackages = 0;
		
		for (SadexifDto dto: list) {
			
			//(Mandatory) Packaging
			if(dto.getEint()>0) {
				totalAmounNumberOfPackages += dto.getEint();
			}
		}
		
		return totalAmounNumberOfPackages;
		
	}
	*/
	
	/*
	private String getGoodsDescription(List<SadexifDto> list) {
		String FIELD_SEPARATOR = " ,";
		StringBuilder strBuilder = new StringBuilder();
		int counter = 0;
		for (SadexifDto dto: list) {
			counter++;
			//(Mandatory) Packaging
			if(StringUtils.isNotEmpty(dto.getEivt())) {
				if(counter==1) {
					strBuilder.append(dto.getEivt());
				}else {
					strBuilder.append(FIELD_SEPARATOR + dto.getEivt());
				}
			}
		}
		
		
		return strBuilder.toString();
		
	}
	*/
	
	private List<Communication> setCommunication(String id, String type) {
		Communication communication = new Communication();
		communication.setIdentifier(id);
		communication.setType(type);
		List tmp = new ArrayList();
		tmp.add(communication);
		return tmp;
	}
	
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
