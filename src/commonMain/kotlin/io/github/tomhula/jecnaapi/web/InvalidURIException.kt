package io.github.tomhula.jecnaapi.web

/**
 * Thrown, when an invalid [java.net.URI] is used.
 * The validity of the [java.net.URI] depends on context.
 */
class InvalidURIException : RuntimeException
{
    constructor(uri: String) : super("Invalid URI '$uri'")

    constructor() : super("Invalid URI.")
}
