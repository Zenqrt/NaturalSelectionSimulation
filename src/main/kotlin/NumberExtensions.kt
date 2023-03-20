import java.text.DecimalFormat

fun Double.round(places: Int): Double = DecimalFormat("#." + "#".repeat(places)).format(this).toDouble()