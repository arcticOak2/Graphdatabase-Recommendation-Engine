package org.annihilator.recommendation.models;

import lombok.Data;

@Data
public class Movie {
	
	private String id;
	
	private String type = "movie";
	
	private String title;
	
	private String genres;
	
	private String imdbId;
	
	private String tmdbId;
}
