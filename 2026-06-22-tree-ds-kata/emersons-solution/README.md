# Fenwick Tree

A study implementation of the Fenwick Tree (also known as **Binary Indexed Tree**, or **BIT**) in Kotlin, including the classic variant for **range update / point query**.

# Project Structure

```text
src
├── main
│   └── kotlin
│
└── test
    └── kotlin
```

# Gradle

This POC uses Gradle to:
- compile the project
- manage dependencies
- run tests

Main files:

```text
build.gradle.kts
settings.gradle.kts
gradlew
gradlew.bat
```

### Build Project

```bash
./gradlew build
```

### Clean Build

```bash
./gradlew clean
```

### Running Tests

```bash
./gradlew test
```

### Test Reports

```text
build/reports/tests/test/index.html
```