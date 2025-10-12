[app]
title = My Reminders
package.name = myreminders
package.domain = com.reminder
source.dir = .
source.include_exts = py,png,jpg,kv,atlas,json
version = 2.5

requirements = python3==3.9.19,kivy==2.3.0,android,pyjnius

orientation = portrait
fullscreen = 0

android.permissions = INTERNET,VIBRATE,POST_NOTIFICATIONS

android.api = 33
android.minapi = 21
android.ndk = 25b
android.accept_sdk_license = True
android.skip_update = False

android.apptheme = @android:style/Theme.Material.Light.NoActionBar

android.enable_androidx = True
android.gradle_dependencies = com.google.android.material:material:1.8.0

android.archs = arm64-v8a

android.add_gradle_repositories = google(), ma[app]
title = My Reminders
package.name = myreminders
package.domain = com.reminder
source.dir = .
source.include_exts = py,png,jpg,kv,atlas,json
version = 2.5

requirements = python3==3.9.19,kivy==2.3.0,android,pyjnius

orientation = portrait
fullscreen = 0

android.permissions = INTERNET,VIBRATE,POST_NOTIFICATIONS

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

p4a.branch = master
p4a.bootstrap = sdl2

[buildozer]
log_level = 2
warn_on_root = 1
