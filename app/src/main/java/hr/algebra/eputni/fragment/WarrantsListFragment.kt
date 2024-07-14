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
import hr.algebra.eputni.adapter.WarrantsAdapter
import hr.algebra.eputni.dao.FirestoreWarrants
import hr.algebra.eputni.dao.WarrantRepository
import hr.algebra.eputni.databinding.FragmentWarrantsListBinding
import hr.algebra.eputni.model.Warrant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WarrantsListFragment : Fragment() {
    private var _binding: FragmentWarrantsListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: WarrantsAdapter
    private val warrants = mutableListOf<Warrant>()
    private val scope = CoroutineScope(Dispatchers.Main)
    private val warrantRepository: WarrantRepository by lazy {
        FirestoreWarrants()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWarrantsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        fetchWarrants()

        binding.fabAddWarrant.setOnClickListener {
            findNavController().navigate(R.id.action_warrantsListFragment_to_warrantsFragment)
        }
    }

    private fun initRecyclerView() {
        adapter = WarrantsAdapter(warrants, onWarrantClick = { warrant ->
            val bundle = Bundle().apply {
                putParcelable(getString(R.string.warrant), warrant)
            }
            findNavController().navigate(R.id.action_warrantsListFragment_to_warrantDetailsFragment, bundle)
        })

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchWarrants() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            scope.launch {
                warrantRepository.fetchWarrants(userId,
                    onSuccess = {
                        CoroutineScope(Dispatchers.Main).launch {
                            warrants.clear()
                            warrants.addAll(it)
                            adapter.notifyDataSetChanged()
                            toggleEmptyListMessage(warrants.isEmpty())
                        }
                    },
                    onFailure = {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    })
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