package no.systema.jservices.tvinn.digitoll.external.house;

import java.util.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Data;


@Data
public class FileAttachmentService {
	private static Logger logger = LoggerFactory.getLogger(FileAttachmentService.class.getName());
	private String attachmentsPath = null;
	public FileAttachmentService(String attachmentsPath) {
		this.attachmentsPath = attachmentsPath;
	}
	
	/**
	 * 
	 * @param msg
	 * @return
	 */
	public List<Path> getFileAttachments(String emdkm, String orgnr) {
		List<Path> list = new ArrayList();
		String FILENAME_SEPARATOR = "_xx";
	    
	    String rootPath	= attachmentsPath;
	    File dir = new File(rootPath);
	    
		try {
        	String searchString = emdkm + FILENAME_SEPARATOR + orgnr + FILENAME_SEPARATOR;
        	logger.info("Search string:" + searchString);
            Files.list((dir.toPath())).filter(p -> p.toString().contains(searchString)).forEach((p) -> {
                try {
                	list.add(p);
                	logger.debug("adding to file list:" + p.getFileName().toString());
                    
                } catch (Exception e) {
                	logger.error(e.toString());
                    e.printStackTrace();
                }
            });
        }catch(Exception e){
        	logger.error(e.toString());
    		e.printStackTrace();
        }
		
		return list;
		
	}
	
	/**
	 * 
	 * @param payloadPath
	 * @return
	 * @throws IOException
	 */
	public String convertImageToBase64(String payloadPath) throws IOException {
        File imageFile = new File(payloadPath);
        if (imageFile.exists()) {
            FileInputStream fileInputStream = new FileInputStream(imageFile);
            byte[] imageData = new byte[(int) imageFile.length()];
            fileInputStream.read(imageData);
            fileInputStream.close();
            return Base64.getEncoder().encodeToString(imageData);
        } else {
            throw new IOException("Image file not found: " + payloadPath);
        }
    }
	/**
	 * 
	 * @param emdkm
	 * @param orgnr
	 * @return
	 */
	public List<Path> deleteFileAttachments(String emdkm, String orgnr) {
		List<Path> list = new ArrayList();
		String FILENAME_SEPARATOR = "_xx";
	    
	    String rootPath	= attachmentsPath;
	    File dir = new File(rootPath);
	    
	    try {
        	String searchString = emdkm + FILENAME_SEPARATOR + orgnr + FILENAME_SEPARATOR;
        	logger.info("Search string:" + searchString);
            Files.list((dir.toPath())).filter(p -> p.toString().contains(searchString)).forEach((p) -> {
                try {
                	logger.debug("Deleting..." + p.getFileName().toString());
                    Files.deleteIfExists(p);
                } catch (Exception e) {
                	logger.error(e.toString());
                    e.printStackTrace();
                }
            });
        }catch(Exception e){
        	logger.error(e.toString());
    		e.printStackTrace();
        }
		
		return list;
		
	}
	
	
}
