package SparkStreamTwitt

import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

object MostPopHtag extends App {

  /** Makes sure only ERROR messages get logged to avoid log spam. */
  def setupLogging() = {
    import org.apache.log4j.{Level, Logger}
    val rootLogger = Logger.getRootLogger()
    rootLogger.setLevel(Level.ERROR)
  }

  /** Configures Twitter service credentials using twiter.txt in the main workspace directory */
  def setupTwitter() = {
    import scala.io.Source

    for (line <- Source.fromFile("twitter.properties").getLines) {
      val fields = line.split(" = ")
      if (fields.length == 2) {
        System.setProperty("twitter4j.oauth." + fields(0), fields(1))
      }
    }
  }

  def printFilesInCurrDir() : Unit = {
    for (file <- (new java.io.File (".") ).listFiles)
      println (file)
  }

  /** Our main function */
  override def main(args: Array[String]) {
    // Configure Twitter credentials using twitter.properties
    setupTwitter()

    // Set up a Spark streaming context named "PopularHashtags" that runs locally using
    // all CPU cores and 10-second batches of data
    val ssc = new StreamingContext("local[*]", "PopularHashtags", Seconds(10))

    // Get rid of log spam (should be called after the context is set up)
    setupLogging()

    // Create a DStream from Twitter using our streaming context
    val tweets = TwitterUtils.createStream(ssc, None)

    // Now extract the text of each status update into DStreams using map()
    val statuses = tweets.map(status => status.getText())

    // split each tweet to single words
    val tweetwords = statuses.flatMap(tweetText => tweetText.split(" "))

    // Now eliminate anything that's not a hashtag
    val hashtags = tweetwords.filter(word => word.startsWith("#"))

    // Map each hashtag to a key/value pair of (hashtag, 1) so we can count them up by adding up the values
    val hashtagKeyValues = hashtags.map(hashtag => (hashtag, 1))

    // Now count them up over a 30 minute window sliding every 30s
    val hashtagCounts = hashtagKeyValues
      .reduceByKeyAndWindow( _ + _, _ - _, Seconds(1800), Seconds(60))

    // Sort the results by the count values
    val sortedResults = hashtagCounts.transform(rdd => rdd.sortBy(x => x._2, false))

    // Print the top 10
    sortedResults.print

    // Set a checkpoint directory, and kick it all off
    ssc.checkpoint("checkpoint/")
    ssc.start()
    ssc.awaitTermination()
    }
  }
