package com.zypex.piflow.profile

import utils.math.*
import kotlin.math.abs

class Linear @JvmOverloads internal constructor(function: SingleBoundedFunction<Derivatives<Vector>>, length: Double = function.upper.position.subtract(function.lower.position).magnitude) : SingleProfileSegment(function, length) {
    //    For the format of ax^3 + bx^2 + cx + d
    private val a: Double
    private val b: Double
    private val c: Double
    private val d: Double
    private val dir: Vector

    init {
        d = function(0.0).position.magnitude
        c = function(0.0).velocity.magnitude
        b = function(0.0).acceleration.magnitude
        a = function(0.0).jerk.magnitude
        dir = function.upper.position.subtract(function.lower.position).normalize()
    }

    override fun getT(pos: Vector): Double {
        val initial = function(0.0).position
        val lower: Double = function.lower.position.subtract(initial).dot(dir.dotInverse())
        val upper: Double = function.upper.position.subtract(initial).dot(dir.dotInverse())
        val rawT = dir.dot(pos.subtract(initial))
        return if (rawT <= lower) lowerBound() else if (rawT >= upper) upperBound() else solve(rawT)
    }

    //    Modified version of the newton's method
    private fun solve(output: Double): Double {
        val initialGuess = (upperBound() + lowerBound()) / 2
        var lastGuess: Double
        var guess = initialGuess
        var error = -1.0
        val function: (Double) -> Double = {a * it * it * it + b * it * it + c * it + d }
        val derivative: (Double) -> Double = {3 * a * it * it + 2 * b * it + c }
        while (error < 0 || error > 1e-15) {
            lastGuess = guess
            guess += (output - function(guess)) / derivative(guess)
            error = abs(guess - lastGuess)
        }
        return guess
    }

    override fun invoke(input: Double): Derivatives<Vector> = function(input)

    override fun upperBound(): Double = function.upperBound()

    override fun lowerBound(): Double = function.lowerBound()

    override fun offset(offset: Double): Linear = Linear(function.offset(offset))
}