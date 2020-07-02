package org.annihilator.recommendation.db;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.annihilator.recommendation.models.EdgePropsReqDTO;
import org.annihilator.recommendation.models.Movie;
import org.annihilator.recommendation.models.MovieByNameReqDTO;
import org.annihilator.recommendation.models.Rating;
import org.annihilator.recommendation.models.User;
import org.annihilator.recommendation.models.VertexPropsReqDTO;
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
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.diskstorage.BackendException;

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

  private JanusGraphTransaction getTransaction(boolean bulkMode) {

    JanusGraphTransaction tx = null;

    if (!bulkMode) {
      tx = graph.newTransaction();
    } else {
      tx = enableBatchLoading();
    }

    return tx;
  }

  public JanusGraphTransaction enableBatchLoading() {
    log.info("Batch Loading enabled!");
    TransactionBuilder builder = graph.buildTransaction();
    JanusGraphTransaction tx = builder.enableBatchLoading().consistencyChecks(false).start();

    return tx;
  }

  public void addMovie(Movie movie, boolean bulkMode) throws Exception {

    JanusGraphTransaction tx = getTransaction(bulkMode);

    String[] genresList = movie.getGenres().split("\\|");
    Set<String> genresSet = new HashSet<>(Arrays.asList(genresList));

    tx.addVertex(T.label, "MOVIE",
        "genres", genresSet,
        "title", movie.getTitle().toLowerCase(),
        "movie_id", movie.getId(),
        "imdb_id", movie.getImdbId(),
        "tmdb_id", movie.getTmdbId());

    tx.commit();
  }

  public void addUser(User user, boolean bulkMode) throws Exception {

    JanusGraphTransaction tx = getTransaction(bulkMode);

    tx.addVertex(T.label, "USER", "user_id", user.getId());
    tx.commit();
  }

  public void addEdge(Rating edge, boolean bulkMode) throws Exception {

    JanusGraphManagement mgmt = graph.openManagement();
    GraphTraversalSource g = graph.traversal();

    Vertex subNode = null;
    Vertex objNode = null;

    try {
      subNode = g.V().has("user_id", edge.getUserId()).next();
      objNode = g.V().has("movie_id", edge.getMovieId()).next();
    } catch (NoSuchElementException e) {
      log.error("Either both of them or none of them exist in database!");
    }
    if (null != subNode && null != objNode) {
      Edge watched = subNode.addEdge("watched", objNode);
      watched.property("rating", edge.getRating());
      watched.property("timestamp", edge.getTimestamp());

      mgmt.commit();
      g.tx().commit();
      g.close();
    }
  }

  public List<Map<Object, Object>> getVertexProperties(VertexPropsReqDTO dto) {

    GraphTraversal<Vertex, Vertex> node = null;
    GraphTraversalSource g = graph.traversal();
    if (null == dto.getMovieId()) {
      node = g.V().has("user_id", dto.getUserId());
    } else {
      node = g.V().has("movie_id", dto.getMovieId());
    }

    return node.local(__.properties().group().by(__.key()).by(__.value()))
        .dedup()
        .toList();
  }

  public List<Map<Object, Object>> getEdgeProperties(EdgePropsReqDTO dto) {
    GraphTraversalSource g = graph.traversal();

    GraphTraversal<Vertex, Edge> node =
        g.V().has("user_id", dto.getUserId()).outE()
            .where(__.inV().has("movie_id", dto.getMovieId()));

    return node.local(__.properties().group().by(__.key()).by(__.value()))
        .dedup()
        .toList();
  }

  public List<Map<Object, Object>> getMovieDetails(MovieByNameReqDTO dto) {

    GraphTraversalSource g = graph.traversal();

    List<Map<Object, Object>> node = g.V().has("title", dto.getMovieName().toLowerCase())
        .local(__.properties().group().by(__.key()).by(__.value()))
        .dedup()
        .toList();

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
