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
    val imageFile = Files.readAllBytes(Paths.get("/tmp/train-images-idx3-ubyte"))
    val labelFile = Files.readAllBytes(Paths.get("/tmp/train-labels-idx1-ubyte"))
    var images = Seq[Array[Double]]()
    var labels = Seq[Double]()
    var a = 0
    var b = 0
    for(a <- 0 until 60000){
      if(a % 1000 == 0){
        logger.info("a " + a)
      }
      var image = Array[Double]()
      for(b <- 0 until 28 * 28){
        image = image :+ imageFile(a * 28 * 28 + b + 16).toDouble
      }
      images = images :+ image
      labels = labels :+ labelFile(8 + a).toDouble
    }

    new TrainingData(sc.parallelize(labels.zip(images)).map{ case (l,i) => Image(i,l) })
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
