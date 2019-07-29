package org.annihilator.recommendation.schema;

import org.apache.tinkerpop.gremlin.structure.Vertex;
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

			// Common property between movie and user
			final PropertyKey id = mgmt.makePropertyKey("id").dataType(String.class).make();
			JanusGraphManagement.IndexBuilder idIndexBuilder = mgmt.buildIndex("id", Vertex.class).addKey(id);
			idIndexBuilder.unique();
			JanusGraphIndex idIndex = idIndexBuilder.buildCompositeIndex();
			mgmt.setConsistency(idIndex, ConsistencyModifier.LOCK);

			// Vertex Labels
			mgmt.makeVertexLabel("Movie").make();
			mgmt.makeVertexLabel("User").make();

			// Edge Labels
			mgmt.makeEdgeLabel("Watched").multiplicity(Multiplicity.SIMPLE).make();

			// Properties key for movie Vertex Label
			mgmt.makePropertyKey("genres").dataType(String.class).make();
			mgmt.makePropertyKey("title").dataType(String.class).make();
			mgmt.makePropertyKey("imdbId").dataType(String.class).make();
			mgmt.makePropertyKey("tmdbId").dataType(String.class).make();

			// Properties key for Watched Edge Label
			mgmt.makePropertyKey("tag").dataType(String.class).make();
			mgmt.makePropertyKey("tagsTimestamp").dataType(String.class).make();
			mgmt.makePropertyKey("rating").dataType(String.class).make();
			mgmt.makePropertyKey("ratingTimestamp").dataType(String.class).make();

			mgmt.commit();
		} catch (SchemaViolationException e) {
			log.warn("Schema already created!");
		}
	}
}
