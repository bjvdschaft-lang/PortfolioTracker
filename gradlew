#!/bin/sh
#
# Gradle start up script for POSIX compatible shells.
APP_HOME="$(cd "$(dirname "$0")" && pwd)"
GRADLE_OPTS="${GRADLE_OPTS:-}"
exec "${APP_HOME}/gradle/wrapper/gradle-wrapper.jar" "$@" 2>/dev/null || \
  java -classpath "${APP_HOME}/gradle/wrapper/gradle-wrapper.jar" \
    org.gradle.wrapper.GradleWrapperMain "$@"
