package com.memu.etc

import com.iapps.libs.helpers.BaseKeys

class APIs : BaseKeys() {
    companion object {
        val BASE_URL = "http://15.207.227.137/api/web/"
        val VEHICLE = "vehicle/"
        val BOOKING = "booking/"
        val USER = "user/"
        val PROFILE = "profile/"
        val PAYMENT = "payment/"
        val MAP_FEEDS = "map-feeds/"

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

        val postUploadProfilePhoto: String
            get() = BASE_URL!!  + PROFILE+ "update-profile-image"

        val currentVehicleLocation: String
            get() = BASE_URL!!  + VEHICLE+ "current-vehicle-location"

        val getUpdateFcmID: String
            get() = BASE_URL!!  + USER+ "update-fcm-id"

        val getPoolerVehicle: String
            get() = BASE_URL!!  + VEHICLE+ "get-pooler-vehicles"

        val postOffersRides: String
            get() = BASE_URL!!  + BOOKING+ "offer-find-ride"

        val postRideTakers: String
            get() = BASE_URL!!  + BOOKING+ "ride-taker-pooler-suggession"

         val postLogin: String
            get() = BASE_URL!!  + USER+ "login"

        val postRequestRide: String
            get() = BASE_URL!!  + BOOKING+ "request-push-notification-to-ridetaker-pooler"

        val postOtp: String
            get() = BASE_URL!!  + USER+ "otp"

        val feedsData: String
            get() = BASE_URL!!  + "map-feeds"

        val getteedsData: String
            get() = BASE_URL!!  + MAP_FEEDS + "data"

        val addfeedsData: String
            get() = BASE_URL!!  + MAP_FEEDS + "add"

        val postAcceptRejectReq : String
            get() = BASE_URL!!  + BOOKING+ "ride-approve-reject"

        val postCompleteRides : String
            get() = BASE_URL!!  + BOOKING+ "my-completed-rides"

        val postRecuringRides : String
            get() = BASE_URL!!  + BOOKING+ "my-recuring-rides"

        val postScheduledRides : String
            get() = BASE_URL!!  + BOOKING+ "my-rides"

        val postEditRecuring : String
            get() = BASE_URL!!  + BOOKING+ "edit-recuring-rides"

        val postStartTrip : String
            get() = BASE_URL!!  + BOOKING+ "start-trip"

        val postCustomerStartTrip : String
            get() = BASE_URL!!  + BOOKING+ "customer-checkin"

        val postEndTrip : String
            get() = BASE_URL!!  + BOOKING+ "end-trip"

        val postCustomerEndTrip : String
            get() = BASE_URL!!  + BOOKING+ "customer-end-trip"

        val customerendtripid : String
            get() = BASE_URL!!  + BOOKING+ "customer-end-trip-id"

        val getTripUsers : String
            get() = BASE_URL!!  + BOOKING+ "get-trip-users"

        val postVehicleList : String
            get() = BASE_URL!!  + USER+ "pooler-vehicle-list"

        val getUserActivities : String
            get() = BASE_URL!!  + PROFILE+ "activities"

        val uploadActivityPhoto : String
            get() = BASE_URL!!  + PROFILE+ "activity-image"

        val userMainData : String
            get() = BASE_URL!!  + PROFILE+ "user-main-data"

        val searchUser : String
            get() = BASE_URL!!  + PROFILE+ "search-user"

        val friendList : String
            get() = BASE_URL!!  + PROFILE+ "friend-list"

        val friendRequest : String
            get() = BASE_URL!!  + PROFILE+ "friend-request"

        val pendingFriendRequest : String
            get() = BASE_URL!!  + PROFILE+ "friend-pending-list"

        val acceptFriendRequest : String
            get() = BASE_URL!!  + PROFILE+ "accept-friend-request"

        val getPaytmCheckSum : String
            get() = BASE_URL!!  + PAYMENT+ "check-sum"

        val getWalletBalance: String
            get() = BASE_URL!!  + PAYMENT+ "wallet-balance"

        val postPay: String
            get() = BASE_URL!!  + PAYMENT+ "pay"


        val gettopearners : String
            get() = BASE_URL!!  + PROFILE+ "top-earners"

        val shortestpathstartstop : String
            get() = BASE_URL!!  + PROFILE+ "shortest-path-start-stop"

        val usermapfeedlike : String
            get() = BASE_URL!!  + PROFILE+ "user-map-feed-like"

        val userDetails : String
            get() = BASE_URL!!  + PROFILE+ "user-details"

        val userupdate : String
            get() = BASE_URL!!  + PROFILE+ "user-update"

        val tripsummary : String
            get() = BASE_URL!!  + BOOKING+ "trip-summary"
    }
}