class FilterChipAdapter(
    val initialLimit: Int = 3,
    private val onShowAllClick: () -> Unit
) {
    private var items: List<String> = emptyList()
    private var isExpanded = false

    fun setItems(newItems: List<String>) {
        items = newItems
    }

    fun getVisibleItems(): List<String> {
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