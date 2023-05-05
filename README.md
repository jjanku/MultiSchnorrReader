# MultiSchnorrReader

This is the card reader app for [MultiSchnorrApplet](https://github.com/jjanku/MultiSchnorrApplet). It implements the second party of the MultiSchnorr multi-signature protocol and works together with the applet to sign the entered message.

![App demo](docs/res/demo.gif)

## Build

The easiest option is to build the project in [Android Studio](https://developer.android.com/studio).

Assuming Android SDK is setup properly, the app can also be built from the command line:

```bash
./gradlew assembleDebug
```

The output is located in `./app/build/outputs/`.

Alternatively, grab a pre-built APK from the _Releases_ section.
