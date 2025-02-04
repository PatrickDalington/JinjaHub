import android.os.Parcel
import android.os.Parcelable

data class ConsultationModel(
    val userId: String,
    val fullName: String,
    val profession: String,
    val profileImage: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""


    )



    override fun describeContents(): Int = 0
    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeString(userId)
        p0.writeString(fullName)
        p0.writeString(profession)
        p0.writeString(profileImage)
    }

    companion object CREATOR : Parcelable.Creator<ConsultationModel> {
        override fun createFromParcel(parcel: Parcel): ConsultationModel {
            return ConsultationModel(parcel)
        }

        override fun newArray(size: Int): Array<ConsultationModel?> {
            return arrayOfNulls(size)
        }
    }
}
