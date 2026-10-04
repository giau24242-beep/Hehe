package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.PrankSettings
import com.example.util.CrackGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Bể Màn Hình", appName)
  }

  @Test
  fun `verify crack generation at touch coordinate`() {
    val impact = CrackGenerator.createImpact(
      touchX = 300f,
      touchY = 500f,
      screenWidth = 1080f,
      screenHeight = 2400f,
      tapCount = 4,
      existingNearbyCount = 1,
      settings = PrankSettings()
    )

    assertNotNull(impact)
    assertEquals(300f, impact.touchX)
    assertEquals(500f, impact.touchY)
    assertTrue("Should generate radial rays", impact.radialRays.isNotEmpty())
    assertTrue("Should generate spiderweb rings", impact.webRings.isNotEmpty())
    assertTrue("Should generate ink blobs on repeated taps", impact.inkBlobs.isNotEmpty())
    assertTrue("Should generate RGB glitch lines on repeated taps", impact.glitchLines.isNotEmpty())
  }
}
