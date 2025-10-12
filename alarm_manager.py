"""
AlarmManager helper for scheduling native Android alarms
This ensures reminders work even when app is closed or phone is off
"""
from kivy.utils import platform

def schedule_exact_alarm(reminder_id, hour, minute, days, text, category, note, priority):
    """Schedule an exact alarm using native AlarmManager and BroadcastReceiver"""
    if platform != 'android':
        return False
    
    try:
        from jnius import autoclass
        
        PythonActivity = autoclass('org.kivy.android.PythonActivity')
        Intent = autoclass('android.content.Intent')
        PendingIntent = autoclass('android.app.PendingIntent')
        AlarmManager = autoclass('android.app.AlarmManager')
        Calendar = autoclass('java.util.Calendar')
        AlarmBroadcastReceiver = autoclass('com.reminder.myreminders.AlarmBroadcastReceiver')
        
        activity = PythonActivity.mActivity
        context = activity.getApplicationContext()
        alarm_manager = context.getSystemService('alarm')
        
        # Find next occurrence
        now = Calendar.getInstance()
        current_time = now.getTimeInMillis()
        
        next_alarm_time = None
        
        for day in days:
            calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            
            # Convert Python weekday (0=Monday) to Java (1=Sunday)
            java_day = ((day + 1) % 7) + 1
            calendar.set(Calendar.DAY_OF_WEEK, java_day)
            
            # If time has passed, schedule for next week
            if calendar.getTimeInMillis() <= current_time:
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
            
            # Find earliest occurrence
            if next_alarm_time is None or calendar.getTimeInMillis() < next_alarm_time:
                next_alarm_time = calendar.getTimeInMillis()
        
        if next_alarm_time:
            # Create intent for BroadcastReceiver
            intent = Intent(context, AlarmBroadcastReceiver)
            intent.setAction("com.reminder.ALARM_TRIGGER")
            intent.putExtra("reminder_id", reminder_id)
            intent.putExtra("reminder_text", text)
            intent.putExtra("reminder_category", category)
            intent.putExtra("reminder_note", note or "")
            intent.putExtra("reminder_priority", priority)
            intent.putExtra("alarm_hour", hour)
            intent.putExtra("alarm_minute", minute)
            intent.putExtra("alarm_days", ','.join(map(str, days)))
            
            # Create PendingIntent
            pending_intent = PendingIntent.getBroadcast(
                context,
                reminder_id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            )
            
            # Schedule exact alarm
            alarm_manager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                next_alarm_time,
                pending_intent
            )
            
            # Calculate time until alarm
            time_diff = (next_alarm_time - current_time) // 1000 // 60
            print(f"✅ Alarm {reminder_id} scheduled for {hour:02d}:{minute:02d} in {time_diff} minutes")
            return True
            
    except Exception as e:
        print(f"❌ Schedule alarm error: {e}")
        import traceback
        traceback.print_exc()
        return False


def cancel_alarm(reminder_id):
    """Cancel a scheduled alarm"""
    if platform != 'android':
        return
    
    try:
        from jnius import autoclass
        
        PythonActivity = autoclass('org.kivy.android.PythonActivity')
        Intent = autoclass('android.content.Intent')
        PendingIntent = autoclass('android.app.PendingIntent')
        AlarmBroadcastReceiver = autoclass('com.reminder.myreminders.AlarmBroadcastReceiver')
        
        activity = PythonActivity.mActivity
        context = activity.getApplicationContext()
        alarm_manager = context.getSystemService('alarm')
        
        intent = Intent(context, AlarmBroadcastReceiver)
        intent.setAction("com.reminder.ALARM_TRIGGER")
        
        pending_intent = PendingIntent.getBroadcast(
            context,
            reminder_id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        )
        
        alarm_manager.cancel(pending_intent)
        pending_intent.cancel()
        
        print(f"✅ Alarm {reminder_id} cancelled")
        
    except Exception as e:
        print(f"❌ Cancel alarm error: {e}")


def reschedule_all_alarms(reminders):
    """Reschedule all enabled alarms - call after boot or app restart"""
    if platform != 'android':
        return
    
    print("🔄 Rescheduling all alarms...")
    count = 0
    
    for idx, reminder in enumerate(reminders):
        if reminder.get('enabled', True):
            success = schedule_exact_alarm(
                idx,
                reminder['time'].hour,
                reminder['time'].minute,
                reminder.get('days', list(range(7))),
                reminder['text'],
                reminder.get('category', 'Personal'),
                reminder.get('note', ''),
                reminder.get('priority', 'Medium')
            )
            if success:
                count += 1
    
    print(f"✅ Rescheduled {count} alarms")
    return count
