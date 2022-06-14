/*
 * Copyright (c) 2020, 2022 Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import org.graalvm.buildtools.gradle.tasks.BuildNativeImageTask

plugins {
    java
    id("org.graalvm.buildtools.native")
}

// tag::select-toolchain[]
graalvmNative {
    binaries {
        named("main") {
            javaLauncher.set(javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(8))
                vendor.set(JvmVendorSpec.matching("GraalVM Community"))
            })
        }
    }
}
// end::select-toolchain[]

if (providers.environmentVariable("DISABLE_TOOLCHAIN").isPresent()) {
// tag::disabling-toolchain[]
    graalvmNative {
        toolchainDetection.set(false)
    }
// end::disabling-toolchain[]
}

// tag::all-config-options[]
graalvmNative {
    binaries {
        named("main") {
            // Main options
            imageName.set("application") // The name of the native image, defaults to the project name
            mainClass.set("org.test.Main") // The main class to use, defaults to the application.mainClass
            debug.set(true) // Determines if debug info should be generated, defaults to false
            verbose.set(true) // Add verbose output, defaults to false
            fallback.set(true) // Sets the fallback mode of native-image, defaults to false
            sharedLibrary.set(false) // Determines if image is a shared library, defaults to false if `java-library` plugin isn't included
            quickBuild.set(false) // Determines if image is being built in quick build mode (alternatively use GRAALVM_QUICK_BUILD environment variable)
            richOutput.set(false) // Determines if native-image building should be done with rich output

            systemProperties.putAll(mapOf("name1" to "value1", "name2" to "value2")) // Sets the system properties to use for the native image builder
            configurationFileDirectories.from(file("src/my-config")) // Adds a native image configuration file directory, containing files like reflection configuration
            excludeConfig.put("org.example.test", listOf("META-INF/native-image/*", "config/*")) // Excludes configuration that matches one of given regexes from JAR of dependency with said coordinates.
            excludeConfig.put(file("path/to/artifact.jar"), listOf("META-INF/native-image/*", "config/*"))

            // Advanced options
            buildArgs.add("-H:Extra") // Passes '-H:Extra' to the native image builder options. This can be used to pass parameters which are not directly supported by this extension
            jvmArgs.add("flag") // Passes 'flag' directly to the JVM running the native image builder

            // Runtime options
            runtimeArgs.add("--help") // Passes '--help' to built image, during "nativeRun" task

            useFatJar.set(true) // Instead of passing each jar individually, builds a fat jar
        }
    }
}
// end::all-config-options[]

// tag::enable-fatjar[]
graalvmNative {
    useFatJar.set(false) // required for older GraalVM releases
    binaries {
        named("main") {
            useFatJar.set(true)
        }
    }
}
// end::enable-fatjar[]

val myFatJar = tasks.register<Jar>("myFatJar")

// tag::custom-fatjar[]
tasks.named<BuildNativeImageTask>("nativeCompile") {
    classpathJar.set(myFatJar.flatMap { it.archiveFile })
}
// end::custom-fatjar[]

// tag::disable-test-support[]
graalvmNative {
    testSupport.set(false)
}
// end::disable-test-support[]

val integTest by sourceSets.creating
val integTest = tasks.register<Test>("integTest")

// tag::custom-binary[]
graalvmNative {
    registerTestBinary("integTest") {
        usingSourceSet(sourceSets.getByName("integTest"))
        forTestTask(tasks.named<Test>("integTest"))
    }
}
// end::custom-binary[]

// tag::add-agent-options[]
graalvmNative {
    agent {
        enableExperimentalPredefinedClasses = true
    }
}
// end::add-agent-options[]

// tag::enable-metadata-repository[]
graalvmNative {
    metadataRepository {
        enabled.set(true)
    }
}
// end::enable-metadata-repository[]

// tag::specify-metadata-repository-version[]
graalvmNative {
    metadataRepository {
        version.set("1.0.0")
    }
}
// end::specify-metadata-repository-version[]


// tag::specify-metadata-repository-file[]
graalvmNative {
    metadataRepository {
        uri(file("metadata-repository"))
    }
}
// end::specify-metadata-repository-file[]

// tag::exclude-module-from-metadata-repo[]
graalvmNative {
    metadataRepository {
        // Exclude this library from automatic metadata
        // repository search
        excludes.add("com.company:some-library")
    }
}
// end::exclude-module-from-metadata-repo[]

// tag::specify-metadata-version-for-library[]
graalvmNative {
    metadataRepository {
        // Force the version of the metadata for a particular library
        moduleToConfigVersion.put("com.company:some-library", "3")
    }
}
// end::specify-metadata-version-for-library[]
