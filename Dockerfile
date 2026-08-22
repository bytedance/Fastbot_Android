FROM debian:bookworm-slim

ARG ANDROID_COMMAND_LINE_TOOLS_VERSION=10406996
ARG ANDROID_COMMAND_LINE_TOOLS_SHA256=8919e8752979db73d8321e9babe2caedcc393750817c1a5f56c128ec442fb540
ARG GRADLE_VERSION=7.6.2
ARG GRADLE_SHA256=a01b6587e15fe7ed120a0ee299c25982a1eee045abd6a9dd5e216b2f628ef9ac

ENV ANDROID_SDK_ROOT=/opt/android-sdk \
    NDK_ROOT=/opt/android-sdk/ndk/25.2.9519653 \
    GRADLE_HOME=/opt/gradle/gradle-7.6.2 \
    PATH=/opt/gradle/gradle-7.6.2/bin:/opt/android-sdk/cmdline-tools/latest/bin:/opt/android-sdk/build-tools/28.0.3:/opt/android-sdk/cmake/3.18.1/bin:${PATH}

SHELL ["/bin/bash", "-o", "pipefail", "-c"]

RUN apt-get update \
    && apt-get install --yes --no-install-recommends \
        ca-certificates \
        curl \
        default-jdk-headless \
        git \
        make \
        ninja-build \
        unzip \
    && rm -rf /var/lib/apt/lists/*

RUN curl --fail --location --show-error \
        --output /tmp/command-line-tools.zip \
        "https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_COMMAND_LINE_TOOLS_VERSION}_latest.zip" \
    && echo "${ANDROID_COMMAND_LINE_TOOLS_SHA256}  /tmp/command-line-tools.zip" | sha256sum --check --strict \
    && mkdir -p "${ANDROID_SDK_ROOT}/cmdline-tools" \
    && unzip -q /tmp/command-line-tools.zip -d /tmp/android-command-line-tools \
    && mv /tmp/android-command-line-tools/cmdline-tools "${ANDROID_SDK_ROOT}/cmdline-tools/latest" \
    && rm -rf /tmp/command-line-tools.zip /tmp/android-command-line-tools \
    && (set +o pipefail; yes | sdkmanager --licenses >/dev/null) \
    && sdkmanager \
        "build-tools;28.0.3" \
        "build-tools;30.0.2" \
        "cmake;3.18.1" \
        "ndk;25.2.9519653" \
        "platforms;android-32"

RUN curl --fail --location --show-error \
        --output /tmp/gradle.zip \
        "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" \
    && echo "${GRADLE_SHA256}  /tmp/gradle.zip" | sha256sum --check --strict \
    && mkdir -p /opt/gradle \
    && unzip -q /tmp/gradle.zip -d /opt/gradle \
    && rm /tmp/gradle.zip \
    && java --version \
    && gradle --version \
    && sdkmanager --version

WORKDIR /workspace

CMD ["bash", "-c", "gradle --no-daemon clean makeJar && dx --dex --output=monkeyq.jar monkey/build/libs/monkey.jar && sed 's/\\r$//' build_native.sh | sh"]
