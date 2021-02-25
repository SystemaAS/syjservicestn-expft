package no.systema.jservices.sandbox;

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
import no.systema.jservices.tvinn.expressfortolling.api.TokenResponseDto;

import java.awt.PageAttributes.MediaType;
import java.time.Clock;
import java.util.*;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:test-configuration.xml")
@TestPropertySource(locations="classpath:application-test.properties")
public class TesterRest {

	
	{
		System.setProperty("catalina.home", "/Library/Tomcat/apache-tomcat-9.0.43");
	}
	
	/*@Test
	public void testCallToPublicJsonList() throws Exception {
		String uriList = "http://dummy.restapiexample.com/api/v1/employees";
		
		// support "text/plain" and json since json is the default
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setSupportedMediaTypes(Arrays.asList(org.springframework.http.MediaType.TEXT_HTML, org.springframework.http.MediaType.APPLICATION_JSON));
		
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(converter);
		
		ResponseEntity<List<EmployeeResponseDto>> response = restTemplate.exchange(uriList,HttpMethod.GET,null, new ParameterizedTypeReference<List<EmployeeResponseDto>>(){});
		List<EmployeeResponseDto> list = response.getBody();
		HttpHeaders headers = response.getHeaders();
		//render headers
		System.out.println("HttpHeaders:" + headers);
		//render payload
		int counter = 0;
		for(EmployeeResponseDto record : list){
			if(counter<5){
				System.out.println(record.getEmployee_name());
			}else{
				break;
			}
			counter++;
		}
	}*/
	
	/*@Test
	public void testCallToPublicJsonObject() throws Exception {
		String uri = "https://jsonplaceholder.typicode.com/todos/1";
		RestTemplate restTemplate = new RestTemplate();
		UserResponseDto dto = restTemplate.getForObject(uri, UserResponseDto.class);
		System.out.println(dto);
		System.out.println("Title:" + dto.getTitle());

	}*/
	
	@Test
	public void testCallToPublicJsonObject() throws Exception {
		String uri = "https://jsonplaceholder.typicode.com/todos/1";
		RestTemplate restTemplate = new RestTemplate();
		String dto = restTemplate.getForObject(uri, String.class);
		System.out.println(dto);
		//System.out.println("Title:" + dto.getTitle());
		final long now = Clock.systemUTC().millis();
		int expiration = 300000;
		System.out.println(new Date(now));
		System.out.println(new Date(now + expiration));
	}

	

	
	
	
}
