package hr.algebra.eputni.framework

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.content.getSystemService

fun View.applyAnimation(animationId: Int) =
    startAnimation(AnimationUtils.loadAnimation(context, animationId))

fun Context.isOnline() : Boolean {
    val connectivityManager = getSystemService<ConnectivityManager>()

    connectivityManager?.activeNetwork?.let {network ->
        connectivityManager.getNetworkCapabilities(network)?.let {capabilities ->
            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        }
    }

    return false
}

fun callDelayed(delay: Long, action: () -> Unit) {
    Handler(Looper.getMainLooper()).postDelayed(
        action,
        delay
    )
}

inline fun <reified T : Activity> Context.startActivity() =
    startActivity(
        Intent(this, T::class.java)
            .apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })

inline fun <reified T : Activity> Context.startActivity(vararg extras: Pair<String, Any?>) {
    val intent = Intent(this, T::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        extras.forEach { pair ->
            val key = pair.first
            when (val value = pair.second) {
                is String -> putExtra(key, value)
                is Int -> putExtra(key, value)
                is Boolean -> putExtra(key, value)
                else -> throw IllegalArgumentException("Unsupported extra type")
            }
        }
    }
    startActivity(intent)
}