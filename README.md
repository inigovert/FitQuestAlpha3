# [smkit-ui-android-demo](https://github.com/sency-ai/smkit-sdk)

1. [ Installation. ](#inst)
2. [ Configure. ](#conf)
3. [ Start. ](#start)

<a name="inst"></a>
## 1. Installation

### Gradle
Here is the current available version of the SMKitUI project:

| Project | Version  |
|---------|:--------:|
| smkitui |  0.1.1   |

At Sency we using different startegy with our Artifactories 
In project level build.gradle please add our repo endpoint url
```groovy
allprojects {
    maven {
        url "https://artifacts.sency.ai/artifactory/release/"
    }
}
```

Sync and add the following dependency in app level build.gradle
```groovy
dependencies {
    implementation 'com.sency.smkitui:smkitui:$latest_version'
}
```

At AndroidManifest.xml the CAMERA permission has to be added
```xml
<!-- For using the camera -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" />
<uses-feature android:name="android.hardware.camera.autofocus" />
```

<a name="conf"></a>
## 2. Configure
```Kotlin
val smKitUI: SMKitUI = SMKitUI.Configuration(applicationContext)
    .setUIKey("YOUR_KEY")
    .configure(object : ConfigurationResult {
        override fun onSuccess() {
            //The configuration was successful
        }

        override fun onFailure() {
            //The configuration failed with error
        }
    })
```
To reduce wait time we recommend to call `configure` on app launch.

**⚠️ SMKitUI will not work if you don't first call configure.**


<a name="start"></a>
## 3. Start
Implement **SMKitUIWorkoutListener**.
```Kotlin
class MainActivity : ComponentActivity(), SMKitUIWorkoutListener {
    // Runtime error callback
    override fun handleWorkoutErrors(error: Error) {
    }

    // Workout session end callback
    override fun workoutDidFinish(summary: WorkoutSummaryData) {
    }

    // Exit workout callback
    overrice fun didExitWorkout(summary: WorkoutSummaryData) {
    }
    
    // When exercise is finished
    override fun exerciseDidFinish(data: ExerciseData) {
    }
}
```

### Start Assessment
**startAssessment** starts one of Sency's blueprint assessments.
```Kotlin
fun startAssessment() {
    try {
        smKitUI.startAssessment(this)
    } catch (e: Exception) {
        println("startAssessment: $e")
    }
}
```

### Start Custom Workout
**startWorkout** starts a custom workout.
```Kotlin
// List of exercises
val intro = applicationContext.getRawResourceUri(R.raw.customWorkoutIntro)
val soundtrack = applicationContext.getRawResourceUri(R.raw.fullBodyLong)
val exercises: List<SMExercise> = listOf(
    SMExercise(
        name = "High Knees",
        exerciseIntro = null, // Custom sound,
        totalSeconds = 30,
        introSeconds = 5,
        videoInstruction = applicationContext.getRawResourceUri(R.raw.HighKnees),
        uiElements = setOf(UiElement.RepsCounter, UiElement.Timer),
        detector = "HighKnees",
        repBased = true,
        exerciseClosure = null // Custom sound
    ),
    SMExercise(
        name = "Squat Regular Static",
        exerciseIntro = null, // Custom sound,
        totalSeconds = 30,
        introSeconds = 5,
        videoInstruction = applicationContext.getRawResourceUri(R.raw.SquatRegularStatic),
        uiElements = setOf(UiElement.GaugeOfMotion, UiElement.Timer),
        detector = "SquatRegularStatic",
        repBased = false,
        exerciseClosure = null // Custom sound
    ),
    SMExercise(
        name = "Plank High Static",
        exerciseIntro = null, // Custom sound,
        totalSeconds = 30,
        introSeconds = 5,
        videoInstruction = applicationContext.getRawResourceUri(R.raw.PlankHighStatic),
        uiElements = setOf(UiElement.GaugeOfMotion, UiElement.Timer),
        detector = "PlankHighStatic",
        repBased = false,
        exerciseClosure = null // Custom sound
    ),
)
val smWorkout = SMWorkout(
    id = "",
    name = "TEST",
    workoutIntro = intro,
    soundtrack = soundtrack,
    exercises = exercises,
    workoutClosure = null // Custom sound
)

try {
    smKitUI.startWorkout(smWorkout, this)
} catch (e: Exception) {
    println("startAssessment: $e")
}
```

Having issues? [Contact us](mailto:support@sency.ai) and let us know what the problem is.
