# Implementation Plan - FoundIT Phase 3 (v1.0.3)

Implement Phase 3 of the FoundIT Lost & Found application across both the Laravel backend (`foundit-api`) and the Android client (`FoundIt`), adding Search by Date, Multiple Photos (up to 5), Item Details thumbnails, Loading/Empty/Error states, Search Stale Request Protection, and Off-UI thread Image Compression/Resizing.

## User Review Required

- **Additive Database Migration**: Adding `item_images` table with foreign key and cascade deletion. Existing items and their `image` field remain fully intact and compatible.
- **Max 5 Photos**: Enforced both on Android UI and Laravel backend validation.
- **Backward Compatibility**: Reports without `item_images` will fall back to `image_url` seamlessly.

## Proposed Changes

### Backend (`foundit-api`)

#### [NEW] [create_item_images_table.php](file:///C:/xampp/htdocs/Laravel%20Projects/foundit-api/database/migrations/2026_10_06_000001_create_item_images_table.php)
- Add additive migration for `item_images` (`id`, `item_id`, `path`, `position`, timestamps).

#### [NEW] [ItemImage.php](file:///C:/xampp/htdocs/Laravel%20Projects/foundit-api/app/Models/ItemImage.php)
- Eloquent model for item images with `image_url` accessor and relationship to `Item`.

#### [MODIFY] [Item.php](file:///C:/xampp/htdocs/Laravel%20Projects/foundit-api/app/Models/Item.php)
- Add `images()` relationship and include `images` in appends / eager loading when appropriate.

#### [MODIFY] [ItemController.php](file:///C:/xampp/htdocs/Laravel%20Projects/foundit-api/app/Http/Controllers/Api/ItemController.php)
- Support `date=YYYY-MM-DD` query parameter in `index()`.
- Support `images[]` upload (up to 5) in `store()` and `update()`, while preserving fallback to single `image`. Set `image` to the first image for v1.0.2 compatibility.
- Ensure proper cleanup of `item_images` and files on update/delete. Eager load `images` in `index`, `show`, `myReports`, `store`, `update`.

---

### Android (`FoundIt`)

#### [NEW] [ItemImage.java](file:///C:/Users/jhed/AndroidStudioProjects/FoundIt/app/src/main/java/com/example/foundit/model/ItemImage.java)
- Model representing an item image (`id`, `item_id`, `path`, `image_url`, `position`).

#### [NEW] [ItemImageAdapter.java](file:///C:/Users/jhed/AndroidStudioProjects/FoundIt/app/src/main/java/com/example/foundit/adapter/ItemImageAdapter.java)
- RecyclerView adapter for displaying image thumbnails in `ReportActivity` and `ItemDetailActivity`.

#### [NEW] [item_thumbnail.xml](file:///C:/Users/jhed/AndroidStudioProjects/FoundIt/app/src/main/res/layout/item_thumbnail.xml)
- Layout for individual image thumbnails.

#### [MODIFY] [Item.java](file:///C:/Users/jhed/AndroidStudioProjects/FoundIt/app/src/main/java/com/example/foundit/model/Item.java)
- Add `public List<ItemImage> images;`.

#### [MODIFY] [ApiService.java](file:///C:/Users/jhed/AndroidStudioProjects/FoundIt/app/src/main/java/com/example/foundit/api/ApiService.java)
- Update `getItems` with `@Query("date") String date`.
- Update `createItem` and `updateItem` to accept `List<MultipartBody.Part> images` (alongside single image for compatibility).

#### [MODIFY] [activity_search.xml](file:///C:/Users/jhed/AndroidStudioProjects/FoundIt/app/src/main/res/layout/activity_search.xml)
- Add Date filter UI elements (date selection view/button, clear date button).

#### [MODIFY] [SearchActivity.java](file:///C:/Users/jhed/AndroidStudioProjects/FoundIt/app/src/main/java/com/example/foundit/SearchActivity.java)
- Implement DatePicker, clear date, proper loading/empty/error states, and Stale Request Protection (request sequence number / call cancellation).

#### [MODIFY] [activity_report.xml](file:///C:/Users/jhed/AndroidStudioProjects/FoundIt/app/src/main/res/layout/activity_report.xml)
- Add horizontal RecyclerView for thumbnails, photo counter (e.g., `0/5`), and add photo button.

#### [MODIFY] [ReportActivity.java](file:///C:/Users/jhed/AndroidStudioProjects/FoundIt/app/src/main/java/com/example/foundit/ReportActivity.java)
- Implement multi-photo selection (up to 5), camera/gallery appending, remove photo logic, counter display, and off-UI thread image compression/resizing.

#### [MODIFY] [activity_item_detail.xml](file:///C:/Users/jhed/AndroidStudioProjects/FoundIt/app/src/main/res/layout/activity_item_detail.xml)
- Add thumbnail RecyclerView below the main image.

#### [MODIFY] [ItemDetailActivity.java](file:///C:/Users/jhed/AndroidStudioProjects/FoundIt/app/src/main/java/com/example/foundit/ItemDetailActivity.java)
- Implement thumbnail selection for main image, fallback to `image_url` if `images` is empty, robust loading/content/error states.

#### [MODIFY] [MainActivity.java](file:///C:/Users/jhed/AndroidStudioProjects/FoundIt/app/src/main/java/com/example/foundit/MainActivity.java) & [MyReportsActivity.java](file:///C:/Users/jhed/AndroidStudioProjects/FoundIt/app/src/main/java/com/example/foundit/MyReportsActivity.java)
- Ensure robust Loading, Empty, Error, and Content states without flashing stale data.

## Verification Plan

### Automated Tests
- Laravel backend: Run phpunit / artisan migrate / model unit checks.
- Android: Gradle build (`app:assembleDebug`).

### Manual Verification
- Verify Search by date, date picker, clear date.
- Verify multi-photo upload (1 to 5 images), rejecting 6th image.
- Verify removal of photos and updates.
- Verify Item Details thumbnail switching and fallback.
- Verify loading states, empty states, error states, and stale search protection.
