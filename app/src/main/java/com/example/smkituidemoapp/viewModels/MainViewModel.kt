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

    fun upperBodyWorkout() = listOf(
//        SMExercise(
//            prettyName = "Shoulder Press",
//            exerciseIntro = "",
//            totalSeconds = 10,
//            introSeconds = 0,
//            videoInstruction = "",
//            uiElements = setOf(UiElement.Timer, UiElement.GaugeOfMotion),
//            detector = "ShouldersPress",
//            repBased = true,
//            exerciseClosure = "",
//            targetReps = 5,
//            targetTime = 0,
//            scoreFactor = 0.5, // Ensure this is Double
//            passCriteria = null,
//        ),
        SMExercise(
            prettyName = "Push Ups",
            totalSeconds = 10,
            introSeconds = 0,
            exerciseIntro = "0",
            videoInstruction = "",
            uiElements = setOf(UiElement.GaugeOfMotion, UiElement.Timer),
            detector = "PushupRegular",
            repBased = true,
            exerciseClosure = "",
            targetReps = 5,
            targetTime = 0,
            scoreFactor = 0.5, // Ensure this is Double
            passCriteria = null,
        ),
        SMExercise(
            prettyName = "Knee to Elbow Plank",
            totalSeconds = 10,
            introSeconds = 0,
            exerciseIntro = "",
            videoInstruction = "",
            uiElements = setOf(UiElement.Timer, UiElement.GaugeOfMotion),
            detector = "PlankHighKneeToElbow",
            repBased = false, //not sure kung repbased
            exerciseClosure = "",
            targetReps = 10,
            targetTime = 0,
            scoreFactor = 0.5, // Ensure this is Double
            passCriteria = null,
        ),
        SMExercise(
            prettyName = "Shoulder Taps Plank",
            totalSeconds = 10,
            introSeconds = 0,
            exerciseIntro = "",
            videoInstruction = "",
            uiElements = setOf(UiElement.Timer, UiElement.GaugeOfMotion),
            detector = "PlankHighShoulderTaps",
            repBased = true,
            exerciseClosure = "",
            targetReps = 10,
            targetTime = 0,
            scoreFactor = 0.5, // Ensure this is Double
            passCriteria = null,
        ),
//        SMExercise(
//            prettyName = "Standing Reverse Air Fly",
//            totalSeconds = 10,
//            introSeconds = 0,
//            exerciseIntro = "",
//            videoInstruction = "",
//            uiElements = setOf(UiElement.Timer, UiElement.GaugeOfMotion),
//            detector = "StandingStepReverseAirFly",
//            repBased = true, //not sure kung rep based
//            exerciseClosure = "",
//            targetReps = 10,
//            targetTime = 0,
//            scoreFactor = 0.5, // Ensure this is Double
//            passCriteria = null,
//        )

    )
    fun coreWorkout() = listOf(
        SMExercise(
            prettyName = "Crunches",
            exerciseIntro = "",
            totalSeconds = 10,
            introSeconds = 0,
            videoInstruction = "",
            uiElements = setOf(UiElement.Timer, UiElement.GaugeOfMotion),
            detector = "Crunches",
            repBased = true,
            exerciseClosure = "",
            targetReps = 5,
            targetTime = 0,
            scoreFactor = 0.5, // Ensure this is Double
            passCriteria = null,
        ),
//        SMExercise(
//            prettyName = "Standing Oblique Crunches",
//            exerciseIntro = "",
//            totalSeconds = 10,
//            introSeconds = 0,
//            videoInstruction = "",
//            uiElements = setOf(UiElement.Timer, UiElement.GaugeOfMotion),
//            detector = "StandingObliqueCrunches",
//            repBased = true,
//            exerciseClosure = "",
//            targetReps = 5,
//            targetTime = 0,
//            scoreFactor = 0.5, // Ensure this is Double
//            passCriteria = null,
//        ),
//        SMExercise(
//            prettyName = "High Plank",
//            exerciseIntro = "",
//            totalSeconds = 15,
//            introSeconds = 0,
//            videoInstruction = "",
//            uiElements = setOf(UiElement.Timer, UiElement.GaugeOfMotion),
//            detector = "PlankHighStatic",
//            repBased = false,
//            exerciseClosure = "",
//            targetReps = 5,
//            targetTime = 10,
//            scoreFactor = 0.5, // Ensure this is Double
//            passCriteria = null,
//        ),
//        SMExercise(
//            prettyName = "Side Plank",
//            exerciseIntro = "",
//            totalSeconds = 10,
//            introSeconds = 0,
//            videoInstruction = "",
//            uiElements = setOf(UiElement.Timer, UiElement.GaugeOfMotion),
//            detector = "Crunches",
//            repBased = false, //unsure
//            exerciseClosure = "",
//            targetReps = 5,
//            targetTime = 5,
//            scoreFactor = 0.5, // Ensure this is Double
//            passCriteria = null,
//        ),
        SMExercise(
            prettyName = "Standing Side Bend Right",
            exerciseIntro = "",
            totalSeconds = 15,
            introSeconds = 0,
            videoInstruction = "",
            uiElements = setOf(UiElement.Timer, UiElement.GaugeOfMotion),
            detector = "StandingSideBendRight",
            repBased = false,
            exerciseClosure = "",
            targetReps = 5,
            targetTime = 10,
            scoreFactor = 0.5, // Ensure this is Double
            passCriteria = null,
        ),SMExercise(
            prettyName = "Standing Side Bend Left",
            exerciseIntro = "",
            totalSeconds = 10,
            introSeconds = 0,
            videoInstruction = "",
            uiElements = setOf(UiElement.Timer, UiElement.GaugeOfMotion),
            detector = "StandingSideBendRightLeft",
            repBased = false, //unsure
            exerciseClosure = "",
            targetReps = 5,
            targetTime = 5,
            scoreFactor = 0.5, // Ensure this is Double
            passCriteria = null,
        )
    )
    fun legsWorkout() = listOf(
        SMExercise(
            prettyName = "Squats",
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
            prettyName = "Front Lunges",
            exerciseIntro = "",
            totalSeconds = 10,
            introSeconds = 0,
            videoInstruction = "",
            uiElements = setOf(UiElement.Timer, UiElement.GaugeOfMotion),
            detector = "LungeFront",
            repBased = true,
            exerciseClosure = "",
            targetReps = 5,
            targetTime = 0,
            scoreFactor = 0.5, // Ensure this is Double
            passCriteria = null,
        ),
        SMExercise(
            prettyName = "Right Side Lunge",
            exerciseIntro = "",
            totalSeconds = 10,
            introSeconds = 0,
            videoInstruction = "",
            uiElements = setOf(UiElement.Timer, UiElement.GaugeOfMotion),
            detector = "LungeSideRight",
            repBased = true,
            exerciseClosure = "",
            targetReps = 5,
            targetTime = 0,
            scoreFactor = 0.5, // Ensure this is Double
            passCriteria = null,
        ),
        SMExercise(
            prettyName = "Left Side Lunge",
            exerciseIntro = "",
            totalSeconds = 10,
            introSeconds = 0,
            videoInstruction = "",
            uiElements = setOf(UiElement.Timer, UiElement.GaugeOfMotion),
            detector = "LungeSideLeft",
            repBased = true,
            exerciseClosure = "",
            targetReps = 5,
            targetTime = 0,
            scoreFactor = 0.5, // Ensure this is Double
            passCriteria = null,
        ),
//        SMExercise(
//            prettyName = "Jumps",
//            exerciseIntro = "",
//            totalSeconds = 10,
//            introSeconds = 0,
//            videoInstruction = "",
//            uiElements = setOf(UiElement.Timer, UiElement.GaugeOfMotion),
//            detector = "Jumps",
//            repBased = true,
//            exerciseClosure = "",
//            targetReps = 5,
//            targetTime = 0,
//            scoreFactor = 0.5, // Ensure this is Double
//            passCriteria = null,
//        )

    )
    fun cardioWorkout() = listOf(
        SMExercise(
            prettyName = "Jumping Jacks",
            exerciseIntro = "",
            totalSeconds = 10,
            introSeconds = 0,
            videoInstruction = "",
            uiElements = setOf(UiElement.Timer, UiElement.GaugeOfMotion),
            detector = "JumpingJacks",
            repBased = true,
            exerciseClosure = "",
            targetReps = 5,
            targetTime = 0,
            scoreFactor = 0.5, // Ensure this is Double
            passCriteria = null,
        ),
//        SMExercise(
//            prettyName = "Side Step Jacks",
//            exerciseIntro = "",
//            totalSeconds = 10,
//            introSeconds = 0,
//            videoInstruction = "",
//            uiElements = setOf(UiElement.Timer, UiElement.GaugeOfMotion),
//            detector = "SideStepJacks",
//            repBased = true,
//            exerciseClosure = "",
//            targetReps = 5,
//            targetTime = 0,
//            scoreFactor = 0.5, // Ensure this is Double
//            passCriteria = null,
//        ),
        SMExercise(
            prettyName = "Skater Hops",
            exerciseIntro = "",
            totalSeconds = 10,
            introSeconds = 0,
            videoInstruction = "",
            uiElements = setOf(UiElement.Timer, UiElement.GaugeOfMotion),
            detector = "SkaterHops",
            repBased = true,
            exerciseClosure = "",
            targetReps = 5,
            targetTime = 0,
            scoreFactor = 0.5, // Ensure this is Double
            passCriteria = null,
        ),
//        SMExercise(
//            prettyName = "Ski Jumps",
//            exerciseIntro = "",
//            totalSeconds = 10,
//            introSeconds = 0,
//            videoInstruction = "",
//            uiElements = setOf(UiElement.Timer, UiElement.GaugeOfMotion),
//            detector = "SkiJumps",
//            repBased = true,
//            exerciseClosure = "",
//            targetReps = 5,
//            targetTime = 0,
//            scoreFactor = 0.5, // Ensure this is Double
//            passCriteria = null,
//        ),
    )

    fun updateExercisePoints(points: Int) {
        _exercisePoints.postValue(points)
    }

}
