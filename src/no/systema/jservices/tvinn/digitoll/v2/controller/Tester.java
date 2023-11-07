package no.systema.jservices.tvinn.digitoll.v2.controller;

import org.apache.commons.lang3.StringUtils;

public class Tester {

	public static void main(String[] args) {
		String mrn = "";
		int counter = 1;
		
			while (StringUtils.isEmpty(mrn)) {
				if(counter>6) {
					break;
				}
				try {
					if(counter==3) {
						
						throw new Exception("Exception message"); 
						//mrn="";
					}
					System.out.println("EMPTY");
					Thread.sleep(10000);
				}catch(Exception e) {
					System.out.println(e.toString());
					
				}finally {
					counter++;
				}
				
			}
		
		System.out.println("OUT");
	}

}
