package me.ele.uetool.cat

/**
 * Extracts the substring after the last occurrence of the character '.'.
 *
 * @param fullString the full string (e.g., "xxx.xxx.xxx").
 * @return the substring after the last '.', or the full string if '.' is not found.
 */
fun extractAfterLastDot(fullString: String): String {
    // Find the index of the last occurrence of '.'.
    val lastDotIndex = fullString.lastIndexOf('.')

    // If there is no '.' in the string, we return the full string.
    return if (lastDotIndex == -1 || lastDotIndex == fullString.length - 1) {
        fullString
    } else fullString.substring(lastDotIndex + 1)

    // Extract the part of the string after the last '.'.
}