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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class VehiclesFragment : Fragment() {
    private var _binding: FragmentVehiclesBinding? = null
    private val binding get() = _binding!!
    private var currentVehicle: Vehicle? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private val vehicleRepository: VehicleRepository by lazy {
        FirestoreVehicles()
    }

    private val licensePlatePattern: Regex = Regex("^[A-Za-z]{2}[A-Za-z0-9]{3,4}[A-Za-z]{1,2}$")

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

        val vehicle: Vehicle? = arguments?.getParcelable(getString(R.string.vehicle))
        currentVehicle = vehicle
        if (vehicle != null) {
            populateFields(vehicle)
            binding.btnSaveVehicle.text = getString(R.string.update_vehicle)
        }
        else {
            binding.btnSaveVehicle.text = getString(R.string.save_vehicle)
        }

        binding.fabListVehicles.setOnClickListener {
            findNavController().navigate(R.id.action_vehicleFragment_to_vehiclesListFragment)
        }

        binding.btnSaveVehicle.setOnClickListener {
            if (!fieldsValidated()) return@setOnClickListener

            scope.launch {
                if (vehicle == null) {
                    saveVehicleData()
                } else {
                    updateVehicleData(currentVehicle!!)
                }
            }
        }
    }

    private suspend fun updateVehicleData(currentVehicle: Vehicle) {
        val vehicleName = binding.etVehicleName.text.toString()
        val vehicleModel = binding.etVehicleModel.text.toString()
        val vehicleType = VehicleType.entries[binding.spinnerVehicleType.selectedItemPosition]
        val licensePlate = binding.etLicensePlate.text.toString()

        val updatedVehicle = currentVehicle.copy(
            vehicleName = vehicleName,
            vehicleModel = vehicleModel,
            vehicleType = vehicleType,
            licensePlate = licensePlate
        )

        vehicleRepository.updateVehicle(updatedVehicle,
            onSuccess = {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.vehicle_updated),
                        Toast.LENGTH_SHORT
                    ).show()
                    clearFields()
                    findNavController().navigate(R.id.action_vehicleFragment_to_vehiclesListFragment)
                }
            },
            onFailure = {
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun populateFields(vehicle: Vehicle) {
        binding.etVehicleName.setText(vehicle.vehicleName)
        binding.etVehicleModel.setText(vehicle.vehicleModel)
        binding.spinnerVehicleType.setSelection(VehicleType.entries.indexOf(vehicle.vehicleType))
        binding.etLicensePlate.setText(vehicle.licensePlate)
    }

    private suspend fun saveVehicleData() {
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
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.vehicle_saved),
                            Toast.LENGTH_SHORT
                        ).show()
                        clearFields()
                        findNavController().navigate(R.id.action_vehicleFragment_to_vehiclesListFragment)
                    }
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
            binding.etVehicleName.error = getString(R.string.mondatory_vehicle_name)
            isValid = false
        }
        if (binding.etVehicleModel.text.isNullOrBlank()) {
            binding.etVehicleModel.error = getString(R.string.mondatory_vehicle_model)
            isValid = false
        }

        if (binding.spinnerVehicleType.selectedItemPosition == AdapterView.INVALID_POSITION) {
            Toast.makeText(requireContext(), getString(R.string.select_car_type), Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (!licensePlatePattern.matches(binding.etLicensePlate.text.toString())) {
            binding.etLicensePlate.error = getString(R.string.invalid_license_plate)
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