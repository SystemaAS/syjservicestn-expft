package no.systema.jservices.tvinn.expressfortolling.controller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import no.systema.jservices.tvinn.expressfortolling.api.ApiUploadClient;

/**
 * File upload entry point
 * 
 * @author oscardelatorre
 * @date Aug 2020
 *
 */
@RestController
public class ExpressFortollingManifestController {
	private static final Logger logger = Logger.getLogger(ExpressFortollingManifestController.class);
	
	@Value("${expft.file.source.directory}")
    private String baseDir;
	@Value("${expft.file.source.directory.sent}")
    private String sentDir;
	@Value("${expft.file.source.directory.error}")
    private String errorDir;
	
	@Value("${expft.upload.url}")
    private String uploadUrl;
	
	@Value("${expft.upload.prod.url}")
    private String uploadProdUrl;
	
	@Autowired
	ApiUploadClient apiUploadClient;

	
    
	/**
	 * Test entry-point
	 * Sends files to the local end-point in order to test payload transmission from multipart client.
	 * 
	 * @param session
	 * @return
	 */
	@RequestMapping(value="testUpload/expressmanifest", method={RequestMethod.GET})
	public ResponseEntity<String> testFileUploadByteArrayResource() {
		
		apiUploadClient.setUploadUrlImmutable(uploadUrl);
		String result = apiUploadClient.uploadPayloads(baseDir, sentDir, errorDir);
		
		return this.getResult(result);
		
	}
	
	
	
	
	/**
	 * Production entry-point
	 * Sends files to the local end-point in order to prod. payload transmission from multipart client.
	 *
	 * @return
	 * 
	 */
	@RequestMapping(value="prodUpload/expressmanifest", method={RequestMethod.GET})
	public ResponseEntity<String> prodFileUploadByteArrayResource() {
		
		apiUploadClient.setUploadUrlImmutable(uploadProdUrl);
		String result = apiUploadClient.uploadPayloads(baseDir, sentDir, errorDir);
		
		return this.getResult(result);
	}
	/**
	 * 
	 * @param result
	 * @return
	 */
	private ResponseEntity<String> getResult(String result) {
		ResponseEntity<String> retval = null;
		
		if(result!=null && result.startsWith("2")){
			if(result.startsWith("204")){
				//meaning no files to fetch
				logger.info(new ResponseEntity<String>(HttpStatus.NO_CONTENT));
				retval = new ResponseEntity<String>(HttpStatus.NO_CONTENT);
			}else{
				retval = new ResponseEntity<String>(HttpStatus.OK);
			}
		}else{
			if(result.startsWith("4")){
				retval = new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
			}else if (result.startsWith("5")){
				retval = new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
			}else{
				retval = new ResponseEntity<String>(HttpStatus.NOT_FOUND);
			}
			
		}
		return retval;
	}
	
}


