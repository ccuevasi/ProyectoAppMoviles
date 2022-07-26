package com.example.proyecto

class ModelComment {

    var id = ""
    var productId = ""
    var timestamp = ""
    var comment = ""
    var uid = ""

    constructor()

    constructor(id: String, productId: String, timestamp: String, comment: String, uid: String) {
        this.id = id
        this.productId = productId
        this.timestamp = timestamp
        this.comment = comment
        this.uid = uid
    }


}