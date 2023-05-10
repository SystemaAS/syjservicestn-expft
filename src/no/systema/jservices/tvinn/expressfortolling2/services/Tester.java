package no.systema.jservices.tvinn.expressfortolling2.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import no.systema.jservices.tvinn.expressfortolling2.util.BigDecimalFormatter;
import no.systema.jservices.tvinn.expressfortolling2.util.DateUtils;

public class Tester {

	public static void main(String[] args) {
		
		Map<String,Double> map = new HashMap<String,Double>();
		Tester tester = new Tester();
		StringBuilder currencyCode = new StringBuilder();
		tester.doIt(map, currencyCode);
		System.out.println(map.get("totalAmount") + " " + currencyCode.toString());
		

	}
	
	private void doIt(Map<String,Double> map, StringBuilder currencyCode) {
		Double totalAmount = 0.00D;
		Double value = 1.73;
		for (int x = 1;x<6; x++) {
			if(x==1) {
				currencyCode.append("NOK");
			}
			totalAmount += value;
		}
		BigDecimal bd = new BigDecimal(totalAmount).setScale(2, RoundingMode.HALF_UP);
		map.put("totalAmount", bd.doubleValue());
	}

}
