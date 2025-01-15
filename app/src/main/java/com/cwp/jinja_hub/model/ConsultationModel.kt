import android.os.Parcel
import android.os.Parcelable

data class ConsultationModel(
    val name: String,
    val specialty: String,
    val imageResId: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(specialty)
        parcel.writeInt(imageResId)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ConsultationModel> {
        override fun createFromParcel(parcel: Parcel): ConsultationModel {
            return ConsultationModel(parcel)
        }

        override fun newArray(size: Int): Array<ConsultationModel?> {
            return arrayOfNulls(size)
        }
    }
}
