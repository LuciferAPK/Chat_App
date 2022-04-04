package com.example.chatapplication.user

class User {
    var name: String? = null
    var email: String? = null
    var uid: String? = null
    var imgUrl: String? = null

    constructor(){}

    constructor(name:String?, email: String?, uid: String?, imgUrl: String?) {
        this.name = name
        this.email = email
        this.uid = uid
        this.imgUrl = imgUrl
    }
}