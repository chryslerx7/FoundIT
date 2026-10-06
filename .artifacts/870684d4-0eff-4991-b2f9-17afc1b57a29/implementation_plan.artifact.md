# Implementation Plan - FoundIT Phase 4A-1 (Global Design System & Authentication Redesign)

Implement Phase 4A-1 of the FoundIT Lost & Found application, establishing the global FoundIT design system with light and dark theme semantic colors, redesigning the Login and Register screens, and upgrading authentication loading and error states while preserving all existing logic, session management, and navigation.

## User Review Required

- **Semantic Color Architecture**: Establishing semantic color tokens (`foundit_background`, `foundit_surface`, `foundit_primary`, `foundit_text_primary`, etc.) across `res/values/colors.xml` and `res/values-night/colors.xml`.
- **UI-Only Auth Redesign**: Redesigning `LoginActivity` and `RegisterActivity` UI layouts with Material 3 TextInputLayouts and polished styling without altering backend calls, session management, or authentication logic.

## Proposed Changes

### Global Design System

#### [MODIFY] [colors.xml](file:///C:/Users/jhed/AndroidStudioProjects/FoundIt/app/src/main/res/values/colors.xml)
- Define comprehensive Light theme semantic colors (`foundit_background`, `foundit_surface`, `foundit_surface_elevated`, `foundit_primary`, `foundit_primary_pressed`, `foundit_primary_light`, `foundit_text_primary`, `foundit_text_secondary`, `foundit_text_muted`, `foundit_border`, `foundit_lost`, `foundit_found`, `foundit_warning`).

#### [MODIFY] [colors.xml (night)](file:///C:/Users/jhed/AndroidStudioProjects/FoundIt/app/src/main/res/values-night/colors.xml)
- Define comprehensive Dark theme semantic colors matching the design spec (Dark slate background `#0F172A`, surface `#1E293B`, primary `#60A5FA`, etc.).

#### [MODIFY] [themes.xml](file:///C:/Users/jhed/AndroidStudioProjects/FoundIt/app/src/main/res/values/themes.xml) & [themes.xml (night)](file:///C:/Users/jhed/AndroidStudioProjects/FoundIt/app/src/main/res/values-night/themes.xml)
- Update Material 3 DayNight themes to map primary colors, status bar, and navigation bar colors correctly.

---

### Authentication Redesign

#### [MODIFY] [activity_login.xml](file:///C:/Users/jhed/AndroidStudioProjects/FoundIt/app/src/main/res/layout/activity_login.xml)
- Redesign login layout using Material 3 `TextInputLayout`, `TextInputEditText`, polished spacing, typography hierarchy, primary blue button, and progress indicator.

#### [MODIFY] [LoginActivity.java](file:///C:/Users/jhed/AndroidStudioProjects/FoundIt/app/src/main/java/com/example/foundit/LoginActivity.java)
- Ensure loading state disables button/inputs, prevents duplicate submissions, and handles errors cleanly.

#### [MODIFY] [activity_register.xml](file:///C:/Users/jhed/AndroidStudioProjects/FoundIt/app/src/main/res/layout/activity_register.xml)
- Redesign register layout with scrollable container, Material 3 inputs for Full Name, Student ID, School Email, Password, and Confirm Password with password toggle.

#### [MODIFY] [RegisterActivity.java](file:///C:/Users/jhed/AndroidStudioProjects/FoundIt/app/src/main/java/com/example/foundit/RegisterActivity.java)
- Ensure robust validation, loading state handling, and inline/toast error presentation.

#### [MODIFY] [layout_bottom_navigation.xml](file:///C:/Users/jhed/AndroidStudioProjects/FoundIt/app/src/main/res/layout/layout_bottom_navigation.xml)
- Update background and tint colors to use semantic tokens (`foundit_surface`, `foundit_muted`, etc.).

## Verification Plan

### Automated Tests
- Build Android project (`app:assembleDebug`) to verify all XML resources, styles, and Java compilation.

### Manual Verification
- Verify Login UI in Light and Dark modes.
- Verify Register UI in Light and Dark modes.
- Verify validation, loading state (duplicate submission prevention), password toggle.
- Verify theme switching (Light, Dark, System Default).
