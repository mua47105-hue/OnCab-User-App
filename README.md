# OnCab User App

Rebuild of the **OnCab** user ride-booking application (Kolkata cab + bus service),
restored end-to-end from the decompiled release APK (`com.oncab.user`, version 1.10.25,
versionCode 40) using the REA (Reverse Engineer Anything) methodology and verified with
the Gauntlet Loop quality process.

## How it was rebuilt

1. **Static analysis (REA)** — full decomposition of the decompiled APK:
   - `AndroidManifest.xml`: package `com.oncab.user`, minSdk 24 / targetSdk 36, all
     activities, services (LocationService, FCM OrderNotification), Maps API key,
     permissions and queries.
   - `sources/`: 405 Java/Kotlin classes — auth flow (`otp-send`, `user-login`,
     `user-create`), Retrofit `WebService` (45+ endpoints), booking flow, bus/shuttle
     module, referral, help, SOS, chats.
   - `resources/`: original layouts, drawables, mipmaps, themes (`Theme.CabBookDriver`),
     fonts, Lottie animations, navigation graph and menus.

2. **Verbatim resource restore** — the complete original `res/` tree (1,220 files) is
   bundled unmodified, so every screen is pixel-identical to the shipped app: the same
   strings, colors, icons, spacing, animations and night-mode variants.

3. **Clean-room code** — the same package/class/flow structure rewritten as
   compilable Java against the original layouts (ViewBinding regenerates the original
   binding classes): splash + banner, onboarding, permissions, phone login, OTP with
   PinView, profile completion, home map, ride history filters, bus bookings tabs,
   profile, settings and all navigation-graph destinations.

4. **Cloud build** — GitHub Actions (sandbox has no Android SDK): Gradle 8.11.1,
   AGP 8.9.2, JDK 17, compileSdk 36. Every push to `main` produces debug + release
   APKs and attaches them to GitHub Release `v1.10.25`.

## Project layout

```
app/src/main/
├── AndroidManifest.xml          # cleaned original manifest
├── java/com/oncab/user/         # rebuilt application code
│   ├── MainActivity             # drawer + bottom nav + nav host
│   ├── Activity/…               # phone validation, OTP, profile completion
│   ├── ui/…                     # home maps, booking, history, shuttle, account…
│   ├── Service/…                # FCM notifications, foreground location
│   └── Utils/…                  # Retrofit WebService, constants, prefs
├── res/                         # 1,220 original resources (verbatim)
└── …
```

## Build locally

```bash
gradle assembleDebug          # debug APK
gradle assembleRelease        # release APK (debug-signed)
```

## API surface (unchanged from the original app)

- Base: `https://oncab.in/control/api/`
- Auth: `otp-send`, `user-login`, `user-create`
- Rides: `user-order-list`, `booking-status`, `get-banners`, `logout`
- Bus: `https://oncab.in/bus-operator-dev/api2/bus/user-bookings`
