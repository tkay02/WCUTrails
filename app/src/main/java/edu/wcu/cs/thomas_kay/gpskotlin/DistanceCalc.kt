package edu.wcu.cs.thomas_kay.gpskotlin

import com.google.android.gms.maps.model.LatLng
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author Thomas Kay
 * @version 5/9/2024
 *
 * Math module to perform calculations of determining the user's distance between points and checks
 * if user is within an appropriate distance away from a point.
 */

/** The radius of a point */
const val RADIUS:Double = 8.0
/** The radius of the Earth in meters */
const val EARTH_RADIUS:Int = 6371000
/** Value to convert from degrees to radians */
const val TO_RADIANS = 180

/**
 * Calculates the distance between two latitude/longitude coordinate points.
 *
 * @param coord1 Coordinate point one (the user's location).
 * @param coord2 Coordinate point two (static distance location).
 * @return The distance between two coordinate points in meters.
 */
fun calculateDistance(coord1: LatLng, coord2: LatLng):Double {
    //Convert to radians
    val lat1 = coord1.latitude * (PI / TO_RADIANS)
    val lng1 = coord1.longitude * (PI / TO_RADIANS)
    val lat2 = coord2.latitude * (PI / TO_RADIANS)
    val lng2 = coord2.longitude * (PI / TO_RADIANS)
    return acos(sin(lat1) * sin(lat2) + cos(lat1) * cos(lat2) * cos(lng2-lng1)) * EARTH_RADIUS
}

/**
 * Checks if the first coordinate point is within a radius of the second coordinate point.
 *
 * @param coord1 Coordinate point one (the user's location).
 * @param coord2 Coordinate point two (static distance location).
 * @param radius The radius of the second coordinate point in meters.
 * @return True if the distance of the two coordinate points are within the radius; false
 * otherwise. The distance is within the radius if the distance is less than or equal to the
 * radius.
 */
fun isNearPoint(coord1: LatLng, coord2: LatLng, radius: Double):Boolean {
    val distanceBtwPoints = calculateDistance(coord1, coord2)
    return distanceBtwPoints <= radius
}