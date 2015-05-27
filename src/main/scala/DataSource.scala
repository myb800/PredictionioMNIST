package org.template.recommendation

import io.prediction.controller.PDataSource
import io.prediction.controller.EmptyEvaluationInfo
import io.prediction.controller.EmptyActualResult
import io.prediction.controller.Params
import io.prediction.data.storage.Event
import io.prediction.data.store.PEventStore

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD

import grizzled.slf4j.Logger

import java.nio.file.{Files, Paths}

case class DataSourceEvalParams(kFold: Int, queryNum: Int)

case class DataSourceParams(
  appName: String,
  evalParams: Option[DataSourceEvalParams]) extends Params

class DataSource(val dsp: DataSourceParams)
  extends PDataSource[TrainingData,
      EmptyEvaluationInfo, Query, TrainingData] {

  @transient lazy val logger = Logger[this.type]

  override
  def readTraining(sc: SparkContext): TrainingData = {
    val imageFile = Files.readAllBytes(Paths.get("/tmp/train-images-idx3-ubyte")).slice(16,16 + 28 * 28 * 60000)
    val labelFile = Files.readAllBytes(Paths.get("/tmp/train-labels-idx1-ubyte")).slice(8,60008)
    val imageRDD = sc.parallelize(imageFile.toSeq).zipWithIndex.map{ case (p,i) => (i / (28 * 28),p.toDouble)}
                                                  .groupByKey.map{ case (i,p) => p.toArray}.cache
    val labelRDD = sc.parallelize(labelFile.toSeq)
    new TrainingData(imageRDD.zip(labelRDD).map{ case (i,l) => Image(i,l.toDouble)})
  }

}

case class Image(
  image: Array[Double],
  label: Double
)

class TrainingData(
  val images: RDD[Image]
) extends Serializable {
  override def toString = {
    ""
  }
}
