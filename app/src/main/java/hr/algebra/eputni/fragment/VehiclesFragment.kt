package hr.algebra.eputni.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import hr.algebra.eputni.R
import hr.algebra.eputni.dao.FirestoreVehicles
import hr.algebra.eputni.dao.VehicleRepository
import hr.algebra.eputni.databinding.FragmentVehiclesBinding
import hr.algebra.eputni.enums.VehicleType
import hr.algebra.eputni.model.Vehicle

class VehiclesFragment : Fragment() {
    private var _binding: FragmentVehiclesBinding? = null
    private val binding get() = _binding!!
    private val vehicleRepository: VehicleRepository by lazy {
        FirestoreVehicles()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVehiclesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        clearFields()
        initVehicleSpinner()

        binding.btnSaveVehicle.setOnClickListener {
            if (!fieldsValidated()) return@setOnClickListener
            saveVehicleData()
        }

        binding.fabListVehicles.setOnClickListener {
            findNavController().navigate(R.id.action_vehicleFragment_to_vehiclesListFragment)
        }
    }

    private fun saveVehicleData() {
        val vehicleName = binding.etVehicleName.text.toString()
        val vehicleModel = binding.etVehicleModel.text.toString()
        val vehicleType = VehicleType.entries[binding.spinnerVehicleType.selectedItemPosition]
        val licensePlate = binding.etLicensePlate.text.toString()

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val vehicleData = Vehicle(
                userId = currentUser.uid,
                vehicleName = vehicleName,
                vehicleModel = vehicleModel,
                vehicleType = vehicleType,
                licensePlate = licensePlate
            )

            vehicleRepository.saveVehicle(vehicleData,
                {
                    Toast.makeText(requireContext(), getString(R.string.vehicle_saved), Toast.LENGTH_SHORT).show()
                    clearFields()
                    findNavController().navigate(R.id.action_vehicleFragment_to_vehiclesListFragment)
                },
                {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
                )
        } else {
            Toast.makeText(requireContext(), getString(R.string.not_logged_in), Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearFields() {
        binding.etVehicleName.text?.clear()
        binding.etVehicleModel.text?.clear()
        binding.spinnerVehicleType.setSelection(0)
        binding.etLicensePlate.text?.clear()
    }

    private fun fieldsValidated(): Boolean {
        var isValid = true

        if (binding.etVehicleName.text.isNullOrBlank()) {
            binding.etVehicleName.error = "Vehicle name is required"
            isValid = false
        }
        if (binding.etVehicleModel.text.isNullOrBlank()) {
            binding.etVehicleModel.error = "Vehicle model is required"
            isValid = false
        }

        if (binding.spinnerVehicleType.selectedItemPosition == AdapterView.INVALID_POSITION) {
            Toast.makeText(requireContext(), getString(R.string.select_car_type), Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (binding.etLicensePlate.text.isNullOrBlank()) {
            binding.etLicensePlate.error = "License plate is required"
            isValid = false
        }

        return isValid
    }

    private fun initVehicleSpinner() {
        val vehicleTypes = VehicleType.entries.map { it.displayName }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, vehicleTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerVehicleType.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}