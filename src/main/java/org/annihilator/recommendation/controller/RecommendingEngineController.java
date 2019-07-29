package org.annihilator.recommendation.controller;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.annihilator.recommendation.config.RecommendationEngineConfiguration;
import org.annihilator.recommendation.db.JanusClient;

@Path("/janusEngine")
public class RecommendingEngineController {
	RecommendationEngineConfiguration config;
	JanusClient client;
	
	public RecommendingEngineController() {
		client = new JanusClient();
	}
	
	public RecommendingEngineController(RecommendationEngineConfiguration config) {
		this.config = config;
	}
	
	@POST
	@Path("/addVertex")
	@Produces(MediaType.APPLICATION_JSON)
    public Response appendVertex(String json) {
    	try {
			client.addNode(json);
		} catch (Exception e) {
			return Response.status(500).build();
		}
    	return Response.ok("{\"message\": \"Vertex added successfully\"}", MediaType.APPLICATION_JSON).build();
    }
	
	@POST
	@Path("/addEdge")
	@Produces(MediaType.APPLICATION_JSON)
    public Response appendEdge(String json) {
    	try {
			client.addEdge(json);
		} catch (Exception e) {
			return Response.status(500).build();
		}
    	return Response.ok("{\"message\": \"Edge added successfully\"}", MediaType.APPLICATION_JSON).build();
    }
	
	@POST
	@Path("/purge")
	@Produces(MediaType.APPLICATION_JSON)
    public Response purgeEngine() {
    	try {
			client.purgeJanus();
		} catch (Exception e) {
			return Response.status(500).build();
		}
    	return Response.ok("{\"message\": \"Purged complete Janus Engine\"}", MediaType.APPLICATION_JSON).build();
    }
}
