package no.systema.jservices.tvinn.kurermanifest.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
	
	/**
	 * 
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public InputStream getFilePayloadStream(String fileName) throws Exception {
		
		File initialFile = new File(fileName);
	    return new FileInputStream(initialFile);

	}
	
}
