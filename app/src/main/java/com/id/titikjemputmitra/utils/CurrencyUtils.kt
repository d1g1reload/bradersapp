package com.id.titikjemputmitra.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

fun formatRupiah(number: Double?): String {
    if (number == null || number == 0.0) return "Rp0"

    val localeID = Locale("in", "ID")
    val symbols = DecimalFormatSymbols(localeID)

    // Mengatur simbol mata uang agar rapat atau berjarak (sesuai selera)
    symbols.currencySymbol = "Rp "
    symbols.monetaryDecimalSeparator = ','
    symbols.groupingSeparator = '.'

    // Format ###,### tanpa desimal di belakang koma
    val formatter = DecimalFormat("#,###", symbols)
    formatter.maximumFractionDigits = 0

    return "Rp " + formatter.format(number)
}