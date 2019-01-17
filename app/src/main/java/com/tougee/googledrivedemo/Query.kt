package com.tougee.googledrivedemo

class Query(
    val filters: List<String>?
) {

    private constructor(builder: Builder) : this(builder.filters)

    companion object {
        inline fun build(block: Builder.() -> Unit) = Builder().apply(block).build()
    }

    override fun toString(): String {
        return filters?.joinToString(" and ") ?: super.toString()
    }

    class Builder {
        var filters: List<String>? = null

        fun build() = Query(this)
    }
}
