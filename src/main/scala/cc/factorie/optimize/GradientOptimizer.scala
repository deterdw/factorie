package cc.factorie.optimize

import cc.factorie._
import cc.factorie.la._

/** Repeatedly call "step" until "isConverged" is true. */
trait GradientOptimizer {
  //def step(gradient:Tensor, value:Double, margin:Double): Unit
  // TODO Why are we passing in weightsSet each time?  Couldn't this be dangerous?  -akm
  def step(weights: WeightsSet, gradient: WeightsMap, value:Double): Unit
  def isConverged: Boolean
  def reset(): Unit
}

/** Include L2 regularization (Gaussian with given scalar as the diagonal covariance) in the gradient and value. */
trait L2Regularization extends GradientOptimizer {
  var variance: Double = 10.0
  abstract override def step(weights: WeightsSet, gradient: WeightsMap, value:Double) {
    gradient += (weights, -1 / variance)
    // TODO: should this be -0.5 / variance * (weightsSet dot weightsSet)? -luke
    super.step(weights, gradient, value - 1 / variance * (weights dot weights))
  }
}