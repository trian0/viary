package com.trian0.viary.data.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ShareImageSharerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `file name removes accents and symbols`() {
        assertEquals("viary-cafe-da-serra-1.png", shareImageFileName("Café da Serra!", 1))
    }

    @Test
    fun `file name falls back when the name has no usable characters`() {
        assertEquals("viary-viary-5.png", shareImageFileName("???", 5))
        assertEquals("viary-viary-5.png", shareImageFileName("", 5))
    }

    @Test
    fun `file name truncates long names without a trailing dash`() {
        val name = "a".repeat(39) + " bbbbbb"
        assertEquals("viary-${"a".repeat(39)}-9.png", shareImageFileName(name, 9))
    }

    @Test
    fun `clear share dir removes previous images and keeps the folder`() {
        val dir = tempFolder.newFolder("share")
        val old = tempFolder.newFile("share/old.png")

        clearShareDir(dir)

        assertFalse(old.exists())
        assertTrue(dir.exists())
    }

    @Test
    fun `clear share dir tolerates a missing folder`() {
        clearShareDir(tempFolder.root.resolve("nope"))
    }
}
