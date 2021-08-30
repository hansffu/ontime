package hansffu.ontime.model

enum class StopListType { FAVORITES, NEARBY }

data class StopListModel(val type: StopListType, val stops: List<Stop>)
