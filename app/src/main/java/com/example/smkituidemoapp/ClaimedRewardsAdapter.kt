import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smkituidemoapp.R

class ClaimedRewardsAdapter(
    private val claimedRewardsList: List<ClaimedReward>
) : RecyclerView.Adapter<ClaimedRewardsAdapter.ClaimedRewardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClaimedRewardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_claimed_reward, parent, false)
        return ClaimedRewardViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClaimedRewardViewHolder, position: Int) {
        val claimedReward = claimedRewardsList[position]
        holder.rewardNameTextView.text = claimedReward.rewardName
        holder.rewardDescriptionTextView.text = claimedReward.rewardDescription
        holder.dateClaimedTextView.text = claimedReward.dateClaimed // Ensure this is set
    }

    override fun getItemCount(): Int = claimedRewardsList.size

    class ClaimedRewardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rewardNameTextView: TextView = view.findViewById(R.id.rewardNameTextView)
        val rewardDescriptionTextView: TextView = view.findViewById(R.id.rewardDescriptionTextView)
        val dateClaimedTextView: TextView = view.findViewById(R.id.dateClaimedTextView) // Add this
    }
}
