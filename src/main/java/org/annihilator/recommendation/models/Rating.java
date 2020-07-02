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
  private String rating;

  @SerializedName("timestamp")
  private String timestamp;
}
