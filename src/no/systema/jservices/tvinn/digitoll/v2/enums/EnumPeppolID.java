package no.systema.jservices.tvinn.digitoll.v2.enums;



/**
 * 
 * @author oscardelatorre
 * Feb 2024
 * 
 */
public enum EnumPeppolID {
	Bulgaria_Vatnr("9926"),
	Denmark_Orgnr("0198"),
	Estonia_Vatnr("9931"),
	Germany_Vatnr("9930"),
	GLN("0088"),
	Latvia_Vatnr("9939"),
	Lithuania_Vatnr("9937"),
	Norway_Orgnr("0192"),
	Poland_Vatnr("9945"),
	
	Sweden_Orgnr("0007");
	
	
	EnumPeppolID(String value) {
        this.value = value;
    }

    private String value;
    public String toString() {
        return value;
    }
}
