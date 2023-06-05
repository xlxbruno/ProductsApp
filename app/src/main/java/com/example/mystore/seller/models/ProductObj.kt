package com.example.mystore.seller.models

data class ProductsObj (
    var productId: String,
    var productName: String,
    var contactPhone: String,
    var productImage: String,
    var productPrice: String
) {
    constructor() : this("","","","","")
}