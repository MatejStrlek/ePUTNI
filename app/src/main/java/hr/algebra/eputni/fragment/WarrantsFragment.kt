package hr.algebra.eputni.fragment

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import hr.algebra.eputni.R
import hr.algebra.eputni.dao.FirestoreUserLogin
import hr.algebra.eputni.dao.FirestoreVehicles
import hr.algebra.eputni.dao.FirestoreWarrants
import hr.algebra.eputni.dao.UserRepository
import hr.algebra.eputni.dao.VehicleRepository
import hr.algebra.eputni.dao.WarrantRepository
import hr.algebra.eputni.databinding.FragmentWarrantsBinding
import hr.algebra.eputni.enums.TripType
import hr.algebra.eputni.model.Vehicle
import hr.algebra.eputni.model.Warrant
import hr.algebra.eputni.util.DialogUtils
import hr.algebra.eputni.util.FileUtils
import hr.algebra.eputni.util.TimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class WarrantsFragment : Fragment() {
    private var _binding: FragmentWarrantsBinding? = null
    private val binding get() = _binding!!
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var vehicleList = listOf<Vehicle>()
    private var isMeasuringDistance = false
    private var activeWarrant: Warrant? = null
    private lateinit var fileUtils: FileUtils
    private val scope = CoroutineScope(Dispatchers.IO)
    private lateinit var scannerLauncher: ActivityResultLauncher<IntentSenderRequest>
    private val vehicleRepository: VehicleRepository by lazy {
        FirestoreVehicles()
    }
    private val warrantRepository: WarrantRepository by lazy {
        FirestoreWarrants()
    }
    private val userRepository: UserRepository by lazy {
        FirestoreUserLogin()
    }

    companion object {
        private const val SCAN_REQUEST_CODE = 1001
        private const val REQUEST_CAMERA_PERMISSION = 1002
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
        initActions()
        fetchVehicles()
        setupActivityResultLauncher()
    }

    private fun setupActivityResultLauncher() {
        scannerLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK) {
                    val scannerResult =
                        GmsDocumentScanningResult.fromActivityResultIntent(result.data)
                    scannerResult?.pdf?.let { pdf ->
                        val pdfUri = pdf.uri
                        scope.launch {
                            fileUtils.uploadFileToFirebase(listOf(pdfUri))
                        }
                    }
                }
            }
    }

    private fun initActions() {
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
            if (checkCameraPermission()) {
                startDocumentScanner()
            } else {
                requestCameraPermission()
            }
            Toast.makeText(context, getString(R.string.scan_receipt), Toast.LENGTH_SHORT).show()
        }
        binding.btnUploadInvoice.setOnClickListener {
            fileUtils.selectPdfFile()
        }
        binding.fabListWarrants.setOnClickListener {
            findNavController().navigate(R.id.action_warrantsFragment_to_warrantsListFragment)
        }
    }

    private fun startDocumentScanner() {
        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(false)
            .setPageLimit(1)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()

        val scanner = GmsDocumentScanning.getClient(options)
        scanner.getStartScanIntent(requireActivity())
            .addOnSuccessListener { intentSender ->
                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), R.string.scanner_error, Toast.LENGTH_SHORT).show()
            }
    }

    private fun requestCameraPermission() {
        requestPermissions(arrayOf(android.Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startDocumentScanner()
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            SCAN_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val fileUrl = data.getStringExtra("scannedFileUrl")
                    if (fileUrl != null) {
                        val fileUtils =
                            FileUtils(this, warrantRepository, getString(R.string.scan_receipt))
                        fileUtils.uploadFileToFirebase(listOf(Uri.parse(fileUrl)))
                    } else {
                        Toast.makeText(context, getString(R.string.error_scan), Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.error_cancelled_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            else -> fileUtils.handleActivityResult(requestCode, resultCode, data)
        }
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
        scope.launch {
            if (userId != null) {
                warrantRepository.getActiveWarrant(userId, { warrant ->
                    if (warrant != null) {
                        activeWarrant = warrant
                        populateFields(warrant)
                        true.disableStartFields()
                        toggleVisibleSecondPart(true)
                    }
                }, {
                    Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show()
                })
            }
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

        scope.launch {
            warrantRepository.startTrip(travelWarrant,
                onSuccess = {
                    activeWarrant = travelWarrant
                },
                onFailure = {
                    Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show()
                })
        }
    }

    private fun startMeasuringDistance() {
        //todo: implement measuring distance
    }

    private fun endTrip() {
        if (!validateDescription()) return

        DialogUtils.showDeleteConfirmationDialog(
            context = requireContext(),
            title = getString(R.string.end_trip_question),
            message = getString(R.string.end_trip_question_message)
        ) {
            //need to change when i add measuring distance
            val endKilometers = if (isMeasuringDistance) 1 else null
            val description = binding.etTripDescription.text.toString()

            scope.launch {
                activeWarrant?.let { warrant ->
                    warrantRepository.endTrip(warrant, endKilometers, description,
                        onSuccess = {
                            Toast.makeText(
                                context,
                                getString(R.string.trip_ended),
                                Toast.LENGTH_SHORT
                            ).show()

                            userRepository.getUser(userId!!, { user ->
                                if (user.email != null) {
                                    createAndSendEmail(
                                        warrant,
                                        user.displayName ?: "")
                                }
                            }, { exception ->
                                Toast.makeText(
                                    context,
                                    exception.localizedMessage,
                                    Toast.LENGTH_SHORT
                                ).show()
                            })

                            false.disableStartFields()
                            binding.btnEndTrip.visibility = View.GONE
                            activeWarrant = null
                        },
                        onFailure = {
                            Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show()
                        })
                }
            }

            clearFields()
            toggleVisibleSecondPart(false)
        }
    }

    private fun createAndSendEmail(
        warrant: Warrant,
        displayName: String
    ) {
        val subject = getString(R.string.eputni_warrant_sent)
        val body = """
        Detalji putnog naloga:
        Ime i prezime člana: $displayName
        Vrijeme završetka puta: ${System.currentTimeMillis()}
        Od: ${warrant.startCity}
        Do: ${warrant.endCity}
        """.trimIndent()

        val emailIntent = Intent(Intent(Intent.ACTION_SEND)).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.email_address_for_sending)))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            `package` = "com.google.android.gm"
        }

        try {
            startActivity(Intent.createChooser(emailIntent, getString(R.string.sending_email)))
        } catch (e: Exception) {
            Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleVisibleSecondPart(isVisible: Boolean) {
        binding.tvTripDescription.visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.etTripDescription.visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.llBillsButtons.visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.btnEndTrip.visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.fabListWarrants.visibility = if (!isVisible) View.VISIBLE else View.GONE
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
        scope.launch {
            if (userId != null) {
                vehicleRepository.fetchVehicles(userId, { vehicles ->
                    vehicleList = vehicles
                    CoroutineScope(Dispatchers.Main).launch {
                        initSpinner()
                    }
                }, { exception ->
                    Toast.makeText(context, exception.localizedMessage, Toast.LENGTH_SHORT).show()
                })
            }
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