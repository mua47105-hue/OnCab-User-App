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
│   ├── ui/nightops/…            # Night Ops module (see below)
│   ├── Service/…                # FCM notifications, foreground location
│   └── Utils/…                  # Retrofit WebService, constants, prefs
├── res/                         # 1,220 original resources (verbatim)
└── …
```

## Night Ops module (`ui/nightops`)

A fully implemented night-mode ride flow, built from the Gauntlet-approved
`proto3_night_ops` design (phase 3) with a pixel-verified palette
(`res/values/colors_night_ops.xml`, measured against the approved PNG).

- **Entry** — "Night Ops" card on the home map sheet opens the forced-dark
  `NightOpsActivity` (`Theme.OnCab.NightOps`).
- **Live night map** — `NightMapView` is a Canvas port of the approved
  cartography: Hooghly river, Kolkata street grid, Maidan block, glowing
  amber route (Park St → Esplanade via Chowringhee), pulsing pickup pin,
  nearby cab dots and a live cab with headlight beams driving the route.
- **Ride flow** — launches in the approved confirmed state: OTP `5731`
  tiles, driver card (Sanjib Das ★4.9 · White Hyundai Aura · WB 02 AB 4412),
  ARRIVING-IN ETA countdown, locked fare ₹160 (no night surge), Wallet
  payment, Share trip (system chooser), SOS.
- **Boarding** — driver "arrives" when the ETA elapses; start-trip OTP is
  verified against the state machine with a 3-attempt lockout.
- **SOS** — full-screen red overlay with a real 3-second cancel countdown
  (`SosCountdown`), haptic on expiry, Call Police 100 (`ACTION_DIAL` only —
  never auto-called) and share-location SMS. No dangerous permissions added.
- **State machine** — `NightOpsStateMachine` (pure Java): confirmed →
  arriving → arrived → in-trip → complete → rating; cancellation rules
  enforced; covered by JUnit suites run in CI (`testDebugUnitTest`), plus
  CI checks that the built APK actually contains the NightOps classes,
  layouts and strings.

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
