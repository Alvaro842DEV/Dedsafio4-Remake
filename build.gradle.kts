plugins {
    id("net.fabricmc.fabric-loom-remap") version "1.17-SNAPSHOT"
    id("com.diffplug.spotless") version "8.10.3"
}

val parchmentMinecraftVersion: String = providers.gradleProperty("parchment_minecraft_version").get()
val parchmentMappingsVersion: String = providers.gradleProperty("parchment_mappings_version").get()
val minecraftVersion: String = providers.gradleProperty("minecraft_version").get()
val fabricLoaderVersion: String = providers.gradleProperty("fabric_loader_version").get()
val fabricApiVersion: String = providers.gradleProperty("fabric_api_version").get()
val modId: String = providers.gradleProperty("mod_id").get()
val modName: String = providers.gradleProperty("mod_name").get()
val modLicense: String = providers.gradleProperty("mod_license").get()
val modVersion: String = providers.gradleProperty("mod_version").get()
val modGroupId: String = providers.gradleProperty("mod_group_id").get()
val modAuthors: String = providers.gradleProperty("mod_authors").get()
val modDescription: String = providers.gradleProperty("mod_description").get()

version = "$modVersion+$minecraftVersion-fabric"
group = modGroupId

base {
    archivesName.set(modId)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    maven("https://maven.parchmentmc.org")
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-$parchmentMinecraftVersion:$parchmentMappingsVersion@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
}

tasks.processResources {
    val replaceProperties = mapOf(
        "mod_id" to modId,
        "mod_name" to modName,
        "mod_license" to modLicense,
        "mod_version" to modVersion,
        "mod_authors" to modAuthors,
        "mod_description" to modDescription,
        "minecraft_version" to minecraftVersion,
        "fabric_loader_version" to fabricLoaderVersion,
    )
    inputs.properties(replaceProperties)
    filesMatching("fabric.mod.json") {
        expand(replaceProperties)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
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
