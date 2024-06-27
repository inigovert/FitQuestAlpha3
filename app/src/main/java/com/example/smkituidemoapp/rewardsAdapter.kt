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
) : RecyclerView.Adapter<RewardsAdapter.RewardsViewHolder>() {

    class RewardsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rewardNameTextView: TextView = itemView.findViewById(R.id.rewardNameTextView)
        val rewardDescriptionTextView: TextView = itemView.findViewById(R.id.rewardDescriptionTextView)
        val requiredPointsTextView: TextView = itemView.findViewById(R.id.requiredPointsTextView)
        val claimRewardButton: Button = itemView.findViewById(R.id.claimRewardButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_reward, parent, false)
        return RewardsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RewardsViewHolder, position: Int) {
        val reward = rewardsList[position]
        holder.rewardNameTextView.text = reward.rewardName
        holder.rewardDescriptionTextView.text = reward.rewardDescription
        holder.requiredPointsTextView.text = "Required Points: ${reward.requiredPoints}"
        holder.claimRewardButton.isEnabled = userPoints >= reward.requiredPoints

        holder.claimRewardButton.setOnClickListener {
            claimRewardCallback(reward)
        }
    }

    override fun getItemCount() = rewardsList.size
}
