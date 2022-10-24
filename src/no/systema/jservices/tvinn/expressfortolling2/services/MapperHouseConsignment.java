package no.systema.jservices.tvinn.expressfortolling2.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.systema.jservices.tvinn.expressfortolling2.dao.ActiveBorderTransportMeans;
import no.systema.jservices.tvinn.expressfortolling2.dao.AdditionalFiscalReferences;
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
import no.systema.jservices.tvinn.expressfortolling2.dao.CountriesOfRoutingOfConsignments;
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
	private static final Logger logger = LoggerFactory.getLogger(MapperHouseConsignment.class);
	
	//JSON spec: https://api-test.toll.no/api/movement/road/v1/swagger-ui/index.html
	public HouseConsignment mapHouseConsignment(SadexhfDto sourceDto) {
		
		HouseConsignment hc = new HouseConsignment();
		//(Mandatory) IssueDate
		//hc.setDocumentIssueDate("2022-08-16T11:49:52Z");
		hc.setDocumentIssueDate(new DateUtils().getZuluTimeWithoutMilliseconds());
		
		//(Mandatory) Declarant
		Declarant dec = new Declarant();
		
		dec.setName(sourceDto.getEhnad());
		dec.setAddress(this.setAddress(sourceDto.getEhpsd(), sourceDto.getEhlkd(), sourceDto.getEhpnd(), sourceDto.getEhad1d(), sourceDto.getEhnrd()));
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
		rep.setAddress(this.setAddress(sourceDto.getEhpsr(), sourceDto.getEhlkr(), sourceDto.getEhpnr(), sourceDto.getEhad1r(), sourceDto.getEhnrr()));
		//
		List rcommList = new ArrayList();
		rcommList.add(this.populateCommunication(sourceDto.getEhemr(), sourceDto.getEhemrt()));
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
	
	
	/**
	 * 
	 * @param sourceDto
	 * @return
	 */
	private HouseConsignmentConsignmentHouseLevel populateHouseConsignmentConsignmentHouseLevel(SadexhfDto sourceDto) {
		DateUtils dateUtils = new DateUtils("yyyyMMdd", "yyyy-MM-dd");
		//(Mandatory) HouseConsignment-ConsignmentHouseLevel
		HouseConsignmentConsignmentHouseLevel chl = new HouseConsignmentConsignmentHouseLevel();
		//(Mandatory) ContainerIndicator
		chl.setContainerIndicator(sourceDto.getEhcnin());
		//(Mandatory) TotalGrossMass
		chl.setTotalGrossMass(sourceDto.getEhvkb());
		//(Optional) UCR
		if(StringUtils.isNotEmpty(sourceDto.getEhucr())) {
			chl.setReferenceNumberUCR(sourceDto.getEhucr());
		}
		//(Optional) ExportFromEU
		List exportFromEUList = new ArrayList();
		if(StringUtils.isNotEmpty(sourceDto.getEheid())) {
			ExportFromEU exportFromEU = new ExportFromEU();
			exportFromEU.setExportId(sourceDto.getEheid());
			//exportFromEU.setExportId("22SEE1452362514521");
			exportFromEU.setTypeOfExport(sourceDto.getEhetypt());
			exportFromEUList.add(exportFromEU);
			chl.setExportFromEU(exportFromEUList);
		}
		//(Mandatory) ImportProcedure
		ImportProcedure importProcedure = new ImportProcedure();
		importProcedure.setImportProcedure(sourceDto.getEhprt());
		//(Optional)TRA/EXP/TRE
		if(StringUtils.isNotEmpty(sourceDto.getEhupr())) {
			importProcedure.setOutgoingProcedure(sourceDto.getEhupr());
			chl.setImportProcedure(importProcedure);
		}
		
		List prevDocsList = new ArrayList();
		if(StringUtils.isNotEmpty(sourceDto.getEhtrnr()) || 
				(StringUtils.isNotEmpty(sourceDto.getEhrg()) && sourceDto.getEh0068b()>0 )) {
			//(1)
			if(StringUtils.isNotEmpty(sourceDto.getEhtrnr())){
				PreviousDocuments prevDocs = new PreviousDocuments();
				prevDocs.setTypeOfReference(sourceDto.getEhtrty());
				prevDocs.setReferenceNumber(sourceDto.getEhtrnr());
				prevDocsList.add(prevDocs);

			}
			//(2)
			if(StringUtils.isNotEmpty(sourceDto.getEhrg()) && sourceDto.getEh0068b()>0) {
				PreviousDocuments prevDocs = new PreviousDocuments();
				prevDocs.setTypeOfReference("CUDE");
				prevDocs.setDeclarantNumber(sourceDto.getEhrg());
				prevDocs.setDeclarationDate(dateUtils.getDate(String.valueOf(sourceDto.getEh0068a())));
				prevDocs.setSequenceNumber(String.valueOf(sourceDto.getEh0068b()));
				prevDocsList.add(prevDocs);
			}
			
			chl.setPreviousDocuments(prevDocsList);
		}
		
		//AdditionalFiscalReferences
		if(StringUtils.isNotEmpty(sourceDto.getEhrga())) {
			AdditionalFiscalReferences additionalFiscalReferences = new AdditionalFiscalReferences();
			additionalFiscalReferences.setVatIdentificationNumber(sourceDto.getEhrga());
			additionalFiscalReferences.setRole(sourceDto.getEhrgro());
			chl.setAdditionalFiscalReferences(additionalFiscalReferences);
		}
		
		//(Optional) PlaceOfLoading
		if(StringUtils.isNotEmpty(sourceDto.getEhsdft())) {
			PlaceOfLoading ploading = new PlaceOfLoading();
			ploading.setLocation(sourceDto.getEhsdft());
			//tillfälligt borta för TESTER. UNLOCODE är inte ok på grönskärm OBS!
			//if(StringUtils.isNotEmpty(sourceDto.getEhsdf())) { ploading.setUnloCode(sourceDto.getEhsdf()); }
			if(StringUtils.isNotEmpty(sourceDto.getEhlkf())) { 
				AddressCountry ploadAddress = new AddressCountry();
				ploadAddress.setCountry(sourceDto.getEhlkf());
				ploading.setAddress(ploadAddress);
			}
	 		chl.setPlaceOfLoading(ploading);
		}
		
		//(Optional) PlaceOfUnloading
		if(StringUtils.isNotEmpty(sourceDto.getEhsdtt())) {
			PlaceOfUnloading puloading = new PlaceOfUnloading();
			puloading.setLocation(sourceDto.getEhsdtt());
			//tillfällit borta för TESTER. UNLOCODE är inte ok på grönskärm OBS!
			//if(StringUtils.isNotEmpty(sourceDto.getEhsdt())) { puloading.setUnloCode(sourceDto.getEhsdt()); }
			if(StringUtils.isNotEmpty(sourceDto.getEhlkt())) {
				AddressCountry ploadAddress = new AddressCountry();
				ploadAddress.setCountry(sourceDto.getEhlkt());
				puloading.setAddress(ploadAddress);
			}
	 		chl.setPlaceOfUnloading(puloading);
		}
		
		//(Mandatory) Consignee
		Consignee consignee = new Consignee();
		consignee.setName(sourceDto.getEhnam());
		consignee.setIdentificationNumber(sourceDto.getEhrgm());
		if(this.isPrivatePerson(sourceDto)) {
			consignee.setTypeOfPerson(1); //personnumer = 11 siffror
		}else {
			consignee.setTypeOfPerson(2); //orgnr = 9 siffror
		}
		//(Optional) Address
		if(StringUtils.isNotEmpty(sourceDto.getEhpsm())) { 
			consignee.setAddress(this.setAddress(sourceDto.getEhpsm(), sourceDto.getEhlkm(), sourceDto.getEhpnm(), sourceDto.getEhad1m(), sourceDto.getEhnrm()));
		}	
		//(Optional) Communication
		if(StringUtils.isNotEmpty(sourceDto.getEhemm())) { 
			consignee.setCommunication(this.setCommunication(sourceDto.getEhemm(), sourceDto.getEhemmt())); 
		}
		chl.setConsignee(consignee);
		
		
		
		//(Mandatory) Consignor
		Consignor consignor = new Consignor();
		consignor.setName(sourceDto.getEhnas());
		consignor.setIdentificationNumber(sourceDto.getEhrgs());
		if(this.isPrivatePerson(sourceDto)) {
			consignor.setTypeOfPerson(1);
		}else {
			consignor.setTypeOfPerson(2);
		}
		//(Optional)Address ... this.setAddress("Oslo", "NO", "0010", "Hausemanns gate", "52");
		if(StringUtils.isNotEmpty(sourceDto.getEhpss())) {
			consignor.setAddress(this.setAddress(sourceDto.getEhpss(), sourceDto.getEhlks(), sourceDto.getEhpns(), sourceDto.getEhad1s(), sourceDto.getEhnrs()));
		}
		//(Optional) Communication
		if(StringUtils.isNotEmpty(sourceDto.getEhems())) { 
			consignor.setCommunication(this.setCommunication(sourceDto.getEhems(), sourceDto.getEhemst()));
		}
		chl.setConsignor(consignor);
		
		//(Mandatory) TransportDocumentHouseLevel
		TransportDocumentHouseLevel transpDocHouseLevel = new TransportDocumentHouseLevel();
		transpDocHouseLevel.setDocumentNumber(sourceDto.getEhdkh());
		transpDocHouseLevel.setType(sourceDto.getEhdkht());
		//(Mandatory) DocumentNumber
		transpDocHouseLevel.setDocumentNumber(sourceDto.getEhdkh());
		//(Mandatory) Type
		transpDocHouseLevel.setType(sourceDto.getEhdkht());
		chl.setTransportDocumentHouseLevel(transpDocHouseLevel);
		
		
		logger.warn("GOODS-ITEM-LIST size:" + String.valueOf(sourceDto.getGoodsItemList().size()));
		if(sourceDto.getGoodsItemList()!=null && sourceDto.getGoodsItemList().size()>0) {
			List goodsItem = this.getGoodsItemList(sourceDto.getGoodsItemList());
			chl.setGoodsItem(goodsItem);
		}else {
			logger.error("###ERROR-ERROR-ERROR --> GOODS-ITEM-LIST on SADEXIF is 0 ??? - not valid for API...");
		}
		
		//(Mandatory) TransportCharges
		TransportCharges transpCharges = new TransportCharges();
		//Expected codes are one of [A=Kontant, B=Kredikort, C, D=Annet, H=Elektronisk pengeöverf., Y, Z=not pre-paid]"
		//(Optional) MethosOfPayment db-field?
		/*if(sourceDto.get??) {
			transpCharges.setMethodOfPayment(sourceDto.get??);
		}*/
		transpCharges.setCurrency(sourceDto.getEhtcva());
		transpCharges.setValue(sourceDto.getEhtcbl());
		chl.setTransportCharges(transpCharges);
		
		//(Optional) CountriesOfRoutingOfConsignments
		if(StringUtils.isNotEmpty(sourceDto.getEhlkr1())) {
			List<CountriesOfRoutingOfConsignments> tmp = new ArrayList<CountriesOfRoutingOfConsignments>();
			CountriesOfRoutingOfConsignments route = new CountriesOfRoutingOfConsignments();
			route.setSequenceNumber(1);
			route.setCountry(sourceDto.getEhlkr1());
			tmp.add(route);
			//check for route 2 to 8 if any ...
			List<CountriesOfRoutingOfConsignments> allRoutes = this.getExtraRoutes(sourceDto, tmp);
			chl.setCountriesOfRoutingOfConsignments(allRoutes);
		}
		
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
		
		
		//(Mandatory)Total Amount Invoiced
		TotalAmountInvoiced totalAmount = new TotalAmountInvoiced();
		totalAmount.setValue(sourceDto.getEhtcbl());
		totalAmount.setCurrency(sourceDto.getEhtcva());
		chl.setTotalAmountInvoiced(totalAmount);
		
		return chl;
		
	}
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
	/**
	 * 	
	 * @param list
	 * @return
	 */
	private List<GoodsItem> getGoodsItemList(List<SadexifDto> list) {
		List<GoodsItem> returnList = new ArrayList<GoodsItem>();
		
		for (SadexifDto dto: list) {
			GoodsItem item = new GoodsItem();
			//(Optionals)
			if(dto.getEili()>0) { item.setDeclarationGoodsItemNumber(String.valueOf(dto.getEili())); }
			if(dto.getEilit()>0) { item.setTransitGoodsItemNumber(String.valueOf(dto.getEilit())); }
			if(StringUtils.isNotEmpty(dto.getEigty())) { item.setTypeOfGoods(dto.getEigty()); }
			if(StringUtils.isNotEmpty(dto.getEiucr())) { item.setReferenceNumberUCR(dto.getEiucr()); }
			
			//(Mandatories)
			ItemAmountInvoiced itemAmountInvoiced = new ItemAmountInvoiced();
			itemAmountInvoiced.setCurrency(dto.getEival());
			itemAmountInvoiced.setValue(dto.getEibl());
			item.setItemAmountInvoiced(itemAmountInvoiced);
			
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
