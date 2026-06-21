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
  - Učebny

- objednávání obědů
- dávání obědů do/z burzy
- kupování obědů z burzy

## Požadavky

- Java 21+ (projekt používá JVM toolchain 21)
- Kotlin 2.2+

## Instalace

JecnaAPI je na [Maven Central](https://central.sonatype.com/artifact/io.github.tomhula/jecnaapi) repozitáři.

### Gradle

###### build.gradle.kts
```kotlin
dependencies {
    implementation("io.github.tomhula:jecnaapi-core:10.3.4")
    /* API na www.spsejecna.cz */
    implementation("io.github.tomhula:jecnaapi-jecna:10.3.4")
    /* API na www.spsejecna.cz pro Javu */
    implementation("io.github.tomhula:jecnaapi-jecna-java:10.3.4")
    /* API na jidelnu */
    implementation("io.github.tomhula:jecnaapi-canteen:10.3.4")
    /* API na jidelnu pro Javu */
    // Neexistuje
}
```

### Maven
######  pom.xml

Nahraďte `{platform}` platformou pro kterou kompilujete, (jvm, android, js, atd.) protože Maven neumí automaticky resolvovat multiplatform knihovny.

```xml
<dependencies>
    <dependency>
        <groupId>io.github.tomhula</groupId>
        <artifactId>jecnaapi-core-{platform}</artifactId>
        <version>10.3.4</version>
    </dependency>
    <!-- API na www.spsejecna.cz -->
    <dependency>
        <groupId>io.github.tomhula</groupId>
        <artifactId>jecnaapi-jecna-{platform}</artifactId>
        <version>10.3.4</version>
    </dependency>
    <!-- API na www.spsejecna.cz pro Javu -->
    <dependency>
        <groupId>io.github.tomhula</groupId>
        <artifactId>jecnaapi-jecna-java</artifactId>
        <version>10.3.4</version>
    </dependency>
    <!-- API na jidelnu -->
    <dependency>
        <groupId>io.github.tomhula</groupId>
        <artifactId>jecnaapi-canteen-{platform}</artifactId>
        <version>10.3.4</version>
    </dependency>
    <!-- API na jidelnu pro Javu -->
    <!-- Neexistuje -->
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
AbsencePage absencePage = jecnaClient.getAbsencePage().join();
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
