pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SmartBoard"

include(":app")
include(":ime")
include(":feature:settings")
include(":feature:onboarding")
include(":feature:clipboard-ui")
include(":core:model")
include(":core:domain")
include(":core:data")
include(":core:ui")
include(":core:common")

project(":feature:settings").projectDir = file("feature/settings")
project(":feature:onboarding").projectDir = file("feature/onboarding")
project(":feature:clipboard-ui").projectDir = file("feature/clipboard-ui")
project(":core:model").projectDir = file("core/model")
project(":core:domain").projectDir = file("core/domain")
project(":core:data").projectDir = file("core/data")
project(":core:ui").projectDir = file("core/ui")
project(":core:common").projectDir = file("core/common")
