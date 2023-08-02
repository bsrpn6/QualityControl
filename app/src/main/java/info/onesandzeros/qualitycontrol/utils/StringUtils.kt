package info.onesandzeros.qualitycontrol.utils

import java.util.Locale

object StringUtils {
    fun formatTabText(tabText: String): String {
        // Split the tabText by underscores
        val words = tabText.split("_".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()

        // Initialize a StringBuilder to hold the formatted text
        val formattedText = StringBuilder()

        // Convert each word to Title case and append to the StringBuilder
        for (word in words) {
            if (word.length > 0) {
                formattedText.append(word[0].uppercaseChar())
                    .append(word.substring(1).lowercase(Locale.getDefault())).append(" ")
            }
        }

        // Remove the trailing space at the end, if any
        if (formattedText.length > 0) {
            formattedText.deleteCharAt(formattedText.length - 1)
        }
        return formattedText.toString()
    }
}
