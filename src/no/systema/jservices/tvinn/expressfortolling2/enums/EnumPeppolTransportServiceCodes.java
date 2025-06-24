package no.systema.jservices.tvinn.expressfortolling2.enums;



/**
 *
 *
 * 
 * @author oscardelatorre
 *
 */
public enum EnumPeppolTransportServiceCodes {
	_Other("1"),
	_ThermoService("2"),
	_DangerousGoodsService("3"),
	_Transport("4"),
	_HandlingService("5"),
	_Consolidation("6"),
	_Splitting("7"),
	_Combined("8"),
	_Single("9"),
	_Loading("10"),
	_Unloading("11"),
	_Insurance("12"),
	_DocumentHandling("13"),
	_AgentService("14"),
	_InspectionService("15"),
	_MaintenanceService("16"),
	_DeviationNotification("17"),
	_DGdeclaration("18"),
	_CustomsDeclaration("19"),
	_ChangeOfStatusNotification("20"),
	_Warehousing("21"),
	_LCL_LCL("22"),
	_LCL_FCL("23"),
	_FCL_FCL("24"),
	_FCL_LCL("25");
	
	EnumPeppolTransportServiceCodes(String value) {
        this.value = value;
    }

    private String value;
    public String toString() {
        return value;
    }
	
}
