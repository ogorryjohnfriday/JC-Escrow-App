pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral() // ✅ Main source of yvos.android now

        // Optional fallback if Youverify republishes SDK here later
        maven {
            url = uri("https://s01.oss.sonatype.org/content/repositories/releases/")
        }

        // Optional — Youverify’s previous private repo, left as fallback
        maven {
            url = uri("https://s01.oss.sonatype.org/content/repositories/co-youverify-1001/")
            isAllowInsecureProtocol = true
        }
    }
}

rootProject.name = "JC escrow"
include(":app")