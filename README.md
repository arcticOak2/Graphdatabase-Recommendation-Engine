# Recommending Engine

Expectation
---

1. Based on viewed movies, the system should recommend unwatched movies to the user
2. Backend should use MovieLens data for recommendation
3. Backend should use Janusgraph to store data
4. Backend should provide different strategies for recommendation

Download MovieLens data from this link <https://grouplens.org/datasets/movielens/>

Pre-Requisites
---

This Dropwizard project is based on storing data to graph database (Janusgraph) and query to get data out of it. It executes logics for different recommendation strategies. Right now, the project just has ETL capabilities with Janusgraph graph and I'm working on materializing recommendation strategies to get recommendation efficiently.

We need to prepare data for Janusgraph graph. For this purpose, I used Apache Spark shell. We can find the Spark Scala code down below. We can use the same chunk of code in Spark shell for data preparation. For the sake of simplicity, I uploaded those files in Google Drive. 

ratings.csv   -----> <https://drive.google.com/file/d/1pSwZIl4YymTQK38Kj7JMr-fAgKa7IPhg/view?usp=sharing>

fullMovie.csv -----> <https://drive.google.com/file/d/1OguNBtmL_LgzWYW8lykfCr3cLMdM1YvB/view?usp=sharing>

users.csv     -----> <https://drive.google.com/file/d/15mQs9ie4GJP6OH3rtagPXcnYthdo3xAf/view?usp=sharing>

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

Once you get the data put it inside `src/main/resources/`. Make sure the name is same as the file name I mentioned above, while giving the google drive link.

Make sure **Apache Cassandra** and **Elasticsearch** is running in the background. If you're running Cassandra or Elasticsearch on the cloud then, configure it in the file `config/janusgraph-cql-es.properties`.

Now, we are all set to start.

Step 1: Load data into Janusgraph
___

We need to start the main method inside `org.annihilator.recommendation.core.LoadMovieLensData.java`.
**NOTE**: By running this file it will first clear all the data from your Janusgraph. 
This will upload all the Vertex (User and Movie) and Edges. It will take almost 5 hours to upload the complete data. As there are almost 20 million relations are there.

Step 2: Start the API
___

To start the API we need to run `org.annihilator.recommendation.controller.RecommendationEngineController.java` with argument parameter server `config/engine.yaml`. For now, there are just four apis to interact with Janusgraph graph.

POST    /janusEngine/addEdge (org.annihilator.recommendation.controller.RecommendingEngineController)

POST    /janusEngine/addVertex (org.annihilator.recommendation.controller.RecommendingEngineController)

GET     /janusEngine/getEdgeProperties (org.annihilator.recommendation.controller.RecommendingEngineController)

GET     /janusEngine/getVertexProperties (org.annihilator.recommendation.controller.RecommendingEngineController)

* POST    /janusEngine/addEdge -> Using this API we can directly add edge:

Example Body: 

```
{
	"userId": "28",
	"movieId": "29",
	"rating": "4",
	"timestamp": "24th Aug, 2019"
} 
```

* /janusEngine/addVertex  -> Using this API we can directly add vertex:

Example Body:

```
{
	"id": "28",
	"type": "user"
}
```

* GET		/janusEngine/getVertexProperties -> Using this API we can fetch all the Vertex properties, like movie name, movie imdbid, etc.

Example Body:

```
{	
	"movieId": "56801"
}
```
or

```
{		
	"userId": "56801"
}
```

* GET     /janusEngine/getVertexProperties  -> Using this API we can fetch all the Edge properties, like movie rating given by an user.

Example Body:

```
{
	"userId": "28",
	"movieId": "23348"
}
```

In future I'll add APIs to get recommendation. Currently I'm working on it.

CONFIGURATIONS
---

All the configurations related with Janusgraph are in the file `config/janusgraph-cql-es.properties`. We can change the configuration related to Janusgraph. Application level configuration, we can change it in the file `config/engine.yaml`. API Log level we can change it in `config.yml` in the root directory.

USAGE
---

This project can be used for the movie recommendation and by the following same kind of strategy, we can build a recommendation engine for any kind of data.

ABOUT ME
---

I'm a Distributed System Engineer. 

Checkout my social profiles for more information:

Website: <https://arcticoak2.github.io/>

Linkedin: <https://www.linkedin.com/in/abhijeet-kumar-983b57a4/>


LICENSE
---

Apache License
                         
Version 2.0, January 2004
                        
https://www.apache.org/licenses/
