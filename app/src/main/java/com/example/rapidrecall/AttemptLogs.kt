package com.example.rapidrecall

// this is data class that is used to store attempt logs throughout each session
data class AttemptLogs (
    val sequenceLength: Int,
    var userInput: String,
    var targetSequence: String,
    var verdict: String,
    var timeStamp: String
)