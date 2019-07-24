package org.annihilator.recommendation.config;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.*;
import javax.validation.Valid;
import javax.validation.constraints.*;
import lombok.Data;

@Data
public class RecommendationEngineConfiguration extends Configuration {
	@Valid
	@NotNull
	@JsonProperty
	private String esConfig;
}
