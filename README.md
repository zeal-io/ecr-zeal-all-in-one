<h1 align="center">ECR Zeal All-in-One (demo ECR app)</h1>

<p align="center"><i>Demo Android ECR (electronic cash register) application used to test the Zeal ECR integration end to end: it receives Zeal transaction broadcasts and answers them, simulating a real third-party ECR.</i></p>

<p align="center">
  <img src="https://img.shields.io/badge/lang-Kotlin-7F52FF" alt="Language">
  <img src="https://img.shields.io/badge/stack-Android%20%2F%20Compose-339933" alt="Stack">
  <img src="https://img.shields.io/badge/status-active-2EA44F" alt="Status">
  <img src="https://img.shields.io/badge/repo-public-24292F" alt="Visibility">
</p>

---
## Contents

- [What it is](#what-it-is)
- [Use it to test](#use-it-to-test)
- [Build](#build)

<p align="right"><a href="#contents">Back to contents</a></p>

## What it is

- Small Kotlin/Compose app (`com.alaa.ecrdemo`, compileSdk 36, minSdk 24) with:
  - `MainActivity.kt` - demo UI driving ECR interactions
  - `EcrResponseReceiver.kt` - broadcast receiver that catches Zeal lifecycle broadcasts (amount entry, card detected, e-receipt, transaction done) and sends responses back
- Built against the [epos-sdk](https://github.com/zeal-io/epos-sdk) communication contract, so it exercises the same intent/Gson/SharedPreferences flow a partner ECR app would.

<p align="right"><a href="#contents">Back to contents</a></p>

## Use it to test

1. Install on a device (or emulator) together with the Zeal POS/Communicator app.
2. Run a transaction on the POS side; this app receives each lifecycle broadcast and lets you approve/decline or supply the response fields.
3. Verifies the full round trip: broadcast -> callback registration -> response within the SDK's ~20s window.

<p align="right"><a href="#contents">Back to contents</a></p>

## Build

```bash
./gradlew :app:assembleDebug
```

## License and contribution

Internal Zeal repository - proprietary; no open-source license applies.

Contribution: open a pull request against the default branch; keep changes minimal and described. For releases/deployments follow the platform CD process - never force-push shared branches.
