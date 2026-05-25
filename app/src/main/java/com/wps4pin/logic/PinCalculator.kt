package com.wps4pin.logic

/**
 * Pure WPS PIN derivation engine — ported 1:1 from Swift (ContentView.swift / File.swift).
 *
 * Algorithm: takes last 3 octets of MAC (suffix), applies GTIN-8 checksum
 * to produce an 8-digit WPS PIN.
 *
 * No Android UI dependency. Pure computation.
 */
object PinCalculator {

    /**
     * Normalises a MAC address string: accepts formats
     *   XX:XX:XX:XX:XX:XX | XX-XX-XX-XX-XX-XX | XXXXXXXXXXXX
     * Returns a list of 6 octets as Ints, or null if invalid.
     */
    fun parseMac(mac: String): List<Int>? {
        val cleaned = mac.trim().uppercase()
        val parts: List<String> = when {
            cleaned.contains(':') -> cleaned.split(':')
            cleaned.contains('-') -> cleaned.split('-')
            else -> {
                if (cleaned.length != 12) return null
                cleaned.chunked(2)
            }
        }
        if (parts.size != 6) return null
        return parts.map { part ->
            val v = part.toIntOrNull(16) ?: return null
            if (v !in 0..255) return null
            v
        }
    }

    /**
     * Calculates WPS PIN from MAC address string.
     * Returns the 8-digit PIN string, or null if MAC is invalid.
     *
     * Ported verbatim from Swift:
     *   1. Take last 3 octets → macSuffix = (p4 << 16) | (p5 << 8) | p6
     *   2. Compute GTIN-8 checksum on macSuffix
     *   3. Return format: "%08d%d" = 8 digits from (suffix % 100_000_000) + checksum digit
     */
    fun calculate(mac: String): String? {
        val octets = parseMac(mac) ?: return null
        val part4 = octets[3].toLong()
        val part5 = octets[4].toLong()
        val part6 = octets[5].toLong()

        val macSuffix = (part4 shl 16) or (part5 shl 8) or part6

        // GTIN-8 checksum (same logic as Swift: alternating *3 / *1 from rightmost digit)
        var oddSum = 0
        var multiplier = 3
        var tempNumber = macSuffix.toInt()
        while (tempNumber > 0) {
            val digit = tempNumber % 10
            oddSum += digit * multiplier
            multiplier = if (multiplier == 3) 1 else 3
            tempNumber /= 10
        }
        val remainder = oddSum % 10
        val lastDigit = if (remainder == 0) 0 else 10 - remainder

        // Format: 8 digits from suffix % 100_000_000 + checksum digit → 9 chars total
        // Swift: String(format: "%08d%d", macSuffix % 100000000, lastDigit)
        return String.format("%08d%d", macSuffix % 100_000_000, lastDigit)
    }
}
