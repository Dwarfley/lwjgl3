# LWJGL publishing projects

### modules

The parent project for all module projects.

### platform

The platform for the modules (maven BOM).

## Design

Ideally, we'd have the following structure:
```
lwjgl
	lwjgl-windows (depends on lwjgl)
glfw (depends on lwjgl)
	glfw-windows (depends on glfw & lwjgl-windows)
stb (depends on lwjgl)
	stb-windows (depends on stb & lwjgl-windows)
```
If a user wanted to use GLFW + stb in their project, running on
the Windows platform, they'd only have to define glfw-windows
and stb-windows as dependencies. This would automatically
resolve stb, glfw, lwjgl and lwjgl-windows as transitive
dependencies. Unfortunately, it is not possible to define such
a relationship between Maven artifacts when using classifiers.
A method to make this work is make the natives-<arch> classified
JARs separate artifacts. We do not do it for aesthetic reasons.
Instead, we assume that a tool is available (on the LWJGL website)
that automatically generates POM/Gradle dependency structures for
projects wanting to use LWJGL. The output is going to be verbose;
the above example is going to look like this in kotlin Gradle:
```kotlin
implementation("org.lwjgl:lwjgl:$lwjglVersion") // NOTE: this is optional, all binding artifacts have a dependency on lwjgl
	implementation("org.lwjgl:lwjgl:$lwjglVersion:natives-$lwjglArch")
implementation("org.lwjgl:lwjgl-glfw:$lwjglVersion")
	implementation("org.lwjgl:lwjgl-glfw:$lwjglVersion:natives-$lwjglArch")
implementation("org.lwjgl:lwjgl-stb:$lwjglVersion")
	implementation("org.lwjgl:lwjgl-stb:$lwjglVersion:natives-$lwjglArch")
```
and a whole lot more verbose in Maven. Hopefully, the automation
is going to alleviate the pain. With gradle module metadata
and the use of the BOM, the above will condense to:
```kotlin
implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
implementation("org.lwjgl:lwjgl")
implementation("org.lwjgl:lwjgl-glfw")
implementation("org.lwjgl:lwjgl-stb")
```
given that the os and arch attributes have been set.