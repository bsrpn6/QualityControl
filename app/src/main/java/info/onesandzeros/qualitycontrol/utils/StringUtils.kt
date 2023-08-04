package info.onesandzeros.qualitycontrol.utils

import java.util.Locale

object StringUtils {
    fun formatTabText(tabText: String): String {
        val prefixToRemove = "product_"
        var formattedText = tabText

        // Check if the tabText starts with the prefix to be removed
        if (tabText.startsWith(prefixToRemove)) {
            // Remove the prefix and capitalize the first letter after the prefix
            formattedText = tabText.removePrefix(prefixToRemove)
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }

        // Split the tabText by underscores
        val words = formattedText.split("_".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()

        // Initialize a StringBuilder to hold the formatted text
        val result = StringBuilder()

        // Convert each word to Title case and append to the StringBuilder
        for (word in words) {
            if (word.isNotEmpty()) {
                result.append(word[0].uppercaseChar())
                    .append(word.substring(1).lowercase(Locale.getDefault())).append(" ")
            }
        }

        // Remove the trailing space at the end, if any
        if (result.isNotEmpty()) {
            result.deleteCharAt(result.length - 1)
        }
        return result.toString()
    }


    fun parseUsername(email: String): String {
        // Define the regular expression pattern to match the username part of the email
        val pattern = Regex("^([^.]+)\\.([^.]+)@.+")

        // Match the pattern against the email
        val matchResult = pattern.find(email)

        // If the pattern matches and captures two groups (first name and last name), return the combined name
        matchResult?.groupValues?.let { (_, firstName, lastName) ->
            return "$firstName $lastName"
        }

        // If the pattern does not match, return the part of the email before the '@' symbol as the username
        val parts = email.split("@")
        if (parts.size == 2) {
            return parts[0]
        }

        // If there is no '@' symbol in the email, return the entire email as the username
        return email
    }
}
