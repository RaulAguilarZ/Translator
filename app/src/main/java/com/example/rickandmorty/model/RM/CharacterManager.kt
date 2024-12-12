package com.example.rickandmorty.model.RM

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
//import org.w3c.dom.CharacterData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CharacterManager{
    private var _characterResponse = mutableStateOf<List<Result>>(emptyList())

    val characterResponse : MutableState<List<Result>> get() = _characterResponse

    init {
        getCharactersRyM()
    }

    private fun getCharactersRyM(){

        val service = Api.retrofitService.getCharacters()

        service.enqueue(object : Callback<CharacterResponse>{
            override fun onResponse(
                call: Call<CharacterResponse>,
                response: Response<CharacterResponse>
            ) {
                if (response.isSuccessful){
                    Log.i("Data", "Data Loaded")
                    _characterResponse.value = response.body()?.results?: emptyList()
                    Log.i("DataStream", _characterResponse.toString())
                }
            }
            override fun onFailure(call: Call<CharacterResponse>, t: Throwable) {
                Log.d("error", "${t.message}")
            }
        })
    }
}