package org.annihilator.recommendation.models;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Rating {

  @SerializedName("user_id")
  private String userId;

  @SerializedName("movie_id")
  private String movieId;

  @SerializedName("rating")
  private Float rating;

  @SerializedName("timestamp")
  private String timestamp;
}
