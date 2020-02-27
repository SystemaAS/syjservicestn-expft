package no.systema.jservices.sandbox;

import lombok.Data;

@Data
public class UserResponseDto {
	private int userId;
	private int id;
	private String title;
	private boolean completed;
	
}
