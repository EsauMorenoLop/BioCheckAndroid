package net.biocheck.biocheckmovil.model

import java.sql.Time
import java.text.FieldPosition
import java.util.*

/**
 * Created by Gabriel Martinez on 09/01/2018.
 */
object ApiData {
    data class ApiError(val debugMessage: String, val message: String, val status: String, val subErrors: List<Any>, val timestamp: String)
    data class User(val checkId: Long, val nip: Long, val subdomain: String)
    data class Employee(val checkId: Long, val checkMobile: Boolean, val company: String, val deleted: Int,
                        var enrolled: Boolean, val id: Long, val lastName: String, val mobilePercentage: Int,
                        val name: String, val nip: Long, val locations:List<Location>)

    data class EnrollState(val imagesCount: Int, val imagesNedeed: Int, val verified: Boolean)

    data class RouteLocation(val checkIn: Boolean,
                             val checkOut: Boolean,
                             val id: Long,
                             val position: Int,
                             val location: Location)

    data class Location(val deleted: Boolean,
                        val distance: Int,
                        val externalNumber: String?,
                        val id: Long,
                        val internalNumber: String?,
                        val latitude: Double,
                        val longitude: Double,
                        val municipality: String?,
                        var suburb: String?,
                        var title: String?,
                        var zipcode: String?,
                        var street:String?)

    data class Route(val deleted: Boolean, var description: String, var id: Long,
                     val routeLocations: List<RouteLocation>, var name: String)

    data class WorkShift(val id: Long, val endTime: Time, val startTime: Time, val days: Int)

    data class DaySettings(val routes:List<Route>, val workShifts:List<WorkShift>, val locations: List<Location>)

    data class Check(val latitude:Double, val longitude:Double, val date: Long, val timeZone: Int, val checkInOut:Boolean)

    data class Record(val checkId:Long, val recTime:Date, val coordinates:String, val checkInOut:Boolean,
                      val serialNumber:String, val operationCode:Boolean) : Comparable<Date>{
        override fun compareTo(other: Date) = when {
            other.after(recTime) -> -1
            other.before(recTime) -> 1
            else -> 0
        }

    }

    data class RecordView(val isRecord: Boolean, val record: Record?, val label: String?)
}