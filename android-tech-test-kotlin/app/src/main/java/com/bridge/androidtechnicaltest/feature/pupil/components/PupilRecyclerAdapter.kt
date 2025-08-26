package com.bridge.androidtechnicaltest.feature.pupil.components

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bridge.androidtechnicaltest.R
import com.bridge.androidtechnicaltest.databinding.PupilItemBinding
import com.bridge.androidtechnicaltest.feature.pupil.components.PupilRecyclerAdapter.PupilViewHolder
import com.bridge.androidtechnicaltest.feature.pupil.models.PupilUI
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions

class PupilRecyclerAdapter(
    private val pupilAction: PupilAction
) : ListAdapter<PupilUI, PupilViewHolder>(PupilDiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PupilViewHolder =
        PupilViewHolder(
            PupilItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: PupilViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (currentItem != null) {
            holder.bind(getItem(position))
        }
    }

    inner class PupilViewHolder(
        private val binding: PupilItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val pupil = getItem(position)
                    pupilAction.navigateToPupilDetail(pupil.pupilId.toInt())
                }
            }
        }

        fun bind(pupil: PupilUI) {
            binding.pupilName.text = pupil.pupilName
            binding.pupilLocation.text = pupil.pupilCountry

            val requestOptions = RequestOptions()
                .placeholder(R.drawable.ic_sync)
                .error(R.drawable.ic_error)

            Glide.with(itemView)
                .load(pupil.pupilImage)
                .apply(requestOptions)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.pupilImage)
        }
    }

    private class PupilDiffCallback : DiffUtil.ItemCallback<PupilUI>() {
        override fun areItemsTheSame(
            oldItem: PupilUI,
            newItem: PupilUI
        ): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(
            oldItem: PupilUI,
            newItem: PupilUI
        ): Boolean =
            oldItem.pupilId == newItem.pupilId
    }
}

interface PupilAction {
    fun navigateToPupilDetail(pupilId: Int)
}
