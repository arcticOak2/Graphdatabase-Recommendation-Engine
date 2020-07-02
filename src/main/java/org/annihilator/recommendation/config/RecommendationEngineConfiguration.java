package org.annihilator.recommendation.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class RecommendationEngineConfiguration extends Configuration {

  @Valid
  @NotNull
  @JsonProperty
  private String esConfig;
}
