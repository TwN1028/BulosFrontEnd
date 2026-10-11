# Device ID verification

The Android client sends an installation-scoped UUID v4 in the `X-Device-ID` header through the shared backend client. The preference file remains excluded from Android cloud backup and device transfer.

## Automated coverage

`InstallationIdPersistenceTest` uses Android's real `SharedPreferences` to verify persistence across `InstallationIdProvider` recreation and header reuse. This is provider recreation in one app process, not proof of a process restart. Invalid values and non-v4 UUIDs are replaced. A controllable store verifies the otherwise difficult-to-induce `SharedPreferences.commit()` failure path, including stable in-memory reuse and a later persistence retry.

## Manual process-restart check

1. Install a debug build and obtain the generated `installation_uuid` from `installation_identity.xml` with Android Studio Device Explorer. Keep it in a secure local note; do not put the full value in logs or bug reports.
2. Force-stop the app (or run `adb shell am force-stop com.example.bulosfrontend`) without clearing app data, then launch it again.
3. Read the preference again and confirm it is unchanged and is a UUID v4.

Uninstalling, clearing app data, or restoring/transferring to another device is expected to produce a new installation ID.

## Backend contract follow-up

The deployed OpenAPI URL was checked on 2026-10-03, but the Render service returned no bytes before repeated 30-second and longer timeouts. No backend source is present in this workspace, and the owner's public GitHub repositories do not include the backend. No translation or history request was made because those calls create records.

The prior audit established that history requires `X-Device-ID` at runtime while its OpenAPI parameter is marked optional. The backend owner still needs to mark that history header as required in OpenAPI. Translation's runtime requirement remains unverified independently; it must be checked from backend source or a non-record-creating/staging test before its OpenAPI declaration is changed. It should not be inferred from history behavior.
