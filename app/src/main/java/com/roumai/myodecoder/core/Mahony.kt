package com.roumai.myodecoder.core

import kotlin.math.atan2
import kotlin.math.sqrt

data class Point(
    val x: Double,
    val y: Double,
    val z: Double,
) {
    fun norm(): Double {
        return sqrt(x * x + y * y + z * z)
    }

    operator fun div(v: Double): Point {
        return Point(x = x / v, y = y / v, z = z / v)
    }

    fun cross(p: Point): Point {
        return Point(
            x = y * p.z - z * p.y,
            y = z * p.x - x * p.z,
            z = x * p.y - y * p.x,
        )
    }

    operator fun times(p: Point): Double {
        return x * p.x + y * p.y + z * p.z
    }

    operator fun times(v: Double): Point {
        return Point(x = x * v, y = y * v, z = z * v)
    }

    operator fun plus(p: Point): Point {
        return Point(x = x + p.x, y = y + p.y, z = z + p.z)
    }

    operator fun minus(p: Point): Point {
        return Point(x = x - p.x, y = y - p.y, z = z - p.z)
    }

    override fun toString(): String {
        return "(%.3f,\t%.3f,\t%.3f)".format(x, y, z)
    }
}

data class Quaternion(
    val w: Double,
    val i: Double,
    val j: Double,
    val k: Double,
) {
    fun norm(): Double {
        return sqrt(w * w + i * i + j * j + k * k)
    }

    fun conjugate(): Quaternion {
        return Quaternion(w, -i, -j, -k)
    }

    operator fun div(v: Double): Quaternion {
        return Quaternion(w / v, i / v, j / v, k / v)
    }

    operator fun times(q: Quaternion): Quaternion {
        val w0 = q.w
        val x0 = q.i
        val y0 = q.j
        val z0 = q.k
        val w1 = w
        val x1 = i
        val y1 = j
        val z1 = k

        return Quaternion(
            -x1 * x0 - y1 * y0 - z1 * z0 + w1 * w0,
            x1 * w0 + y1 * z0 - z1 * y0 + w1 * x0,
            -x1 * z0 + y1 * w0 + z1 * x0 + w1 * y0,
            x1 * y0 - y1 * x0 + z1 * w0 + w1 * z0
        )
    }

    operator fun times(v: Double): Quaternion {
        return Quaternion(
            w = w * v,
            i = i * v,
            j = j * v,
            k = k * v,
        )
    }

    operator fun plus(q: Quaternion): Quaternion {
        return Quaternion(
            w = w + q.w,
            i = i + q.i,
            j = j + q.j,
            k = k + q.k,
        )
    }

    fun rotationMatrix(): Matrix {
        return Matrix(
            Point(
                1f - 2f * (j * j + k * k),
                2f * (i * j - w * k),
                2f * (i * k + w * j)
            ),
            Point(
                2f * (i * j + w * k),
                1f - 2f * (i * i + k * k),
                2f * (j * k - w * k),
            ),
            Point(
                2f * (i * k - w * j),
                2f * (w * i + j * k),
                1f - 2f * (i * i + j * j),
            ),
        )
    }

    override fun toString(): String {
        return "(%.3f,\t%.3f,\t%.3f,\t%.3f)".format(w, i, j, k)
    }
}

data class Matrix(
    val a: Point,
    val b: Point,
    val c: Point,
) {
    operator fun times(v: Point): Point {
        return Point(
            x = a.x * v.x + a.y * v.y + a.z * v.z,
            y = b.x * v.x + b.y * v.y + b.z * v.z,
            z = c.x * v.x + c.y * v.y + c.z * v.z,
        )
    }

    fun toAngles(): Point {
        val sy = sqrt(a.x * a.x + b.x * b.x)
        val singular = sy < 1e-6
        val x = if (!singular) {
            atan2(c.y, c.z)
        } else {
            atan2(-b.z, b.y)
        }
        val y = atan2(-c.x, sy)
        val z = if (!singular) {
            atan2(b.x, a.x)
        } else {
            0.0
        }
        return Point(x, y, z)
    }
}

class Mahony {
    private var ki = 1.0
    private var kp = 10.0
    private var pidErr = Point(0.0, 0.0, 0.0)
    private var quaternion = Quaternion(1.0, 0.0, 0.0, 0.0)

    fun updateMag(gyro: Point, acc: Point, mag: Point, deltaTime: Double): Quaternion {
        var q = quaternion.copy()
        var g = gyro.copy()
        var a = acc.copy()
        var m = mag.copy()
        val anorm = a.norm()
        val mnorm = m.norm()
        if (anorm < 1e-6 || mnorm < 1e-6) {
            return q
        }
        a /= anorm
        m /= anorm
        var h = Quaternion(0.0, m.x, m.y, m.z) * q.conjugate()
        h = q * h
        val hPoint = Point(h.i, h.j, h.k)
        val b = Quaternion(
            0.0,
            hPoint.norm(),
            0.0,
            h.k
        )
        val v = Point(
            x = 2.0 * (q.i * q.k - q.w * q.j),
            y = 2.0 * (q.w * q.i + q.j * q.k),
            z = q.w * q.w - q.i * q.i - q.j * q.j + q.k * q.k
        )
        val w = Point(
            x = 2.0 * b.i * (0.5 - q.j * q.j - q.k * q.k) + 2.0 * b.k * (q.i * q.k - q.w * q.j),
            y = 2.0 * b.i * (q.i * q.j - q.w * q.k) + 2.0 * b.k * (q.w * q.i + q.j * q.k),
            z = 2.0 * b.i * (q.w * q.j + q.i * q.k) + 2.0 * b.k * (0.5 - q.i * q.i - q.j * q.j)
        )
        val e = a.cross(v) + m.cross(w)
        if (ki > 0.0) {
            pidErr += e * deltaTime
        }
        g += e * kp + pidErr * ki
        val qDot = q * Quaternion(0.0, g.x, g.y, g.z) * 0.5
        q += qDot * deltaTime
        quaternion = q / q.norm()
        return quaternion
    }
}