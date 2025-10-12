package com.reminder.myreminders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Reschedules all alarms after device boot
 * This ensures reminders continue working after phone restart
 */
public class BootReceiver extends BroadcastReceiver {
    
    private static final String TAG = "MyReminders";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || 
            "android.intent.action.QUICKBOOT_POWERON".equals(action)) {
            
            Log.d(TAG, "Device booted - launching app to reschedule alarms");
            
            // Launch app to reschedule alarms
            Intent launchIntent = context.getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName());
            
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                launchIntent.putExtra("reschedule_alarms", true);
                context.startActivity(launchIntent);
                Log.d(TAG, "App launched for alarm rescheduling");
            }
        }
    }
}
