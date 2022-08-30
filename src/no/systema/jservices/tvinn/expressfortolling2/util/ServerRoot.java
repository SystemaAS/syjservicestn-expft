package no.systema.jservices.tvinn.expressfortolling2.util;

import javax.servlet.http.HttpServletRequest;

public abstract class ServerRoot {
	
	public static String getServerRoot(HttpServletRequest request) {
		return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
	}

}
