package io.github.tomhula.jecnaapi.parser

class HtmlElementNotFoundException private constructor(message: String) : ParseException(message)
{
    companion object
    {
        fun bySelector(selector: String) = HtmlElementNotFoundException("Element with selector '$selector' not found.")

        fun bySelector(parentSelector: String, selector: String) = HtmlElementNotFoundException("Element with selector '$selector' not found in element with selector '$parentSelector'.")

        fun byName(name: String) = HtmlElementNotFoundException("Element '$name' not found.")

        fun byName(parentSelector: String, name: String) = HtmlElementNotFoundException("Element '$name' not found in element with selector '$parentSelector'.")
    }
}
