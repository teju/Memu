package com.memu.etc

import com.iapps.libs.helpers.BaseKeys

class APIs : BaseKeys() {
    companion object {
        val BASE_URL = "http://memu.world/api/web/"
        val VEHICLE = "vehicle/"
        val USER = "user/"

        val getVehicleType: String
            get() = BASE_URL!!  + VEHICLE+ "vehicle-type"

        val postUserSignup: String
            get() = BASE_URL!!  + USER+ "user-signup"

        val postUserSignupWithOtp: String
            get() = BASE_URL!!  + USER+ "user-signup-otp"

        val postRequestOtp: String
            get() = BASE_URL!!  + USER+ "request-mobile-otp"

        val postVerifyOtp: String
            get() = BASE_URL!!  + USER+ "mobile-otp-verify"

        val postUploadVehicleCertPhotp: String
            get() = BASE_URL!!  + USER+ "upload-registration-certificate"

        val postUploadVehiclePhotp: String
            get() = BASE_URL!!  + USER+ "upload-vehicle-photo"

        val postUploadDlPhoto: String
            get() = BASE_URL!!  + USER+ "upload-driving-licence"

        val currentVehicleLocation: String
            get() = BASE_URL!!  + USER+ "current-vehicle-location"

        val getUpdateFcmID: String
            get() = BASE_URL!!  + USER+ "update-fcm-id"
    }
}