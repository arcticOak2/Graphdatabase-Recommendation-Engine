package org.annihilator.recommendation.application;

import org.annihilator.recommendation.config.RecommendationEngineConfiguration;
import org.annihilator.recommendation.service.RecommendingEngineService;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

public class RecommendationEngineApplication extends Application<RecommendationEngineConfiguration> {

	public static void main(final String[] args) throws Exception {
        new RecommendationEngineApplication().run(args);
    }


    @Override
    public void run(final RecommendationEngineConfiguration configuration,
                    final Environment environment) {
    	environment.jersey().register(new RecommendingEngineService(configuration));
    }
    
}
