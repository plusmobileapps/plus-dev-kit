package com.plusmobileapps.devkit.newfeature

object BuildGradleDefaults {
    fun getDefaultPublicBuildGradle(): String {
        return """
            plugins {
                kotlin("multiplatform")
            }
            
            kotlin {
                jvm()
                js(IR) {
                    browser()
                    nodejs()
                }
                
                sourceSets {
                    val commonMain by getting {
                        dependencies {
                            // Public module dependencies
                        }
                    }
                    val commonTest by getting {
                        dependencies {
                            implementation(kotlin("test"))
                        }
                    }
                }
            }
        """.trimIndent()
    }

    fun getDefaultImplBuildGradle(): String {
        return """
            plugins {
                kotlin("multiplatform")
            }
            
            kotlin {
                jvm()
                js(IR) {
                    browser()
                    nodejs()
                }
                
                sourceSets {
                    val commonMain by getting {
                        dependencies {
                            implementation(project("${'$'}projectDirectory:${'$'}directoryName:public"))
                            // Implementation module dependencies
                        }
                    }
                    val commonTest by getting {
                        dependencies {
                            implementation(kotlin("test"))
                        }
                    }
                }
            }
        """.trimIndent()
    }

    fun getDefaultTestingBuildGradle(): String {
        return """
            plugins {
                kotlin("multiplatform")
            }
            
            kotlin {
                jvm()
                js(IR) {
                    browser()
                    nodejs()
                }
                
                sourceSets {
                    val commonMain by getting {
                        dependencies {
                            implementation(project("${'$'}projectDirectory:${'$'}directoryName:public"))
                            implementation(project("${'$'}projectDirectory:${'$'}directoryName:impl"))
                            // Testing module dependencies
                            implementation(kotlin("test"))
                        }
                    }
                    val commonTest by getting {
                        dependencies {
                            implementation(kotlin("test"))
                        }
                    }
                }
            }
        """.trimIndent()
    }
}