package com.example.mystore

sealed class SideNavItems(var route: String, var icon: Int, var title: String ){
    object ViewInventory : SideNavItems("view_inventory", R.drawable.viewstore, "View Inventory")
    object SellerDashboard : SideNavItems("seller_dasboard", R.drawable.viewstore, "SellerDashboard")
    object Logout : SideNavItems("logout", androidx.recyclerview.R.drawable.notification_icon_background, "Logout")
}