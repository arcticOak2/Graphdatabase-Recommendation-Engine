package org.annihilator.recommendation.schema;

import lombok.extern.slf4j.Slf4j;
import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.Cardinality;
import org.janusgraph.core.EdgeLabel;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.Multiplicity;
import org.janusgraph.core.PropertyKey;
import org.janusgraph.core.SchemaViolationException;
import org.janusgraph.core.schema.ConsistencyModifier;
import org.janusgraph.core.schema.JanusGraphIndex;
import org.janusgraph.core.schema.JanusGraphManagement;

@Slf4j
public class LoadSchema {

  public static void loadSchema(JanusGraph graph) {
    graph.tx().rollback(); // Never create new indexes while a transaction is active
    JanusGraphManagement janusGraphManagement = graph.openManagement();
    try {

      // Vertex properties keys

      final PropertyKey movieId = janusGraphManagement
          .makePropertyKey("movie_id")
          .dataType(String.class)
          .cardinality(Cardinality.SINGLE)
          .make();

      final PropertyKey genres = janusGraphManagement
          .makePropertyKey("genres")
          .dataType(String.class)
          .cardinality(Cardinality.SET)
          .make();

      final PropertyKey title = janusGraphManagement
          .makePropertyKey("title")
          .dataType(String.class)
          .cardinality(Cardinality.SINGLE)
          .make();

      final PropertyKey imdbId = janusGraphManagement
          .makePropertyKey("imdb_id")
          .dataType(String.class)
          .cardinality(Cardinality.SINGLE)
          .make();

      final PropertyKey tmdbId = janusGraphManagement
          .makePropertyKey("tmdb_id")
          .dataType(String.class)
          .cardinality(Cardinality.SINGLE)
          .make();

      final PropertyKey userId = janusGraphManagement
          .makePropertyKey("user_id")
          .dataType(String.class)
          .cardinality(Cardinality.SINGLE)
          .make();

      // Edge property keys

      final PropertyKey rating = janusGraphManagement
          .makePropertyKey("rating")
          .dataType(Integer.class)
          .cardinality(Cardinality.SINGLE)
          .make();

      final PropertyKey timestamp = janusGraphManagement
          .makePropertyKey("timestamp")
          .dataType(String.class)
          .cardinality(Cardinality.SINGLE)
          .make();

      // Indexing for vertices

      JanusGraphIndex movieIdIndex = janusGraphManagement.buildIndex("movie_id", Vertex.class)
          .addKey(movieId).unique().buildCompositeIndex();
      JanusGraphIndex titleIndex = janusGraphManagement.buildIndex("title", Vertex.class)
          .addKey(title).buildCompositeIndex();
      JanusGraphIndex imdbIdIndex = janusGraphManagement.buildIndex("imdb_id", Vertex.class)
          .addKey(imdbId).unique().buildCompositeIndex();
      JanusGraphIndex tmdbIdIndex = janusGraphManagement.buildIndex("tmdb_id", Vertex.class)
          .addKey(tmdbId).unique().buildCompositeIndex();
      JanusGraphIndex userIdIndex = janusGraphManagement.buildIndex("user_id", Vertex.class)
          .addKey(userId).unique().buildCompositeIndex();
      JanusGraphIndex genresIndex = janusGraphManagement.buildIndex("genres", Vertex.class)
          .addKey(genres).buildCompositeIndex();

      janusGraphManagement.setConsistency(movieIdIndex, ConsistencyModifier.LOCK);
      janusGraphManagement.setConsistency(imdbIdIndex, ConsistencyModifier.LOCK);
      janusGraphManagement.setConsistency(tmdbIdIndex, ConsistencyModifier.LOCK);
      janusGraphManagement.setConsistency(userIdIndex, ConsistencyModifier.LOCK);

      janusGraphManagement.makeVertexLabel("MOVIE").make();
      janusGraphManagement.makeVertexLabel("USER").make();

      // Indexing for edges

      JanusGraphIndex ratingIndex = janusGraphManagement.buildIndex("rating", Edge.class).addKey(rating).buildCompositeIndex();

      EdgeLabel watched = janusGraphManagement.makeEdgeLabel("watched")
          .multiplicity(Multiplicity.SIMPLE).make();
      janusGraphManagement.buildEdgeIndex(watched, "watched", Direction.BOTH, Order.desc, rating);

      janusGraphManagement.commit();
    } catch (SchemaViolationException e) {
      log.warn("Error while creating schema", e);
    }
  }
}
