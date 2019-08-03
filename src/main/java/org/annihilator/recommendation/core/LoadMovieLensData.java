package org.annihilator.recommendation.core;

import java.io.BufferedReader;
import java.io.FileReader;
import org.annihilator.recommendation.db.JanusClient;
import org.annihilator.recommendation.models.Movie;
import org.annihilator.recommendation.models.Rating;
import org.annihilator.recommendation.models.User;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoadMovieLensData {

	static Gson gson = new Gson();
	static JanusClient client = new JanusClient();

	private static void loadMoviesDataToJanus(String data) throws Exception {
		String[] datas = data.split(";", -1);
		Movie movie = new Movie();
		
		movie.setId(datas[0]);
		movie.setTitle(datas[1]);
		movie.setGenres(datas[2]);
		movie.setImdbId(datas[3]);
		movie.setTmdbId(datas[4]);

		String json = gson.toJson(movie);
		client.addNode(json, true);
		
	}
	
	private static void loadUserDataToJanus(String id) throws Exception {
		User user = new User();
		
		user.setId(id);
		
		String json = gson.toJson(user);
		client.addNode(json, true);
	}
	
	private static void loadRelationDataToJanus(String data) throws Exception {
		Rating rating = new Rating();
		String[] datas = data.split(",", -1);
		
		rating.setUserId(datas[0]);
		rating.setMovieId(datas[1]);
		rating.setRating(datas[2]);
		rating.setTimestamp(datas[3]);
		
		log.info(datas[0] + "----------------->" + datas[1]);
		
		String json = gson.toJson(rating);
		client.addEdge(json, false);
	}

	public static void main(String[] args) throws Exception {
		client.purgeJanus();
		FileReader in = new FileReader("src/main/resources/fullMovies.csv");
		BufferedReader br = new BufferedReader(in);
		String line;
		while ((line = br.readLine()) != null) {
			loadMoviesDataToJanus(line);
		}
		
		in = new FileReader("src/main/resources/users.csv");
		br = new BufferedReader(in);
		while ((line = br.readLine()) != null) {
			loadUserDataToJanus(line);
		}
		
		in = new FileReader("src/main/resources/ratings.csv");
		br = new BufferedReader(in);
		while ((line = br.readLine()) != null) {
			loadRelationDataToJanus(line);
		}
		
		in.close();
		
	}
}
