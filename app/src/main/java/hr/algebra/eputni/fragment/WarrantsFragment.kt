package hr.algebra.eputni.fragment

import android.content.Intent
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
import hr.algebra.eputni.dao.FirestoreWarrants
import hr.algebra.eputni.dao.VehicleRepository
import hr.algebra.eputni.dao.WarrantRepository
import hr.algebra.eputni.databinding.FragmentWarrantsBinding
import hr.algebra.eputni.enums.TripType
import hr.algebra.eputni.model.Vehicle
import hr.algebra.eputni.model.Warrant
import hr.algebra.eputni.util.FileUtils

@Suppress("DEPRECATION")
class WarrantsFragment : Fragment() {
    private var _binding: FragmentWarrantsBinding? = null
    private val binding get() = _binding!!
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var vehicleList = listOf<Vehicle>()
    private var isMeasuringDistance = false
    private var activeWarrant: Warrant? = null
    private lateinit var fileUtils: FileUtils
    private val vehicleRepository: VehicleRepository by lazy {
        FirestoreVehicles()
    }
    private val warrantRepository: WarrantRepository by lazy {
        FirestoreWarrants()
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

        fileUtils = FileUtils(this, warrantRepository, getString(R.string.select_file))

        checkActiveWarrant()

        binding.btnStartTrip.setOnClickListener {
            if (!fieldsValidationForTripStart()) return@setOnClickListener
            startTrip()
        }
        binding.btnEndTrip.setOnClickListener {
            endTrip()
        }
        binding.rbOptions.setOnCheckedChangeListener { _, checkedId ->
            binding.llCities.visibility =
                if (checkedId == R.id.rbEnterCities) View.VISIBLE else View.GONE
        }

        binding.btnScanReceipt.setOnClickListener {
        }
        binding.btnUploadInvoice.setOnClickListener {
            fileUtils.selectPdfFile()
        }

        fetchVehicles()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        fileUtils.handleActivityResult(requestCode, resultCode, data)
    }

    private fun fieldsValidationForTripStart(): Boolean {
        var isValid = true

        if (binding.etStartKilometers.text.isNullOrEmpty()) {
            binding.etStartKilometers.error = getString(R.string.mondatory_start_km)
            isValid = false
        }
        if (binding.spinnerSelectCar.selectedItem == null) {
            Toast.makeText(context, getString(R.string.select_car), Toast.LENGTH_SHORT).show()
            isValid = false
        }
        if (binding.rbOptions.checkedRadioButtonId == -1) {
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

    private fun checkActiveWarrant() {
        if (userId != null) {
            warrantRepository.getActiveWarrant(userId, { warrant ->
                if (warrant != null) {
                    activeWarrant = warrant
                    populateFields(warrant)
                    true.disableStartFields()
                    binding.btnEndTrip.visibility = View.VISIBLE
                }
            }, {
                Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show()
            })
        }
    }

    private fun populateFields(warrant: Warrant) {
        binding.etStartKilometers.setText(warrant.startKilometers.toString())
        binding.spinnerSelectCar.setSelection(vehicleList.indexOfFirst { it.id == warrant.vehicleId })
        if (warrant.tripType == TripType.CITY_BASED) {
            binding.rbEnterCities.isChecked = true
            binding.etStartCity.setText(warrant.startCity)
            binding.etEndCity.setText(warrant.endCity)
        } else {
            binding.rbMeasureDistance.isChecked = true
        }
    }

    private fun startTrip() {
        true.disableStartFields()
        toggleVisibleSecondPart(true)
        Toast.makeText(context, getString(R.string.trip_started), Toast.LENGTH_SHORT).show()

        val selectedVehicleId = vehicleList[binding.spinnerSelectCar.selectedItemPosition].id
        val startKilometers = binding.etStartKilometers.text.toString().toInt()

        val travelWarrant = if (binding.rbEnterCities.isChecked) {
            val startCity = binding.etStartCity.text.toString()
            val endCity = binding.etEndCity.text.toString()
            Warrant(
                userId = userId!!,
                vehicleId = selectedVehicleId,
                startKilometers = startKilometers,
                startCity = startCity,
                endCity = endCity,
                tripType = TripType.CITY_BASED
            )
        } else {
            isMeasuringDistance = true
            startMeasuringDistance()
            Warrant(
                userId = userId!!,
                vehicleId = selectedVehicleId,
                startKilometers = startKilometers,
                tripType = TripType.DISTANCE_BASED
            )
        }

        warrantRepository.startTrip(travelWarrant,
            onSuccess = {
                activeWarrant = travelWarrant
            },
            onFailure = {
                Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show()
            })
    }

    private fun startMeasuringDistance() {
        //todo: implement measuring distance
    }

    private fun endTrip() {
        if (!validateDescription()) return

        //need to change when i add measuring distance
        val endKilometers = if (isMeasuringDistance) 1 else null
        val description = binding.etTripDescription.text.toString()

        activeWarrant?.let { warrant ->
            warrantRepository.endTrip(warrant, endKilometers, description,
                onSuccess = {
                    Toast.makeText(context, getString(R.string.trip_ended), Toast.LENGTH_SHORT)
                        .show()
                    false.disableStartFields()
                    binding.btnEndTrip.visibility = View.GONE
                    activeWarrant = null
                },
                onFailure = {
                    Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show()
                })
        }

        clearFields()
        toggleVisibleSecondPart(false)
    }

    private fun toggleVisibleSecondPart(isVisible: Boolean) {
        binding.tvTripDescription.visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.etTripDescription.visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.llBillsButtons.visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.btnEndTrip.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun validateDescription(): Boolean {
        if (binding.etTripDescription.text.isNullOrEmpty()) {
            binding.etTripDescription.error = getString(R.string.mondatory_description)
            Toast.makeText(context, getString(R.string.mondatory_description), Toast.LENGTH_SHORT)
                .show()
            return false
        }
        return true
    }

    private fun clearFields() {
        binding.etStartKilometers.text?.clear()
        binding.etStartCity.text?.clear()
        binding.etEndCity.text?.clear()
        binding.spinnerSelectCar.setSelection(0)
        binding.rbOptions.clearCheck()
        binding.llCities.visibility = View.GONE
        binding.etTripDescription.text?.clear()
    }

    private fun Boolean.disableStartFields() {
        binding.etStartKilometers.isEnabled = !this
        binding.spinnerSelectCar.isEnabled = !this
        binding.rbOptions.isEnabled = !this
        binding.rbEnterCities.isEnabled = !this
        binding.rbEnterCities.isEnabled = !this
        binding.etStartCity.isEnabled = !this
        binding.etEndCity.isEnabled = !this

        binding.btnStartTrip.isEnabled = !this
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
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, vehicleNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSelectCar.adapter = adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}