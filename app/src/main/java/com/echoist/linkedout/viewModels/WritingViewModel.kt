package com.echoist.linkedout.viewModels

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.echoist.linkedout.api.EssayApi
import com.echoist.linkedout.page.Token
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Inject

@HiltViewModel
class WritingViewModel @Inject constructor(
) : ViewModel() {
    private val socialLoginViewModel = SocialLoginViewModel()

    var accessToken by mutableStateOf("")

    init { // 아마 이 뷰모델이 관계없는 뷰모델이라 안되는거. 방법은 내일 생각해보자
        accessToken = socialLoginViewModel.accessToken
    }
    val maxLength = 4000
    val minLength = 10

    var focusState = mutableStateOf(false)
    var titleFocusState = mutableStateOf(false)

    var title = mutableStateOf(TextFieldValue(""))
    var content = mutableStateOf(TextFieldValue(""))
    var date = mutableStateOf("")
    var ringTouchedTime = mutableStateOf(5)
    var isCanCelClicked = mutableStateOf(false)
    var isDeleteClicked = mutableStateOf(false)

    var latitute by mutableStateOf("")
    var longitude by mutableStateOf("")

    var isHashTagClicked by mutableStateOf(false)
    var hashTagText by mutableStateOf("")
    var hashTagList by mutableStateOf(mutableStateListOf<String>())

    var locationText by mutableStateOf("")
    var locationList by mutableStateOf(mutableStateListOf<String>())
    var isLocationClicked by mutableStateOf(false)

    var imageBitmap: MutableState<Bitmap?> = mutableStateOf(null)
    //todo image bitmap 레트로핏으로 보내는방법


    var isTextFeatOpened = mutableStateOf(false)
    fun initialize() {
        titleFocusState.value = false
        focusState.value = false
        title.value = TextFieldValue("")
        content.value = TextFieldValue("")
        date.value = ""
        ringTouchedTime.value = 5
        isCanCelClicked.value = false
        isDeleteClicked.value = false
        latitute = ""
        longitude = ""
        isHashTagClicked = false
        hashTagText = ""
        hashTagList = mutableStateListOf()
        locationText = ""
        locationList = mutableStateListOf()
        isLocationClicked = false
        imageBitmap = mutableStateOf(null)
        isTextFeatOpened.value = false
    }


    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()


    private val api = Retrofit
        .Builder()
        .baseUrl("https://www.linkedoutapp.com/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(EssayApi::class.java)


    //에세이 작성 후 서버에 post
    //api 통신 성공했을시에만 화면 이동
    fun writeEssay(
        navController: NavController, //저장할래요는 기본 둘다 false
        published: Boolean = false, //나눠볼래요
        linkedOut: Boolean = false //놓아줄래요
    ) {
        viewModelScope.launch {
            try {
                val essayData = EssayApi.EssayData(
                    title.value.text,
                    content.value.text,
                    linkedOutGauge = ringTouchedTime.value,
                    published = published,
                    linkedOut = linkedOut
                )
                val response = api.writeEssay(
                    accessToken,
                    essayData = essayData
                )
                if (response.isSuccessful) {
                    accessToken = (response.headers()["authorization"].toString())
                    Token.accessToken = accessToken
                    Log.e("writeEssayApiSuccess 성공!", "${response.headers()}")
                    Log.e("writeEssayApiSuccess", response.body()?.data?.title!!)

                    Log.e("writeEssayApiSuccess", "${response.code()}")
                    navController.popBackStack("OnBoarding", false) //onboarding까지 전부 삭제.
                    navController.navigate("HOME/$accessToken")
                    initialize()
                } else {
                    Log.e("writeEssayApiFailed token", "Failed to write essay: $accessToken")
                    Log.e("writeEssayApiFailed1", "Failed to write essay: ${response.code()}")
                }

            } catch (e: Exception) {
                // api 요청 실패
                e.printStackTrace()

                Log.e("writeEssayApiFailed 아예", "Failed to write essay: ${e.printStackTrace()}")
                Log.e("writeEssayApiFailed 아예", "Failed to write essay: ${e.message}")
            }
        }
    }

    //에세이 수정 후 서버에 put

    fun modifyEssay(navController: NavController) {
        viewModelScope.launch {
            try {

                val response = api.modifyEssay(/*todo 토큰값. 매번변경*/ accessToken,
                    title.value.text,
                    content.value.text
                )

                navController.navigate("HOME") {
                    popUpTo("HOME") {
                        inclusive = false
                    }
                }
                if (response.code() == 202) {
                    //블랙리스트 코드 이동
                }

            } catch (e: Exception) {
                // api 요청 실패
                Log.e("writeEssayApiFailed", "Failed to write essay: ${e.message}")
            }
        }
    }
}