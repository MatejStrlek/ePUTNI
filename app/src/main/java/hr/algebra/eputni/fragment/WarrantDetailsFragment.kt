package hr.algebra.eputni.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import hr.algebra.eputni.R
import hr.algebra.eputni.databinding.FragmentWarrantDetailsBinding
import hr.algebra.eputni.model.Warrant
import hr.algebra.eputni.util.TimeUtils

@Suppress("DEPRECATION")
class WarrantDetailsFragment : Fragment() {
    private var _binding: FragmentWarrantDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWarrantDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        val warrant: Warrant? = arguments?.getParcelable(getString(R.string.warrant))
        warrant?.let {
            populateFields(it)
        }
    }

    private fun populateFields(warrant: Warrant) {
        binding.tvStartTime.text = TimeUtils.millsToReadableDate(warrant.startTime)
        binding.tvEndTime.text = TimeUtils.millsToReadableDate(warrant.endTime!!)
        binding.tvStartCity.text = warrant.startCity
        binding.tvEndCity.text = warrant.endCity
        binding.tvStartKilometers.text = warrant.startKilometers.toString()
        binding.tvEndKilometers.text = warrant.endKilometers.toString()
        binding.tvDescription.text = warrant.description
        binding.tvFileCount.text = warrant.files.size.toString()
        binding.tvCheckedByFinanceTeam.text = warrant.checkedByFinanceTeam.toString()
    }
}