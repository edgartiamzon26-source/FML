# Focus Lock

Android app that locks your phone to chosen apps for a set time.

- **Timer lock:** start a session (1-480 min); a full-screen lock screen with countdown takes over.
- **Allowed apps only:** pick which apps may open; everything else (other apps, Settings, notification shade) is bounced back to the lock screen.
- **Wait-to-open:** each allowed app has a delay (0/10/30/60 s). After the countdown it opens and stays unlocked for 10 minutes.
- **Safety:** keyboards and the default dialer (emergency calls) are always allowed. There is no cancel button, so choose the duration carefully.

## Get the APK
1. Push this repo to GitHub.
2. Open **Actions > Build APK > Run workflow** (it also runs on every push).
3. Download the `focuslock-debug-apk` artifact, unzip, install `app-debug.apk`.
4. Open the app, enable **Focus Lock** under Accessibility, choose apps, start a session.

## Limits
Accessibility-based blocking is strong but not a true kiosk. For an unbreakable lock, set the app as Device Owner and use lock task mode. If the phone restarts mid-session, the lock resumes once the accessibility service reloads.
