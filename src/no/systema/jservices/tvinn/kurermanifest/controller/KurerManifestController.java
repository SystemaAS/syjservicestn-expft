package no.systema.jservices.tvinn.kurermanifest.controller;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import no.systema.jservices.tvinn.kurermanifest.api.ApiKurerUploadClient;
import no.systema.jservices.tvinn.kurermanifest.server.controller.FileInfo;
import no.systema.jservices.tvinn.kurermanifest.server.controller.UploadEndPointController;
import no.systema.jservices.tvinn.kurermanifest.util.Utils;


@RestController
@RequestMapping("testUpload")
public class KurerManifestController {
	private static final Logger logger = Logger.getLogger(KurerManifestController.class);
	
	@Autowired
	private RestTemplate restTemplate;
	
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


