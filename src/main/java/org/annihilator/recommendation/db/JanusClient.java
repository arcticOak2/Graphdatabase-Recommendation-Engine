package org.annihilator.recommendation.db;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.annihilator.recommendation.schema.LoadSchema;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.JanusGraphTransaction;
import org.janusgraph.core.TransactionBuilder;
import org.janusgraph.core.attribute.Text;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.diskstorage.BackendException;
import org.json.JSONObject;

@Slf4j
public class JanusClient {

  /**
   * This class is to upload data in bulk to the JanusGraph. Enabling the storage.batch-loading
   * configuration option will have the biggest positive impact on bulk loading times for most
   * applications. So, make sure you enabled this option in Janus configuration.
   * <p>
   * Author: Abhijeet Kumar, Distributed Systems Engineer
   */

  JanusGraph graph = JanusGraphFactory.open("config/janusgraph-cql-es.properties");

  public JanusClient() {
    LoadSchema.loadSchema(graph);
  }

  public JanusGraphTransaction enableBatchLoading() {
    log.info("Batch Loading enabled!");
    TransactionBuilder builder = graph.buildTransaction();
    JanusGraphTransaction tx = builder.enableBatchLoading().consistencyChecks(false).start();
    return tx;
  }

  public boolean addNode(String json, boolean bulkMode) throws Exception {

    JSONObject jsonObject = new JSONObject(json);

    /**
     * There can be two types of json:
     *
     * 1. Movie a. genres b. title c. movieId d. imdbId e. tmdbId
     *
     * 2. User a. userId
     */

    String typeOfVertex = jsonObject.getString("type");
    JanusGraphTransaction tx = null;
    if (!bulkMode) {
      tx = graph.newTransaction();
    } else {
      tx = enableBatchLoading();
    }
    String id = jsonObject.getString("id");

    if (typeOfVertex.equals("movie")) {
      String genres = jsonObject.getString("genres");
      String[] genresList = genres.split("\\|");
      Set<String> genresSet = new HashSet<>(Arrays.asList(genresList));
      String title = jsonObject.getString("title");
      String imdbId = jsonObject.getString("imdbId");
      String tmdbId = jsonObject.getString("tmdbId");

      tx.addVertex(T.label, "Movie", "genres", genresSet, "title", title, "movieId", id, "imdbId",
          imdbId, "tmdbId", tmdbId);

      tx.commit();
      log.info("id: " + id + " added!");

      return true;
    } else if (typeOfVertex.equals("user")) {
      tx.addVertex(T.label, "User", "userId", id);
      tx.commit();

      return true;
    } else {
      log.error("Unrecognized type of vertex");

      return false;
    }
  }

  public boolean addEdge(String json, boolean bulkMode) throws Exception {

    JSONObject jsonObject = new JSONObject(json);

    String userId = jsonObject.getString("userId");
    String movieId = jsonObject.getString("movieId");
    String rating = jsonObject.getString("rating");
    String timestamp = jsonObject.getString("timestamp");

    JanusGraphManagement mgmt = graph.openManagement();
    GraphTraversalSource g = graph.traversal();

    Vertex subNode = null;
    Vertex objNode = null;

    try {
      subNode = g.V().has("userId", userId).next();
      objNode = g.V().has("movieId", movieId).next();
    } catch (NoSuchElementException e) {
      log.error("Either both of them or none of them exist in database!");
      return false;
    }
    if (null != subNode && null != objNode) {
      Edge watched = subNode.addEdge("watched", objNode);
      watched.property("rating", rating);
      watched.property("timestamp", timestamp);

      mgmt.commit();
      g.tx().commit();
      g.close();

      return true;
    } else {
      mgmt.commit();
      g.tx().commit();
      g.close();

      return false;
    }
  }

  public List<Map<Object, Object>> getVertexProperties(String json) {
    JSONObject jsonObject = new JSONObject(json);
    String id;
    GraphTraversal<Vertex, Vertex> node = null;
    GraphTraversalSource g = graph.traversal();
    if (!jsonObject.isNull("userId")) {
      id = jsonObject.getString("userId");
      node = g.V().has("userId", id);
    } else if (!jsonObject.isNull("movieId")) {
      id = jsonObject.getString("movieId");
      node = g.V().has("movieId", id);
    } else {
      log.error("Invalid Input: " + json);
    }

    if (null != node) {
      return node.valueMap().toList();
    } else {
      return null;
    }
  }

  public List<Map<Object, Object>> getEdgeProperties(String json) {
    JSONObject jsonObject = new JSONObject(json);
    GraphTraversalSource g = graph.traversal();

    String userId = jsonObject.getString("userId");
    String movieId = jsonObject.getString("movieId");

    GraphTraversal<Vertex, Edge> node =
        g.V().has("userId", userId).outE().where(__.inV().has("movieId", movieId));

    return node.valueMap().toList();
  }

  public List<Map<Object, Object>> getMovieDetails(String json) {
    JSONObject jsonObject = new JSONObject(json);
    GraphTraversalSource g = graph.traversal();

    String movieName = jsonObject.getString("movieName");

    List<Map<Object, Object>> node =
        g.V().has("title", Text.textContains(movieName)).valueMap().toList();

    return node;
  }

  // CAUTION:
  // This method is not recommended to have in production environment
  // This method will delete all the data in JanusGraph database
  public void purgeJanus() throws BackendException {

    JanusGraphFactory.drop(graph);
    graph = null;
    graph = JanusGraphFactory.open("config/janusgraph-cql-es.properties");

    // To load schema again
    LoadSchema.loadSchema(graph);
  }
}
