package com.example.smkituidemoapp.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sency.smkitui.model.SMExercise
import com.sency.smkitui.model.ExerciseData
import com.sency.smkitui.model.UiElement
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {



    private var _configured = MutableLiveData(false)
    val configured: LiveData<Boolean>
        get() = _configured

    private val _exercisePoints = MutableLiveData(0)
    val exercisePoints: LiveData<Int>
        get() = _exercisePoints

    fun setConfigured(configured: Boolean) {
        viewModelScope.launch {
            _configured.postValue(configured)
        }
    }

    fun exercises() = listOf(
        SMExercise(
            prettyName = "Squat",
            exerciseIntro = "",
            totalSeconds = 10,
            introSeconds = 0,
            videoInstruction = "",
            uiElements = setOf(UiElement.Timer, UiElement.GaugeOfMotion),
            detector = "SquatRegular",
            repBased = true,
            exerciseClosure = "",
            targetReps = 5,
            targetTime = 0,
            scoreFactor = 0.5, // Ensure this is Double
            passCriteria = null,
        ),
        SMExercise(
            prettyName = "Plank",
            totalSeconds = 10,
            introSeconds = 0,
            exerciseIntro = "0",
            videoInstruction = "",
            uiElements = setOf(UiElement.GaugeOfMotion, UiElement.Timer),
            detector = "PlankHighStatic",
            repBased = false,
            exerciseClosure = "",
            targetReps = 5,
            targetTime = 0,
            scoreFactor = 0.5, // Ensure this is Double
            passCriteria = null,
        ),
        SMExercise(
            prettyName = "High Knees",
            totalSeconds = 10,
            introSeconds = 0,
            exerciseIntro = "",
            videoInstruction = "",
            uiElements = setOf(UiElement.Timer, UiElement.GaugeOfMotion),
            detector = "HighKnees",
            repBased = true,
            exerciseClosure = "",
            targetReps = 10,
            targetTime = 0,
            scoreFactor = 0.5, // Ensure this is Double
            passCriteria = null,
        )
    )

    fun updateExercisePoints(points: Int) {
        _exercisePoints.postValue(points)
    }

}
