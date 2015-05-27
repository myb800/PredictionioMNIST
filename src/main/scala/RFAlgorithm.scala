package org.template.recommendation

import io.prediction.controller.PAlgorithm
import io.prediction.controller.Params
import io.prediction.controller.IPersistentModel
import io.prediction.controller.IPersistentModelLoader
import io.prediction.data.storage.BiMap

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.classification.LogisticRegressionWithSGD
import org.apache.spark.mllib.classification.LogisticRegressionModel
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import grizzled.slf4j.Logger


case class LRAlgorithmParams(
  numTrees: Int,
  depth: Double) extends Params

class RFAlgorithm(val ap: LRAlgorithmParams)
  extends PAlgorithm[PreparedData, LRModel, Query, PredictedResult] {

  @transient lazy val logger = Logger[this.type]

  def train(sc: SparkContext, data: PreparedData): LRModel = {
    // MLLib ALS cannot handle empty training data.
    require(!data.images.take(1).isEmpty,
      s"RDD[Rating] in PreparedData cannot be empty." +
      " Please check if DataSource generates TrainingData" +
      " and Preprator generates PreparedData correctly.")
    // Convert user and item String IDs to Int index for MLlib
    LRModel(
      LogisticRegressionWithSGD.train(data.images.map{ i => LabeledPoint(i.label,Vectors.dense(i.image)) },100)
    )
    
  }

  def predict(model: LRModel, query: Query): PredictedResult = {
    // Convert String ID to Int index for Mllib
    new PredictedResult(0)
  }

}
case class LRModel(
  lr: LogisticRegressionModel)
  extends Serializable
  with IPersistentModel[LRAlgorithmParams] {

  def save(id: String, params: LRAlgorithmParams,
    sc: SparkContext): Boolean = {
    sc.parallelize(Seq(lr)).saveAsObjectFile(s"/tmp/${id}/lr")
    true
  }
}

object LRModel
  extends IPersistentModelLoader[LRAlgorithmParams, LRModel] {
  def apply(id: String, params: LRAlgorithmParams,
    sc: Option[SparkContext]) = {
    new LRModel(
      //sc.get.objectFile[Model](s"/tmp/${id}/model").first,
      sc.get.objectFile[LogisticRegressionModel](s"/tmp/${id}/lr").first
    )
  }
}