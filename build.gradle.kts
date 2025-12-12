// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.13.2" apply false
    id("com.android.library") version "8.13.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    // Plugin Google Services bắt buộc phải khai báo ở đây
    id("com.google.gms.google-services") version "4.4.2" apply false
}