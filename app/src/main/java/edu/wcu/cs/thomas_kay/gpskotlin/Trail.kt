package edu.wcu.cs.thomas_kay.gpskotlin

import com.google.android.gms.maps.model.LatLng

/**
 * Data structure that stores the information of a trail.
 *
 * @param trailName The name of the trail.
 */
class Trail(val trailName:String) {
    /** The length of the trail */
    private var size:Int = 0
    /** The starting point of the trail */
    private var head:TrailPoint? = null
    /** The end of the trail */
    private var tail:TrailPoint? = null
    /** The current point that a user is observing; can be used as a cursor */
    private var currentPoint:TrailPoint? = null

    /**
     * Adds a new trail point to the end of the trail.
     *
     * @param lat The latitude coordinate of the trail point.
     * @param lng The longitude coordinate of the trail point.
     */
    fun add(lat:Double, lng:Double) {
        //Creates new trail point, with id being the size of the list + 1
        val trailPoint = TrailPoint(this.size + 1, lat, lng, null, null)
        if(this.size == 0) {
            this.head = trailPoint
            this.tail = trailPoint
        } else {
            this.tail?.next = trailPoint
            trailPoint.prev = this.tail
            this.tail = trailPoint
        }
        this.size++
    }

    /**
     * Removes the tail, or the most recently added point, from the trail.
     */
    fun removeTail() {
        when (size) {
            0 -> {} //Does nothing
            1 -> {
                this.head = null
                this.tail = null
                this.currentPoint = null
                this.size--
            } else -> {
                if(currentPoint == this.tail) {
                    this.currentPoint = null
                }
                this.tail = this.tail?.prev
                this.tail?.next = null
                this.size--
            }
        }
    }

    /**
     * Removes the head, or the least recently added point, from the trail.
     */
    fun removeHead() {
        when (size) {
            0 -> {} //Does nothing
            1 -> {
                this.head = null
                this.tail = null
                this.currentPoint = null
                this.size--
            } else -> {
                if(currentPoint == this.head) {
                    this.currentPoint = null
                }
                this.head = this.head?.next
                this.head?.prev = null
                this.size--
            }
        }
    }

    /**
     * Removes the current point from the trail.
     */
    public fun removeCurrentPoint() {
        //Removes current point if it is not equal to null and that the size is greater than zero
        if(currentPoint != null && this.size > 0) {
            when (currentPoint) {
                this.head -> removeHead()
                this.tail -> removeTail()
                else -> {
                    val prev = this.currentPoint?.prev
                    val next = this.currentPoint?.next
                    prev?.next = next
                    next?.prev = prev
                    this.currentPoint = null
                    this.size--
                }
            }
        }
    }

    //Needs testing
    fun getNextPoint():TrailPoint? {
        if(this.size > 0) {
            when (this.currentPoint) {
                null, this.tail -> this.currentPoint = this.head
                else -> this.currentPoint = this.currentPoint?.next
            }
            return currentPoint
        }
        return null
    }

    //Needs testing
    fun getPrevPoint():TrailPoint? {
        if(this.size > 0) {
            when (this.currentPoint) {
                null, this.head -> this.currentPoint = this.tail
                else -> this.currentPoint = this.currentPoint?.prev
            }
            return currentPoint
        }
        return null
    }

    fun iterate():ArrayList<LatLng> {
        val latlngList = ArrayList<LatLng>()
        var cursor = this.head
        while(cursor != null) {
            latlngList.add(LatLng(cursor.lat, cursor.lng))
            cursor = cursor.next
        }
        return latlngList
    }

    /**
     * Inner class for Trail data structure. Stores an ID value that indicates which the order of
     * the trail points, depending on the length of the trail. Also stores information about the
     * coordinates of the point and the next
     */
    data class TrailPoint(val pointID:Int, val lat:Double, val lng:Double,
                     var prev:TrailPoint?, var next:TrailPoint?)

}