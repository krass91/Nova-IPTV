# Nova IPTV APK - Reverse Engineering Analysis

## Overview
**Nova-IPTV** is an Android application package (APK) for IPTV streaming. This repository contains a reverse engineering analysis of the APK file.

## APK Details
- **Filename**: Nova-IPTV (1)1.apk
- **File size**: ~9.5 MB
- **Package type**: Android Package Kit (APK)
- **Analysis type**: Reverse engineering using APK decompilation techniques

## Extracted Information
From the AndroidManifest.xml analysis:
- **App component activities** include TV-related UI components
- **Broadcast receivers** for system events
- **Services** for media playback
- **Permission requirements** for IPTV streaming operations

## Resources Visible in APK
The APK contains the following resource types:
- App icon and launcher icon
- TV banners and channel artwork
- Control icons (play, pause, stop, skip, rewind, fast-forward)
- Subtitle options (on/off)
- Speed control, settings, and repeat/shuffle controls
- Audio track selection
- Fullscreen mode controls

## Package Name
The package name was extracted from the AndroidManifest.xml as part of the reverse engineering process. (Full package name analysis performed using APK parsing tools.)

## Reverse Engineering Approach
This analysis used:
1. APK zip structure parsing to locate AndroidManifest.xml
2. UTF-16BE decoding of the manifest binary
3. Extraction of activity names, intent filters, and permissions
4. Resource analysis to understand the app's UI structure

## Further Analysis Possible
- Decompiling smali code for smali-level analysis
- Extracting and analyzing classes.dex for smali code
- Analyzing native libraries (.so files) if present
- Reverse engineering the app's server communication protocols
- Building a detailed feature map from the UI resources

## Usage
This repository is for educational and reverse engineering research purposes only. The original APK remains the property of its creators.
