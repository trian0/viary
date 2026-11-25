package com.trian0.viary.ui.viarydetails.share

import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.trian0.viary.R
import com.trian0.viary.data.models.Checkpoint
import com.trian0.viary.data.models.Viary
import com.trian0.viary.data.utils.formatMoney
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class ViaryShareCardTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val viary = Viary(
        name = "Serra da Mantiqueira",
        origin = "Campinas",
        departureTime = Date(0),
        arrivalTime = Date(3_600_000),
        status = Viary.ViaryStatus.COMPLETED,
        kmEnd = 120f,
        selectedImage = null,
        climate = null,
    )

    private fun checkpoints(count: Int) = List(count) { index ->
        Checkpoint(placeName = "Local ${index + 1}", expense = 10.0 * (index + 1))
    }

    private fun money(amount: Double) = formatMoney(amount, "R$", Locale.getDefault())

    private fun count(quantity: Int) = composeTestRule.activity.resources
        .getQuantityString(R.plurals.share_card_checkpoint_count, quantity, quantity)

    private fun origin() = composeTestRule.activity.getString(R.string.viary_details_screen_origin_prefix, "Campinas")

    private fun showCard(options: ShareOptions, checkpointCount: Int = 2) {
        composeTestRule.setContent {
            ViaryShareCard(
                viary = viary,
                checkpoints = checkpoints(checkpointCount),
                durationFormatted = "1h 0m",
                currencySymbol = "R$",
                options = options,
                map = null,
                cover = null,
            )
        }
    }

    private fun hasNode(text: String) =
        composeTestRule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()

    @Test
    fun withEverythingOn_showsNamesAndTotal() {
        showCard(ShareOptions())

        assertTrue("trip name missing", hasNode(viary.name))
        assertTrue("origin missing", hasNode(origin()))
        assertTrue("checkpoint count missing", hasNode(count(2)))
        assertTrue("checkpoint names missing", hasNode("Local 1, Local 2"))
        assertTrue("total expense missing", hasNode(money(30.0)))
    }

    @Test
    fun withExpensesOff_hidesTheTotal() {
        showCard(ShareOptions(showExpenses = false), checkpointCount = 5)

        assertFalse("total expense should be hidden", hasNode(money(150.0)))
        assertTrue("checkpoint count missing", hasNode(count(5)))
    }

    @Test
    fun withPlaceNamesOff_showsOnlyTheCountAndHidesTheOrigin() {
        showCard(ShareOptions(showPlaceNames = false))

        assertTrue("checkpoint count missing", hasNode(count(2)))
        assertFalse("checkpoint names should be hidden", hasNode("Local 1, Local 2"))
        assertFalse("origin should be hidden", hasNode(origin()))
    }

    @Test
    fun withManyCheckpoints_namesThreeAndCountsTheRest() {
        showCard(ShareOptions(), checkpointCount = 6)

        assertTrue("checkpoint count missing", hasNode(count(6)))
        assertTrue("first names missing", hasNode("Local 1, Local 2, Local 3"))
        assertTrue("remaining count missing", hasNode("+3"))
    }

    @Test
    fun withoutCheckpoints_hidesTheSummary() {
        showCard(ShareOptions(), checkpointCount = 0)

        assertFalse("summary should be hidden", hasNode(count(0)))
    }

    @Test
    fun exportCard_capturesAt1080x1350RegardlessOfScreenDensity() {
        lateinit var layer: GraphicsLayer
        composeTestRule.setContent {
            layer = rememberGraphicsLayer()
            ExportCard(
                layer = layer,
                viary = viary,
                checkpoints = checkpoints(2),
                durationFormatted = "1h 0m",
                currencySymbol = "R$",
                options = ShareOptions(),
                map = null,
                cover = null,
            )
        }
        composeTestRule.waitForIdle()

        val bitmap = runBlocking { layer.toImageBitmap() }

        assertEquals(1080, bitmap.width)
        assertEquals(1350, bitmap.height)
    }
}
