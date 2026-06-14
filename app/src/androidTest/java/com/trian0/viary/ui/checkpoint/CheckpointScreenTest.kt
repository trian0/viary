package com.trian0.viary.ui.checkpoint

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.trian0.viary.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CheckpointScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun viaryName_isDisplayed() {
        composeTestRule.setContent {
            CheckpointScreenView(
                modifier = Modifier.fillMaxSize(),
                viaryName = "Viagem Teste",
            )
        }
        composeTestRule.onNodeWithText("Viagem Teste")
            .fetchSemanticsNode("viaryName text not found")
    }

    @Test
    fun previewAccumulated_isDisplayed() {
        composeTestRule.setContent {
            CheckpointScreenView(
                modifier = Modifier.fillMaxSize(),
                viaryName = "Viagem",
                previewAccumulated = 99.5,
            )
        }
        // previewAccumulated is rendered via Double.toString() → "99.5"
        composeTestRule.onNodeWithText(99.5.toString())
            .fetchSemanticsNode("previewAccumulated '99.5' not found")
    }

    @Test
    fun saveButton_isVisible() {
        composeTestRule.setContent {
            CheckpointScreenView(
                modifier = Modifier.fillMaxSize(),
                viaryName = "Viagem",
            )
        }
        val label = composeTestRule.activity.getString(
            R.string.checkpoint_screen_checkpoint_save_checkpoint_button
        )
        composeTestRule.onNodeWithText(label).fetchSemanticsNode("save button not found")
    }
}
