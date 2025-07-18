/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
import java.net.*

plugins {
    `java-platform`
    id("lwjgl-publish")
}

val lwjglVersion: String by project
val signingKeyId: String? by project
val signingKey: String? by project
val signingPassword: String? by project
val sonatypeUsername: String? by project
val sonatypePassword: String? by project

val canSign: Boolean = signingKeyId != null && signingKey != null && signingPassword != null
val canRemotePublish: Boolean = sonatypeUsername != null && sonatypePassword != null

defaultTasks = mutableListOf("publish")
buildDir = file("bin/MAVEN")
group = "org.lwjgl"
version = lwjglVersion

enum class BuildType {
    LOCAL,
    SNAPSHOT,
    RELEASE
}

data class Deployment(
    val type: BuildType,
    val repo: URI
)

val deployment = when {
    hasProperty("release") -> {
        setStatus("release")
        Deployment(
            type = BuildType.RELEASE,
            repo = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
        )
    }
    hasProperty("snapshot") -> {
        version = "$version-SNAPSHOT"
        setStatus("milestone")
        Deployment(
            type = BuildType.SNAPSHOT,
            repo = uri("https://central.sonatype.com/repository/maven-snapshots/")
        )
    }
    else -> {
        version = "$version-SNAPSHOT"
        setStatus("integration")
        Deployment(
            type = BuildType.LOCAL,
            repo = repositories.mavenLocal().url
        )
    }
}

println("${deployment.type.name} BUILD")

if (deployment.type !== BuildType.LOCAL && !canSign) {
    throw GradleException("Must specify 'signingKeyId', 'signingKey' and 'signingPassword' properties for ${deployment.type.name} builds")
}

if (deployment.type !== BuildType.LOCAL && !canRemotePublish) {
    throw GradleException("Must specify 'sonatypeUsername' and 'sonatypePassword' properties for ${deployment.type.name} builds")
}

enum class Platform(
    val os: String,
    val arch: String
) {
    FREEBSD_X64("freebsd", "x64"),
    LINUX_X64("linux", "x64"),
    LINUX_ARM64("linux", "arm64"),
    LINUX_ARM32("linux", "arm32"),
    LINUX_PPC64LE("linux", "ppc64le"),
    LINUX_RISCV64("linux", "riscv64"),
    MACOS_X64("macos", "x64"),
    MACOS_ARM64("macos", "arm64"),
    WINDOWS_X64("windows", "x64"),
    WINDOWS_X86("windows", "x86"),
    WINDOWS_ARM64("windows", "arm64");

    val classifier: String get() =
        if(arch == "x64")
            "natives-${os}"
        else
            "natives-${os}-${arch}"

    companion object {
        val ALL = values()
        val FREEBSD = arrayOf(FREEBSD_X64)
        val LINUX = arrayOf(LINUX_X64, LINUX_ARM64, LINUX_ARM32, LINUX_PPC64LE, LINUX_RISCV64)
        val MACOS = arrayOf(MACOS_X64, MACOS_ARM64)
        val WINDOWS = arrayOf(WINDOWS_X64, WINDOWS_X86, WINDOWS_ARM64)
    }
}

enum class Module(
    val id: String,
    val title: String,
    val description: String,
    vararg val platforms: Platform
) {
    CORE(
        "lwjgl", "LWJGL",
        "The LWJGL core library.",
        *Platform.ALL
    ),
    ASSIMP(
        "lwjgl-assimp", "LWJGL - Assimp bindings",
        "A portable Open Source library to import various well-known 3D model formats in a uniform manner.",
        *Platform.ALL
    ),
    BGFX(
        "lwjgl-bgfx", "LWJGL - bgfx bindings",
        "A cross-platform, graphics API agnostic rendering library. It provides a high performance, low level abstraction for common platform graphics APIs like OpenGL, Direct3D and Apple Metal.",
        *Platform.FREEBSD, *Platform.LINUX, *Platform.MACOS, Platform.WINDOWS_X64, Platform.WINDOWS_X86
    ),
    EGL(
        "lwjgl-egl", "LWJGL - EGL bindings",
        "An interface between Khronos rendering APIs such as OpenGL ES or OpenVG and the underlying native platform window system."
    ),
    FMOD(
        "lwjgl-fmod", "LWJGL - FMOD bindings",
        "An end-to-end solution for adding sound and music to any game."
    ),
    FREETYPE(
        "lwjgl-freetype", "LWJGL - FreeType bindings",
        "A freely available software library to render fonts.",
        *Platform.ALL
    ),
    GLFW(
        "lwjgl-glfw", "LWJGL - GLFW bindings",
        "A multi-platform library for OpenGL, OpenGL ES and Vulkan development on the desktop. It provides a simple API for creating windows, contexts and surfaces, receiving input and events.",
        *Platform.ALL
    ),
    HARFBUZZ(
        "lwjgl-harfbuzz", "LWJGL - HarfBuzz bindings",
        "A text shaping library that allows programs to convert a sequence of Unicode input into properly formatted and positioned glyph output — for any writing system and language.",
        *Platform.ALL
    ),
    HWLOC(
        "lwjgl-hwloc", "LWJGL - hwloc bindings",
        "A portable abstraction of the hierarchical topology of modern architectures, including NUMA memory nodes, sockets, shared caches, cores and simultaneous multithreading.",
        *Platform.ALL
    ),
    JAWT(
        "lwjgl-jawt", "LWJGL - JAWT bindings",
        "The AWT native interface."
    ),
    JEMALLOC(
        "lwjgl-jemalloc", "LWJGL - jemalloc bindings",
        "A general purpose malloc implementation that emphasizes fragmentation avoidance and scalable concurrency support.",
        *Platform.ALL
    ),
    KTX(
        "lwjgl-ktx", "LWJGL - KTX (Khronos Texture) bindings",
        "A lightweight container for textures for OpenGL®, Vulkan® and other GPU APIs.",
        *Platform.FREEBSD, *Platform.LINUX, *Platform.MACOS, Platform.WINDOWS_X64, Platform.WINDOWS_ARM64
    ),
    LLVM(
        "lwjgl-llvm", "LWJGL - LLVM/Clang bindings",
        "A collection of modular and reusable compiler and toolchain technologies.",
        *Platform.ALL
    ),
    LMDB(
        "lwjgl-lmdb", "LWJGL - LMDB bindings",
        "A compact, fast, powerful, and robust database that implements a simplified variant of the BerkeleyDB (BDB) API.",
        *Platform.ALL
    ),
    LZ4(
        "lwjgl-lz4", "LWJGL - LZ4 bindings",
        "A lossless data compression algorithm that is focused on compression and decompression speed.",
        *Platform.ALL
    ),
    MESHOPTIMIZER(
        "lwjgl-meshoptimizer", "LWJGL - meshoptimizer bindings",
        "A library that provides algorithms to help optimize meshes.",
        *Platform.ALL
    ),
    MSDFGEN(
        "lwjgl-msdfgen", "LWJGL - msdfgen bindings",
        "Multi-channel signed distance field generator.",
        *Platform.ALL
    ),
    NANOVG(
        "lwjgl-nanovg", "LWJGL - NanoVG & NanoSVG bindings",
        "A small antialiased vector graphics rendering library for OpenGL. Also includes NanoSVG, a simple SVG parser.",
        *Platform.ALL
    ),
    NFD(
        "lwjgl-nfd", "LWJGL - Native File Dialog bindings",
        "A small C library that portably invokes native file open, folder select and file save dialogs.",
        *Platform.ALL
    ),
    NUKLEAR(
        "lwjgl-nuklear", "LWJGL - Nuklear bindings",
        "A minimal state immediate mode graphical user interface toolkit.",
        *Platform.ALL
    ),
    ODBC(
        "lwjgl-odbc", "LWJGL - ODBC bindings",
        "A C programming language interface that makes it possible for applications to access data from a variety of database management systems (DBMSs)."
    ),
    OPENAL(
        "lwjgl-openal", "LWJGL - OpenAL bindings",
        "A cross-platform 3D audio API appropriate for use with gaming applications and many other types of audio applications.",
        *Platform.ALL
    ),
    OPENCL(
        "lwjgl-opencl", "LWJGL - OpenCL bindings",
        "An open, royalty-free standard for cross-platform, parallel programming of diverse processors found in personal computers, servers, mobile devices and embedded platforms."
    ),
    OPENGL(
        "lwjgl-opengl", "LWJGL - OpenGL bindings",
        "The most widely adopted 2D and 3D graphics API in the industry, bringing thousands of applications to a wide variety of computer platforms.",
        *Platform.ALL
    ),
    OPENGLES(
        "lwjgl-opengles", "LWJGL - OpenGL ES bindings",
        "A royalty-free, cross-platform API for full-function 2D and 3D graphics on embedded systems - including consoles, phones, appliances and vehicles.",
        *Platform.ALL
    ),
    OPENXR(
        "lwjgl-openxr", "LWJGL - OpenXR bindings",
        "A royalty-free, open standard that provides high-performance access to Augmented Reality (AR) and Virtual Reality (VR)—collectively known as XR—platforms and devices.",
        *Platform.FREEBSD, *Platform.LINUX, *Platform.WINDOWS
    ),
    OPUS(
        "lwjgl-opus", "LWJGL - Opus bindings",
        "A totally open, royalty-free, highly versatile audio codec.",
        *Platform.ALL
    ),
    PAR(
        "lwjgl-par", "LWJGL - par_shapes bindings",
        "Generate parametric surfaces and other simple shapes.",
        *Platform.ALL
    ),
    REMOTERY(
        "lwjgl-remotery", "LWJGL - Remotery bindings",
        "A realtime CPU/GPU profiler hosted in a single C file with a viewer that runs in a web browser.",
        *Platform.FREEBSD, *Platform.LINUX, *Platform.MACOS, Platform.WINDOWS_X64, Platform.WINDOWS_X86
    ),
    RPMALLOC(
        "lwjgl-rpmalloc", "LWJGL - rpmalloc bindings",
        "A public domain cross platform lock free thread caching 16-byte aligned memory allocator implemented in C.",
        *Platform.ALL
    ),
    SDL(
        "lwjgl-sdl", "LWJGL - SDL bindings",
        "Simple DirectMedia Layer is a cross-platform development library designed to provide low level access to audio, keyboard, mouse, joystick, and graphics hardware.",
        *Platform.ALL
    ),
    SHADERC(
        "lwjgl-shaderc", "LWJGL - Shaderc bindings",
        "A collection of libraries for shader compilation.",
        *Platform.ALL
    ),
    SPNG(
        "lwjgl-spng", "LWJGL - spng bindings",
        "libspng (simple png) is a C library for reading and writing Portable Network Graphics (PNG) format files with a focus on security and ease of use.",
        *Platform.ALL
    ),
    SPVC(
        "lwjgl-spvc", "LWJGL - SPIRV-Cross bindings",
        "A library for performing reflection on SPIR-V and disassembling SPIR-V back to high level languages.",
        *Platform.ALL
    ),
    STB(
        "lwjgl-stb", "LWJGL - stb bindings",
        "Single-file public domain libraries for fonts, images, ogg vorbis files and more.",
        *Platform.ALL
    ),
    TINYEXR(
        "lwjgl-tinyexr", "LWJGL - Tiny OpenEXR bindings",
        "A small library to load and save OpenEXR(.exr) images.",
        *Platform.ALL
    ),
    TINYFD(
        "lwjgl-tinyfd", "LWJGL - Tiny File Dialogs bindings",
        "Provides basic modal dialogs.",
        *Platform.ALL
    ),
    VMA(
        "lwjgl-vma", "LWJGL - Vulkan Memory Allocator bindings",
        "An easy to integrate Vulkan memory allocation library.",
        *Platform.ALL
    ),
    VULKAN(
        "lwjgl-vulkan", "LWJGL - Vulkan bindings",
        "A new generation graphics and compute API that provides high-efficiency, cross-platform access to modern GPUs used in a wide variety of devices from PCs and consoles to mobile phones and embedded platforms.",
        *Platform.MACOS
    ),
    XXHASH(
        "lwjgl-xxhash", "LWJGL - xxHash bindings",
        "An extremely fast hash algorithm, running at RAM speed limits.",
        *Platform.ALL
    ),
    YOGA(
        "lwjgl-yoga", "LWJGL - Yoga bindings",
        "An open-source, cross-platform layout library that implements Flexbox.",
        *Platform.ALL
    ),
    ZSTD(
        "lwjgl-zstd", "LWJGL - Zstandard bindings",
        "A fast lossless compression algorithm, targeting real-time compression scenarios at zlib-level and better compression ratios.",
        *Platform.ALL
    );

    private fun path(buildDir: String) =
        "bin/${buildDir}/${id}"

    val isPresent get() =
        File(path("RELEASE")).exists()

    fun hasArtifact(classifier: String) =
        File("${path("RELEASE")}/${id}-${classifier}.jar").exists()

    fun getArtifact(classifier: String? = null) =
        if (classifier === null)
            File("${path("MAVEN")}/${id}.jar")
        else
            File("${path("MAVEN")}/${id}-${classifier}.jar")

}

publishing {
    repositories {
        maven {
            url = deployment.repo

            if (deployment.type !== BuildType.LOCAL) {
                credentials {
                    username = sonatypeUsername
                    password = sonatypePassword
                }
            }
        }
    }
    publications {
        /*
        Ideally, we'd have the following structure:
        -------------------------------------------
        lwjgl
            lwjgl-windows (depends on lwjgl)
        glfw (depends on lwjgl)
            glfw-windows (depends on glfw & lwjgl-windows)
        stb (depends on lwjgl)
            stb-windows (depends on stb & lwjgl-windows)
        -------------------------------------------
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
        the above example is going to look like this in Gradle:
        -------------------------------------------
        compile 'org.lwjgl:lwjgl:$lwjglVersion' // NOTE: this is optional, all binding artifacts have a dependency on lwjgl
            compile 'org.lwjgl:lwjgl:$lwjglVersion:natives-$lwjglArch'
        compile 'org.lwjgl:lwjgl-glfw:$lwjglVersion'
            compile 'org.lwjgl:lwjgl-glfw:$lwjglVersion:natives-$lwjglArch'
        compile 'org.lwjgl:lwjgl-stb:$lwjglVersion'
            compile 'org.lwjgl:lwjgl-stb:$lwjglVersion:natives-$lwjglArch'
        -------------------------------------------
        and a whole lot more verbose in Maven. Hopefully, the automation
        is going to alleviate the pain.
         */
        fun MavenPom.setupPom(pomName: String, pomDescription: String, pomPackaging: String) {
            name.set(pomName)
            description.set(pomDescription)
            url.set("https://www.lwjgl.org")
            packaging = pomPackaging

            scm {
                connection.set("scm:git:https://github.com/LWJGL/lwjgl3.git")
                developerConnection.set("scm:git:https://github.com/LWJGL/lwjgl3.git")
                url.set("https://github.com/LWJGL/lwjgl3.git")
            }

            licenses {
                license {
                    name.set("BSD-3-Clause")
                    url.set("https://www.lwjgl.org/license")
                    distribution.set("repo")
                }
            }

            developers {
                developer {
                    id.set("spasi")
                    name.set("Ioannis Tsakpinis")
                    email.set("iotsakp@gmail.com")
                    url.set("https://github.com/Spasi")
                }
            }
        }

        Module.values().forEach { module ->
            if (module.isPresent) {
                val moduleComponent = componentFactory.createComponent(module.id, module.getArtifact(), (project.version as String)) {
                    if (deployment.type !== BuildType.LOCAL || module.hasArtifact("sources")) {
                        sources(module.getArtifact("sources"))
                    }
                    if (deployment.type !== BuildType.LOCAL || module.hasArtifact("javadoc")) {
                        javadoc(module.getArtifact("javadoc"))
                    }

                    module.platforms.forEach { platform ->
                        if (deployment.type !== BuildType.LOCAL || module.hasArtifact(platform.classifier)) {
                            native(module.getArtifact(platform.classifier), platform.os, platform.arch, platform.classifier)
                        }
                    }
                }

                create<MavenPublication>(moduleComponent.name) {
                    artifactId = module.id

                    from(moduleComponent)

                    pom {
                        setupPom(module.title, module.description, "jar")
                    }
                }
            }
        }

        create<MavenPublication>("LwjglBom") {
            from(components["javaPlatform"])
            artifactId = "lwjgl-bom"

            pom {
                setupPom("LWJGL BOM", "LWJGL 3 Bill of Materials.", "pom")

                withXml {
                    asElement().getElementsByTagName("dependencyManagement").item(0).apply {
                        asElement().getElementsByTagName("dependencies").item(0).apply {
                            Module.values().forEach { module ->
                                module.platforms.forEach { platform ->
                                    ownerDocument.createElement("dependency").also(::appendChild).apply {
                                        appendChild(ownerDocument.createElement("groupId").also(::appendChild).apply { textContent = "org.lwjgl" })
                                        appendChild(ownerDocument.createElement("artifactId").also(::appendChild).apply { textContent = module.id })
                                        appendChild(ownerDocument.createElement("version").also(::appendChild).apply { textContent = project.version as String })
                                        appendChild(ownerDocument.createElement("classifier").also(::appendChild).apply { textContent = platform.classifier })
                                    }
                                }
                            }
                        }
                    }

                    // Workaround for https://github.com/gradle/gradle/issues/7529
                    asNode()
                }
            }
        }
    }
}

signing {
    setRequired({
        canSign
    })
    useInMemoryPgpKeys(
        signingKeyId,
        signingKey,
        signingPassword
    )
    sign(publishing.publications)
}

val copyArchives = tasks.create<Copy>("copyArchives") {
    from("bin/RELEASE")
    include("**")
    destinationDir = buildDir
}

tasks.withType<Sign> {
    dependsOn(copyArchives)
}

dependencies {
    constraints {
        Module.values().forEach { module ->
            api("org.lwjgl:${module.id}:${project.version}")
        }
    }
}