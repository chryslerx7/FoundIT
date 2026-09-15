# Implementation Plan - Android Application Icon Fix

This plan details the steps to correctly update the FoundIT application icon using the new source file `new-cion.png`, ensuring all legacy and adaptive icon resources are synchronized.

## User Review Required

> [!IMPORTANT]
> - **Icon Source**: `new-cion.png` will be used as the definitive source for all launcher icon resources.
> - **Density Scaling**: Since I cannot perform high-quality image resizing, I will use the `new-cion.png` content for all density folders. This ensures the correct design is visible on all devices, though it may result in larger resource sizes than optimized icons.
> - **Adaptive Icon**: The existing FoundIT blue background (`#2563EB`) will be preserved.

## Proposed Changes

### 1. Launcher Icon Resources (Mipmaps)

#### [MODIFY] [res/mipmap-*](file:///C:/Users/jhed/AndroidStudioProjects/FoundIt/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png)
- Replace all instances of `ic_launcher.png`, `ic_launcher_round.png`, and `ic_launcher_foreground.png` in all density folders (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`) with the content of `new-cion.png`.
- Remove any remaining `ic_launcher.webp` or `ic_launcher_round.webp` files to prevent conflicts.

### 2. Adaptive Icon Configuration

#### [MODIFY] [ic_launcher.xml](file:///C:/Users/jhed/AndroidStudioProjects/FoundIt/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml)
#### [MODIFY] [ic_launcher_round.xml](file:///C:/Users/jhed/AndroidStudioProjects/FoundIt/app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml)
- Ensure they point to `@mipmap/ic_launcher_foreground`.

### 3. Splash Screen & Branding

#### [MODIFY] [ic_launcher_playstore.png](file:///C:/Users/jhed/AndroidStudioProjects/FoundIt/app/src/main/res/drawable/ic_launcher_playstore.png)
- Update this drawable resource to match `new-cion.png`, as it is used in the `activity_splash.xml`.

### 4. Manifest & Themes

#### [VERIFY] [AndroidManifest.xml](file:///C:/Users/jhed/AndroidStudioProjects/FoundIt/app/src/main/AndroidManifest.xml)
- Confirm `android:icon` and `android:roundIcon` point to `@mipmap/ic_launcher` and `@mipmap/ic_launcher_round`.

#### [VERIFY] [themes.xml](file:///C:/Users/jhed/AndroidStudioProjects/FoundIt/app/src/main/res/values/themes.xml)
- Confirm `windowSplashScreenAnimatedIcon` points to `@mipmap/ic_launcher`.

## Verification Plan

### Automated Tests
- `gradle_build` (assembleDebug) to verify resource linking.

### Manual Verification
1. **Launcher Icon**: Deploy the app and verify the icon on the home screen and app drawer.
2. **Splash Screen**: Launch the app and verify the logo displayed during splash matches `new-cion.png`.
3. **App Info**: Check the icon in the system "App Info" settings page.
