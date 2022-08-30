package no.systema.jservices.tvinn.expressfortolling2.util;

public class TesterDate {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String dateZulu = "2022-08-25T19:43:36Z";
		String tmp = dateZulu.replaceAll("-", "").substring(0,8);
		System.out.println(tmp);
		
		DateUtils dateUtil = new DateUtils();
		String zulu = dateUtil.getZuluTimeWithoutMilliseconds(20220825, 100003);
		System.out.println(zulu);
		
		DateUtils dUtil = new DateUtils("yyyyMMdd", "yyyy-MM-dd");
		System.out.println(dUtil.getDate("20220820"));
		
	}

}
