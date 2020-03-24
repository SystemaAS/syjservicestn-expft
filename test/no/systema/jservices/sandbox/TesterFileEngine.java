package no.systema.jservices.sandbox;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import io.jsonwebtoken.Claims;
import no.systema.jservices.common.util.FileManager;
import no.systema.jservices.tvinn.expressfortolling.api.TokenResponseDto;
import no.systema.jservices.tvinn.kurermanifest.polling.TestSchedularService;

import java.awt.PageAttributes.MediaType;
import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:test-configuration.xml")
@TestPropertySource(locations="classpath:application-test.properties")
public class TesterFileEngine {
	private Logger logger = Logger.getLogger(TesterFileEngine.class);
	private FileManager fileMgr = new FileManager();
	
	{
		System.setProperty("catalina.home", "/Library/Tomcat/apache-tomcat-9.0.8");
	}
	
	
	@Test
	public void testCallToPublicJsonObject() throws Exception {
		String retval = "202";
		String baseDir = "/zzz/test/";
		String sentDir = "/zzz/test/sent/";
		String errorDir = "/zzz/test/error/";
		
		int maxLimitOfFilesPerLoop = 2;
		
		if (Files.exists(Paths.get(baseDir))) {
			fileMgr.secureTargetDir(sentDir);
			fileMgr.secureTargetDir(errorDir);
			
			int counter = 0;
			try {
				/*
				try (Stream<Path> walk = Files.walk(Paths.get(baseDir))) {
				List<String> files = walk.filter(Files::isRegularFile)
						.map(x -> x.toString()).collect(Collectors.toList());
				files.forEach(System.out::println);
				*/
				List<File> files = fileMgr.getValidFilesInDirectory(baseDir);
				files.forEach(System.out::println);
				
				/*
				//Send each file per restTemplate call
				for(File file: files){
						String fileName = file.getAbsolutePath();
						//String fileName = Paths.get(filePath).getFileName().toString();
						counter++;
						//there is a bug in Toll.no for more than 2 files in the same REST loop ... ? To be researched ...
						if (counter<=maxLimitOfFilesPerLoop){
						
							//menos de 2
							logger.info(fileName);
							//Toll.no does take a json payload as String not as multipart
							//retval = upload_via_jsonString(new Utils().getFilePayloadStream(fileName), fileName, authTokenDto);
							logger.info("#########:" + retval);
							//Toll.no does not take a file as Multipart. Could be a reality in version 2
							//retval = this.upload_via_streaming(new Utils().getFilePayloadStream(fileName), fileName, authTokenDto);
							if(retval.startsWith("2")){
								logger.info(Paths.get(fileName) + " " + Paths.get(sentDir + Paths.get(fileName).getFileName().toString()));
								Path temp = Files.move( Paths.get(fileName), Paths.get(sentDir + Paths.get(fileName).getFileName().toString()));
							}else{
								logger.info(Paths.get(fileName) + " " + Paths.get(errorDir + Paths.get(fileName).getFileName().toString()));
								Path temp = Files.move( Paths.get(fileName), Paths.get(errorDir + Paths.get(fileName).getFileName().toString()));
							}
						}
				
				
				}*/
				
			}catch(Exception e){
				e.toString();
			}
		
		}else{
			logger.error("No directory found: " + baseDir);
		}
		
		
		

	}
	

}
