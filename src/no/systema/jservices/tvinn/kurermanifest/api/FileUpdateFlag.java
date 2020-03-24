package no.systema.jservices.tvinn.kurermanifest.api;



public enum FileUpdateFlag {
	U_("u_"),
	D_("d_")
	;
	
	private final String code;
	FileUpdateFlag(String code) { this.code = code; }
	public String getCode() { return code; }
}
