package hr.algebra.eputni.util

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    fun millsToReadableDate(
        timeInMills: Long,
        pattern: String = "dd.MM.yyyy. HH:mm"
    ): String {
        val date = Date(timeInMills)
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(date)
    }
}