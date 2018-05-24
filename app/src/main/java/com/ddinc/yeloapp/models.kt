package com.ddinc.yeloapp

class Model() {

    lateinit var id: String
    lateinit var name: String
    lateinit var mail: String
    lateinit var picUrl: String
    lateinit var address: String

    constructor(id: String,
                name: String,
                mail: String,
                picUrl: String,
                address: String) : this() {
        this.id = id
        this.name = name
        this.mail = mail
        this.picUrl = picUrl
        this.address = address
    }
}