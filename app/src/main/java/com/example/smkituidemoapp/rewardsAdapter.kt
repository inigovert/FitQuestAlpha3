package com.example.smkituidemoapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RewardsAdapter(
    private val rewardsList: List<Reward>,
    private val userPoints: Long,
    private val claimRewardCallback: (Reward) -> Unit
) : RecyclerView.Adapter<RewardsAdapter.RewardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_reward, parent, false)
        return RewardViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RewardViewHolder, position: Int) {
        val reward = rewardsList[position]
        holder.bind(reward)
    }

    override fun getItemCount() = rewardsList.size

    inner class RewardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rewardNameTextView: TextView = itemView.findViewById(R.id.rewardNameTextView)
        private val rewardDescriptionTextView: TextView = itemView.findViewById(R.id.rewardDescriptionTextView)
        private val requiredPointsTextView: TextView = itemView.findViewById(R.id.requiredPointsTextView)
        private val rewardStatusTextView: TextView = itemView.findViewById(R.id.rewardStatusTextView)
        private val claimRewardButton: Button = itemView.findViewById(R.id.claimRewardButton)

        fun bind(reward: Reward) {
            rewardNameTextView.text = reward.rewardName
            rewardDescriptionTextView.text = reward.rewardDescription
            requiredPointsTextView.text = "Required Points: ${reward.requiredPoints}"
            rewardStatusTextView.text = "Status: ${reward.status}"

            claimRewardButton.isEnabled = reward.status == "claimable" && userPoints >= reward.requiredPoints
            claimRewardButton.setOnClickListener {
                claimRewardCallback(reward)
            }
        }
    }
}
