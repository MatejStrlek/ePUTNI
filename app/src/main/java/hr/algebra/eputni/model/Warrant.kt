package hr.algebra.eputni.model

import android.os.Parcel
import android.os.Parcelable
import hr.algebra.eputni.enums.TripType

data class Warrant (
    var id: String? = "",
    var userId: String? = "",
    var vehicleId: String? = "",
    var startKilometers: Int? = 0,
    var endKilometers: Int? = null,
    var startCity: String? = null,
    var endCity: String? = null,
    var startTime: Long = System.currentTimeMillis(),
    var endTime: Long? = null,
    var finished: Boolean = false,
    var tripType: TripType = TripType.CITY_BASED
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readByte() != 0.toByte(),
        TripType.valueOf(parcel.readString() ?: TripType.CITY_BASED.name)
    )

    //for Firebase needed
    constructor() : this("", "", "", 0, 0, "",
        "", System.currentTimeMillis(), null, false, TripType.CITY_BASED)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(userId)
        parcel.writeString(vehicleId)
        parcel.writeValue(startKilometers)
        parcel.writeValue(endKilometers)
        parcel.writeString(startCity)
        parcel.writeString(endCity)
        parcel.writeLong(startTime)
        parcel.writeValue(endTime)
        parcel.writeByte(if (finished) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Warrant> {
        override fun createFromParcel(parcel: Parcel): Warrant {
            return Warrant(parcel)
        }

        override fun newArray(size: Int): Array<Warrant?> {
            return arrayOfNulls(size)
        }
    }
}