# FoundIT - Project Documentation

FoundIT is a comprehensive Lost and Found management system designed for campus or small community environments. It consists of a native Android application and a Laravel PHP backend API.

---

## 1. System Architecture

The project follows a **Client-Server Architecture**:
- **Frontend:** Native Android (Java) utilizing Retrofit for API communication and Glide for image handling.
- **Backend:** Laravel PHP Framework (RESTful API) with a MySQL database.
- **Communication:** JSON over HTTP, secured by Laravel Sanctum (Bearer Tokens).

---

## 2. Database Schema (MySQL)

### `users`
Tracks authenticated users and their roles.
- `id`: Unique identifier (Primary Key).
- `name`: Full name of the user.
- `student_id`: Unique identifier for campus tracking.
- `email`: Login credential.
- `role`: `enum('student','teacher','staff','visitor')`.
- `profile_image`: Path to stored image.
- `password`: Hashed credential.

### `items`
Stores lost and found reports.
- `id`: Unique identifier (Primary Key).
- `user_id`: Reference to the reporter.
- `item_name`: Name of the item.
- `category`: Category of the item.
- `description`: Detailed text.
- `location`: Where the item was lost/found.
- `date`: Date of the event.
- `type`: `enum('LOST','FOUND')`.
- `status`: `enum('ACTIVE','RESOLVED')`.
- `image`: Path to the item image.

### `conversations` & `messages`
Handles real-time communication between reporters.
- `conversations`: Links a Lost report to a Found report.
- `messages`: Stores individual chat logs between two users.

### `notifications`
- Tracks updates like "Report Submitted" or "Item Matched".

---

## 3. Laravel API Reference

### Authentication
- `POST /api/register`: Creates a new account.
- `POST /api/login`: Validates credentials and returns a Bearer Token.
- `POST /api/logout`: Revokes the current access token.
- `GET /api/me`: Returns the current user profile.

### Item Management
- `GET /api/items`: List all active items (supports `search`, `type`, and `category` filters).
- `POST /api/items`: Create a new report (supports Multipart Image upload).
- `PUT /api/items/{id}`: Update an existing report.
- `DELETE /api/items/{id}`: Remove a report.
- `POST /api/items/{id}/resolve`: Mark an item as resolved.

### Profile
- `PUT /api/profile`: Update name, email, student ID, password, or profile image.

---

## 4. Android Application Components

### Core Activities
- **LoginActivity / RegisterActivity:** Handles user onboarding.
- **MainActivity:** Dashboard showing the latest lost/found items.
- **SearchActivity:** Advanced filtering with auto-refresh on selection.
- **ReportActivity:** Form for submitting new lost or found reports.
- **EditProfileActivity:** Allows users to update info or remove their profile photo.
- **ChatActivity:** Real-time messaging between users.

### Utilities
- `SessionManager`: Manages SharedPreferences for token and user data persistence.
- `RetrofitClient`: Configures the HTTP client, including logging and base URL.
- `ThemeManager`: Handles light/dark mode switching.

---

## 5. Scalability & Performance Analysis

The application is designed to handle **100+ users** efficiently:

### Backend Scalability
- **MySQL Indexing:** Columns like `email`, `student_id`, and `type` are indexed, ensuring searches remain fast as the user base grows.
- **Stateless Auth:** Laravel Sanctum uses database-backed tokens rather than server sessions, which minimizes memory usage per user.
- **Pagination:** The `GET /api/items` endpoint limits responses to 50 items per page, preventing server overload and reducing mobile data consumption.

### Frontend Performance
- **Image Optimization:** Glide handles memory caching and image downscaling, ensuring the UI doesn't stutter even when displaying many photos.
- **Lazy Loading:** RecyclerView is used for all lists, recycling views as the user scrolls to keep memory usage low.

---

## 6. Maintenance & Setup

### Backend Setup
1. Configure `.env` with database credentials and `MAX_USERS`.
2. Run `php artisan storage:link` to make images accessible.
3. Ensure the PC's IPv4 address matches the `BASE_URL` in Android's `RetrofitClient.java`.

### Image Storage
Images are stored in `storage/app/public/`. 
- Profiles: `storage/app/public/profiles/`
- Items: `storage/app/public/items/`

---

## 7. Changelog

### [v1.0.0] - 2026-09-04
- Initial release with core Lost and Found management features.
- Android application with Java + Retrofit architecture.
- Laravel backend with MySQL and Sanctum authentication.

### [v1.0.1] - 2026-09-04
#### Fixed
- **System UI Overlap:** Implemented full Edge-to-Edge support across all Android screens, ensuring headers and navigation bars do not overlap with the Android status bar and navigation area.
- **Report Form Scrolling:** Fixed measurement issues in Lost and Found report forms; added `windowSoftInputMode="adjustResize"` and `NestedScrollView` to ensure all fields are reachable when the keyboard is visible.
- **Item Details Flicker:** Resolved owner-action button flicker by implementing a loading state and hiding sensitive actions until ownership is positively confirmed.
- **Possible Match Access:** Restricted match results to report owners only. Added backend authorization and UI guards to prevent unrelated users from viewing or messaging through matches they don't own.

#### Improved
- **Possible Match Algorithm:** Refactored matching logic to use a weighted scoring model (Name, Category, Location, and Date proximity) instead of broad OR queries, significantly increasing match accuracy.
- **UI UX:** Added "Not Matched" button to the Possible Match screen for easier navigation back to the Home screen.
- **Backend Security:** Enforced strict ownership checks for match retrieval and unauthorized API requests.

---

> [!TIP]
> **Proactive Debugging:** Always check `logcat` in Android Studio and `storage/logs/laravel.log` on the server if issues arise.
