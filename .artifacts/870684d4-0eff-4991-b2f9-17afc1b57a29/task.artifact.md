# Icon Fix Tasks

- `[x]` 1. Replace legacy mipmap density resources with `new-cion.png`
    - `[x]` Update `mdpi` resources
    - `[x]` Update `hdpi` resources
    - `[x]` Update `xhdpi` resources
    - `[x]` Update `xxhdpi` resources
    - `[x]` Update `xxxhdpi` resources
- `[x]` 2. Update Adaptive Icon Configuration
    - `[x]` Verify `res/mipmap-anydpi-v26/ic_launcher.xml`
    - `[x]` Verify `res/mipmap-anydpi-v26/ic_launcher_round.xml`
- `[x]` 3. Update Splash Screen & Branding
    - `[x]` Update `res/drawable/new_cion.png` with `new-cion.png`
- `[x]` 4. Verify Manifest and Themes
    - `[x]` Verify `AndroidManifest.xml` points to `@mipmap/ic_launcher`
    - `[x]` Verify `themes.xml` points to `@mipmap/ic_launcher`
- `[x]` 5. Verification
    - `[x]` Run Gradle build (assembleDebug)
