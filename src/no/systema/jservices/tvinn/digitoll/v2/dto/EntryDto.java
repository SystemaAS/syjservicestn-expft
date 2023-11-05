package no.systema.jservices.tvinn.digitoll.v2.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntryDto {
	private String entrySummaryDeclarationMRN;
	private String estimatedTimeOfArrival;
	private EntryTranspDocDto transportDocumentHouseLevel;
	private EntryActiveTransportMeansDto activeBorderTransportMeans;
	private EntryRoutingResultDto routingResult;
}
