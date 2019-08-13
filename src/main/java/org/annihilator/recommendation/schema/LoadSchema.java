package org.annihilator.recommendation.schema;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.Cardinality;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.Multiplicity;
import org.janusgraph.core.PropertyKey;
import org.janusgraph.core.SchemaViolationException;
import org.janusgraph.core.schema.ConsistencyModifier;
import org.janusgraph.core.schema.JanusGraphIndex;
import org.janusgraph.core.schema.JanusGraphManagement;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoadSchema {

	private static String MIXED_INDEX_NAME = "MovieRecommendationEngine";

	public static void loadSchema(JanusGraph graph) {
		graph.tx().rollback(); // Never create new indexes while a transaction is active
		JanusGraphManagement mgmt = graph.openManagement();
		try {
			// Properties key for movie Vertex Label
			final PropertyKey movieId = mgmt.makePropertyKey("movieId").dataType(String.class).make();
			final PropertyKey genres = mgmt.makePropertyKey("genres").dataType(String.class)
					.cardinality(Cardinality.SET).make();
			final PropertyKey title = mgmt.makePropertyKey("title").dataType(String.class).make();
			final PropertyKey imdbId = mgmt.makePropertyKey("imdbId").dataType(String.class).make();
			final PropertyKey tmdbId = mgmt.makePropertyKey("tmdbId").dataType(String.class).make();

			// Properties key for Watched Edge Label
			final PropertyKey userId = mgmt.makePropertyKey("userId").dataType(String.class).make();
			final PropertyKey rating = mgmt.makePropertyKey("rating").dataType(String.class).make();
			final PropertyKey timestamp = mgmt.makePropertyKey("timestamp").dataType(String.class).make();
			
			JanusGraphManagement.IndexBuilder movieIdIndexBuilder = mgmt.buildIndex("movieId", Vertex.class).addKey(movieId);
			JanusGraphManagement.IndexBuilder titleIndexBuilder = mgmt.buildIndex("title", Vertex.class).addKey(title);
			JanusGraphManagement.IndexBuilder imdbIdIndexBuilder = mgmt.buildIndex("imdbId", Vertex.class).addKey(imdbId);
			JanusGraphManagement.IndexBuilder tmdbIdIndexBuilder = mgmt.buildIndex("tmdbId", Vertex.class).addKey(tmdbId);
			JanusGraphManagement.IndexBuilder userIdIndexBuilder = mgmt.buildIndex("userId", Vertex.class).addKey(userId);
			
			movieIdIndexBuilder.unique();
			imdbIdIndexBuilder.unique();
			tmdbIdIndexBuilder.unique();
			userIdIndexBuilder.unique();
			
			JanusGraphIndex movieIndex = movieIdIndexBuilder.buildCompositeIndex();
			JanusGraphIndex titleIndex = titleIndexBuilder.buildCompositeIndex();
			JanusGraphIndex imdbIndex = imdbIdIndexBuilder.buildCompositeIndex();
			JanusGraphIndex tmdbIndex = tmdbIdIndexBuilder.buildCompositeIndex();
			JanusGraphIndex userIndex = userIdIndexBuilder.buildCompositeIndex();
			
			mgmt.setConsistency(movieIndex, ConsistencyModifier.LOCK);
			mgmt.setConsistency(titleIndex, ConsistencyModifier.LOCK);
			mgmt.setConsistency(imdbIndex, ConsistencyModifier.LOCK);
			mgmt.setConsistency(tmdbIndex, ConsistencyModifier.LOCK);
			mgmt.setConsistency(userIndex, ConsistencyModifier.LOCK);
			
			mgmt.makeVertexLabel("Movie").make();
			mgmt.makeVertexLabel("User").make();
			
			mgmt.makeEdgeLabel("Watched").multiplicity(Multiplicity.SIMPLE).make();
			
			mgmt.commit();
		} catch (SchemaViolationException e) {
			log.warn("Schema already created!");
		}
	}
}
