package no.systema.jservices.tvinn.digitoll.v2.enums;



/**
 * 
 * @author oscardelatorre
 * Feb 2024
 * 
 */
public enum EnumPeppolID {
	Norway_Orgnr("0192"),
	Sweden_Orgnr("0007"),
	Denmark_Orgnr("0198"),
	Latvia_Vatnr("9939"),
	Poland_Vatnr("9945"),
	Estonia_Vatnr("9931"),
	GLN("0088");

	EnumPeppolID(String value) {
        this.value = value;
    }

    private String value;
    public String toString() {
        return value;
    }
}
