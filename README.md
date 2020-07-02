# Recommending Engine

Current Capabilities
---

1. Backend is capable of loading data in bulk 
2. Backend is open to add more users and movies to the database
3. Backend is capable to search movies (by movie name and movie id) to show its properties
4. Backend is capable to search user (by user id) to show its properties


Future Plan
---

1. Based on viewed movies, the system should recommend unwatched movies to the user

Download raw data
---

Download MovieLens data from this link <https://grouplens.org/datasets/movielens/>

Pre-Requisites
---
To ingest data to the backend, we need to format it. To directly download the data, refer to the drive link mentioned below.

ratings.csv   -----> <https://drive.google.com/file/d/1pSwZIl4YymTQK38Kj7JMr-fAgKa7IPhg/view?usp=sharing>

fullMovie.csv -----> <https://drive.google.com/file/d/1OguNBtmL_LgzWYW8lykfCr3cLMdM1YvB/view?usp=sharing>

users.csv     -----> <https://drive.google.com/file/d/15mQs9ie4GJP6OH3rtagPXcnYthdo3xAf/view?usp=sharing>

If you're curious to know how I got this formatted, please refer the information given below. I had used Spark (spark-shell) for this purpose.

**Merge movies.csv with links.csv**
___

`/opt/spark/spark-shell --master local` (In my system spark is installed inside /opt. So, change it with yours!)


```
val movies = spark.read.format("csv").option("header", "true").option("inferSchema", true).load("/path/to/movies.csv")
val links = spark.read.format("csv").option("header", "true").option("inferSchema", true).load("/path/to/links.csv")
movies.show()
links.show()
val joinedMovieInfo = movies.join(links, Seq("movieId"), "inner")
joinedMovieInfo.show()
/**
 * Movies name can contain ',' so, using csv format is not a good option. I'm saving it using semicolon separated value.
 */
val ssvWithHeaderOptions: Map[String, String] = Map(("delimiter", ";"), ("header", "true"))
joinedMovieInfo.write.options(tsvWithHeaderOptions).csv("path/to/store/ssv")
```


**To get data for UserId**
___
```
val ratings = spark.read.format("csv").option("header", "true").option("inferSchema", true).load("/path/to/ratings.csv")
val users = ratings.select("userId")
val user = users.dropDuplicates()
user.coalesce(1).write.csv("path/to/write/users.csv")
```
---

Once you get the data put it inside `src/main/resources/`. Make sure the name is same as the file name mentioned above in drive link.

Make sure **Apache Cassandra** and **Elasticsearch** is running in the background. If you're running Cassandra or Elasticsearch on the cloud then, configure it in the file `config/janusgraph-cql-es.properties`.

Now, we are all set to start.

Step 1: Load data into Janusgraph
___

We need to start the main method inside `org.annihilator.recommendation.core.LoadMovieLensData.java`.
**NOTE**: By running this file it will first clear all the data from your Janusgraph. 
This will upload all the Vertex (User and Movie) and Edges. It will take almost 5 hours to upload the complete data (20 Million relations).

Step 2: Start the API
___

To start the API run `org.annihilator.recommendation.controller.RecommendationEngineController.java` with argument parameter `server config/engine.yaml`.

```
POST    /janus_engine/add_edge (org.annihilator.recommendation.controller.RecommendingEngineController)
POST    /janus_engine/add_movie (org.annihilator.recommendation.controller.RecommendingEngineController)
POST    /janus_engine/add_user (org.annihilator.recommendation.controller.RecommendingEngineController)
POST    /janus_engine/get_edge_properties (org.annihilator.recommendation.controller.RecommendingEngineController)
POST    /janus_engine/get_movie_details_by_name (org.annihilator.recommendation.controller.RecommendingEngineController)
POST    /janus_engine/get_vertex_properties (org.annihilator.recommendation.controller.RecommendingEngineController)
POST    /janus_engine/purge (org.annihilator.recommendation.controller.RecommendingEngineController)
(Work in Progress)POST    /janus_engine/get_similar_movies (org.annihilator.recommendation.controller.RecommendingEngineController)
```

Postman Collection
---

For more information download Postman collection and export it to your Postman console

<https://www.getpostman.com/collections/4c2fd9093cd8b8dc234f>

CONFIGURATIONS
---

All the configurations related with Janusgraph are in the file `config/janusgraph-cql-es.properties`. Application level configuration, find it in the file `config/engine.yaml`. API Log level we can change it in `config.yml` in the root directory.

USAGE
---

This project can be used for the recommendation, specially for study and research purposes. I always wanted to have a powerful rule based recommendation engine, so here the journey begins. 

ABOUT ME
---

I love coming up with elegant simple solutions to solve the most complex problems. I find joy and satisfaction in building a solution, that is intuitive and simple on its surface and yet complex and intelligent at its core. 

Checkout my social profiles for more information:

Website: <https://arcticoak2.github.io/>

Linkedin: <https://www.linkedin.com/in/abhijeet-kumar-983b57a4/>


LICENSE
---

Apache License
                         
Version 2.0, January 2004
                        
https://www.apache.org/licenses/
