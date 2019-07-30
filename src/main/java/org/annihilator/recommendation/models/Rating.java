package org.annihilator.recommendation.models;

import lombok.Data;

@Data
public class Rating {
	private String userId;
	
	private String movieId;
	
	private String rating;
	
	private String timestamp;
}
