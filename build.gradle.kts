plugins {
    `java-library`
    id("net.neoforged.moddev") version "2.0.148"
    idea
    id("com.diffplug.spotless") version "8.10.3"
}

val parchmentMinecraftVersion: String = providers.gradleProperty("parchment_minecraft_version").get()
val parchmentMappingsVersion: String = providers.gradleProperty("parchment_mappings_version").get()
val minecraftVersion: String = providers.gradleProperty("minecraft_version").get()
val minecraftVersionRange: String = providers.gradleProperty("minecraft_version_range").get()
val neoVersion: String = providers.gradleProperty("neo_version").get()
val neoVersionRange: String = providers.gradleProperty("neo_version_range").get()
val loaderVersionRange: String = providers.gradleProperty("loader_version_range").get()
val modId: String = providers.gradleProperty("mod_id").get()
val modName: String = providers.gradleProperty("mod_name").get()
val modLicense: String = providers.gradleProperty("mod_license").get()
val modVersion: String = providers.gradleProperty("mod_version").get()
val modGroupId: String = providers.gradleProperty("mod_group_id").get()
val modAuthors: String = providers.gradleProperty("mod_authors").get()
val modDescription: String = providers.gradleProperty("mod_description").get()

tasks.named<Wrapper>("wrapper") {
    distributionType = Wrapper.DistributionType.BIN
}

version = modVersion
group = modGroupId

base {
    archivesName.set(modId)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

neoForge {
    version = neoVersion

    parchment {
        mappingsVersion = parchmentMappingsVersion
        minecraftVersion = parchmentMinecraftVersion
    }

    mods {
        create(modId) {
            sourceSet(sourceSets.main.get())
        }
    }
}

val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
    val replaceProperties = mapOf(
        "minecraft_version_range" to minecraftVersionRange,
        "neo_version_range" to neoVersionRange,
        "loader_version_range" to loaderVersionRange,
        "mod_id" to modId,
        "mod_name" to modName,
        "mod_license" to modLicense,
        "mod_version" to modVersion,
        "mod_authors" to modAuthors,
        "mod_description" to modDescription,
    )

    inputs.properties(replaceProperties)
    expand(replaceProperties)
    from("src/main/templates")
    into(layout.buildDirectory.dir("generated/sources/modMetadata"))
}

sourceSets {
    main {
        resources.srcDir(generateModMetadata)
    }
}

neoForge.ideSyncTask(generateModMetadata)

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-processing", "-Xlint:-serial"))
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

spotless {
    java {
        target("src/*/java/**/*.java")
        importOrder()
        removeUnusedImports()
        palantirJavaFormat("2.85.0")
        leadingTabsToSpaces(4)
        trimTrailingWhitespace()
        endWithNewline()
    }

    json {
        target("src/**/*.json")
        gson().indentWithSpaces(2)
    }
}
