package no.systema.jservices.tvinn.kurermanifest.controller;

import org.apache.log4j.Logger;
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
@RequestMapping("testUpload")
public class KurerManifestController {
	private static final Logger logger = Logger.getLogger(KurerManifestController.class);
	
	@Autowired
	ApiKurerUploadClient apiKurerUploadClient;

	@Value("${kurer.file.source.directory}")
    private String baseDir;
	
	@Value("${kurer.upload.url}")
    private String uploadUrl;
    
	/**
	 * Sends files to the local end-point in order to test payload transmission from multipart client.
	 * 
	 * @param session
	 * @return
	 */
	@RequestMapping(method={RequestMethod.GET})
	public ResponseEntity<String> testFileUploadByteArrayResource() {
		
		apiKurerUploadClient.setUploadUrl(uploadUrl);
		
		String result = apiKurerUploadClient.uploadPayloads(baseDir);
		if(result!=null && result.startsWith("2")){
			return new ResponseEntity<String>(HttpStatus.OK);
		}else{
			return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
		}
	}
	
}


