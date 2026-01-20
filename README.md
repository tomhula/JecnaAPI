# JecnaAPI
![Maven Central version](https://img.shields.io/maven-central/v/io.github.tomhula/jecnaapi)
![GitHub License](https://img.shields.io/github/license/tomhula/jecnaapi)
![GitHub branch check runs](https://img.shields.io/github/check-runs/tomhula/jecnaapi/main)
![GitHub Issues or Pull Requests](https://img.shields.io/github/issues/tomhula/jecnaapi)

##### [English version here](README_en.md)

JecnaAPI je Kotlin/Java knihovna, díky které lze přistupovat k datům webu [spsejecna.cz](https://spsejecna.cz). Tato knihovna vznikla primárně pro účely [JecnaMobile](https://github.com/tomhula/JecnaMobile), ale může ji použít kdokoliv.
JecnaAPI podporuje Kotlin Multiplatform pro tyto targety: `jvm`, `android`, `wasmJs`, `js`, `linuxX64`.

## Funkce

- čtení:
  - Novinky
  - Známky
  - Rozvrh
  - Příchody a odchody
  - Učitelský sbor
  - Obědy
  - Absence a omluvný list
  - Profil studenta a jeho obrázek

- objednávání obědů
- dávání obědů do/z burzy
- kupování obědů z burzy

## Požadavky

- Java 21+ (projekt používá JVM toolchain 21)
- Kotlin 2.2+

## Instalace

JecnaAPI je na [Maven Central](https://central.sonatype.com/artifact/io.github.tomhula/jecnaapi) repozitáři.

### Gradle

###### build.gradle (Groovy)
```groovy
dependencies {
    implementation 'io.github.tomhula:jecnaapi:8.0.1'
    /* Pouze pokud chcete používat z Javy, musíte přidat i následující. */
    implementation 'io.github.tomhula:jecnaapi-java:8.0.1'
}
```

###### build.gradle.kts (Kotlin)
```kotlin
dependencies {
    implementation("io.github.tomhula:jecnaapi:8.0.1")
    /* Pouze pokud chcete používat z Javy, musíte přidat i následující. */
    implementation("io.github.tomhula:jecnaapi-java:8.0.1")
}
```

### Maven
######  pom.xml
```xml
<dependencies>
    <dependency>
        <groupId>io.github.tomhula</groupId>
        <artifactId>jecnaapi</artifactId>
        <version>8.0.1</version>
    </dependency>
    <!-- Pouze pokud chcete používat z Javy, musíte přidat i následující. -->
    <dependency>
      <groupId>io.github.tomhula</groupId>
      <artifactId>jecnaapi-java</artifactId>
      <version>8.0.1</version>
    </dependency>
</dependencies>
```

## Použití

Knihovna je primárně naprogramovaná v Kotlinu, ale je možné ji používat i z Javy. Chcete‑li ji používat i z Javy, musíte přidat závislost na `jecnaapi-java` (viz [Instalace](#instalace)). V Javě se místo Kotlin Coroutines používá API `CompletableFuture`.

### Vytvoření JecnaClient objektu

##### Kotlin
```kotlin
val jecnaClient = JecnaClient()
```

##### Java
```java
JecnaClientJavaWrapper jecnaClient = new JecnaClientJavaWrapper();
```

### Přihlášení

Přihlášení je nezbytné k čtení dat studenta.

##### Kotlin
```kotlin
/* runBlocking, nebo jiný coroutine scope. */
runBlocking {
    jecnaClient.login("username", "password")
}
```

##### Java
```java
// přihlášení (počkejte na dokončení)
jecnaClient.login("username", "password").join();
```

### Čtení dat

##### Kotlin
```kotlin
/* runBlocking, nebo jiný coroutine scope. */
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

Některé metody berou období (např. rok) jako parametr.

##### Kotlin
```kotlin
/* runBlocking, nebo jiný coroutine scope. */
runBlocking {
/* Získání známek z roku 2021/2022 z druhého pololetí.  */
  val gradesPage = jecnaClient.getGradesPage(SchoolYear(2021), SchoolYearHalf.SECOND)
}
```

##### Java
```java
/* Získání známek z roku 2021/2022 z druhého pololetí.  */
GradesPage gradesPage = jecnaClient.getGradesPage(new SchoolYear(2021), SchoolYearHalf.SECOND).join();
```

Více příkladů najdete ve složkách [kotlin-examples](/src/examples/kotlin) a [java-examples](/jecnaapi-java/src/examples/java).

# Kontribuce

Kontribuce jsou vítané, stačí založit Pull Request. Pokud jde o nějaké zásadní změny (což samozřejmě můžete), je dobré je se mnou nejdříve probrat, ať je neděláte zbytečně.

## License

[GNU GPLv3](LICENSE) © Tomáš Hůla
