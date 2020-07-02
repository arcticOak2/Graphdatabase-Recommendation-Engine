package org.annihilator.recommendation.models;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class User {

  @SerializedName("id")
  private String id;
}
