package daniel.avila.rnm.kmm.domain.model.exchange_rate

enum class Tag(val displayName: String) {
    CLOSEST("Ближайший"),
    BEST_SELL("Самый выгодный"),
    BEST_BUY("Самый выгодный"),
    UNKNOWN("Ошибка");

    companion object {
        fun fromValue(value: String): Tag {
            return when (value) {
                "closest" -> CLOSEST
                "best_sell" -> BEST_SELL
                "best_buy" -> BEST_BUY
                else -> UNKNOWN
            }
        }
    }
}