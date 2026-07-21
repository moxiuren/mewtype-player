#!/bin/bash
# Build script for MewType Player - uses JDK 21 for Android compatibility
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@21/21.0.11/libexec/openjdk.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH
cd "$(dirname "$0")"
exec gradle "$@"
