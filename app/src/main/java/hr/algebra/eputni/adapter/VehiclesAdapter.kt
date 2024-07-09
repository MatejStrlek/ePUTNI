package hr.algebra.eputni.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hr.algebra.eputni.R
import hr.algebra.eputni.databinding.CarListItemBinding
import hr.algebra.eputni.enums.VehicleType
import hr.algebra.eputni.model.Vehicle

class VehiclesAdapter(
    private val vehicles: List<Vehicle>
) :
    RecyclerView.Adapter<VehiclesAdapter.VehicleViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VehicleViewHolder {
        val binding = CarListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VehicleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        holder.bind(vehicles[position])
    }

    override fun getItemCount(): Int = vehicles.size

    class VehicleViewHolder(
        private val binding: CarListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(vehicle: Vehicle) {
            binding.tvVehicleName.text = vehicle.vehicleName
            binding.tvVehicleModel.text = vehicle.vehicleModel

            setIconOfVehicleType(vehicle.vehicleType.name)
        }

        private fun setIconOfVehicleType(vehicleType: String) {
            when (VehicleType.valueOf(vehicleType)) {
                VehicleType.PRIVATE -> binding.ivVehicleTypeIcon.setImageResource(R.drawable.ic_private)
                VehicleType.BUSINESS -> binding.ivVehicleTypeIcon.setImageResource(R.drawable.ic_business)
            }
        }
    }
}