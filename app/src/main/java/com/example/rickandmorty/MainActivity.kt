package com.example.rickandmorty

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.rickandmorty.ui.theme.RickAndMortyTheme

import com.example.rickandmorty.model.RM.CharacterManager
import com.example.rickandmorty.model.RM.Result

import coil.compose.rememberAsyncImagePainter

// for icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.example.rickandmorty.ui.theme.db.AppDatabase
import com.example.rickandmorty.ui.theme.db.CharacterEntity
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.flow.internal.NoOpContinuation.context
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initializes the database
        db = AppDatabase.getInstance(applicationContext)

        setContent {
            RickAndMortyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface (
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ){
                        CharacterList()
                    }

                }
            }
        }
    }
    @OptIn(DelicateCoroutinesApi::class)
    fun insertCharacter(character: Result) {
        GlobalScope.launch {
            db.characterDao().insertCharacter(character.toCharacterEntity())
            Log.d("INSERTED:", "${character.name} into database")
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun deleteCharacter(character: Result) {
        GlobalScope.launch {
            // Check if character.id is not null
            character.id?.let { id ->
                db.characterDao().deleteCharacter(id)
                Log.d("DELETED:", "${character.name} from database")
            }
        }
    }

}

// Extension to convert Result to CharacterEntity
fun Result.toCharacterEntity(): CharacterEntity {
    return CharacterEntity(
        id = this.id ?: -1,
        name = this.name ?: "Unknown",
        species = this.species ?: "Unknown",
        gender = this.gender ?: "Unknown",
        origin = this.origin?.name ?: "Unknown",
        image = this.image ?: ""
    )
}

@Composable
fun CharacterList(){
    val characterManager = remember { CharacterManager() }
    //val characters by characterManager.characterResponse
    val characters = characterManager.characterResponse.value

    // Display the list of characters
    LazyColumn (
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        items(characters.size) {index ->
            CharacterItem(character  = characters [index])
        }
    }
}

@Composable
fun CharacterItem(character: Result) {
    val context = LocalContext.current
    val activity = context as MainActivity
    Row (
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .background(Color.LightGray)
            .padding(5.dp),

    ){
        // Display the character's image
        Image(
            painter = rememberAsyncImagePainter(character.image),
            contentDescription = null,
            modifier = Modifier
                .size(170.dp)
                .padding(3.dp),
            contentScale = ContentScale.Crop

        )
        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Display the character's attributes
            Text(
                text = "ID: ${character.id}",
                fontSize = 12.sp
            )
            Text(
                text = "Name: ${character.name}",
                fontSize = 12.sp
            )
            Text(
                text = "Species: ${character.species}",
                fontSize = 12.sp
            )
            Text(
                text = "Gender: ${character.gender}",
                fontSize = 12.sp
            )
            Text(
                text = "Origin: ${character.origin?.name ?: "Unknown"}",
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.padding(5.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End, // Align to the right
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Add button
                IconButton(onClick = {
                    // inserts a record into the database
                    activity.insertCharacter(character)
                }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = Color.DarkGray
                    )
                }

                // Space between icons
                Spacer(modifier = Modifier.width(80.dp))

                // Delete button
                IconButton(onClick = {
                    // Deletes a record in the database
                    activity.deleteCharacter(character)
                }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.DarkGray
                    )
                }
            }
        }
    }
}


