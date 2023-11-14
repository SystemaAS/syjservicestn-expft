package no.systema.jservices.tvinn.expressfortolling2.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiMrnHouseQueryRecordDto {
	private String status;
	private String requestId;
	private String notificationDate;
	private String mrn;
	private String[] incompleteDocumentationReasonList = new String[80];
	private List<ApiMrnHouseQueryRecordChildDto> validationErrorList = new ArrayList<ApiMrnHouseQueryRecordChildDto>();
}

/*
{
"status": "SUCCESS",
"requestId": "fa90bb2d-7cfd-400a-bb5a-8d5df75ae412",
"notificationDate": "2022-04-08T11:51:00Z",
"mrn": "22NO4TU2HUD59UCBT8",
"incompleteDocumentationReasonList": [
  "string"
],
"validationErrorList": [
  {
    "description": "Ugyldig verdi",
    "pointer": {
      "messageElementPath": "masterConsignment.documentIssueDate"
    }
  }
]*/