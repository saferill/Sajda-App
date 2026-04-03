#!/usr/bin/env bash

# Sajda App release build helper
# IMPORTANT:
# - Every release build must reuse the same old keystore from keystore.properties.
# - Never create a new keystore for release builds.
# - Reusing the old keystore keeps the APK signature stable so updates remain trusted.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
APP_BUILD_FILE="${ROOT_DIR}/app/build.gradle.kts"
KEYSTORE_PROPERTIES="${ROOT_DIR}/keystore.properties"
APK_OUTPUT_DIR="${ROOT_DIR}/app/build/outputs/apk/release"

fail() {
    echo
    echo "[ERROR] $*" >&2
    exit 1
}

cleanup() {
    if [[ -n "${VERIFY_LOG:-}" && -f "${VERIFY_LOG}" ]]; then
        rm -f "${VERIFY_LOG}"
    fi
}

trap cleanup EXIT
trap 'fail "Build release gagal. Lihat log error di atas."' ERR

read_property() {
    local key="$1"
    local value
    value="$(grep -E "^${key}=" "${KEYSTORE_PROPERTIES}" | head -n 1 | cut -d'=' -f2- || true)"
    value="${value%$'\r'}"
    printf '%s' "${value}"
}

ensure_gradle_setting() {
    local pattern="$1"
    local message="$2"
    grep -Eq "${pattern}" "${APP_BUILD_FILE}" || fail "${message}"
}

run_gradle() {
    if [[ -f "${ROOT_DIR}/gradlew" ]]; then
        bash "${ROOT_DIR}/gradlew" "$@"
        return
    fi

    if command -v gradle >/dev/null 2>&1; then
        gradle "$@"
        return
    fi

    fail "Gradle tidak ditemukan. Tambahkan Gradle ke PATH atau gunakan Gradle wrapper."
}

resolve_apksigner() {
    if command -v apksigner >/dev/null 2>&1; then
        command -v apksigner
        return
    fi

    local sdk_root="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}"
    if [[ -n "${sdk_root}" && -d "${sdk_root}/build-tools" ]]; then
        local candidate
        candidate="$(find "${sdk_root}/build-tools" -type f \( -name 'apksigner' -o -name 'apksigner.bat' -o -name 'apksigner.cmd' \) | sort -V | tail -n 1 || true)"
        if [[ -n "${candidate}" ]]; then
            printf '%s' "${candidate}"
            return
        fi
    fi

    fail "apksigner tidak ditemukan. Pastikan Android SDK Build-Tools terpasang dan ANDROID_SDK_ROOT/ANDROID_HOME sudah benar."
}

run_apksigner_verify() {
    local apksigner_path="$1"
    local apk_path="$2"

    if [[ "${apksigner_path}" == *.bat || "${apksigner_path}" == *.cmd ]]; then
        command -v cmd.exe >/dev/null 2>&1 || fail "cmd.exe tidak tersedia untuk menjalankan apksigner.bat."
        cmd.exe //c "\"${apksigner_path}\" verify --verbose --print-certs \"${apk_path}\""
        return
    fi

    "${apksigner_path}" verify --verbose --print-certs "${apk_path}"
}

echo "==> Memeriksa konfigurasi project"
[[ -f "${KEYSTORE_PROPERTIES}" ]] || fail "keystore.properties tidak ditemukan. Release wajib memakai keystore lama, jangan buat keystore baru."
[[ -f "${APP_BUILD_FILE}" ]] || fail "File app/build.gradle.kts tidak ditemukan."

STORE_FILE_VALUE="$(read_property storeFile)"
STORE_PASSWORD_VALUE="$(read_property storePassword)"
KEY_ALIAS_VALUE="$(read_property keyAlias)"
KEY_PASSWORD_VALUE="$(read_property keyPassword)"

[[ -n "${STORE_FILE_VALUE}" ]] || fail "storeFile kosong di keystore.properties."
[[ -n "${STORE_PASSWORD_VALUE}" ]] || fail "storePassword kosong di keystore.properties."
[[ -n "${KEY_ALIAS_VALUE}" ]] || fail "keyAlias kosong di keystore.properties."
[[ -n "${KEY_PASSWORD_VALUE}" ]] || fail "keyPassword kosong di keystore.properties."

if [[ "${STORE_FILE_VALUE}" = /* || "${STORE_FILE_VALUE}" =~ ^[A-Za-z]:[\\/].* ]]; then
    KEYSTORE_PATH="${STORE_FILE_VALUE}"
else
    KEYSTORE_PATH="${ROOT_DIR}/${STORE_FILE_VALUE}"
fi

[[ -f "${KEYSTORE_PATH}" ]] || fail "Keystore lama tidak ditemukan di ${KEYSTORE_PATH}."

ensure_gradle_setting 'compileSdk\s*=\s*33' "compileSdk harus 33."
ensure_gradle_setting 'minSdk\s*=\s*21' "minSdk harus 21."
ensure_gradle_setting 'targetSdk\s*=\s*33' "targetSdk harus 33."

APKSIGNER_BIN="$(resolve_apksigner)"

echo "==> Membersihkan build lama"
run_gradle clean --no-daemon --stacktrace --console=plain

echo "==> Assemble release APK dengan keystore lama"
run_gradle assembleRelease --no-daemon --stacktrace --console=plain

APK_PATH="$(find "${APK_OUTPUT_DIR}" -maxdepth 1 -type f -name '*.apk' | sort | tail -n 1 || true)"
[[ -n "${APK_PATH}" ]] || fail "APK release tidak ditemukan di ${APK_OUTPUT_DIR}."

VERIFY_LOG="$(mktemp)"

echo "==> Verifikasi signature APK (v1, v2, v3)"
run_apksigner_verify "${APKSIGNER_BIN}" "${APK_PATH}" >"${VERIFY_LOG}" 2>&1 || {
    cat "${VERIFY_LOG}"
    fail "Verifikasi APK dengan apksigner gagal."
}
cat "${VERIFY_LOG}"

grep -Eq 'Verified using v1 scheme .*: true' "${VERIFY_LOG}" || fail "Signature v1 belum terverifikasi."
grep -Eq 'Verified using v2 scheme .*: true' "${VERIFY_LOG}" || fail "Signature v2 belum terverifikasi."
grep -Eq 'Verified using v3 scheme .*: true' "${VERIFY_LOG}" || fail "Signature v3 belum terverifikasi."

echo
echo "Release APK siap:"
echo "${APK_PATH}"

echo
echo "Checklist install manual:"
echo "[ ] Uninstall versi debug/release Sajda App sebelumnya dari device"
echo "[ ] Transfer APK ke device"
echo "[ ] Install APK dari file di atas"
echo "[ ] Pastikan verifikasi signature v1/v2/v3 di atas semuanya TRUE"
