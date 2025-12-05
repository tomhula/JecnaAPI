[![](https://jitpack.io/v/tomhula/JecnaAPI.svg)](https://jitpack.io/#tomhula/JecnaAPI)
# JecnaAPI

JecnaAPI je Kotlin knihovna, díky které lze přistupovat k datům webu [spsejecna.cz](https://spsejecna.cz).

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

- obědnávání obědů
- dávání obědů do/z burzy

## Instalace

`<version>` referuje na [![](https://jitpack.io/v/tomhula/JecnaAPI.svg)](https://jitpack.io/#tomhula/JecnaAPI).

### Gradle

###### build.gradle (Groovy)
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    implementation 'com.github.tomhula.JecnaAPI:jecnaapi:<version>'
    /* Pokud chcete používat z Javy, přidejte i následující. */
    implementation 'com.github.tomhula.JecnaAPI:jecnaapi-java:<version>'
}
```

###### build.gradle.kts (Kotlin)
```kotlin
repositories {
    maven("https://jitpack.io")
}
dependencies {
    implementation("com.github.tomhula.JecnaAPI:jecnaapi:<version>")
    /* Pokud chcete používat z Javy, přidejte i následující. */
    implementation("com.github.tomhula.JecnaAPI:jecnaapi-java:<version>")
}
```

### Maven
######  pom.xml
```xml
<repositories>
    ...
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
<dependencies>
    ...
    <dependency>
        <groupId>com.github.tomhula.JecnaAPI</groupId>
        <artifactId>jecnaapi</artifactId>
        <version>VERSION</version>
    </dependency>
    <!-- Pokud chcete používat z Javy, přidejte i následující. -->
    <dependency>
      <groupId>com.github.tomhula.JecnaAPI</groupId>
      <artifactId>jecnaapi-java</artifactId>
      <version>VERSION</version>
    </dependency>
</dependencies>
```

## Použití

Knihovna je primárně naprogramovaná v Kotlinu, ale je možné ji používat i z Javy. Chcete-li jí používat z Javy, musíte přidat dependency na `jecnaapi-java` (viz [Instalace](#instalace)). Java místo Kotlin coroutines používá `CompletableFuture` API.

### Vytvoření JecnaClient objektu

##### Kotlin
```kotlin
val jecnaClient = JecnaClient()
```

`Java`
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
jecnaClient.login("username", "password");
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
