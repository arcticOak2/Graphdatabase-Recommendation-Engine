package org.annihilator.recommendation.config;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public class RecommendationEngineConfiguration extends Configuration {
  @Valid
  @NotNull
  @JsonProperty
  private String esConfig;
}
