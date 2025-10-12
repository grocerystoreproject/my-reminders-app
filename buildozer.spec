[app]
title = My Reminders
package.name = myreminders
package.domain = com.reminder
source.dir = .
source.include_exts = py,png,jpg,kv,atlas,json
version = 2.6

requirements = python3==3.9.19,kivy==2.3.0,android,pyjnius

orientation = portrait
fullscreen = 0

# ALL required permissions for reliable alarms
android.permissions = INTERNET,VIBRATE,POST_NOTIFICATIONS,WAKE_LOCK,RECEIVE_BOOT_COMPLETED,SCHEDULE_EXACT_ALARM,USE_EXACT_ALARM,FOREGROUND_SERVICE,READ_EXTERNAL_STORAGE,WRITE_EXTERNAL_STORAGE,REQUEST_IGNORE_BATTERY_OPTIMIZATIONS

android.api = 33
android.minapi = 21
android.ndk = 25b
android.accept_sdk_license = True
android.skip_update = False

android.apptheme = @android:style/Theme.Material.Light.NoActionBar

android.enable_androidx = True
android.gradle_dependencies = com.google.android.material:material:1.8.0

android.archs = arm64-v8a

android.add_gradle_repositories = google(), mavenCentral()

# Add Java source directory for BroadcastReceivers
android.add_src = java

# Register BroadcastReceivers in AndroidManifest.xml
android.manifest.application = """
    <receiver
        android:name="com.reminder.myreminders.AlarmBroadcastReceiver"
        android:enabled="true"
        android:exported="true">
        <intent-filter>
            <action android:name="com.reminder.ALARM_TRIGGER" />
        </intent-filter>
    </receiver>
    
    <receiver
        android:name="com.reminder.myreminders.BootReceiver"
        android:enabled="true"
        android:exported="true"
        android:permission="android.permission.RECEIVE_BOOT_COMPLETED">
        <intent-filter>
            <action android:name="android.intent.action.BOOT_COMPLETED" />
            <action android:name="android.intent.action.QUICKBOOT_POWERON" />
            <category android:name="android.intent.category.DEFAULT" />
        </intent-filter>
    </receiver>
"""

p4a.branch = master
p4a.bootstrap = sdl2

[buildozer]
log_level = 2
warn_on_root = 1
