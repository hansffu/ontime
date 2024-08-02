package dev.hansffu.ontime.ui.navigation

import androidx.navigation.NavController
import dev.hansffu.ontime.model.Stop

interface NavigationControl {

    fun toStops(stop: Stop): Unit
    fun toNearby()
}

class NavigationControlImpl(private val navController: NavController) : NavigationControl {
    override fun toStops(stop: Stop): Unit = navController.navigate(Screen.Timetable.link(stop))
    override fun toNearby() = navController.navigate(Screen.Nearby.route)
}

object NavigationControlMock : NavigationControl {
    override fun toStops(stop: Stop) {}
    override fun toNearby() {}
}