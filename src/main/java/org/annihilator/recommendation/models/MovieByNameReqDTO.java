package org.annihilator.recommendation.models;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class MovieByNameReqDTO {

  @SerializedName("movie_name")
  private String movieName;
}
