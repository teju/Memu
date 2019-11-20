package com.iapps.gon.etc.callback


interface PermissionListener {

    fun onCheckPermission(permission: String, isGranted: Boolean)

    fun onPermissionAlreadyGranted()

    fun onUserNotGrantedThePermission()

}