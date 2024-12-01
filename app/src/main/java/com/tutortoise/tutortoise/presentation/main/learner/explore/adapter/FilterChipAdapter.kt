class FilterChipAdapter<T>(
    val initialLimit: Int = 3,
    private val onShowAllClick: () -> Unit
) {
    private var items: List<T> = emptyList()
    private var isExpanded = false

    fun setItems(newItems: List<T>) {
        items = newItems
    }

    fun getVisibleItems(): List<T> {
        return if (!isExpanded && items.size > initialLimit) {
            items.take(initialLimit)
        } else {
            items
        }
    }

    fun shouldShowViewAll(): Boolean = items.size > initialLimit && !isExpanded

    fun getExpandedState(): Boolean = isExpanded

    fun toggleExpanded() {
        isExpanded = !isExpanded
        onShowAllClick()
    }
}