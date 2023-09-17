package no.systema.jservices.tvinn.digitoll.v2.enums;



/**
 * 
 * @author oscardelatorre
 * sep 2023
 * 
 */
public enum EnumMoAir {
	_40("40"),
	_41("41"),
	_42("42"),
	_43("43");
	
	
	EnumMoAir(String value) {
        this.value = value;
    }

    private String value;
    public String toString() {
        return value;
    }
}
