package org.annihilator.recommendation.models;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Movie {

  @SerializedName("id")
  private String id;

  @SerializedName("title")
  private String title;

  @SerializedName("genres")
  private String genres;

  @SerializedName("imdb_id")
  private String imdbId;

  @SerializedName("tmdb_id")
  private String tmdbId;
}
