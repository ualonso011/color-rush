# Android SDK Docker build environment
#
# Base image: thyrlian/android-sdk:10.0
# Digest: sha256:bb9ed3686968550d927228777bca787dd7913e679f1e73e85525ba0094ea170d
# Published: 2024-09-29 | Ubuntu 24.04.1 | JDK 17 | Gradle 8.10.2 | Kotlin 2.0.20
#
# Image decision: thyrlian/android-sdk was chosen over mobiledevops/android-sdk because
# thyrlian provides a barebone SDK (no platform pre-installed), allowing us to pin the
# exact SDK platform version via sdkmanager. The :10.0 tag is the latest stable release
# with a pinned SHA for reproducible builds.
#
# Android SDK 35 is installed here (not pre-bundled) via sdkmanager.
# Build-tools 35.0.0 provides dx/d8 and aapt2 required by AGP 8.x.
FROM thyrlian/android-sdk@sha256:bb9ed3686968550d927228777bca787dd7913e679f1e73e85525ba0094ea170d

# Accept licenses non-interactively
RUN yes | sdkmanager --licenses > /dev/null 2>&1 || true

# Install Android SDK 35 platform and build tools
RUN sdkmanager \
    "platforms;android-35" \
    "build-tools;35.0.0" \
    "platform-tools"

# Set ANDROID_SDK_ROOT (AGP 8.x prefers this over ANDROID_HOME)
ENV ANDROID_SDK_ROOT=${ANDROID_HOME}

# Gradle user home inside the container — allows mounting a cache volume
ENV GRADLE_USER_HOME=/gradle-cache

# Project workdir
WORKDIR /project
