package org.annihilator.recommendation.models;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class EdgePropsReqDTO {

  @SerializedName("user_id")
  private String userId;

  @SerializedName("movie_id")
  private String movieId;
}
