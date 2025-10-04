package fi.vuorenkoski.tuguview

import java.util.Date

class Sensor(
    val id: String,
    val sensorName: String,
    val sensorFullname: String,
    val sensorUnit: String,
    val lastValue: Double,
    val date: Date?
)