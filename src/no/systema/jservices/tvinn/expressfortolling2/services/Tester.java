package no.systema.jservices.tvinn.expressfortolling2.services;

public class Tester {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/*String value = "19820503";
		
		String year = value.substring(0,4);
		String month = value.substring(4,6);
		String day = value.substring(6,8);
		System.out.println(year + "-" + month + "-" + day);
		*/
		
		String tariff = "105000";
		if(tariff.length()<8) {
			tariff = new no.systema.jservices.common.util.StringUtils().leadingStringWithNumericFiller(tariff, 8, "0");
			System.out.println(tariff);
		}
		System.out.println(tariff.substring(0,6));
		System.out.println(tariff.substring(6));
		
		

	}

}
