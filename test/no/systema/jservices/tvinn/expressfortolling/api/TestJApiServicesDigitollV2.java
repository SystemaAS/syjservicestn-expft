package no.systema.jservices.tvinn.expressfortolling.api;

import java.io.*;
import java.util.*;

import org.slf4j.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;



import no.systema.jservices.tvinn.digitoll.v2.dao.*;
import no.systema.jservices.tvinn.digitoll.v2.dto.SadmomfDto;
import no.systema.jservices.tvinn.digitoll.v2.services.*;
import no.systema.jservices.tvinn.expressfortolling.TestJBase;
import no.systema.jservices.tvinn.expressfortolling2.util.GenericJsonStringPrinter;


@RunWith(SpringJUnit4ClassRunner.class)
//@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:test-configuration.xml")
@TestPropertySource(locations="classpath:application-test.properties")
//@ContextConfiguration(classes = {ApiServices.class})

public class TestJApiServicesDigitollV2 extends TestJBase {

	@Autowired
	ApiServices apiServices;
	
	
	private Authorization authorization;
	private static final Logger logger = LoggerFactory.getLogger(TestJApiServicesDigitollV2.class);
	//0eb9f81d-3385-4baa-95aa-07c73d4d8fd3 ORIG-simple-test
	//private final String manifestId = "2350cab2-98f0-4b54-a4f7-a2ae453e61bd";
	private final String manifestId = "e35a52a6-18ae-4746-a4b4-9e3f0edbacc6";
	
	
	
	
	
		//////////////////////////////
	//nya exprf. movement road
	/////////////////////////////
	@Test //OK
	public void testAuthExpressMovementRoad() throws Exception {
		String json = apiServices.testAuthExpressMovementRoad();
		System.out.println("JSON = " + json);
	}
	
	@Test //for validating the raw json swagger spec
	public void writeJsonTransport() throws Exception {
		//this will be populated by the SADxxx Dto in real-world. We can not test it here unfortunately ...
		//Transport entity = new MapperTransport().mapTransport(new Object()); 
		//Debug
		//System.out.println(GenericJsonStringPrinter.debug(entity));
		
	}
	
	@Test //for validating the raw json swagger spec
	public void writeJsonMasterConsignment() throws Exception {
		//this will be populated by the SADxxx Dto in real-world. We can not test it here unfortunately ...
		MasterConsignment entity = new MapperMasterConsignment().mapMasterConsignment(new SadmomfDto()); 
		//Debug
		System.out.println(GenericJsonStringPrinter.debug(entity));
		
	}
	
	/*
	@Test //for validating the raw json swagger spec
	public void writeJsonHouseConsignment() throws Exception {
		//this will be populated by the SADxxx Dto in real-world. We can not test it here unfortunately ...
		HouseConsignment entity = new MapperHouseConsignment().mapHouseConsignment(new Object()); 
		//Debug
		System.out.println(GenericJsonStringPrinter.debug(entity));
		
	}*/
	
	
	
}
