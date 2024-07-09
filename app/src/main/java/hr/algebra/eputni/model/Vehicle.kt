package hr.algebra.eputni.model

import hr.algebra.eputni.enums.VehicleType

data class Vehicle(
    var vehicleName: String = "",
    var vehicleModel: String = "",
    var vehicleType: VehicleType = VehicleType.PRIVATE,
    var licensePlate: String = "",
    var userId: String = "",
) {
    //for Firebase needed
    constructor() : this("", "", VehicleType.PRIVATE, "", "")
}
