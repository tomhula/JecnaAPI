package io.github.tomhula.jecnaapi.parser

/**
 * Is thrown when there's some error during parsing.
 * Could be a wrong or incomplete source.
 */
open class ParseException : RuntimeException
{
    constructor()

    constructor(message: String) : super(message)

    constructor(message: String, cause: Throwable) : super(message, cause)

    constructor(cause: Throwable) : super(cause)
}
