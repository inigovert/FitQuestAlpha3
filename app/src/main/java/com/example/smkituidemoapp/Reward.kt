package com.example.smkituidemoapp

data class Reward(
    val rewardName: String = "",
    val rewardDescription: String = "",
    val requiredPoints: Int = 0,
    var status: String = "claimable",
    var quantity: Int = 0
)

