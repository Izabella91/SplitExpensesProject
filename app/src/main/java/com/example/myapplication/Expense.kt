package com.example.myapplication

import android.os.Parcel
import android.os.Parcelable

data class Expense(
    val id: String? = null,
    val description: String? = null,
    val amount: Double? = null,
    val category: String? = null,
    val paidBy: String? = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readDouble().takeIf { it != 0.0 },
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(description)
        parcel.writeDouble(amount ?: 0.0)
        parcel.writeString(category)
        parcel.writeString(paidBy)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Expense> {
        override fun createFromParcel(parcel: Parcel): Expense {
            return Expense(parcel)
        }

        override fun newArray(size: Int): Array<Expense?> {
            return arrayOfNulls(size)
        }
    }
}