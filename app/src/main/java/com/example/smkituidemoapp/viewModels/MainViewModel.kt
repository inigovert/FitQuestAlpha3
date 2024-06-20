package com.example.smkituidemoapp.viewModels


import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sency.smkitui.model.SMExercise
import com.sency.smkitui.model.UiElement
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private var _configured = MutableLiveData(false)
    val configured: LiveData<Boolean>
        get() = _configured

    fun setConfigured(configured: Boolean) {
        viewModelScope.launch {
            _configured.postValue(configured)
        }
    }
    fun exercises() = listOf(
        SMExercise(
            name = "High Knees",
            exerciseIntro = Uri.EMPTY, // Custom sound,
            totalSeconds = 30,
            introSeconds = 5,
            videoInstruction = Uri.EMPTY,
            uiElements = setOf(UiElement.RepsCounter, UiElement.Timer),
            detector = "HighKnees",
            repBased = true,
            exerciseClosure = Uri.EMPTY // Custom sound
        ),
        SMExercise(
            name = "Squats",
            exerciseIntro = Uri.EMPTY, // Custom sound,
            totalSeconds = 30,
            introSeconds = 5,
            videoInstruction = Uri.EMPTY,
            uiElements = setOf(UiElement.RepsCounter, UiElement.Timer),
            detector = "SquatRegular",
            repBased = true,
            exerciseClosure = Uri.EMPTY // Custom sound
        ),
        SMExercise(
            name = "Plank",
            exerciseIntro = Uri.EMPTY, // Custom sound,
            totalSeconds = 30,
            introSeconds = 5,
            videoInstruction = Uri.EMPTY,
            uiElements = setOf(UiElement.GaugeOfMotion, UiElement.Timer),
            detector = "PlankHighStatic",
            repBased = false,
            exerciseClosure = Uri.EMPTY // Custom sound
        ),
    )
    // Add this property to control point awarding behavior
    var awardPointsPerExercise: Boolean = true

}