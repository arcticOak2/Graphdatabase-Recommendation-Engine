# recommending-engine

Expectation
---

1. Based on viewed movies, the system should recommend unwatched movies to the user
2. Backend should use MovieLens for recommendation
3. Backend should use Janusgraph to store data (Janusgraph is an opensource graph database)
4. Backend should provide different strategy for recommendation

Download MovieLens data from this link <https://grouplens.org/datasets/movielens/>

##Merge movies.csv with links.csv

Joining movies.csv with links.csv in java code can be a problem because the files size can more than your heap size. The other problem will be speed. So, I preferred Apache Spark for this. You can use whatever suits you better.

I have used spark-shell for this purpose. After merging the these two files put it inside sr/main/resources folder with the name `fullMovies.csv`

Code:

`/opt/spark/spark-shell --master local` (In my system spark is inside /opt. So, change it with yours!)

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

// To get data for UserId

val ratings = spark.read.format("csv").option("header", "true").option("inferSchema", true).load("/path/to/ratings.csv")
val users = ratings.select("userId")
val user = users.dropDuplicates()
user.coalesce(1).write.csv("path/to/write/users.csv")
```
