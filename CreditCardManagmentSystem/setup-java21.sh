#!/bin/bash
# Java 21 Environment Setup Script
# Source this file before running Maven commands: source ./setup-java21.sh

export JAVA_HOME="/opt/homebrew/Cellar/openjdk@21/21.0.10/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

echo "✅ Java 21 environment configured"
echo "JAVA_HOME: $JAVA_HOME"
java -version
