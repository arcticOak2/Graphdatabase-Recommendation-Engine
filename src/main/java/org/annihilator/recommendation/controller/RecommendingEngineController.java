package org.annihilator.recommendation.controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.annihilator.recommendation.config.RecommendationEngineConfiguration;

@Path("/engine")
public class RecommendingEngineController {
	RecommendationEngineConfiguration config;
	
	public RecommendingEngineController(RecommendationEngineConfiguration config) {
		this.config = config;
	}
	
	@GET
	@Path("/callback")
	@Produces(MediaType.APPLICATION_JSON)
    public Response callBack(String json) {
    	System.out.println(json);
    	System.out.println(config.getEsConfig());
    	return Response.ok("{\"hello\": \"lolo\"}", MediaType.APPLICATION_JSON).build();
    }
}
