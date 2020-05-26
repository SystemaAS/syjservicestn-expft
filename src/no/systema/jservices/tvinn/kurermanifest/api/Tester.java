package no.systema.jservices.tvinn.kurermanifest.api;

import no.systema.jservices.tvinn.kurermanifest.util.Utils;

public class Tester {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Utils client = new Utils();
		System.out.println(client.getUUID("/zzz/test/877ad4bd-5a60-4ffd-801d-8a7d2c4fe78a.txt"));
	}

}
