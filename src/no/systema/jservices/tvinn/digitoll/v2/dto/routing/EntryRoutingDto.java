package no.systema.jservices.tvinn.digitoll.v2.dto.routing;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntryRoutingDto {
	private String entrySummaryDeclarationMRN;
	private String estimatedTimeOfArrival;
	private String scheduledTimeOfArrival;
	private EntryTranspDocDto transportDocumentHouseLevel;
	private EntryActiveTransportMeansDto activeBorderTransportMeans;
	private EntryRoutingResultDto routingResult;
}
