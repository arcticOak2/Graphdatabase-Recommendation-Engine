package org.annihilator.recommendation.controller;

import com.google.gson.Gson;
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
import org.annihilator.recommendation.models.EdgePropsReqDTO;
import org.annihilator.recommendation.models.Movie;
import org.annihilator.recommendation.models.MovieByNameReqDTO;
import org.annihilator.recommendation.models.Rating;
import org.annihilator.recommendation.models.ResponseStructure;
import org.annihilator.recommendation.models.User;
import org.annihilator.recommendation.models.VertexPropsReqDTO;
import org.janusgraph.core.SchemaViolationException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
@Path("/janus_engine")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class RecommendingEngineController {

  private static final Logger logger;
  private static final Gson gson;

  RecommendationEngineConfiguration config;
  JanusClient client = new JanusClient();

  public RecommendingEngineController(RecommendationEngineConfiguration config) {
    this.config = config;
  }

  static {
    logger = LoggerFactory.getLogger(RecommendingEngineController.class);
    gson = new Gson();
  }

  private boolean isMovieValid(Movie movie) {
    if (null == movie.getId()
        || null == movie.getGenres()
        || null == movie.getTitle()
        || null == movie.getImdbId()
        || null == movie.getTmdbId()) {
      return false;
    }

    return true;
  }

  private boolean isUserValid(User user) {
    if (null == user.getId()) {
      return false;
    }

    return true;
  }

  private boolean isEdgeValid(Rating edge) {
    if (null == edge.getMovieId()
        || null == edge.getRating()
        || null == edge.getTimestamp()
        || null == edge.getUserId()
        || edge.getRating() <= 0
        || edge.getRating() > 5) {
      return false;
    }

    return true;
  }

  private boolean isVertexPropsReqValid(VertexPropsReqDTO dto) {
    if (null == dto.getMovieId() && null == dto.getUserId()) {
      return false;
    }

    return true;
  }

  private boolean isEdgePropsReqValid(EdgePropsReqDTO dto) {
    if (null == dto.getMovieId() || null == dto.getUserId()) {
      return false;
    }

    return true;
  }

  @POST
  @Path("/add_movie")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes({MediaType.APPLICATION_JSON})
  public Response addMovie(String rawBody) {

    Movie movie = gson.fromJson(rawBody, Movie.class);

    if (isMovieValid(movie)) {
      try {
        client.addMovie(movie, false);
      } catch (SchemaViolationException e) {
        return Response.ok(gson.toJson(ResponseStructure.getDataExistResponse())).build();
      } catch (Exception e) {
        e.printStackTrace();
        return Response.status(500).build();
      }
      return Response.ok(gson.toJson(ResponseStructure.getSuccessResponse())).build();
    } else {
      return Response.status(422).entity(gson.toJson(ResponseStructure.getInvalidInputResponse()))
          .build();
    }
  }

  @POST
  @Path("/add_user")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes({MediaType.APPLICATION_JSON})
  public Response addUser(String rawBody) {

    User user = gson.fromJson(rawBody, User.class);

    if (isUserValid(user)) {
      try {
        client.addUser(user, false);
      } catch (SchemaViolationException e) {
        return Response.ok(gson.toJson(ResponseStructure.getDataExistResponse())).build();
      } catch (Exception e) {
        logger.error("Error while ingesting user to the database", e);
        return Response.status(500).build();
      }
      return Response.ok(gson.toJson(ResponseStructure.getSuccessResponse())).build();
    } else {
      return Response.status(422).entity(gson.toJson(ResponseStructure.getInvalidInputResponse()))
          .build();
    }
  }

  @POST
  @Path("/add_edge")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes({MediaType.APPLICATION_JSON})
  public Response appendEdge(String json) {

    Rating edge = gson.fromJson(json, Rating.class);
    if (isEdgeValid(edge)) {
      try {
        client.addEdge(edge, false);
      } catch (Exception e) {
        logger.error("Error while creating edge", e);
        return Response.status(500).build();
      }
    } else {
      return Response.status(422).entity(gson.toJson(ResponseStructure.getInvalidInputResponse()))
          .build();
    }

    return Response.ok(ResponseStructure.getSuccessResponse()).build();
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

    return Response.ok(ResponseStructure.getSuccessResponse()).build();
  }

  @POST
  @Path("/get_vertex_properties")
  @Produces({MediaType.APPLICATION_JSON})
  @Consumes({MediaType.APPLICATION_JSON})
  public Response getVertex(String json) {

    VertexPropsReqDTO dto = gson.fromJson(json, VertexPropsReqDTO.class);
    if (isVertexPropsReqValid(dto)) {
      List<Map<Object, Object>> response = client.getVertexProperties(dto);

      if (response.size() != 0) {
        return Response.ok(gson
            .toJson(ResponseStructure
                .getSuccessResponseWithBody(null, new JSONObject(response.get(0)).toString())))
            .build();
      } else {
        return Response.ok(gson.toJson(ResponseStructure.getNotFoundResponse())).build();
      }
    } else {
      return Response.status(422).entity(gson.toJson(ResponseStructure.getInvalidInputResponse()))
          .build();
    }
  }

  @POST
  @Path("/get_edge_properties")
  @Produces({MediaType.APPLICATION_JSON})
  @Consumes({MediaType.APPLICATION_JSON})
  public Response getEdge(String json) {

    EdgePropsReqDTO dto = gson.fromJson(json, EdgePropsReqDTO.class);

    if (isEdgePropsReqValid(dto)) {
      List<Map<Object, Object>> response = client.getEdgeProperties(dto);
      if (response.size() != 0) {
        return Response.ok(new JSONObject(response.get(0)).toString()).build();
      } else {
        log.info("No value exist in database for given input");
        return Response.ok(gson.toJson(ResponseStructure.getNotFoundResponse())).build();
      }
    } else {
      return Response.status(422).entity(gson.toJson(ResponseStructure.getInvalidInputResponse()))
          .build();
    }
  }

  @POST
  @Path("/get_movie_details_by_name")
  @Produces({MediaType.APPLICATION_JSON})
  @Consumes({MediaType.APPLICATION_JSON})
  public Response getMovieDetailsByName(String json) {

    MovieByNameReqDTO dto = gson.fromJson(json, MovieByNameReqDTO.class);

    if(null != dto.getMovieName()) {
      List<Map<Object, Object>> movies = client.getMovieDetails(dto);

      if (movies.size() == 0) {
        return Response.ok(gson.toJson(ResponseStructure.getNotFoundResponse())).build();
      }

      JSONObject movieResponse;

      JSONArray jsonResponse = new JSONArray();
      for (Map<Object, Object> movie : movies) {
        movieResponse = new JSONObject(movie);
        jsonResponse.put(movieResponse);
      }

      return Response.ok(gson
          .toJson(ResponseStructure.getSuccessResponseWithBody(null, jsonResponse.toString())))
          .build();
    }

    return Response.status(422).entity(gson.toJson(ResponseStructure.getInvalidInputResponse())).build();
  }

  @POST
  @Path("/get_similar_movies")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes({MediaType.APPLICATION_JSON})
  public Response getSimilarMovies() {

    return null;
  }
}
