# Sajda App release build helper
# IMPORTANT:
# - Every release build must reuse the same old keystore from keystore.properties.
# - Never create a new keystore for release builds.
# - Reusing the old keystore keeps the APK signature stable so updates remain trusted.

$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RootDir = Split-Path -Parent $ScriptDir
$AppBuildFile = Join-Path $RootDir "app\build.gradle.kts"
$KeystorePropertiesPath = Join-Path $RootDir "keystore.properties"
$ApkOutputDir = Join-Path $RootDir "app\build\outputs\apk\release"

function Fail([string]$Message) {
    Write-Host ""
    Write-Error $Message
    exit 1
}

function Read-Property([string]$Key) {
    $line = Get-Content $KeystorePropertiesPath | Where-Object { $_ -match "^$Key=" } | Select-Object -First 1
    if (-not $line) { return "" }
    return ($line -replace "^$Key=", "").Trim()
}

function Ensure-GradleSetting([string]$Pattern, [string]$Message) {
    if (-not (Select-String -Path $AppBuildFile -Pattern $Pattern -Quiet)) {
        Fail $Message
    }
}

function Resolve-Gradle {
    $wrapperBat = Join-Path $RootDir "gradlew.bat"
    if (Test-Path $wrapperBat) {
        return $wrapperBat
    }

    $gradleCommand = Get-Command gradle.bat -ErrorAction SilentlyContinue
    if ($gradleCommand) {
        return $gradleCommand.Source
    }

    $gradleCommand = Get-Command gradle -ErrorAction SilentlyContinue
    if ($gradleCommand) {
        return $gradleCommand.Source
    }

    $cachedGradle = Get-ChildItem "$env:USERPROFILE\.gradle\wrapper\dists" -Recurse -Filter gradle.bat -ErrorAction SilentlyContinue |
        Sort-Object FullName |
        Select-Object -Last 1
    if ($cachedGradle) {
        return $cachedGradle.FullName
    }

    Fail "Gradle tidak ditemukan. Tambahkan Gradle ke PATH atau gunakan Gradle wrapper."
}

function Resolve-ApkSigner {
    $apkSigner = Get-Command apksigner.bat -ErrorAction SilentlyContinue
    if ($apkSigner) {
        return $apkSigner.Source
    }

    $sdkRoot = if ($env:ANDROID_SDK_ROOT) { $env:ANDROID_SDK_ROOT } elseif ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { "" }
    if ($sdkRoot -and (Test-Path (Join-Path $sdkRoot "build-tools"))) {
        $candidate = Get-ChildItem (Join-Path $sdkRoot "build-tools") -Recurse -Filter apksigner.bat -ErrorAction SilentlyContinue |
            Sort-Object FullName |
            Select-Object -Last 1
        if ($candidate) {
            return $candidate.FullName
        }
    }

    Fail "apksigner tidak ditemukan. Pastikan Android SDK Build-Tools terpasang."
}

try {
    Write-Host "==> Memeriksa konfigurasi project"

    if (-not (Test-Path $KeystorePropertiesPath)) {
        Fail "keystore.properties tidak ditemukan. Release wajib memakai keystore lama, jangan buat keystore baru."
    }

    if (-not (Test-Path $AppBuildFile)) {
        Fail "File app/build.gradle.kts tidak ditemukan."
    }

    $storeFile = Read-Property "storeFile"
    $storePassword = Read-Property "storePassword"
    $keyAlias = Read-Property "keyAlias"
    $keyPassword = Read-Property "keyPassword"

    if ([string]::IsNullOrWhiteSpace($storeFile)) { Fail "storeFile kosong di keystore.properties." }
    if ([string]::IsNullOrWhiteSpace($storePassword)) { Fail "storePassword kosong di keystore.properties." }
    if ([string]::IsNullOrWhiteSpace($keyAlias)) { Fail "keyAlias kosong di keystore.properties." }
    if ([string]::IsNullOrWhiteSpace($keyPassword)) { Fail "keyPassword kosong di keystore.properties." }

    $keystorePath = if ([System.IO.Path]::IsPathRooted($storeFile)) {
        $storeFile
    } else {
        Join-Path $RootDir $storeFile
    }

    if (-not (Test-Path $keystorePath)) {
        Fail "Keystore lama tidak ditemukan di $keystorePath."
    }

    Ensure-GradleSetting "compileSdk\s*=\s*33" "compileSdk harus 33."
    Ensure-GradleSetting "minSdk\s*=\s*21" "minSdk harus 21."
    Ensure-GradleSetting "targetSdk\s*=\s*33" "targetSdk harus 33."

    $gradleBin = Resolve-Gradle
    $apkSignerBin = Resolve-ApkSigner

    Write-Host "==> Membersihkan build lama"
    & $gradleBin clean --no-daemon --stacktrace --console=plain

    Write-Host "==> Assemble release APK dengan keystore lama"
    & $gradleBin assembleRelease --no-daemon --stacktrace --console=plain

    $apk = Get-ChildItem $ApkOutputDir -Filter *.apk -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTime |
        Select-Object -Last 1

    if (-not $apk) {
        Fail "APK release tidak ditemukan di $ApkOutputDir."
    }

    Write-Host "==> Verifikasi signature APK (v1, v2, v3)"
    $verifyOutput = & $apkSignerBin verify --verbose --print-certs $apk.FullName 2>&1
    $verifyOutput | ForEach-Object { Write-Host $_ }

    if (-not ($verifyOutput -match "Verified using v1 scheme .*: true")) {
        Fail "Signature v1 belum terverifikasi."
    }
    if (-not ($verifyOutput -match "Verified using v2 scheme .*: true")) {
        Fail "Signature v2 belum terverifikasi."
    }
    if (-not ($verifyOutput -match "Verified using v3 scheme .*: true")) {
        Fail "Signature v3 belum terverifikasi."
    }

    Write-Host ""
    Write-Host "Release APK siap:"
    Write-Host $apk.FullName

    Write-Host ""
    Write-Host "Checklist install manual:"
    Write-Host "[ ] Uninstall versi debug/release Sajda App sebelumnya dari device"
    Write-Host "[ ] Transfer APK ke device"
    Write-Host "[ ] Install APK dari file di atas"
    Write-Host "[ ] Pastikan verifikasi signature v1/v2/v3 di atas semuanya TRUE"
}
catch {
    Fail "Build release gagal. $($_.Exception.Message)"
}
