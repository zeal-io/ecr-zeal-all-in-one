# ECR Zeal All-in-One (demo ECR app)

> Demo Android ECR (electronic cash register) application used to test the Zeal ECR integration end to end: it receives Zeal transaction broadcasts and answers them, simulating a real third-party ECR.

## What it is

- Small Kotlin/Compose app (`com.alaa.ecrdemo`, compileSdk 36, minSdk 24) with:
  - `MainActivity.kt` - demo UI driving ECR interactions
  - `EcrResponseReceiver.kt` - broadcast receiver that catches Zeal lifecycle broadcasts (amount entry, card detected, e-receipt, transaction done) and sends responses back
- Built against the [epos-sdk](https://github.com/zeal-io/epos-sdk) communication contract, so it exercises the same intent/Gson/SharedPreferences flow a partner ECR app would.

## Use it to test

1. Install on a device (or emulator) together with the Zeal POS/Communicator app.
2. Run a transaction on the POS side; this app receives each lifecycle broadcast and lets you approve/decline or supply the response fields.
3. Verifies the full round trip: broadcast -> callback registration -> response within the SDK's ~20s window.

## Build

```bash
./gradlew :app:assembleDebug
```
