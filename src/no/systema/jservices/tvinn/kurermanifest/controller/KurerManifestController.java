package no.systema.jservices.tvinn.kurermanifest.controller;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import no.systema.jservices.tvinn.kurermanifest.api.ApiKurerUploadClient;


@RestController
public class KurerManifestController {
	private static final Logger logger = LoggerFactory.getLogger(KurerManifestController.class);
	
	@Autowired
	ApiKurerUploadClient apiKurerUploadClient;

	@Value("${kurer.file.source.directory}")
    private String baseDir;
	@Value("${kurer.file.source.directory.sent}")
    private String sentDir;
	@Value("${kurer.file.source.directory.error}")
    private String errorDir;
	
	@Value("${kurer.upload.url}")
    private String uploadUrl;
	
	@Value("${kurer.upload.prod.url}")
    private String uploadProdUrl;
	
	
    
	/**
	 * Test entry-point
	 * Sends files to the local end-point in order to test payload transmission from multipart client.
	 * 
	 * @param session
	 * @return
	 */
	@RequestMapping(value="testUpload", method={RequestMethod.GET})
	public ResponseEntity<String> testFileUploadByteArrayResource() {
		
		apiKurerUploadClient.setUploadUrlImmutable(uploadUrl);
		
		String result = apiKurerUploadClient.uploadPayloads(baseDir, sentDir, errorDir);
		if(result!=null && result.startsWith("2")){
			if(result.startsWith("204")){
				//meaning no files to fetch
				logger.info(new ResponseEntity<String>(HttpStatus.NO_CONTENT).toString());
				return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
			}else{
				return new ResponseEntity<String>(HttpStatus.OK);
			}
		}else{
			if(result.startsWith("4")){
				return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
			}else if (result.startsWith("5")){
				return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
			}else{
				return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
			}
			
		}
	}
	
	/**
	 * Production entry-point
	 * Sends files to the local end-point in order to prod. payload transmission from multipart client.
	 *
	 * @return
	 * 
	 */
	@RequestMapping(value="prodUpload", method={RequestMethod.GET})
	public ResponseEntity<String> prodFileUploadByteArrayResource() {
		
		apiKurerUploadClient.setUploadUrlImmutable(uploadProdUrl);
		
		String result = apiKurerUploadClient.uploadPayloads(baseDir, sentDir, errorDir);
		if(result!=null && result.startsWith("2")){
			if(result.startsWith("204")){
				//meaning no files to fetch
				logger.info(new ResponseEntity<String>(HttpStatus.NO_CONTENT).toString());
				return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
			}else{
				return new ResponseEntity<String>(HttpStatus.OK);
			}
		}else{
			return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
		}
	}
	
}


