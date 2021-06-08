package com.yen.streamKafkaToHDFS

/**
 *  Spark stream process Kafka event WITH case class
 *  1) print in console
 *  2) sink (save) to HDFS WITH DIFFERENT TOPICS
 *  3) sink (save) to HDFS with bzip2 compression WITH DIFFERENT TOPICS
 *
 * // event source :
 * // https://github.com/yennanliu/KafkaSparkPoc/blob/main/kafka/src/main/scala/com/yen/Producer/producerV3.scala
 */

import org.apache.log4j.Logger
import org.apache.spark.sql.SparkSession

object streamKafkaToHDFSV3 {

  @transient lazy val logger: Logger = Logger.getLogger(getClass.getName)

  val spark = SparkSession
    .builder
    .appName(this.getClass.getName)
    .master("local[*]")
    .config("spark.sql.warehouse.dir", "/temp") // Necessary to work around a Windows bug in Spark 2.0.0; omit if you're not on Windows.
    .getOrCreate()

  // kafka config
  val bootStrapServers = "127.0.0.1:9092"
  // digest event from a list of topics
  val topic = "raw_data_1,raw_data_2,raw_data_3"

  // subscribe to topic
  val streamDF = spark
    .readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", bootStrapServers)
    .option("subscribe",topic) // subscribe multiple topics : https://spark.apache.org/docs/2.2.0/structured-streaming-kafka-integration.html
    .load()

//
//  val tmpStreamDF = streamDF.selectExpr(
//    "topic", // select topic as col
//    "CAST(key AS STRING)",
//    "CAST(value AS STRING)"
//  )
}
