// Stonecutter 多版本共享构建脚本：每个 versions/<版本> 子项目都会用它构建，
// stonecutter.current.version 是当前目标版本；版本专属数值放在 versions/<版本>/gradle.properties。
plugins {
    id("fabric-loom") version "1.19.3"
}

val mcVersion = stonecutter.current.version
val javaRelease = (property("java_release") as String).toInt()

// 1.19.4 / 1.20.1 继续走 Statement 路线；1.21.11 / 26.3 用自带的状态注入（无 Statement）。
val statementEra = mcVersion == "1.19.4" || mcVersion == "1.20.1"
// 1.21.11 / 26.3 使用官方映射源码（src/main/mojmap/java）。
val mojmapEra = mcVersion == "1.21.11" || mcVersion == "26.3"

version = "${property("mod_version")}+$mcVersion"
group = property("maven_group") as String
base { archivesName = property("archives_base_name") as String }

// 两套源码按版本挂载：yarn 命名（1.19.4 / 1.20.1）与官方命名（1.21.11 / 26.3），
// 类名相同、实现不同，靠 stonecutter 指令处理版本内的差异。
if (mojmapEra) {
    sourceSets.main {
        java.srcDir("src/main/mojmap/java")
    }
} else {
    sourceSets.main {
        java.srcDir("src/main/yarn/java")
    }
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    if (mcVersion == "26.3") {
        // 26.1+ 起游戏自带官方名、不再混淆：没有映射文件也不需要重映射，
        // 依赖按 Fabric 迁移指南用普通 implementation（versions/26.3 已开 disableObfuscation）。
        add("implementation", "net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    } else if (mojmapEra) {
        // 1.21.11 共用 26.3 的官方名源码，因此同样使用官方映射而不是 Yarn。
        add("mappings", loom.officialMojangMappings())
        add("modImplementation", "net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    } else {
        add("mappings", "net.fabricmc:yarn:${property("yarn_mappings")}:v2")
        add("modImplementation", "net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
        if (statementEra) {
            // Statement 4.2.9 单 jar 覆盖 1.14.4-1.20.4，1.19.4 / 1.20.1 共用。
            add("modApi", "com.github.Virtuoel:Statement:4.2.9") {
                exclude(group = "net.fabricmc.fabric-api")
            }
        }
    }
}

tasks.named<ProcessResources>("processResources") {
    inputs.property("version", project.version)
    filteringCharset = "UTF-8"
    // 只做占位符替换。不要用 Gradle expand：它会把 JSON 字符串里的 \n 解释成真实换行，
    // 造成源文件合法、最终 JAR 里的 fabric.mod.json 却损坏。
    // 占位符的值在任务作用域外取好：Task 上也有一个动态 property()，会遮蔽 Project 的同名方法。
    val modId = project.property("mod_id") as String
    val modName = project.property("mod_name") as String
    val modAuthor = project.property("mod_author") as String
    val modDescription = project.property("mod_description") as String
    val modHome = project.property("mod_home") as String
    val modIssues = project.property("mod_issues") as String
    val modLicense = project.property("mod_license") as String
    val loaderVersion = project.property("fabric_loader_version") as String
    val mcDependency = project.property("mc_dependency") as String
    val statementDepends = if (statementEra) ",\"statement\": \"*\"" else ""
    val extraMixins = if (!statementEra) ",\"lavalogged.states.mixins.json\"" else ""
    filesMatching("fabric.mod.json") {
        filter { line -> line
            .replace("\${version}", project.version.toString())
            .replace("\${mod_id}", modId)
            .replace("\${mod_name}", modName)
            .replace("\${mod_author}", modAuthor)
            .replace("\${mod_description}", modDescription)
            .replace("\${mod_home}", modHome)
            .replace("\${mod_issues}", modIssues)
            .replace("\${mod_license}", modLicense)
            .replace("\${fabric_loader_version}", loaderVersion)
            .replace("\${mc_dependency}", mcDependency)
            .replace("\${statement_depends}", statementDepends)
            .replace("\${extra_mixins}", extraMixins)
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = javaRelease
}

java {
    // 不固定 toolchain：编译用 options.release 切 17/21，运行构建的 JVM 由环境提供（>=21 即可）。
    sourceCompatibility = JavaVersion.toVersion(javaRelease)
    targetCompatibility = JavaVersion.toVersion(javaRelease)
    withSourcesJar()
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${base.archivesName.get()}" }
    }
}
