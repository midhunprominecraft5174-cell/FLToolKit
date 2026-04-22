# MDM Admin App

A Device Owner MDM app that:
- ✅ Keeps **Developer Options** always ON
- ✅ Keeps **Multi-User** always ON
- ✅ Shows **all active UserRestrictions** from any app
- ✅ Lets you **remove restrictions** set by this app or other apps (as Device Owner)
- ✅ Reapplies policies on **every boot**

---

## Setup Instructions

### Step 1 — Install the APK
Build and install on the device via Android Studio or:
```
adb install app-debug.apk
```

### Step 2 — Remove Google Accounts (IMPORTANT)
Android blocks setting a Device Owner if any Google/user accounts exist.
- Go to **Settings → Accounts** and remove ALL accounts
- Or use a freshly factory-reset device

### Step 3 — Enable USB Debugging
- Settings → About Phone → tap Build Number 7 times
- Settings → Developer Options → enable USB Debugging

### Step 4 — Set as Device Owner via ADB
Connect device via USB and run:
```bash
adb shell dpm set-device-owner com.mdm.deviceowner/.AdminReceiver
```

Expected output:
```
Success: Device owner set to package com.mdm.deviceowner
```

### Step 5 — Open the App
The dashboard will show:
- ✅ Device Owner — Full Control Active
- Toggles for Developer Options and Multi-User
- List of all active restrictions with Remove buttons

---

## How Restrictions Work

| Source | Can We Remove? |
|--------|----------------|
| Set by this app | ✅ Always |
| Set by another Device Owner | ❌ Conflicts — only one Device Owner allowed |
| Set by a Profile Owner | ⚠️ Sometimes (Device Owner > Profile Owner for some) |
| System-level restrictions | ❌ Cannot override |

---

## File Structure
```
app/src/main/
├── java/com/mdm/deviceowner/
│   ├── AdminReceiver.java       ← Device Admin/Owner receiver
│   ├── DeviceOwnerManager.java  ← All MDM logic
│   ├── MainActivity.java        ← Dashboard UI
│   ├── RestrictionAdapter.java  ← RecyclerView adapter
│   └── RestrictionItem.java     ← Data model
└── res/
    ├── layout/activity_main.xml
    ├── layout/item_restriction.xml
    ├── values/strings.xml
    └── xml/device_admin.xml     ← Required admin policy declaration
```
