package no.systema.jservices.tvinn.expressfortolling.controller;

import javax.servlet.http.HttpSession;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import no.systema.jservices.common.dao.services.BridfDaoService;
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
	private static final Logger logger = LoggerFactory.getLogger(ExpressFortollingManifestController.class);
	
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
	
	@Value("${expft.upload.docs.url}")
	private String uploadDocsUrl;
	
	@Value("${expft.upload.docs.url.v2}")
	private String uploadDocsUrlV2;
	
	@Autowired
	ApiUploadClient apiUploadClient;

	@Autowired
	private BridfDaoService bridfDaoService;
	
    
	/**
	 * Test entry-point
	 * Sends files to the local end-point in order to test payload transmission from multipart client.
	 * 
	 * @param session
	 * @return
	 */
	@RequestMapping(value="testUpload/expressmanifest", method={RequestMethod.GET})
	public ResponseEntity<String> testFileUploadByteArrayResource(HttpSession session) {
		
		apiUploadClient.setUploadUrlImmutable(uploadUrl);
		String result = apiUploadClient.uploadPayloads(baseDir, sentDir, errorDir);
		
		session.invalidate();
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
	public ResponseEntity<String> prodFileUploadByteArrayResource(HttpSession session) {
		logger.warn("upload to API from baseDir:" + baseDir);
		apiUploadClient.setUploadUrlImmutable(uploadProdUrl);
		String result = apiUploadClient.uploadPayloads(baseDir, sentDir, errorDir);
		
		session.invalidate();
		return this.getResult(result);
	}
	
	
	/**
	 * Entry point for the Document API to toll.no when the end-user uploads a document manually
	 * 
	 * @param session
	 * @param user
	 * @param declarationId
	 * @param documentType
	 * @param fileName
	 * @return
	 * @throws Exception
	 * 
	 * PRODUCTION: http://localhost:8080/syjservicestn-expft/uploadFileByUser.do
	 */
	@RequestMapping(value="uploadFileByUser.do", method={RequestMethod.GET, RequestMethod.POST}, produces = {MediaType.ALL_VALUE})
	public String uploadFileByUser(HttpSession session,
			@RequestParam(value = "user", required = true) String user,
			@RequestParam(value = "declId", required = true) String declId, 
			@RequestParam(value = "docType", required = true) String docType, 
			@RequestParam(value = "docPath", required = true) String docPath) throws Exception {

		checkUser(user);
		//now process
		
		apiUploadClient.setUploadUrlImmutable(uploadDocsUrl);
		logger.warn("starting ...");
		logger.warn(declId + "XXX" + docType + "XXX" + docPath);
		String result = apiUploadClient.uploadDocumentsByUser(declId, docType, docPath);
		logger.warn(result);
		session.invalidate();
		return result;
	}
	
	/**
	 * Entry point for the Document API to toll.no when the end-user uploads a document manually
	 * This is version 2 with maskinport token + toll token according to the new double authentication as in Express fortollning V2 Movment road
	 * https://toll.github.io/api/document-api.html
	 * 
	 * @param session
	 * @param user
	 * @param declarationId
	 * @param documentType
	 * @param fileName
	 * @return
	 * @throws Exception
	 * 
	 * PRODUCTION: http://localhost:8080/syjservicestn-expft/uploadFileByUserV2.do
	 */
	@RequestMapping(value="uploadFileByUserV2.do", method={RequestMethod.GET, RequestMethod.POST}, produces = {MediaType.ALL_VALUE})
	public String uploadFileByUserV2(HttpSession session,
			@RequestParam(value = "user", required = true) String user,
			@RequestParam(value = "declId", required = true) String declId, 
			@RequestParam(value = "docType", required = true) String docType, 
			@RequestParam(value = "docPath", required = true) String docPath) throws Exception {

		checkUser(user);
		//now process
		
		apiUploadClient.setUploadUrlImmutable(uploadDocsUrlV2);
		logger.warn("starting ...");
		logger.warn(declId + "XXX" + docType + "XXX" + docPath);
		String result = apiUploadClient.uploadDocumentsByUserV2(declId, docType, docPath);
		logger.warn(result);
		session.invalidate();
		return result;
	}
	
	/**
	 * 
	 * @param user
	 */
	private void checkUser(String user) {
		if (!bridfDaoService.userNameExist(user)) {
			throw new RuntimeException("ERROR: parameter, user, is not valid!");
		}		
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
				logger.warn(new ResponseEntity<String>(HttpStatus.NO_CONTENT).toString());
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


