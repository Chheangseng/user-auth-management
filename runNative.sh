#!/bin/bash

# Load .env file
if [ -f .env ]; then
    echo "Loading .env file..."
    export $(grep -v '^#' .env | xargs)
else
    echo "Warning: .env file not found!"
fi

# Run the .exe file
echo "Running application.exe..."

# Check if file exists
if [ -f ./target/user-auth-management.exe ]; then
    # Make executable (if needed)
    chmod +x ./target/user-auth-management.exe

    # Run the executable
    ./target/user-auth-management.exe
else
    echo "Error: ./target/user-auth-management.exe not found!"
    exit 1
fi