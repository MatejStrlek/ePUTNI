package hr.algebra.eputni.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import hr.algebra.eputni.R
import hr.algebra.eputni.adapter.VehiclesAdapter
import hr.algebra.eputni.dao.FirestoreVehicles
import hr.algebra.eputni.dao.VehicleRepository
import hr.algebra.eputni.databinding.FragmentVehiclesListBinding
import hr.algebra.eputni.model.Vehicle
import hr.algebra.eputni.util.DialogUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VehiclesListFragment : Fragment() {
    private var _binding: FragmentVehiclesListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: VehiclesAdapter
    private val vehicles = mutableListOf<Vehicle>()
    private val scope = CoroutineScope(Dispatchers.Main)
    private val vehicleRepository: VehicleRepository by lazy {
        FirestoreVehicles()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVehiclesListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        fetchCars()

        binding.fabAddVehicle.setOnClickListener {
            findNavController().navigate(R.id.action_vehiclesListFragment_to_vehicleFragment)
        }
    }

    private fun initRecyclerView() {
        adapter = VehiclesAdapter(vehicles,
            onVehicleClick = { vehicle ->
            val bundle = Bundle().apply {
                putParcelable(getString(R.string.vehicle), vehicle)
            }
            findNavController().navigate(R.id.action_vehiclesListFragment_to_vehicleFragment, bundle)
        }
        ) { vehicle, position ->
            DialogUtils.showDeleteConfirmationDialog(
                context = requireContext(),
                title = getString(R.string.delete_vehicle),
                message = getString(R.string.delete_confirmation_message)
            ) {
                scope.launch {
                    deleteVehicle(vehicle, position)
                }
            }
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private suspend fun deleteVehicle(vehicle: Vehicle, position: Int) {
        vehicleRepository.deleteVehicle(vehicle,
            onSuccess = {
                vehicles.removeAt(position)
                adapter.notifyItemRemoved(position)
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.vehicle_deleted), Toast.LENGTH_SHORT
                    ).show()
                    toggleEmptyListMessage(vehicles.isEmpty())
                }
            },
            onFailure = {
                Toast.makeText(requireContext(),
                    getString(R.string.error_deleting_vehicle), Toast.LENGTH_SHORT).show()
            })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchCars() {
        scope.launch {
            val userID = FirebaseAuth.getInstance().currentUser?.uid
            if (userID != null) {
                vehicleRepository.fetchVehicles(userID,
                    {
                        CoroutineScope(Dispatchers.Main).launch {
                            vehicles.clear()
                            vehicles.addAll(it)
                            adapter.notifyDataSetChanged()
                            toggleEmptyListMessage(vehicles.isEmpty())
                        }
                    },
                    {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    })
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.not_logged_in),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun toggleEmptyListMessage(isEmpty: Boolean) {
        binding.tvEmptyListMessage.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}