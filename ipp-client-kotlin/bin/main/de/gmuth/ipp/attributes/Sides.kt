package de.gmuth.ipp.attributes

/**
 * Copyright (c) 2020-2023 Gerhard Muth
 */

import de.gmuth.ipp.core.IppAttribute
import de.gmuth.ipp.core.IppAttributeBuilder
import de.gmuth.ipp.core.IppAttributesGroup
import de.gmuth.ipp.core.IppTag.Keyword

enum class Sides(private val keyword: String) : IppAttributeBuilder {

    OneSided("one-sided"),
    TwoSidedLongEdge("two-sided-long-edge"),
    TwoSidedShortEdge("two-sided-short-edge");

    override fun buildIppAttribute(printerAttributes: IppAttributesGroup) =
        IppAttribute("sides", Keyword, keyword)

    companion object {
        // 可以访问私有属性 keyword，因为 companion 在同一类中
        private val MAP: Map<String, Sides> = values().associateBy { it.keyword }

        @JvmStatic
        fun fromKeyword(keyword: String): Sides? = MAP[keyword]

        @JvmStatic
        fun fromKeywordOptional(keyword: String): java.util.Optional<Sides> =
            java.util.Optional.ofNullable(MAP[keyword])

        @JvmStatic
        fun fromKeywordOrThrow(keyword: String): Sides =
            MAP[keyword] ?: throw IllegalArgumentException("Unknown Sides keyword: $keyword")
    }

}