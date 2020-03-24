package no.systema.jservices.tvinn.kurermanifest.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import no.systema.jservices.tvinn.kurermanifest.api.FileUpdateFlag;

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
	
	/**
	 * extracts the file id based on the file name
	 * @param fileName
	 * @return
	 */
	public String getUUID(String fileNameAbsolutPath){
		File file = new File(fileNameAbsolutPath);
		String fileName = file.getName();
		String id = "_";
		//extract the file id
		int x = -1;
		if(fileName.toLowerCase().startsWith(FileUpdateFlag.U_.getCode()) || fileName.toLowerCase().startsWith(FileUpdateFlag.D_.getCode()) ){
			//update file
			fileName.toLowerCase().indexOf(FileUpdateFlag.U_.getCode());
			String temp = fileName.substring(x+3);
			id = temp.substring(0,temp.indexOf("."));
			
		} else if(fileName.toLowerCase().startsWith(FileUpdateFlag.D_.getCode()) ){
			fileName.toLowerCase().indexOf(FileUpdateFlag.D_.getCode());
			String temp = fileName.substring(x+3);
			id = temp.substring(0,temp.indexOf("."));
			
		} else{
			//new file
			id = fileName.substring(0,fileName.indexOf("."));
		}
		return id;
	}
	
	
}
