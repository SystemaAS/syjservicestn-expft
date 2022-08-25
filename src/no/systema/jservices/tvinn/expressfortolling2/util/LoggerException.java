package no.systema.jservices.tvinn.expressfortolling2.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

final public class LoggerException {

	static public Writer doLog(Exception e) {
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		e.printStackTrace(printWriter);
		return writer;
		
	}
}
