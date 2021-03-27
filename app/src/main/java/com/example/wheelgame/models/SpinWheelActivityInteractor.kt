package com.example.wheelgame.models


import com.example.wheelgame.contract.ContractInterface.*
import com.example.wheelgame.services.WheelEntryService
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class SpinWheelActivityInteractor: Interactor {

    val scope = MainScope()

    override fun requestGetDataAPI(onAPICompleteListener: OnAPICompleteListener) {
        val service = Retrofit.Builder()
            .baseUrl("http://mockbin.org/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(WheelEntryService::class.java)
       scope.launch {
           try {
               val data = service.getData()
               onAPICompleteListener.onResultSuccess(data)
           }
           catch(e: Exception){
               //could customize error message based on exception type here
               val error_message = "Unable to load spinwheel data"
               onAPICompleteListener.onResultFail(error_message)
           }
        }
    }

}