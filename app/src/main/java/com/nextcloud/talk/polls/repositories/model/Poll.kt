package com.nextcloud.talk.polls.repositories.model

import android.os.Parcelable
import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonObject
data class Poll(
    @JsonField(name = ["id"])
    var id: Int = 0,

    @JsonField(name = ["question"])
    var question: String? = null,

    @JsonField(name = ["options"])
    var options: ArrayList<String>? = null,

    @JsonField(name = ["votes"])
    var votes: ArrayList<Int>? = null,

    @JsonField(name = ["actorType"])
    var actorType: String? = null,

    @JsonField(name = ["actorId"])
    var actorId: String? = null,

    @JsonField(name = ["actorDisplayName"])
    var actorDisplayName: String? = null,

    @JsonField(name = ["status"])
    var status: Int = 0,

    @JsonField(name = ["resultMode"])
    var resultMode: Int = 0,

    @JsonField(name = ["maxVotes"])
    var maxVotes: Int = 0,

    @JsonField(name = ["votedSelf"])
    var votedSelf: ArrayList<Int>? = null,

    ) : Parcelable {
    // This constructor is added to work with the 'com.bluelinelabs.logansquare.annotation.JsonObject'
    constructor() : this(0, null, null, null, null, null, null, 0, 0, 0, null)
}
