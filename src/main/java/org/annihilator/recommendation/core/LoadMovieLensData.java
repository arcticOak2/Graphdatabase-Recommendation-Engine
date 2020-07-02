package org.annihilator.recommendation.core;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.FileReader;
import lombok.extern.slf4j.Slf4j;
import org.annihilator.recommendation.db.JanusClient;
import org.annihilator.recommendation.models.Movie;
import org.annihilator.recommendation.models.Rating;
import org.annihilator.recommendation.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class LoadMovieLensData {

  private static final Logger logger = LoggerFactory.getLogger(LoadMovieLensData.class);

  private static Gson gson = new Gson();
  private static JanusClient client = new JanusClient();

  private static void loadMoviesDataToJanus(String data) throws Exception {
    String[] datas = data.split(";", -1);
    Movie movie = new Movie();

    movie.setId(datas[0]);
    movie.setTitle(datas[1]);
    movie.setGenres(datas[2]);
    movie.setImdbId(datas[3]);
    movie.setTmdbId(datas[4]);

    client.addMovie(movie, true);

  }

  private static void loadUserDataToJanus(String id) throws Exception {
    User user = new User();

    user.setId(id);

    client.addUser(user, true);
  }

  private static void loadRelationDataToJanus(String data) throws Exception {
    Rating rating = new Rating();
    String[] datas = data.split(",", -1);

    try {
      rating.setUserId(datas[0]);
      rating.setMovieId(datas[1]);
      rating.setRating(Integer.parseInt(datas[2]));
      rating.setTimestamp(datas[3]);

      log.info(datas[0] + "----------------->" + datas[1]);

      String json = gson.toJson(rating);
      client.addEdge(rating, false);
    } catch (Exception e) {
      logger.error("error while parsing: " + data);
    }
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
