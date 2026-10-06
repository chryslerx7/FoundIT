# Implementation Plan - FoundIT Phase 4 (Modern Glass Campus Visual Language & Home/Dashboard Redesign)

Implement the approved **Modern Glass Campus** design language across the FoundIT application, beginning with refining the semantic design system (especially for Dark Mode glassmorphic depth, luminous borders, and refined card surfaces) and redesigning the Home/Dashboard experience (`MainActivity` & `item_card.xml`).

## User Review Required

- **Modern Glass Campus Dark Mode**: Utilizing deep night slate backgrounds (`#0F172A`), translucent glass surface layers (`#1E293B`), subtle luminous borders (`#334155`), and vibrant campus accents (`#60A5FA` primary blue, `#4ADE80` found green, `#F87171` lost red).
- **Home/Dashboard Redesign**: Upgrading `activity_main.xml` and `item_card.xml` to feature modern metric summary cards, search entry banner, and polished listing cards without breaking existing feed or bottom navigation logic.

## Proposed Changes

### Modern Glass Campus Design System & Home Redesign

#### [MODIFY] [colors.xml (night)](file:///C:/Users/jhed/AndroidStudioProjects/FoundIt/app/src/main/res/values-night/colors.xml)
- Evolve dark mode tokens to support Modern Glass Campus aesthetic (`foundit_surface`: `#1E293B`, `foundit_surface_elevated`: `#263449`, `foundit_border`: `#334155`).

#### [MODIFY] [activity_main.xml](file:///C:/Users/jhed/AndroidStudioProjects/FoundIt/app/src/main/res/layout/activity_main.xml)
- Redesign Home header, metric summary cards (Lost vs Found counts), and search entry banner with Modern Glass Campus hierarchy.

#### [MODIFY] [item_card.xml](file:///C:/Users/jhed/AndroidStudioProjects/FoundIt/app/src/main/res/layout/item_card.xml)
- Redesign item cards with CardView, rounded image corners, status badges (LOST/FOUND), and clean typography.

## Verification Plan

### Automated Tests
- Build Android project (`app:assembleDebug`) to verify all XML layouts, resources, and Java compilation.

### Manual Verification
- Verify Home/Dashboard in Light and Dark (Glass Campus) modes.
- Verify listing card rendering, badge styling, and pull-to-refresh.
