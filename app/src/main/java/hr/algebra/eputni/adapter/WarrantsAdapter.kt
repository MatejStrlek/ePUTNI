package hr.algebra.eputni.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hr.algebra.eputni.R
import hr.algebra.eputni.databinding.WarrantListItemBinding
import hr.algebra.eputni.model.Warrant
import hr.algebra.eputni.util.TimeUtils

class WarrantsAdapter(
    private val warrants: MutableList<Warrant>,
    private val onWarrantClick: (Warrant) -> Unit
) :
    RecyclerView.Adapter<WarrantsAdapter.WarrantViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WarrantViewHolder {
        val binding = WarrantListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return WarrantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WarrantViewHolder, position: Int) {
        val warrant = warrants[position]

        holder.itemView.setOnClickListener {
            onWarrantClick(warrant)
        }

        holder.bind(warrant)
    }

    override fun getItemCount(): Int = warrants.size

    class WarrantViewHolder(
        private val binding: WarrantListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(warrant: Warrant) {
            binding.tvWarrantDestination.text = warrant.startCity + " - " + warrant.endCity
            binding.tvWarrantFinishedDate.text = TimeUtils.millsToReadableDate(warrant.endTime ?: 0)

            val iconChecked = if (warrant.checkedByFinanceTeam) {
                R.drawable.ic_checked_yes
            } else {
                R.drawable.ic_checked_no
            }

            binding.ivWarrantChecked.setImageResource(iconChecked)
        }
    }
}