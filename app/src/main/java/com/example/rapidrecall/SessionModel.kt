package com.example.rapidrecall

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel


// this is a ViewModel class, and it stores the game summaries and
// even attemptLogs list for the game screen to store data into it
// and the start screen to retrieve data from it
class SessionModel: ViewModel() {

    var attempts by mutableIntStateOf(0)
    var correctAttempts by mutableIntStateOf(0)
    var overallAccuracy by mutableDoubleStateOf(0.0)




    private var _attemptLogs = mutableListOf<AttemptLogs>(

    )

    val attemptLogs: List<AttemptLogs>
        get() = _attemptLogs

    fun addAttemptLog(attempt: AttemptLogs){
        _attemptLogs.add(attempt)
    }

    fun setData(attempts:Int, correctAttempts:Int,overallAccuracy: Double){
        this.attempts = attempts
        this.correctAttempts=correctAttempts
        this.overallAccuracy = overallAccuracy
    }

}