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
	public static void loadSchema(JanusGraph graph) {
		graph.tx().rollback(); // Never create new indexes while a transaction is active
		JanusGraphManagement mgmt = graph.openManagement();
		try {
			// Vertex Labels
			mgmt.makeVertexLabel("Movie").make();
			mgmt.makeVertexLabel("User").make();

			// Edge Labels
			mgmt.makeEdgeLabel("Watched").multiplicity(Multiplicity.SIMPLE).make();

			// Properties key for movie Vertex Label
			final PropertyKey movieId = mgmt.makePropertyKey("movieId").dataType(String.class).make();
			mgmt.makePropertyKey("genres").dataType(String.class).cardinality(Cardinality.SET).make();
			mgmt.makePropertyKey("title").dataType(String.class).make();
			mgmt.makePropertyKey("imdbId").dataType(String.class).make();
			mgmt.makePropertyKey("tmdbId").dataType(String.class).make();
			
			JanusGraphManagement.IndexBuilder movieIdIndexBuilder = mgmt.buildIndex("movieId", Vertex.class).addKey(movieId);
			movieIdIndexBuilder.unique();
			JanusGraphIndex movieIdIndex = movieIdIndexBuilder.buildCompositeIndex();
			mgmt.setConsistency(movieIdIndex, ConsistencyModifier.LOCK);

			// Properties key for Watched Edge Label
			final PropertyKey userId = mgmt.makePropertyKey("userId").dataType(String.class).make();
			mgmt.makePropertyKey("tag").dataType(String.class).make();
			mgmt.makePropertyKey("tagsTimestamp").dataType(String.class).make();
			mgmt.makePropertyKey("rating").dataType(String.class).make();
			mgmt.makePropertyKey("ratingTimestamp").dataType(String.class).make();
			
			JanusGraphManagement.IndexBuilder userIdIndexBuilder = mgmt.buildIndex("userId", Vertex.class).addKey(userId);
			userIdIndexBuilder.unique();
			JanusGraphIndex userIdIndex = userIdIndexBuilder.buildCompositeIndex();
			mgmt.setConsistency(userIdIndex, ConsistencyModifier.LOCK);

			mgmt.commit();
		} catch (SchemaViolationException e) {
			log.warn("Schema already created!");
		}
	}
}
