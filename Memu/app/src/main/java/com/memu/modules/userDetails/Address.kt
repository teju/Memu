package com.memu.modules.userDetails

data class Address(
    val home: Home = Home(),
    val office: Office = Office()
)