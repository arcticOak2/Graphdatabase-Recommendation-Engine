package org.annihilator.recommendation.core;

import java.io.BufferedReader;
import java.io.FileReader;
import org.annihilator.recommendation.db.JanusClient;
import org.annihilator.recommendation.models.Movie;
import com.google.gson.Gson;

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

	public static void main(String[] args) throws Exception {
		client.purgeJanus();
		FileReader in = new FileReader("src/main/resources/fullMovies.csv");
		BufferedReader br = new BufferedReader(in);
		String line;
		while ((line = br.readLine()) != null) {
			loadMoviesDataToJanus(line);
		}
		
		in = new FileReader("src/main/resources/fullMovies.csv");
		br = new BufferedReader(in);
		while ((line = br.readLine()) != null) {
			loadMoviesDataToJanus(line);
		}
		in.close();
	}
}
