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

- Lunch ordering
- Putting lunches to/from exchange (market)
- Buying lunches from the exchange (market)

## Requirements

- Java 21+ (the project uses JVM toolchain 21)
- Kotlin 2.2+

## Installation

JecnaAPI is available on the [Maven Central](https://central.sonatype.com/artifact/io.github.tomhula/jecnaapi) repository.

### Gradle

###### build.gradle.kts
```kotlin
dependencies {
    implementation("io.github.tomhula:jecnaapi-core:10.3.2")
    /* API for www.spsejecna.cz */
    implementation("io.github.tomhula:jecnaapi-jecna:10.3.2")
    /* API for www.spsejecna.cz for Java */
    implementation("io.github.tomhula:jecnaapi-jecna-java:10.3.2")
    /* API for canteen */
    implementation("io.github.tomhula:jecnaapi-canteen:10.3.2")
    /* API for canteen for Java */
    // Does not exist
}
```

### Maven
######  pom.xml

Replace `{platform}` with the platform you are targeting, (jvm, android, js, etc.) because Maven does not support automatic kotlin multiplatform library resolution.

```xml
<dependencies>
    <dependency>
        <groupId>io.github.tomhula</groupId>
        <artifactId>jecnaapi-core-{platform}</artifactId>
        <version>10.3.2</version>
    </dependency>
    <!-- API for www.spsejecna.cz -->
    <dependency>
        <groupId>io.github.tomhula</groupId>
        <artifactId>jecnaapi-jecna-{platform}</artifactId>
        <version>10.3.2</version>
    </dependency>
    <!-- API for www.spsejecna.cz for Java -->
    <dependency>
        <groupId>io.github.tomhula</groupId>
        <artifactId>jecnaapi-jecna-java</artifactId>
        <version>10.3.2</version>
    </dependency>
    <!-- API for canteen -->
    <dependency>
        <groupId>io.github.tomhula</groupId>
        <artifactId>jecnaapi-canteen-{platform}</artifactId>
        <version>10.3.2</version>
    </dependency>
    <!-- API for canteen for Java -->
    <!-- Does not exist -->
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
AbsencePage absencePage = jecnaClient.getAbsencePage().join();
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

[GNU GPLv3](LICENSE) © Tomáš Hůla
