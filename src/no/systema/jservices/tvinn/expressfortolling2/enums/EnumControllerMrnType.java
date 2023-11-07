package no.systema.jservices.tvinn.expressfortolling2.enums;



/**
 *
 *
 * 
 * @author oscardelatorre
 *
 */
public enum EnumControllerMrnType {
	TRANSPORT("transport"),
	MASTER("master"),
	HOUSE("house");
	
	
	EnumControllerMrnType(String value) {
        this.value = value;
    }

    private String value;
    public String toString() {
        return value;
    }
	
}
