package hr.algebra.eputni.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hr.algebra.eputni.R
import hr.algebra.eputni.databinding.CarListItemBinding
import hr.algebra.eputni.enums.VehicleType
import hr.algebra.eputni.model.Vehicle

class VehiclesAdapter(
    private val context: Context,
    private val vehicles: MutableList<Vehicle>,
    private val onVehicleClick: (Vehicle) -> Unit,
    private val onVehicleLongClick: (Vehicle, Int) -> Unit
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
        val vehicle = vehicles[position]

        holder.itemView.setOnLongClickListener {
            onVehicleLongClick(vehicle, position)
            true
        }

        holder.itemView.setOnClickListener {
            onVehicleClick(vehicle)
        }

        holder.bind(vehicle)
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
                VehicleType.PRIVATE -> binding.ivVehicleTypeIcon.setImageResource(R.drawable.ic_private_car)
                VehicleType.BUSINESS -> binding.ivVehicleTypeIcon.setImageResource(R.drawable.ic_business_car)
            }
        }
    }
}