package com.trian0.viary.ui.create

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.trian0.viary.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun startTripButton_isVisible() {
        composeTestRule.setContent {
            CreateScreenView(modifier = Modifier.fillMaxSize())
        }
        val label = composeTestRule.activity.getString(R.string.create_screen_start_viary_button)
        composeTestRule.onNodeWithText(label).fetchSemanticsNode("start button not found")
    }

    @Test
    fun allClimateOptions_areRendered() {
        composeTestRule.setContent {
            CreateScreenView(modifier = Modifier.fillMaxSize())
        }
        val activity = composeTestRule.activity
        listOf(
            R.string.create_screen_climate_sunny,
            R.string.create_screen_climate_cloudy,
            R.string.create_screen_climate_rainy,
            R.string.create_screen_climate_chill,
        ).forEach { resId ->
            val label = activity.getString(resId)
            composeTestRule.onNodeWithText(label).fetchSemanticsNode("climate option '$label' not found")
        }
    }

    @Test
    fun viaryNameField_acceptsInput() {
        composeTestRule.setContent {
            CreateScreenView(modifier = Modifier.fillMaxSize())
        }
        // First text field with text input action = viaryName field
        composeTestRule.onAllNodes(hasSetTextAction())[0].performTextInput("Lisboa")
        composeTestRule.onNodeWithText("Lisboa").fetchSemanticsNode("typed text 'Lisboa' not found")
    }

    @Test
    fun screenTitle_isDisplayed() {
        composeTestRule.setContent {
            CreateScreenView(modifier = Modifier.fillMaxSize())
        }
        val title = composeTestRule.activity.getString(R.string.create_screen_title)
        composeTestRule.onNodeWithText(title).fetchSemanticsNode("screen title not found")
    }
}
