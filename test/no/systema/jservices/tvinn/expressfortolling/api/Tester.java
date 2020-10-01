package no.systema.jservices.tvinn.expressfortolling.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.systema.jservices.common.dto.expressfortolling.ManifestTypesOfExportDto;
import no.systema.main.util.ObjectMapperHalJson;


public class Tester   {
	private static final Logger logger = Logger.getLogger(Tester.class);
	
	@Test
	public void run() throws Exception{
		final String data = "{\"_embedded\":{\"typesOfExport\":[{\"_links\":{\"self\":{\"href\":\"https://external-manifest-api-stage-exf-external.apps.toll.no/v2/ekspressfortolling/type-of-export/EUEIR_EXPORT\"}},\"code\":\"EUEIR_EXPORT\",\"name\":\"EUEIR\",\"translations\":{\"NOR\":\"EUEIR\"}},{\"_links\":{\"self\":{\"href\":\"https://external-manifest-api-stage-exf-external.apps.toll.no/v2/ekspressfortolling/type-of-export/TRANSIT_EXPORT\"}},\"code\":\"TRANSIT_EXPORT\",\"name\":\"Transitt\",\"translations\":{\"NOR\":\"Transitt\"}},{\"_links\":{\"self\":{\"href\":\"https://external-manifest-api-stage-exf-external.apps.toll.no/v2/ekspressfortolling/type-of-export/ALE_EXPORT\"}},\"code\":\"ALE_EXPORT\",\"name\":\"ALE\",\"translations\":{\"NOR\":\"ALE\"}},{\"_links\":{\"self\":{\"href\":\"https://external-manifest-api-stage-exf-external.apps.toll.no/v2/ekspressfortolling/type-of-export/UGE_EXPORT\"}},\"code\":\"UGE_EXPORT\",\"name\":\"UGE\",\"translations\":{\"NOR\":\"UGE\"}},{\"_links\":{\"self\":{\"href\":\"https://external-manifest-api-stage-exf-external.apps.toll.no/v2/ekspressfortolling/type-of-export/KVALITETSSEKRAD_EXPORT\"}},\"code\":\"KVALITETSSEKRAD_EXPORT\",\"name\":\"Kvalitetssäkrad\",\"translations\":{\"NOR\":\"Kvalitetssäkrad\"}}]},\"_links\":{\"self\":{\"href\":\"https://external-manifest-api-stage-exf-external.apps.toll.no/v2/ekspressfortolling/type-of-export\"}}}";
		
		ObjectMapperHalJson objMapper = new ObjectMapperHalJson(data, "/_embedded/typesOfExport");
		StringBuffer jsonToConvert = new StringBuffer();
		ArrayList<ManifestTypesOfExportDto> exports = objMapper.getObjectMapper(jsonToConvert).readValue(jsonToConvert.toString(), new TypeReference<List<ManifestTypesOfExportDto>>() {
        });
		logger.info(exports.toString());
	}
	
}
