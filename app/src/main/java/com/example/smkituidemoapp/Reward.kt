package com.example.smkituidemoapp

data class Reward(
    val rewardName: String = "",
    val rewardDescription: String = "",
    val requiredPoints: Long = 0,
    var status: String = "claimable"
)
