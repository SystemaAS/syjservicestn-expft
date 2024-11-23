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
import no.systema.jservices.tvinn.digitoll.v2.dto.SadmohfDto;
import no.systema.jservices.tvinn.digitoll.v2.dto.SadmoifDto;
import no.systema.jservices.tvinn.expressfortolling2.util.BigDecimalFormatter;
import no.systema.jservices.tvinn.expressfortolling2.util.DateUtils;


/**
 * SPEC. SWAGGER
 * https://api-test.toll.no/api/movement/road/v2/swagger-ui/index.html
 * 
 * @author oscardelatorre
 * Aug 2023
 * 
 */
public class MapperHouseConsignment {
	private static final Logger logger = LoggerFactory.getLogger(MapperHouseConsignment.class);
	
	public HouseConsignment mapHouseConsignment(SadmohfDto dto) {
		
		HouseConsignment hc = new HouseConsignment();
		//(Mandatory) IssueDate
		//hc.setDocumentIssueDate("2022-08-16T11:49:52Z");
		hc.setDocumentIssueDate(new DateUtils().getZuluTimeWithoutMillisecondsUTC());
		
		
		//(Optional) Representative (do not exist at db level, fetch from Transport if applicable = TODO
		/*
		Representative rep = new Representative();
		rep.setName("sourceDto.getEhnar()");
		rep.setIdentificationNumber("sourceDto.getEhrgr()");
		rep.setAddress(this.setAddress("sourceDto.getEhpsr()", "sourceDto.getEhlkr()", "sourceDto.getEhpnr()", "sourceDto.getEhad1r()", "sourceDto.getEhnrr()"));
		//
		List rcommList = new ArrayList();
		rcommList.add(this.populateCommunication("sourceDto.getEhemr()", "sourceDto.getEhemrt()"));
		rep.setCommunication(rcommList);
		hc.setRepresentative(rep);
		*/
		
		//(Mandatory) consignmentHouseLevel
		hc.setHouseConsignmentConsignmentHouseLevel(this.populateHouseConsignmentConsignmentHouseLevel(dto));
		
		
		return hc;
	}
	
	/**
	 * Only issueDate for delete
	 * @param sourceDto
	 * @return
	 * 
	 */
	public HouseConsignment mapHouseConsignmentForDelete(SadmohfDto dto) {
		
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
	
	private HouseConsignmentConsignmentHouseLevel populateHouseConsignmentConsignmentHouseLevel(SadmohfDto dto) {
		DateUtils dateUtils = new DateUtils("yyyyMMdd", "yyyy-MM-dd");
		//(Mandatory) HouseConsignment-ConsignmentHouseLevel
		HouseConsignmentConsignmentHouseLevel chl = new HouseConsignmentConsignmentHouseLevel();
		
		//(Optional) Bara för postsäckar. Då får man INTE sända consignmentMasterLevel...
		if(StringUtils.isNotEmpty(dto.getEhrecid())) {
			chl.setReceptacleIdentificationNumber(dto.getEhrecid());
		}
		//(Mandatory) ContainerIndicator
		chl.setContainerIndicator(dto.getEhcnin());
		//(Mandatory) TotalGrossMass
		chl.setTotalGrossMass(dto.getEhvkb());
		//(Mandatory) numberOfPackages
		chl.setNumberOfPackages(dto.getEhntk());
		//(Mandatory) goodsDescription ()
		chl.setGoodsDescription(dto.getEhvt());
		
		//(Mandatory) TransportDocumentHouseLevel
		TransportDocumentHouseLevel transpDocHouseLevel = new TransportDocumentHouseLevel();
		transpDocHouseLevel.setDocumentNumber(dto.getEhdkh());
		transpDocHouseLevel.setType(dto.getEhdkht());
		chl.setTransportDocumentHouseLevel(transpDocHouseLevel);
		
		//(Optional but required anyway -> consignmentMasterLevel but must be in place before the carrier arrives to the border! ergo = required as long as we do not have a ReceptacleIdNumber above
		if(StringUtils.isEmpty(dto.getEhrecid())) {
			//check if this house is an external house meaning that the house MUST be sent with an external MasterId/type/carrierOrgNr. Coming from the party owning the Tranport.
			boolean isExternalHouse = this.isValidExternalHouse(dto);
			HouseConsignmentMasterLevel consignmentMasterLevel = new HouseConsignmentMasterLevel();
			TransportDocumentMasterLevel transpDocMasterLevel = new TransportDocumentMasterLevel();
			if(isExternalHouse) {
				consignmentMasterLevel.setCarrierIdentificationNumber(dto.getMasterDto().getEmrgt_ff());
				transpDocMasterLevel.setDocumentNumber(dto.getMasterDto().getEmdkm_ff());
				transpDocMasterLevel.setType(dto.getMasterDto().getEmdkmt_ff());
				
			}else {
				consignmentMasterLevel.setCarrierIdentificationNumber(dto.getMasterDto().getEmrgt());
				transpDocMasterLevel.setDocumentNumber(dto.getMasterDto().getEmdkm());
				transpDocMasterLevel.setType(dto.getMasterDto().getEmdkmt());
				
			}
			consignmentMasterLevel.setTransportDocumentMasterLevel(transpDocMasterLevel);
			chl.setConsignmentMasterLevel(consignmentMasterLevel);
		}
		
		//(Optional) Previous Documents
		if(StringUtils.isNotEmpty(dto.getEhprt()) && !dto.getEhprt().contains("VOEC")) { //VOEC should not have any of this below!!
			List prevDocsList = new ArrayList();
			if(StringUtils.isNotEmpty(dto.getEhtrnr()) || (StringUtils.isNotEmpty(dto.getEhrg()) && dto.getEh0068b()>0 )) {
				//(1)
				if(StringUtils.isNotEmpty(dto.getEhtrnr())){
					PreviousDocuments prevDocs = new PreviousDocuments();
					//test for example 2: https://toll.github.io/api/mo-eksempler.html (special thing)
					if(StringUtils.isNotEmpty(dto.getEhtrnr()) && (StringUtils.isNotEmpty(dto.getEhrg()) && dto.getEh0068b()>0 )) {
						if("CUDE".equals(dto.getEhtrty())) {
							//this case will surely never happen in real life but we must comply for the TestCase...
							prevDocs.setTypeOfReference("N820");
						}else if("GONU".equals(dto.getEhtrty())) {
							//Example 9 - Godsnr
							prevDocs.setTypeOfReference(dto.getEhtrty());
						}
					}else {
						prevDocs.setTypeOfReference(dto.getEhtrty()); //CUDE=Tolldeklarasjon, RETR=Oppstart transittering, GONU=Godsnummer
					}
					prevDocs.setReferenceNumber(dto.getEhtrnr());
					prevDocsList.add(prevDocs);
	
				}
				//(2)
				if(StringUtils.isNotEmpty(dto.getEhrg()) && dto.getEh0068b()>0) {
					//Valid only for none-GONU
					if(!"GONU".equals(dto.getEhtrty())) {
						PreviousDocuments prevDocs = new PreviousDocuments();
						prevDocs.setTypeOfReference(dto.getEhtrty()); //CUDE=Tolldeklarasjon, RETR=Oppstart transittering
						prevDocs.setDeclarantNumber(dto.getEhrg());
						prevDocs.setDeclarationDate(dateUtils.getDate(String.valueOf(dto.getEh0068a())));
						prevDocs.setSequenceNumber(String.valueOf(dto.getEh0068b()));
						prevDocsList.add(prevDocs);
					}
				}
				//populate extras if any
				this.populateTransit2_10(dto, prevDocsList);
				chl.setPreviousDocuments(prevDocsList);
			}
		}
		//(Optional) ExportFromEU
		List exportFromEUList = new ArrayList();
		if(StringUtils.isNotEmpty(dto.getEheid()) || StringUtils.isNotEmpty(dto.getEhetypt() )) {
			if(StringUtils.isNotEmpty(dto.getEheid())) {
				ExportFromEU exportFromEU = new ExportFromEU();
				exportFromEU.setExportId(dto.getEheid());
				exportFromEU.setTypeOfExport(dto.getEhetypt());
				exportFromEUList.add(exportFromEU);
				
			}
			if(StringUtils.isNotEmpty(dto.getEheid2())) {
				ExportFromEU exportFromEU = new ExportFromEU();
				exportFromEU.setExportId(dto.getEheid2());
				exportFromEU.setTypeOfExport(dto.getEhetypt2());
				exportFromEUList.add(exportFromEU);
			}
			//populate extras if any
			this.populateExportFromEU3_10(dto, exportFromEUList);
			chl.setExportFromEU(exportFromEUList);
		}
		
		//(Mandatory) ImportProcedure
		ImportProcedure importProcedure = new ImportProcedure();
		importProcedure.setImportProcedure(dto.getEhprt());
		//(Optional)TRA/EXP/TRE/FALSE
		if(StringUtils.isNotEmpty(dto.getEhupr())) {
			if(dto.getEhupr().equalsIgnoreCase("FALSE")){
				importProcedure.setHasOutgoingProcedure(dto.getEhupr());
			}else {
				//TRA/EXP/TRE
				importProcedure.setOutgoingProcedure(dto.getEhupr());	
			}	
		}
		chl.setImportProcedure(importProcedure);
		
		
		//(Mandatory) Consignor
		Consignor consignor = new Consignor();
		consignor.setName(dto.getEhnas());
		consignor.setTypeOfPerson(dto.getEhtpps());
		
		//Optional Orgnr
		if(StringUtils.isNotEmpty(dto.getEhrgs())) {
			consignor.setIdentificationNumber(dto.getEhrgs());
		}
		//(Optional)Address ... this.setAddress("Oslo", "NO", "0010", "Hausemanns gate", "52");
		if(StringUtils.isNotEmpty(dto.getEhpss())) {
			consignor.setAddress(this.setAddress(dto.getEhpss(), dto.getEhlks(), dto.getEhpns(), dto.getEhad1s(), dto.getEhpbs(), dto.getEhnrs()));
		}
		//(Optional) Communication
		if(StringUtils.isNotEmpty(dto.getEhems())) { 
			consignor.setCommunication(this.setCommunication(dto.getEhems(), dto.getEhemst()));
		}
		chl.setConsignor(consignor);
				
		
		//(Mandatory) Consignee
		Consignee consignee = new Consignee();
		consignee.setName(dto.getEhnam());
		consignee.setTypeOfPerson(dto.getEhtppm()); //orgnr = 9 siffror, personnumer = 11 siffror ??
		
		//Optional Orgnr
		if(StringUtils.isNotEmpty(dto.getEhrgm())) {
			consignee.setIdentificationNumber(dto.getEhrgm());
		}
		//(Optional) Address
		if(StringUtils.isNotEmpty(dto.getEhpsm())) { 
			consignee.setAddress(this.setAddress(dto.getEhpsm(), dto.getEhlkm(), dto.getEhpnm(), dto.getEhad1m(), dto.getEhpbm(), dto.getEhnrm()));
		}	
		//(Optional) Communication
		if(StringUtils.isNotEmpty(dto.getEhemm())) { 
			consignee.setCommunication(this.setCommunication(dto.getEhemm(), dto.getEhemmt())); 
		}
		chl.setConsignee(consignee);
		
		//(Optional) PlaceOfAcceptance
		if(StringUtils.isNotEmpty(dto.getEhlka())) {
			PlaceOfAcceptance placc = new PlaceOfAcceptance();
			if(StringUtils.isNotEmpty(dto.getEhsdat())) { placc.setLocation(dto.getEhsdat()); }
			if(StringUtils.isNotEmpty(dto.getEhsda())) { placc.setUnloCode(dto.getEhsda()); }
			if(StringUtils.isNotEmpty(dto.getEhlka())) {
				AddressCountry addressCountry = new AddressCountry();
				addressCountry.setCountry(dto.getEhlka());
				placc.setAddress(addressCountry);
			}
			chl.setPlaceOfAcceptance(placc);
		}
		
		//(Optional) PlaceOfDelivery
		if(StringUtils.isNotEmpty(dto.getEhlkd())) {
			PlaceOfDelivery pldel = new PlaceOfDelivery();
			if(StringUtils.isNotEmpty(dto.getEhsddt())) { pldel.setLocation(dto.getEhsddt()); }
			if(StringUtils.isNotEmpty(dto.getEhsdd())) { pldel.setUnloCode(dto.getEhsdd()); }
			if(StringUtils.isNotEmpty(dto.getEhlkd())) {
				AddressCountry addressCountry = new AddressCountry();
				addressCountry.setCountry(dto.getEhlkd());
				pldel.setAddress(addressCountry);
			}
			chl.setPlaceOfDelivery(pldel);
		}
		
			
			
		//TODO (Optional) goodsItem
		// Only for VOEC
		
		if(dto.getGoodsItemList()!=null && !dto.getGoodsItemList().isEmpty()) {
			List<GoodsItem> goodsItemList = new ArrayList<GoodsItem>();
			for(SadmoifDto goodsItemDto : dto.getGoodsItemList()) {
				logger.warn("GOODS-ITEM-LIST size:" + String.valueOf(dto.getGoodsItemList().size()));
				GoodsItem goodsItem = new GoodsItem();
				//ItemAmount
				ItemAmountInvoicedVOEC iaiVOEC = new ItemAmountInvoicedVOEC();
				if(goodsItemDto.getEistk()!=null && goodsItemDto.getEistk().contains(".") ) {
					Integer x = goodsItemDto.getEistk().indexOf(".");
					String tmp = goodsItemDto.getEistk().substring(0, x);
					iaiVOEC.setNumberOfItems(Integer.valueOf(tmp));
				}else {
					iaiVOEC.setNumberOfItems(Integer.valueOf(goodsItemDto.getEistk()));
				}
				iaiVOEC.setValue(Double.valueOf(goodsItemDto.getEibl()));
				goodsItem.setItemAmountInvoicedVOEC(iaiVOEC);
				//Commodity
				CommodityCodeVOEC ccVOEC = new CommodityCodeVOEC();
				ccVOEC.setHarmonizedSystemSubheadingCode(String.valueOf(goodsItemDto.getEivnt()));
				goodsItem.setCommodityCodeVOEC(ccVOEC);
				//FiscalRefs
				AdditionalFiscalReferences afr = new AdditionalFiscalReferences();
				afr.setVatIdentificationNumber(goodsItemDto.getEirge());
				afr.setRole(goodsItemDto.getEiroe());
				goodsItem.setAdditionalFiscalReferences(afr);
				//put the mapped goodsItem in the list
				goodsItemList.add(goodsItem);
			}
			//now put the goodsItemList in the parent element
			chl.setGoodsItem(goodsItemList);
		}
		
		//(Optional)Transport Equipment
		/*List<TransportEquipment> list = this.populateTransportEquipment(dto);
		if(!list.isEmpty()) {
			chl.setTranportEquipment(list);
		}*/
		
		
		return chl;
		
	}
	
	private boolean isValidExternalHouse(SadmohfDto dto) {
		boolean retval = false;
		if(dto.getMasterDto()!=null) {
			if (StringUtils.isNotEmpty(dto.getMasterDto().getEmdkm_ff()) && StringUtils.isNotEmpty(dto.getMasterDto().getEmdkmt_ff()) && StringUtils.isNotEmpty(dto.getMasterDto().getEmrgt_ff())  ) {
				if (!"null".equals(dto.getMasterDto().getEmdkm_ff()) && !"null".equals(dto.getMasterDto().getEmdkmt_ff()) && !"null".equals(dto.getMasterDto().getEmrgt_ff())  ) {
					retval = true;
				}
			}
		}
		
		
		return retval;
	}
	//Populate list if needed
	private void populateExportFromEU3_10(SadmohfDto dto, List exportFromEUList) {
		
		if(StringUtils.isNotEmpty(dto.getEheid3())) {
			ExportFromEU exportFromEU = new ExportFromEU();
			exportFromEU.setExportId(dto.getEheid3());
			exportFromEU.setTypeOfExport(dto.getEhetypt3());
			exportFromEUList.add(exportFromEU);
		}
		if(StringUtils.isNotEmpty(dto.getEheid4())) {
			ExportFromEU exportFromEU = new ExportFromEU();
			exportFromEU.setExportId(dto.getEheid4());
			exportFromEU.setTypeOfExport(dto.getEhetypt4());
			exportFromEUList.add(exportFromEU);
		}
		if(StringUtils.isNotEmpty(dto.getEheid5())) {
			ExportFromEU exportFromEU = new ExportFromEU();
			exportFromEU.setExportId(dto.getEheid5());
			exportFromEU.setTypeOfExport(dto.getEhetypt5());
			exportFromEUList.add(exportFromEU);
		}
		if(StringUtils.isNotEmpty(dto.getEheid6())) {
			ExportFromEU exportFromEU = new ExportFromEU();
			exportFromEU.setExportId(dto.getEheid6());
			exportFromEU.setTypeOfExport(dto.getEhetypt6());
			exportFromEUList.add(exportFromEU);
		}
		if(StringUtils.isNotEmpty(dto.getEheid7())) {
			ExportFromEU exportFromEU = new ExportFromEU();
			exportFromEU.setExportId(dto.getEheid7());
			exportFromEU.setTypeOfExport(dto.getEhetypt7());
			exportFromEUList.add(exportFromEU);
		}
		if(StringUtils.isNotEmpty(dto.getEheid8())) {
			ExportFromEU exportFromEU = new ExportFromEU();
			exportFromEU.setExportId(dto.getEheid8());
			exportFromEU.setTypeOfExport(dto.getEhetypt8());
			exportFromEUList.add(exportFromEU);
		}
		if(StringUtils.isNotEmpty(dto.getEheid9())) {
			ExportFromEU exportFromEU = new ExportFromEU();
			exportFromEU.setExportId(dto.getEheid9());
			exportFromEU.setTypeOfExport(dto.getEhetypt9());
			exportFromEUList.add(exportFromEU);
		}
		if(StringUtils.isNotEmpty(dto.getEheid10())) {
			ExportFromEU exportFromEU = new ExportFromEU();
			exportFromEU.setExportId(dto.getEheid10());
			exportFromEU.setTypeOfExport(dto.getEhetypt10());
			exportFromEUList.add(exportFromEU);
		}
	}
	
	/**
	 * Populates extra transits as needed ...
	 * Each transit belongs to the one-and-only parent Declaration
	 * @param dto
	 * @param prevDocsList
	 */
	private void populateTransit2_10(SadmohfDto dto, List prevDocsList ) {
		
		if(StringUtils.isNotEmpty(dto.getEhtrnr2())){
			PreviousDocuments prevDocs = new PreviousDocuments();
			prevDocs.setTypeOfReference(dto.getEhtrty2());
			prevDocs.setReferenceNumber(dto.getEhtrnr2());
			prevDocsList.add(prevDocs);
			//Parent Tolldekl (often CUDE). The CUDE is valid for all transit above (
			/* ? if(StringUtils.isNotEmpty(dto.getEhrg()) && dto.getEh0068b()>0) {
				prevDocsList.add(this.getTollDeklParent(dto));
			}*/

		}
		if(StringUtils.isNotEmpty(dto.getEhtrnr3())){
			PreviousDocuments prevDocs = new PreviousDocuments();
			prevDocs.setTypeOfReference(dto.getEhtrty3());
			prevDocs.setReferenceNumber(dto.getEhtrnr3());
			prevDocsList.add(prevDocs);
			//Parent Tolldekl (often CUDE). The CUDE is valid for all transit above (
			/* if(StringUtils.isNotEmpty(dto.getEhrg()) && dto.getEh0068b()>0) {
				prevDocsList.add(this.getTollDeklParent(dto));
			}*/
		}
		if(StringUtils.isNotEmpty(dto.getEhtrnr4())){
			PreviousDocuments prevDocs = new PreviousDocuments();
			prevDocs.setTypeOfReference(dto.getEhtrty4());
			prevDocs.setReferenceNumber(dto.getEhtrnr4());
			prevDocsList.add(prevDocs);
			//Parent Tolldekl (often CUDE). The CUDE is valid for all transit above (
			/*if(StringUtils.isNotEmpty(dto.getEhrg()) && dto.getEh0068b()>0) {
				prevDocsList.add(this.getTollDeklParent(dto));
			}*/
		}
		if(StringUtils.isNotEmpty(dto.getEhtrnr5())){
			PreviousDocuments prevDocs = new PreviousDocuments();
			prevDocs.setTypeOfReference(dto.getEhtrty5());
			prevDocs.setReferenceNumber(dto.getEhtrnr5());
			prevDocsList.add(prevDocs);
			//Parent Tolldekl (often CUDE). The CUDE is valid for all transit above (
			/*if(StringUtils.isNotEmpty(dto.getEhrg()) && dto.getEh0068b()>0) {
				prevDocsList.add(this.getTollDeklParent(dto));
			}*/
		}
		if(StringUtils.isNotEmpty(dto.getEhtrnr6())){
			PreviousDocuments prevDocs = new PreviousDocuments();
			prevDocs.setTypeOfReference(dto.getEhtrty6());
			prevDocs.setReferenceNumber(dto.getEhtrnr6());
			prevDocsList.add(prevDocs);
			//Parent Tolldekl (often CUDE). The CUDE is valid for all transit above (
			/*if(StringUtils.isNotEmpty(dto.getEhrg()) && dto.getEh0068b()>0) {
				prevDocsList.add(this.getTollDeklParent(dto));
			}*/
		}
		if(StringUtils.isNotEmpty(dto.getEhtrnr7())){
			PreviousDocuments prevDocs = new PreviousDocuments();
			prevDocs.setTypeOfReference(dto.getEhtrty7());
			prevDocs.setReferenceNumber(dto.getEhtrnr7());
			prevDocsList.add(prevDocs);
			//Parent Tolldekl (often CUDE). The CUDE is valid for all transit above (
			/*if(StringUtils.isNotEmpty(dto.getEhrg()) && dto.getEh0068b()>0) {
				prevDocsList.add(this.getTollDeklParent(dto));
			}*/
		}
		if(StringUtils.isNotEmpty(dto.getEhtrnr8())){
			PreviousDocuments prevDocs = new PreviousDocuments();
			prevDocs.setTypeOfReference(dto.getEhtrty8());
			prevDocs.setReferenceNumber(dto.getEhtrnr8());
			prevDocsList.add(prevDocs);
			//Parent Tolldekl (often CUDE). The CUDE is valid for all transit above (
			/*if(StringUtils.isNotEmpty(dto.getEhrg()) && dto.getEh0068b()>0) {
				prevDocsList.add(this.getTollDeklParent(dto));
			}*/
		}
		if(StringUtils.isNotEmpty(dto.getEhtrnr9())){
			PreviousDocuments prevDocs = new PreviousDocuments();
			prevDocs.setTypeOfReference(dto.getEhtrty9());
			prevDocs.setReferenceNumber(dto.getEhtrnr9());
			prevDocsList.add(prevDocs);
			//Parent Tolldekl (often CUDE). The CUDE is valid for all transit above (
			/*if(StringUtils.isNotEmpty(dto.getEhrg()) && dto.getEh0068b()>0) {
				prevDocsList.add(this.getTollDeklParent(dto));
			}*/
		}
		if(StringUtils.isNotEmpty(dto.getEhtrnr10())){
			PreviousDocuments prevDocs = new PreviousDocuments();
			prevDocs.setTypeOfReference(dto.getEhtrty10());
			prevDocs.setReferenceNumber(dto.getEhtrnr10());
			prevDocsList.add(prevDocs);
			//Parent Tolldekl (often CUDE). The CUDE is valid for all transit above (
			/*if(StringUtils.isNotEmpty(dto.getEhrg()) && dto.getEh0068b()>0) {
				prevDocsList.add(this.getTollDeklParent(dto));
			}*/
		}
		
	}
	/**
	 * 
	 * @param dto
	 * @return
	 */
	private PreviousDocuments getTollDeklParent(SadmohfDto dto) {
		DateUtils dateUtils = new DateUtils("yyyyMMdd", "yyyy-MM-dd");
		
		PreviousDocuments prevDocsTollDekl = new PreviousDocuments();
		prevDocsTollDekl.setTypeOfReference(dto.getEhtrty()); //CUDE=Tolldeklarasjon, RETR=Oppstart transittering
		prevDocsTollDekl.setDeclarantNumber(dto.getEhrg());
		prevDocsTollDekl.setDeclarationDate(dateUtils.getDate(String.valueOf(dto.getEh0068a())));
		prevDocsTollDekl.setSequenceNumber(String.valueOf(dto.getEh0068b()));
		
		return prevDocsTollDekl;
	}
	
	
	/*
	private List<TransportEquipment> populateTransportEquipment(SadmohfDto dto) {
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
		}

			
		return listTranspEquip;
		
	}
	*/
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
	
	private Address setAddress(String city, String country, String postCode, String street, String poBox, String number) {
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
		//this is in order to catch anomalies in the SADH and or Kundreg regarding address1 and address2 (poBox is a place holder)
		if(StringUtils.isNotEmpty(poBox)) {
			address.setStreetAdditionalLine(poBox);
			address.setPoBox(poBox);
		}
		if(StringUtils.isNotEmpty(number)) {
			address.setNumber(number);
		}
		
		return address;
	}
	
}
