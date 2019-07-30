package org.annihilator.recommendation.controller;

import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.annihilator.recommendation.config.RecommendationEngineConfiguration;
import org.annihilator.recommendation.db.JanusClient;
import org.janusgraph.core.SchemaViolationException;
import org.json.JSONObject;

import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Path("/janusEngine")

public class RecommendingEngineController {
	RecommendationEngineConfiguration config;
	JanusClient client = new JanusClient();

	public RecommendingEngineController(RecommendationEngineConfiguration config) {
		this.config = config;
	}

	@POST
	@Path("/addVertex")
	@Produces(MediaType.APPLICATION_JSON)
	public Response appendVertex(String json) {
		try {
			client.addNode(json, false);
		} catch (SchemaViolationException e) {
			return Response.ok("{\"message\": \"Vertex already exist with same Id\"}", MediaType.APPLICATION_JSON)
					.build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).build();
		}
		return Response.ok("{\"message\": \"Vertex added successfully\"}", MediaType.APPLICATION_JSON).build();
	}

	@POST
	@Path("/addEdge")
	@Produces(MediaType.APPLICATION_JSON)
	public Response appendEdge(String json) {
		try {
			client.addEdge(json, false);
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

	@GET
	@Path("/getVertexProperties")
	@Produces(MediaType.APPLICATION_JSON)
    public Response getVertex(String json) {
		List<Map<String, Object>> response = null;
		JSONObject jsonResponse = null;
    	try {
			response = client.getVertexAllProperties(json);

			if(response.size()!=0)
				jsonResponse = new JSONObject(response.get(0));
			else
				log.info("No value exist in database for given input: " + "\n" + json);
		} catch (Exception e) {
			return Response.status(500).build();
		}
    	return Response.ok("{\"properties\": " +  jsonResponse + "}", MediaType.APPLICATION_JSON).build();
    }

	@GET
	@Path("/getEdgeProperties")
	@Produces(MediaType.APPLICATION_JSON)
    public Response getEdge(String json) {
		List<Map<String, Object>> response = null;
		JSONObject jsonResponse = null;
    	try {
			response = client.getEdgeAllProperties(json);
			if(response.size()!=0)
				jsonResponse = new JSONObject(response.get(0));
			else
				log.info("No value exist in database for given input: " + "\n" + json);
		} catch (Exception e) {
			return Response.status(500).build();
		}
    	
    	return Response.ok("{\"properties\": " +  jsonResponse + "}", MediaType.APPLICATION_JSON).build();
    }
}
