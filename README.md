# AC Remote Easy

A calm, ad-free Android IR remote for air conditioners — **Kotlin**, **Jetpack Compose**, **Material 3**.

Point your phone, pick a brand pack, test a few codes, and control power / temperature / mode / fan / swing. Favorites and timer stubs round out the v1 UX.

> **Disclaimer:** AC Remote Easy is an independent open-source project. It is **not affiliated with, endorsed by, or sponsored by** Voltas, LG, Samsung, Daikin, Haier, Panasonic, Carrier, Blue Star, or any other AC manufacturer. Brand names are used only to identify compatible device categories.

## Plan summary

| Area | v1 |
|------|----|
| Brands | Voltas, LG, Samsung, Daikin, Haier, Panasonic, Carrier, Blue Star |
| Controls | Power, temp ±, mode, fan, swing, test codes |
| Setup | Guided add flow: brand → name → test → save |
| Storage | DataStore for devices, favorites, timer stubs |
| IR | `ConsumerIrManager` via `IrTransmitter` + haptic on send |
| Learn | UI stub / tips only (capture pipeline later) |
| Ads | None |

## IR requirements

- Android phone with a hardware **IR blaster** (`android.hardware.consumerir`)
- Permission: `TRANSMIT_IR` (declared; feature marked **optional** so the app still installs without IR)
- Clear empty / banner UI when `hasIrEmitter()` is false — browse and set up still work; transmit is disabled

### Placeholder brand packs

JSON packs live under `app/src/main/assets/brands/`. They contain **minimal sample patterns** (NEC-like placeholder timings) so the app structure, loader, and test flow work end-to-end.

**These codes are not verified against real remotes.** Expand each pack with measured captures (or a trusted database) before relying on daily use. See the `protocolNote` field in each JSON file.

## Features

- **Home** — device list, premium empty state, IR-unavailable banner, FAB to add
- **Add brand** — stepped flow with progress, radio brand pick, naming, guided test buttons, learn-mode tip
- **Remote** — large touch targets, power / temp / mode / fan / swing, test codes, favorite + delete
- **Favorites** — one-tap send with haptics
- **Timers** — polished stub UI (exact background alarms planned later)
- **No ads**, MIT license

## Requirements

- Android Studio Ladybug (2024.2+) or newer recommended
- JDK 17
- minSdk **26**, targetSdk / compileSdk **35**
- Device with IR preferred for transmit testing

## Open in Android Studio

1. Clone this repo
2. **File → Open** the project root
3. Let Gradle sync
4. Run the **app** configuration

## Build a debug APK

```bash
./gradlew assembleDebug
```

Output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Project layout

```text
app/src/main/
  assets/brands/          # 8 brand pack JSON files (placeholders)
  java/com/ijad/acremoteeasy/
    MainActivity.kt
    ir/IrTransmitter.kt
    data/                 # models, BrandPackLoader, AppRepository (DataStore)
    ui/
      home/ add/ remote/ favorites/ timer/
      components/ theme/ navigation/
```

## License

MIT — see [LICENSE](LICENSE).
