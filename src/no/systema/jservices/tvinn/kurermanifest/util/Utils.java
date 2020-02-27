package no.systema.jservices.tvinn.kurermanifest.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Utils {

	/**
	 * 
	 * @param content
	 * @return
	 * @throws IOException
	 */
	public byte[] getFilePayload(String fileName) throws Exception {
		
		Path path = Paths.get(fileName);
		return Files.readAllBytes(path);
		
		
	}
}
