#!/bin/bash

# Credit Card Management System - Quick Start Script
# ===================================================

echo "======================================================"
echo "   Credit Card Management System - Quick Start        "
echo "======================================================"

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo ""
    echo "❌ Java is not installed!"
    echo ""
    echo "Please install Java 17 first:"
    echo ""
    echo "  Option 1 - Using Homebrew (recommended for macOS):"
    echo "    brew tap homebrew/cask-versions"
    echo "    brew install --cask temurin17"
    echo ""
    echo "  Option 2 - Download directly:"
    echo "    https://adoptium.net/temurin/releases/?version=17"
    echo ""
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
echo "✅ Java version: $(java -version 2>&1 | head -1)"

# Check if Maven is installed, else use wrapper
if command -v mvn &> /dev/null; then
    MVN_CMD="mvn"
    echo "✅ Maven found: $(mvn -version 2>&1 | head -1)"
else
    echo "⚠️  Maven not found. Installing Maven wrapper..."
    if command -v curl &> /dev/null; then
        # Download mvnw if not present
        if [ ! -f "./mvnw" ]; then
            echo "Please install Maven: brew install maven"
            exit 1
        fi
        MVN_CMD="./mvnw"
    fi
fi

echo ""
echo "🚀 Starting Credit Card Management System..."
echo ""
echo "   - API:       http://localhost:8080/api"
echo "   - H2 Console: http://localhost:8080/h2-console"
echo "   - JDBC URL:   jdbc:h2:mem:creditcarddb"
echo ""
echo "   Default Credentials:"
echo "   - Admin: admin / admin123"
echo "   - User1: john  / john123"
echo "   - User2: jane  / jane123"
echo ""
echo "Press Ctrl+C to stop the server."
echo "======================================================"

$MVN_CMD spring-boot:run
