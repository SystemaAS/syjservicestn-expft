package no.systema.jservices.tvinn.expressfortolling2.util;

public class TesterDate {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String dateZulu = "2022-08-25T19:43:36Z";
		String tmp = dateZulu.replaceAll("-", "").substring(0,8);
		System.out.println(tmp);
		
	}

}
