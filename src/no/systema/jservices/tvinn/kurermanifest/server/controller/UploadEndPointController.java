package no.systema.jservices.tvinn.kurermanifest.server.controller;

import org.apache.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;


@RestController
public class UploadEndPointController {
	
    private static final Logger logger = Logger.getLogger(UploadEndPointController.class);

    /**
     * Simulation for file payload as multipartFile ...
     * Test end-point
     * @param inputFile
     * @return
     */
    
    @RequestMapping(value = "upload", method = RequestMethod.POST)
    public ResponseEntity<String> upload(@RequestParam("user-file") MultipartFile multipartFile) throws IOException {
    	try{
	    	String name = multipartFile.getOriginalFilename();
	        logger.info("File name: " + name);
	        //Ideally you shall read bytes using multipartFile.getInputStream() and store it appropriately
	        byte[] bytes = multipartFile.getBytes();
	        logger.info("File uploaded content:" + new String(bytes));
	        
		    HttpHeaders headers = new HttpHeaders();
		    headers.add("File Uploaded Successfully - ", name);
			return new ResponseEntity<String>(headers, HttpStatus.OK);		    	  
		 
	      } catch (Exception e) {    
	    	return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
	      }
	     
    }
    /**
     * Simulation for Sting payload end-point
     * @param payload
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "upload_v2", method = RequestMethod.POST)
    public ResponseEntity<String> uploadx(@RequestBody String payload) throws IOException {
    	try{
	    	logger.info("Json payload: " + payload);
	        
	    	HttpHeaders headers = new HttpHeaders();
		    headers.add("File Uploaded Successfully - ", "OK");
			return new ResponseEntity<String>(headers, HttpStatus.OK);		    	  
		 
	      } catch (Exception e) {    
	    	return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
	      }
	     
    }
}