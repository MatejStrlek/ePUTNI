package hr.algebra.eputni.model

import android.os.Parcel
import android.os.Parcelable
import hr.algebra.eputni.enums.VehicleType

data class Vehicle(
    var id: String? = "",
    var vehicleName: String? = "",
    var vehicleModel: String? = "",
    var vehicleType: VehicleType? = VehicleType.PRIVATE,
    var licensePlate: String? = "",
    var userId: String? = "",
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        VehicleType.valueOf(parcel.readString() ?: VehicleType.PRIVATE.name),
        parcel.readString(),
        parcel.readString()
    )

    //for Firebase needed
    constructor() : this("", "", "", VehicleType.PRIVATE, "", "")

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(vehicleName)
        parcel.writeString(vehicleModel)
        parcel.writeString(vehicleType?.name)
        parcel.writeString(licensePlate)
        parcel.writeString(userId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Vehicle> {
        override fun createFromParcel(parcel: Parcel): Vehicle {
            return Vehicle(parcel)
        }

        override fun newArray(size: Int): Array<Vehicle?> {
            return arrayOfNulls(size)
        }
    }
}
