package org.template.recommendation

import io.prediction.controller.IEngineFactory
import io.prediction.controller.Engine

case class Query(
  user: String,
  num: Int
) extends Serializable

case class PredictedResult(
  num: Int
) extends Serializable


object RecommendationEngine extends IEngineFactory {
  def apply() = {
    new Engine(
      classOf[DataSource],
      classOf[Preparator],
      Map("als" -> classOf[RFAlgorithm]),
      classOf[Serving])
  }
}
