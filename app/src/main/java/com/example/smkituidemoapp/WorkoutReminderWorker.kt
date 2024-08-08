import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.smkituidemoapp.R

class WorkoutReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        sendNotification()
        return Result.success()
    }

    private fun sendNotification() {
        val channelId = "workout_reminder_channel"
        val notificationId = 1

        try {
            // Create the notification channel if needed
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "Workout Reminder"
                val descriptionText = "Time to do your daily workout!"
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(channelId, name, importance).apply {
                    description = descriptionText
                }
                val notificationManager: NotificationManager =
                    applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }

            // Build the notification
            val builder = NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(R.drawable.ic_workout) // Use your app's icon
                .setContentTitle("Workout Reminder")
                .setContentText("Time to do your daily workout!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            // Check permission before showing the notification
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
                with(NotificationManagerCompat.from(applicationContext)) {
                    notify(notificationId, builder.build())
                }
            }

        } catch (e: SecurityException) {
            // Handle the SecurityException if the permission is not granted
            e.printStackTrace()
        }
    }
}
