package com.reminder.myreminders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.app.AlarmManager;
import android.media.RingtoneManager;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import androidx.core.app.NotificationCompat;
import android.os.Vibrator;
import android.os.VibrationEffect;
import java.util.Calendar;

/**
 * BroadcastReceiver that handles alarms even when app is completely closed
 * This is triggered by AlarmManager and reschedules itself
 */
public class AlarmBroadcastReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (action != null && action.equals("com.reminder.ALARM_TRIGGER")) {
            // Get reminder data
            int reminderId = intent.getIntExtra("reminder_id", -1);
            String reminderText = intent.getStringExtra("reminder_text");
            String reminderCategory = intent.getStringExtra("reminder_category");
            String reminderNote = intent.getStringExtra("reminder_note");
            String reminderPriority = intent.getStringExtra("reminder_priority");
            int alarmHour = intent.getIntExtra("alarm_hour", 0);
            int alarmMinute = intent.getIntExtra("alarm_minute", 0);
            String alarmDaysStr = intent.getStringExtra("alarm_days");
            
            // Wake device
            wakeUpDevice(context);
            
            // Show notification
            showAlarmNotification(context, reminderId, reminderText, reminderCategory, 
                                reminderNote, reminderPriority);
            
            // Vibrate
            vibrateDevice(context, reminderPriority);
            
            // CRITICAL: Reschedule for next occurrence
            if (alarmDaysStr != null && !alarmDaysStr.isEmpty()) {
                rescheduleAlarm(context, intent, reminderId, alarmHour, alarmMinute, 
                              alarmDaysStr, reminderText, reminderCategory, 
                              reminderNote, reminderPriority);
            }
        }
    }
    
    private void rescheduleAlarm(Context context, Intent originalIntent, int reminderId, 
                                 int hour, int minute, String daysStr, String text, 
                                 String category, String note, String priority) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            
            // Parse days
            String[] dayParts = daysStr.split(",");
            
            // Find next occurrence
            Calendar now = Calendar.getInstance();
            long currentTime = now.getTimeInMillis();
            Long nextAlarmTime = null;
            
            for (String dayStr : dayParts) {
                int day = Integer.parseInt(dayStr.trim());
                
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                
                // Convert Python weekday to Java
                int javaDay = ((day + 1) % 7) + 1;
                calendar.set(Calendar.DAY_OF_WEEK, javaDay);
                
                // If time passed, schedule next week
                if (calendar.getTimeInMillis() <= currentTime) {
                    calendar.add(Calendar.WEEK_OF_YEAR, 1);
                }
                
                // Find earliest
                if (nextAlarmTime == null || calendar.getTimeInMillis() < nextAlarmTime) {
                    nextAlarmTime = calendar.getTimeInMillis();
                }
            }
            
            if (nextAlarmTime != null) {
                // Create new intent
                Intent newIntent = new Intent(context, AlarmBroadcastReceiver.class);
                newIntent.setAction("com.reminder.ALARM_TRIGGER");
                newIntent.putExtra("reminder_id", reminderId);
                newIntent.putExtra("reminder_text", text);
                newIntent.putExtra("reminder_category", category);
                newIntent.putExtra("reminder_note", note);
                newIntent.putExtra("reminder_priority", priority);
                newIntent.putExtra("alarm_hour", hour);
                newIntent.putExtra("alarm_minute", minute);
                newIntent.putExtra("alarm_days", daysStr);
                
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    reminderId,
                    newIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );
                
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextAlarmTime,
                    pendingIntent
                );
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void wakeUpDevice(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK | 
            PowerManager.ACQUIRE_CAUSES_WAKEUP |
            PowerManager.ON_AFTER_RELEASE,
            "MyReminders::AlarmWakeLock"
        );
        wakeLock.acquire(60000);
    }
    
    private void showAlarmNotification(Context context, int reminderId, String text, 
                                      String category, String note, String priority) {
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        String channelId = "reminder_alarm_channel";
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                channelId,
                "Reminder Alarms",
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Alarm notifications");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            channel.setBypassDnd(true);
            
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build();
            channel.setSound(alarmSound, audioAttributes);
            
            notificationManager.createNotificationChannel(channel);
        }
        
        Intent launchIntent = context.getPackageManager()
            .getLaunchIntentForPackage(context.getPackageName());
        if (launchIntent != null) {
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            launchIntent.putExtra("alarm_triggered", true);
            launchIntent.putExtra("reminder_id", reminderId);
        }
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            reminderId + 10000,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        String priorityEmoji = "High".equals(priority) ? "🔥 " : "";
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(priorityEmoji + "⏰ " + category)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .setFullScreenIntent(pendingIntent, true)
            .setContentIntent(pendingIntent)
            .setVibrate(new long[]{0, 1000, 500, 1000, 500, 1000})
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
        
        if (note != null && !note.isEmpty()) {
            builder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(text + "\n\n📝 " + note));
        }
        
        notificationManager.notify(3000 + reminderId, builder.build());
    }
    
    private void vibrateDevice(Context context, String priority) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        
        long[] pattern;
        if ("High".equals(priority)) {
            pattern = new long[]{0, 1000, 500, 1000, 500, 1000};
        } else {
            pattern = new long[]{0, 500, 200, 500};
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            VibrationEffect effect = VibrationEffect.createWaveform(pattern, 0);
            vibrator.vibrate(effect);
        } else {
            vibrator.vibrate(pattern, 0);
        }
    }
}
