Skills
=============
RPG Attributes Platform for Sponge

## Prerequisites
* [Java] 8
* [Gradle] 3.5+

## Cloning
The following steps will ensure your project is cloned properly.  
1. `git clone git@github.com:AlmuraDev/Skills.git`  
2. `cd Skills`

## Setup
__Note:__ If you do not have [Gradle] installed then use ./gradlew for Unix systems or Git Bash and gradlew.bat for Windows systems in place of any 'gradle' command.

__For [IntelliJ]__
  1. Run `gradle generateDatabaseClasses`  
  2. Make sure you have the Gradle plugin enabled (File > Settings > Plugins).
  3. Click File > Import Module and select the **build.gradle** file for Skills.
  4. On the import screen, uncheck `Create separate module per source set`

## Building
__Note:__ If you do not have [Gradle] installed then use ./gradlew for Unix systems or Git Bash and gradlew.bat for Windows systems in place of any 'gradle' command.

In order to build Skills you simply need to run the `gradle` command. You can find the compiled JAR files in `./build/libs` but in most cases you'll only need `skills-x.x.x-SNAPSHOT.jar`.                                                                                   

[Gradle]: http://www.gradle.org/
[IntelliJ]: http://www.jetbrains.com/idea/
[Java]: http://java.oracle.com/
