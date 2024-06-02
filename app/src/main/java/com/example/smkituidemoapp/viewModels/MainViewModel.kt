package com.example.smkituidemoapp.viewModels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sency.smkitui.model.SMExercise
import com.sency.smkitui.model.SMWorkout
import com.sency.smkitui.model.UiElement
import dagger.hilt.android.qualifiers.ApplicationContext
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

    fun exercies() = listOf(
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
            name = "Squat Regular Static",
            exerciseIntro = Uri.EMPTY, // Custom sound,
            totalSeconds = 30,
            introSeconds = 5,
            videoInstruction = Uri.EMPTY,
            uiElements = setOf(UiElement.GaugeOfMotion, UiElement.Timer),
            detector = "SquatRegularStatic",
            repBased = false,
            exerciseClosure = Uri.EMPTY // Custom sound
        ),
        SMExercise(
            name = "Plank High Static",
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

}