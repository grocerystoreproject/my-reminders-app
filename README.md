# ⏰ My Reminders - Android App

A professional reminder application with background service support.

## Features

- 📝 Create and manage reminders
- 📂 Categories: Work, Personal, Health, Shopping, Other
- ⚠️ Priority levels: High, Medium, Low
- 🔔 Custom notification sounds
- 📅 Flexible scheduling (specific days, weekdays, weekends, daily)
- 😴 Smart snooze functionality
- 🔄 Background service for reliable alarms
- ✅ Enable/disable reminders without deletion
- 📝 Optional notes for each reminder

## Build

The app builds automatically via GitHub Actions when you push to main branch.

### Manual Build

```bash
pip install buildozer cython==0.29.33
buildozer android debug
```

### Download APK

Go to Actions tab → Latest workflow run → Download artifact

## Permissions

The app requires:
- Notifications
- Exact alarms (Android 12+)
- Vibration
- Wake lock
- Foreground service
- Storage (for custom ringtones)

## Tech Stack

- **Python 3.9** + **Kivy 2.3.0**
- **Buildozer** for Android packaging
- **Background service** for alarm management
- **AlarmManager** for reliable notifications

## Developers

- Harshvardhan Chandanshive
- Nikita Bagate
- Shreya Dalwai

SBGI Miraj Third Year Students

## License

Educational project
