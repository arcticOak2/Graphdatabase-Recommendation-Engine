package org.annihilator.recommendation.db;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.annihilator.recommendation.schema.LoadSchema;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.JanusGraphTransaction;
import org.janusgraph.core.JanusGraphVertex;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.diskstorage.BackendException;
import org.json.JSONObject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JanusClient {

	/**
	 * 
	 * This class is to upload data in bulk to the JanusGraph. Enabling the
	 * storage.batch-loading configuration option will have the biggest positive
	 * impact on bulk loading times for most applications. So, make sure you enabled
	 * this option in Janus configuration.
	 * 
	 * Author: Abhijeet Kumar, Distributed Systems Engineer
	 * 
	 */

	JanusGraph graph = JanusGraphFactory.open("config/janusgraph-cql-es.properties");

	public JanusClient() {
		LoadSchema.loadSchema(graph);
	}

	public boolean addNode(String json) throws Exception {

		JSONObject jsonObject = new JSONObject(json);

		/**
		 * There can be two types of json:
		 * 
		 * 1. Movie a. genres b. title c. movieId d. imdbId e. tmdbId
		 * 
		 * 2. User a. userId
		 */

		String typeOfVertex = jsonObject.getString("type");
		JanusGraphTransaction tx = graph.newTransaction();
		String id = jsonObject.getString("id");

		if (typeOfVertex.equals("movie")) {
			String genres = jsonObject.getString("genres");
			String[] genresList = genres.split("\\|");
			String title = jsonObject.getString("title");
			String imdbId = jsonObject.getString("imdbId");
			String tmdbId = jsonObject.getString("tmdbId");

			tx.addVertex(T.label, "genres", genresList, "Movie", "title", title, "id", id, "imdbId", imdbId,
					"tmdbId", tmdbId);

			tx.commit();

			return true;
		} else if (typeOfVertex.equals("user")) {
			tx.addVertex(T.label, "User", "id", id);
			tx.commit();

			return true;
		} else {
			log.error("Unrecognized type of vertex");

			return false;
		}
	}

	public boolean addEdge(String json) throws Exception {
		JSONObject jsonObject = new JSONObject(json);

		String userId = jsonObject.getString("userId");
		String movieId = jsonObject.getString("movieId");
		String tags = jsonObject.getString("tags");
		String tagsTimestamp = jsonObject.getString("tagsTimestamp");
		String rating = jsonObject.getString("rating");
		String ratingTimestamp = jsonObject.getString("ratingTimestamp");

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
			watched.property("ratingTimestamp", ratingTimestamp);
			watched.property("tags", tags);
			watched.property("tagsTimestamp", tagsTimestamp);
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

	public List<Map<String, Object>> getVertexAllProperties(String json) {
		JSONObject jsonObject = new JSONObject(json);

		String id = jsonObject.getString("id");
		GraphTraversalSource g = graph.traversal();

		GraphTraversal<Vertex, Vertex> node = g.V().has("id", id);

		return node.valueMap().toList();
	}

	// CAUTION:
	// This method is not recommended to have in production environment
	// This method will delete all the data in JanusGraph database
	public void purgeJanus() throws BackendException {

		JanusGraphFactory.drop(graph);
		graph = null;

		graph = JanusGraphFactory.open("config/janusgraph-cql-es.properties");
	}
}
