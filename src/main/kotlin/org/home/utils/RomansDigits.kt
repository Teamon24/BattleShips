package org.home.utils

import java.util.*
import java.util.stream.Collectors


object RomansDigits {
    enum class RomanNumeral(val value: Int) {
        I(1), IV(4), V(5), IX(9), X(10), XL(40), L(50), XC(90), C(100), CD(400), D(500), CM(900), M(1000);

        companion object {
            val reverseSortedValues: List<RomanNumeral>
                get() = Arrays.stream(values())
                    .sorted(Comparator.comparing { e: RomanNumeral -> e.value }.reversed())
                    .collect(Collectors.toList())
        }
    }

    fun romanToArabic(input: String): Int {
        var romanNumeral = input.uppercase(Locale.getDefault())
        var result = 0
        val romanNumerals: List<RomanNumeral> = RomanNumeral.reverseSortedValues
        var i = 0
        while (romanNumeral.isNotEmpty() && i < romanNumerals.size) {
            val symbol: RomanNumeral = romanNumerals[i]
            if (romanNumeral.startsWith(symbol.name)) {
                result += symbol.value
                romanNumeral = romanNumeral.substring(symbol.name.length)
            } else {
                i++
            }
        }
        require(romanNumeral.isEmpty()) { "$input cannot be converted to a Roman Numeral" }
        return result
    }

    fun arabicToRoman(number: Int): String {
        var number = number
        require(!(number <= 0 || number > 4000)) { "$number is not in range (0,4000]" }
        val romanNumerals: List<RomanNumeral> = RomanNumeral.reverseSortedValues
        var i = 0
        val sb = StringBuilder()
        while (number > 0 && i < romanNumerals.size) {
            val currentSymbol = romanNumerals[i]
            if (currentSymbol.value <= number) {
                sb.append(currentSymbol.name)
                number -= currentSymbol.value
            } else {
                i++
            }
        }
        return sb.toString()
    }
}