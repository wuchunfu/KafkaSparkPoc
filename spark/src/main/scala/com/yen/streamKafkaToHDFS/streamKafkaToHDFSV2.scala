package com.yen.streamKafkaToHDFS

/**
 *  Spark stream process Kafka event WITH DIFFERENT TOPICS
 *  1) print in console
 *  2) sink (save) to HDFS WITH DIFFERENT TOPICS
 *  3) sink (save) to HDFS with bzip2 compression WITH DIFFERENT TOPICS
 *
 * // event source :
 * // https://github.com/yennanliu/KafkaSparkPoc/blob/main/kafka/src/main/scala/com/yen/Producer/producerV2.scala
 */

import org.apache.hadoop.io.compress.BZip2Codec
import org.apache.log4j.Logger
import org.apache.spark.sql.SparkSession

object streamKafkaToHDFSV2 extends App {

  @transient lazy val logger: Logger = Logger.getLogger(getClass.getName)

  val spark = SparkSession
    .builder
    .appName(this.getClass.getName)
    .master("local[*]")
    .config("spark.sql.warehouse.dir", "/temp") // Necessary to work around a Windows bug in Spark 2.0.0; omit if you're not on Windows.
    .getOrCreate()

  import spark.implicits._

  // kafka config
  val bootStrapServers = "127.0.0.1:9092"
  // digest event from a list of topics
  val topic = "raw_data_1,raw_data_2,raw_data_3"

  // subscribe to topic
  val streamDF = spark
    .readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", bootStrapServers)
    .option("subscribe",topic) // https://spark.apache.org/docs/2.2.0/structured-streaming-kafka-integration.html
    .load()

  val tmpStreamDF = streamDF.selectExpr("CAST(value AS STRING)")

  val ToStreamDF = tmpStreamDF
    .select("value")

  ToStreamDF.createOrReplaceTempView("to_stream")

  /** V1 : print out in console */
  //    val query = ToStreamDF.writeStream
  //      .format("console")
  //      .option("checkpointLocation", "chk-point-dir2")
  //      .start()

  /** V2 : save into HDFS */
  val query = ToStreamDF.writeStream
        .format("json")
        .option("checkpointLocation", "chk-point-dir2")
        .option("path", s"streamKafkaToHDFSV1/$topic")
        .start()

  /** V3 : save into HDFS with compression */
  //  val query = ToStreamDF.writeStream
  //    .format("json")
  //    .option("checkpointLocation", "chk-point-dir2")
  //    .option("path", "streamKafkaToHDFSV1")
  //    .option("compression", "org.apache.hadoop.io.compress.BZip2Codec") // https://sites.google.com/a/einext.com/einext_original/apache-spark/compress-output-files-in-spark
  //    .start()

  logger.info("Listen from Kafka : 127.0.0.1:9092")

  query.awaitTermination()
}
