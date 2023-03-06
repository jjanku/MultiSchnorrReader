package com.example.multischnorrreader.crypto

import org.bouncycastle.math.ec.ECPoint
import java.math.BigInteger

operator fun ECPoint.unaryMinus(): ECPoint = negate()
operator fun ECPoint.plus(other: ECPoint): ECPoint = add(other)
operator fun ECPoint.minus(other: ECPoint): ECPoint = subtract(other)

operator fun ECPoint.times(scalar: BigInteger): ECPoint = multiply(scalar)
operator fun BigInteger.times(point: ECPoint): ECPoint = point.multiply(this)
