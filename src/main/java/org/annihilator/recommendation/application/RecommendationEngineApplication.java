package org.annihilator.recommendation.application;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import org.annihilator.recommendation.config.RecommendationEngineConfiguration;
import org.annihilator.recommendation.controller.RecommendingEngineController;

public class RecommendationEngineApplication
    extends Application<RecommendationEngineConfiguration> {

  public static void main(final String[] args) throws Exception {
    new RecommendationEngineApplication().run(args);
  }

  @Override
  public void run(final RecommendationEngineConfiguration configuration,
      final Environment environment) {
    environment.jersey().register(new RecommendingEngineController(configuration));
  }
}
