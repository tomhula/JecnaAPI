# JecnaAPI
![Maven Central version](https://img.shields.io/maven-central/v/io.github.tomhula/jecnaapi)
![GitHub License](https://img.shields.io/github/license/tomhula/jecnaapi)
![GitHub branch check runs](https://img.shields.io/github/check-runs/tomhula/jecnaapi/main)
![GitHub Issues or Pull Requests](https://img.shields.io/github/issues/tomhula/jecnaapi)

JecnaAPI is a Kotlin/Java library for accessing data from the [spsejecna.cz](https://spsejecna.cz) website. The library was originally created for [JecnaMobile](https://github.com/tomhula/JecnaMobile) but it can be used by anyone.
JecnaAPI supports Kotlin Multiplatform for these targets: `jvm`, `android`, `wasmJs`, `js`, `linuxX64`.

## Features

- Reading:
  - News
  - Grades
  - Timetable
  - Arrivals and departures
  - Teaching staff
  - Canteen (lunch menu)
  - Absence and excuse sheet
  - Student profile and profile picture
  - Classrooms
  - Substitutions

- Lunch ordering
- Putting lunches to/from exchange (market)
- Buying lunches from the exchange (market)

## Requirements

- Java 21+ (the project uses JVM toolchain 21)
- Kotlin 2.2+

## Installation

JecnaAPI is available on the [Maven Central](https://central.sonatype.com/artifact/io.github.tomhula/jecnaapi) repository.

### Gradle

###### build.gradle (Groovy)
```groovy
dependencies {
    implementation 'io.github.tomhula:jecnaapi:7.0.0'
    /* Only if you want to use it from Java, you must also add the following. */
    implementation 'io.github.tomhula:jecnaapi-java:7.0.0'
}
```

###### build.gradle.kts (Kotlin)
```kotlin
dependencies {
    implementation("io.github.tomhula:jecnaapi:7.0.0")
    /* Only if you want to use it from Java, you must also add the following. */
    implementation("io.github.tomhula:jecnaapi-java:7.0.0")
}
```

### Maven
###### pom.xml
```xml
<dependencies>
    <dependency>
        <groupId>io.github.tomhula</groupId>
        <artifactId>jecnaapi</artifactId>
        <version>7.0.0</version>
    </dependency>
    <!-- Only if you want to use it from Java, you must also add the following. -->
    <dependency>
      <groupId>io.github.tomhula</groupId>
      <artifactId>jecnaapi-java</artifactId>
      <version>7.0.0</version>
    </dependency>
    
</dependencies>
```

## Usage

The library is primarily written in Kotlin, but it can also be used from Java. If you want to use it from Java, you must add a dependency on `jecnaapi-java` (see [Installation](#installation)). In Java, `CompletableFuture` is used instead of Kotlin Coroutines.

### Creating a `JecnaClient` instance

##### Kotlin
```kotlin
val jecnaClient = JecnaClient()
```

##### Java
```java
JecnaClientJavaWrapper jecnaClient = new JecnaClientJavaWrapper();
```

### Login

Logging in is required to read student data.

##### Kotlin
```kotlin
/* runBlocking, or another coroutine scope. */
runBlocking {
    jecnaClient.login("username", "password")
}
```

##### Java
```java
// login (wait for completion)
jecnaClient.login("username", "password").join();
```

### Reading data

##### Kotlin
```kotlin
/* runBlocking, or another coroutine scope. */
runBlocking {
  val newsPage = jecnaClient.getNewsPage()
  val gradesPage = jecnaClient.getGradesPage()
  val timetablePage = jecnaClient.getTimetablePage()
  val attendancePage = jecnaClient.getAttendancePage()
  val teachersPage = jecnaClient.getTeachersPage()
  val absencePage = jecnaClient.getAbsencePage()
}
```

##### Java
```java
NewsPage newsPage = jecnaClient.getNewsPage().join();
GradesPage gradesPage = jecnaClient.getGradesPage().join();
TimetablePage timetablePage = jecnaClient.getTimetablePage().join();
AttendancePage attendancePage = jecnaClient.getAttendancePage().join();
TeachersPage teachersPage = jecnaClient.getTeachersPage().join();
```

Some methods take a period (e.g., school year) as a parameter.

##### Kotlin
```kotlin
/* runBlocking, or another coroutine scope. */
runBlocking {
  /* Getting grades from the 2021/2022 school year, second half. */
  val gradesPage = jecnaClient.getGradesPage(SchoolYear(2021), SchoolYearHalf.SECOND)
}
```

##### Java
```java
/* Getting grades from the 2021/2022 school year, second half. */
GradesPage gradesPage = jecnaClient.getGradesPage(new SchoolYear(2021), SchoolYearHalf.SECOND).join();
```

You can find more examples in the [kotlin-examples](/src/examples/kotlin) and [java-examples](/jecnaapi-java/src/examples/java) directories.

## Contributing

Contributions are welcome — just open a Pull Request. If it’s a significant change (which you definitely can do), it’s a good idea to discuss it with me first to avoid unnecessary work.

## License

[MIT](LICENSE) © Tomáš Hůla
