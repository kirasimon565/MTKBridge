#!/bin/bash
cat << 'INNER_EOF' > app/src/test/kotlin/com/blackmoon/mtkbridge/MainActivityTest.kt
package com.blackmoon.mtkbridge

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.Robolectric
import androidx.test.core.app.ActivityScenario

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class MainActivityTest {

    @Test
    fun testForegroundServiceRequirement() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
    }
}
INNER_EOF
./gradlew test
