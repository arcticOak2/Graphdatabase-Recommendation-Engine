package org.annihilator.recommendation.controller;

import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.annihilator.recommendation.config.RecommendationEngineConfiguration;
import org.annihilator.recommendation.db.JanusClient;
import org.janusgraph.core.SchemaViolationException;
import org.json.JSONArray;
import org.json.JSONObject;

@Slf4j
@Path("/janus_engine")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class RecommendingEngineController {

  RecommendationEngineConfiguration config;
  JanusClient client = new JanusClient();

  public RecommendingEngineController(RecommendationEngineConfiguration config) {
    this.config = config;
  }

  @POST
  @Path("/add_vertex")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes({MediaType.APPLICATION_JSON})
  public Response appendVertex(String json) {
    try {
      client.addNode(json, false);
    } catch (SchemaViolationException e) {
      return Response
          .ok("{\"message\": \"Vertex already exist with same Id\"}", MediaType.APPLICATION_JSON)
          .build();
    } catch (Exception e) {
      e.printStackTrace();
      return Response.status(500).build();
    }
    return Response.ok("{\"message\": \"Vertex added successfully\"}", MediaType.APPLICATION_JSON)
        .build();
  }

  @POST
  @Path("/add_edge")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes({MediaType.APPLICATION_JSON})
  public Response appendEdge(String json) {
    try {
      client.addEdge(json, false);
    } catch (Exception e) {
      return Response.status(500).build();
    }
    return Response.ok("{\"message\": \"Edge added successfully\"}", MediaType.APPLICATION_JSON)
        .build();
  }

  @POST
  @Path("/purge")
  @Produces({MediaType.APPLICATION_JSON})
  @Consumes({MediaType.APPLICATION_JSON})
  public Response purgeEngine() {
    try {
      client.purgeJanus();
    } catch (Exception e) {
      e.printStackTrace();
      return Response.status(500).build();
    }
    return Response
        .ok("{\"message\": \"Purged complete Janus Engine\"}", MediaType.APPLICATION_JSON).build();
  }

  @POST
  @Path("/get_vertex_properties")
  @Produces({MediaType.APPLICATION_JSON})
  @Consumes({MediaType.APPLICATION_JSON})
  public Response getVertex(String json) {
    List<Map<Object, Object>> response = null;
    JSONObject jsonResponse = null;
    try {
      response = client.getVertexProperties(json);

      if (response.size() != 0) {
        jsonResponse = new JSONObject(response.get(0));
      } else {
        log.info("No value exist in database for given input: " + "\n" + json);
      }
    } catch (Exception e) {
      return Response.status(500).build();
    }
    return Response.ok("{\"properties\": " + jsonResponse + "}", MediaType.APPLICATION_JSON)
        .build();
  }

  @POST
  @Path("/get_edge_properties")
  @Produces({MediaType.APPLICATION_JSON})
  @Consumes({MediaType.APPLICATION_JSON})
  public Response getEdge(String json) {
    List<Map<Object, Object>> response = null;
    JSONObject jsonResponse = null;
    try {
      response = client.getEdgeProperties(json);
      if (response.size() != 0) {
        jsonResponse = new JSONObject(response.get(0));
      } else {
        log.info("No value exist in database for given input: " + "\n" + json);
      }
    } catch (Exception e) {
      e.printStackTrace();
      return Response.status(500).build();
    }

    return Response.ok("{\"properties\": " + jsonResponse + "}", MediaType.APPLICATION_JSON)
        .build();
  }

  @POST
  @Path("/get_movie_details_by_name")
  @Produces({MediaType.APPLICATION_JSON})
  @Consumes({MediaType.APPLICATION_JSON})
  public Response getMovieDetailsByName(String json) {
    JSONArray jsonResponse = new JSONArray();

    try {
      List<Map<Object, Object>> movies = client.getMovieDetails(json);
      JSONObject movieResponse;

      for (Map<Object, Object> movie : movies) {
        movieResponse = new JSONObject(movie);
        System.out.println(movieResponse);
        jsonResponse.put(movieResponse);
      }
    } catch (Exception e) {
      e.printStackTrace();
      return Response.status(500).build();
    }

    return Response.ok("{\"movies\": " + jsonResponse + "}", MediaType.APPLICATION_JSON).build();

  }

  @POST
  @Path("/get_similar_movies")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes({MediaType.APPLICATION_JSON})
  public Response getSimilarMovies() {

    return null;
  }
}
