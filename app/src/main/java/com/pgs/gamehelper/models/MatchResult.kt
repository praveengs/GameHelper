package com.pgs.gamehelper.models

import android.os.Parcelable

data class MatchResult(
    val teamAScore: Int = 0,
    val teamBScore: Int = 0,
    val shuttlesUsed: Int = 0
)
