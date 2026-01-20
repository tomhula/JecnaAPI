package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.data.room.Room

interface HtmlRoomParser
{
    fun parse(html: String): Room
}
