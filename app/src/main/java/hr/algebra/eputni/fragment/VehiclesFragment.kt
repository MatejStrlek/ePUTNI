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
import com.google.firebase.firestore.FirebaseFirestore
import hr.algebra.eputni.R
import hr.algebra.eputni.dao.FirestoreUserLogin
import hr.algebra.eputni.dao.UserRepository
import hr.algebra.eputni.databinding.FragmentVehiclesBinding
import hr.algebra.eputni.enums.VehicleType

class VehiclesFragment : Fragment() {
    private var _binding: FragmentVehiclesBinding? = null
    private val binding get() = _binding!!

    private val userRepository: UserRepository by lazy {
        FirestoreUserLogin()
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

        initVehicleSpinner()

        binding.btnSaveVehicle.setOnClickListener {
            if (!fieldsValidated()) return@setOnClickListener
            saveVehicleData()
        }
    }

    private fun saveVehicleData() {
        val vehicleName = binding.etVehicleName.text.toString()
        val vehicleModel = binding.etVehicleModel.text.toString()
        val vehicleType = VehicleType.entries[binding.spinnerVehicleType.selectedItemPosition]
        val licensePlate = binding.etLicensePlate.text.toString()

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            userRepository.getUser(
                currentUser.uid,
                { user ->
                    val carData = hashMapOf(
                        "vehicleName" to vehicleName,
                        "vehicleModel" to vehicleModel,
                        "vehicleType" to vehicleType.name,
                        "licensePlate" to licensePlate,
                        "userId" to user.uid
                    )

                    FirebaseFirestore.getInstance().collection("vehicles")
                        .add(carData)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(),
                                getString(R.string.vehicle_saved), Toast.LENGTH_SHORT).show()
                                findNavController().navigate(R.id.profileFragment)
                        }
                        .addOnFailureListener { error ->
                            Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                        }
                },
                { error ->
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            Toast.makeText(requireContext(), getString(R.string.not_logged_in), Toast.LENGTH_SHORT).show()
        }
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