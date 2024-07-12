package hr.algebra.eputni.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import hr.algebra.eputni.R
import hr.algebra.eputni.dao.FirestoreVehicles
import hr.algebra.eputni.dao.VehicleRepository
import hr.algebra.eputni.databinding.FragmentWarrantsBinding
import hr.algebra.eputni.model.Vehicle

class WarrantsFragment : Fragment() {
    private var _binding: FragmentWarrantsBinding? = null
    private val binding get() = _binding!!
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var vehicleList = listOf<Vehicle>()
    private var selectedVehicle: Vehicle? = null
    private var isMeasuringDistance = false
    private val vehicleRepository: VehicleRepository by lazy {
        FirestoreVehicles()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWarrantsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnStartTrip.setOnClickListener {
            if (!fieldsValidated()) return@setOnClickListener
            startTrip()
            disableStartFields(false)
            binding.btnEndTrip.visibility = View.VISIBLE
            Toast.makeText(context, getString(R.string.trip_started), Toast.LENGTH_SHORT).show()
        }
        binding.btnEndTrip.setOnClickListener {
            endTrip()
        }
        binding.rbOptions.setOnCheckedChangeListener { _, checkedId ->
            binding.llCities.visibility = if (checkedId == R.id.rbEnterCities) View.VISIBLE else View.GONE
        }
        fetchVehicles()
    }

    private fun disableStartFields(enabled: Boolean) {
        binding.etStartKilometers.isEnabled = enabled
        binding.spinnerSelectCar.isEnabled = enabled
        binding.rbOptions.isEnabled = enabled
        binding.rbEnterCities.isEnabled = enabled
        binding.rbEnterCities.isEnabled = enabled
        binding.etStartCity.isEnabled = enabled
        binding.etEndCity.isEnabled = enabled

        binding.btnStartTrip.isEnabled = enabled
    }

    private fun fieldsValidated(): Boolean {
        var isValid = true

        if(binding.etStartKilometers.text.isNullOrEmpty()) {
            binding.etStartKilometers.error = getString(R.string.mondatory_start_km)
            isValid = false
        }
        if (binding.spinnerSelectCar.selectedItem == null) {
            Toast.makeText(context, getString(R.string.select_car), Toast.LENGTH_SHORT).show()
            isValid = false
        }
        if(binding.rbOptions.checkedRadioButtonId == -1) {
            Toast.makeText(context, getString(R.string.select_option), Toast.LENGTH_SHORT).show()
            isValid = false
        }
        if (binding.rbEnterCities.isChecked && binding.etStartCity.text.isNullOrEmpty()) {
            binding.etStartCity.error = getString(R.string.mondatory_start_city)
            isValid = false
        }
        if (binding.rbEnterCities.isChecked && binding.etEndCity.text.isNullOrEmpty()) {
            binding.etEndCity.error = getString(R.string.mondatory_end_city)
            isValid = false
        }

        return isValid
    }

    private fun startTrip() {

    }

    private fun endTrip() {

    }

    private fun fetchVehicles() {
        if (userId != null) {
            vehicleRepository.fetchVehicles(userId, { vehicles ->
                vehicleList = vehicles
                initSpinner()
            }, { exception ->
                Toast.makeText(context, exception.localizedMessage, Toast.LENGTH_SHORT).show()
            })
        }
    }

    private fun initSpinner() {
        val vehicleNames = vehicleList.map { it.vehicleName }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, vehicleNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSelectCar.adapter = adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}