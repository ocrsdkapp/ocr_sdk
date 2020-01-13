package com.smartcardreader

import java.io.Serializable

data class CardInfo (
    var images : List<String> = emptyList(),
    var emails : List<String> = emptyList(),
    var email: String="",
    var phoneNumbers : List<String> = emptyList(),
    var phoneNumber : String="",
    var websites: List<String> = emptyList(),
    var website: String="",
    var firstName: String="",
    var lastName: String="",
    var designation: String="",
    var address: String="",
    var rawString : String = "",
    var company : String = "",
    var miscellaneous  : String = ""
):Serializable