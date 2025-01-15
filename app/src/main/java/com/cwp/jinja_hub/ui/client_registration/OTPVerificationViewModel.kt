import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cwp.jinja_hub.repository.OTPVerificationRepository

class OTPVerificationViewModel(private val otpVerificationRepository: OTPVerificationRepository) : ViewModel() {

    private val _otpCode = MutableLiveData<String>()
    val otpCode: LiveData<String> = _otpCode

    private val _isCodeConfirmed = MutableLiveData<Boolean>()
    val isCodeConfirmed: LiveData<Boolean> = _isCodeConfirmed

    private val _verificationId = MutableLiveData<String>()

    fun setVerificationId(verificationId: String) {
        _verificationId.value = verificationId
    }

    fun setOtpCode(index: Int, value: String) {
        val currentCode = _otpCode.value.orEmpty().toCharArray().toMutableList()
        if (index < currentCode.size) {
            currentCode[index] = value.firstOrNull() ?: ' '
        } else if (index == currentCode.size) {
            currentCode.add(value.firstOrNull() ?: ' ')
        }
        _otpCode.value = currentCode.joinToString("")
    }

    fun confirmCodeComplete(): Boolean {
        val code = _otpCode.value.orEmpty()
        if (code.length == 4) {
            val verificationId = _verificationId.value.orEmpty()
            otpVerificationRepository.verifyOtp(verificationId, code) { isSuccess ->
                _isCodeConfirmed.postValue(isSuccess)
            }
            return true
        }
        _isCodeConfirmed.postValue(false)
        return false
    }
}
