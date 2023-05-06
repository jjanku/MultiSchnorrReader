# MultiSchnorrReader

This is the card reader app for [MultiSchnorrApplet](https://github.com/jjanku/MultiSchnorrApplet). It implements the second party of the MultiSchnorr multi-signature protocol and works together with the applet to sign the entered message.

![App demo](docs/res/demo.gif)

## Quickstart

1. Enter a message you wish to sign (ASCII, 32 chars max, will be padded by zero bytes).
2. Optionally, enable any of the optimizations below.
3. Hold a card with the applet installed near the back of the phone.
4. The phone establishes a new group with the card and subsequently signs the selected message. The next time the same card is used, the group key is not regenerated unless a different card was used in the meantime or the state of the app was reset.
5. Share the created signature by tapping on the displayed card. The output is in JSON format shown below and can be verified using the bundled script [verify.sage](./verify.sage).

```json
{
    "group": {
        "x": 82621371836311322635731610237257798577123535778674168916413120265184559472745,
        "y": 21073772193838990103938873926270519023402430518253746786624584882368307768791
    },
    "message": "68656c6c6f000000000000000000000000000000000000000000000000000000",
    "nonce": {
        "x": 28362538501509449895377600989128143307925603662865794767456834195405898871613,
        "y": 16208392399936672946292064678176285964271568821992841796429050116123184365933
    },
    "signature": 104434989019765740973662633870938994847065819938220884369704931829053739864387
}
```

### Optimizations

_Probabilistic_: Requests the card to speed up its computation by guessing some intermediate results. The app retries the signing process until the card succeeds. This option has no effect when the applet is compiled with `OperationSupport.EC_HW_XY = true`.

_Piggyback_: When enabled, the app signs the message as before but additionally asks the card to prepare for the next signature. The status "cached nonce" gets displayed. If you then disable this option, the following signature attempt will be faster, thanks to the preprocessing.

## Build

The easiest option is to build the project in [Android Studio](https://developer.android.com/studio).

Assuming Android SDK is setup properly, the app can also be built from the command line:

```bash
./gradlew assembleDebug
```

The output is located in `./app/build/outputs/`.

Alternatively, grab a pre-built APK from the _Releases_ section.
