package com.example.rickandmorty

import android.Manifest
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rickandmorty.model.TranslationApi
import com.example.rickandmorty.model.TextRequestBody
import com.example.rickandmorty.screens.TranslationsRecordsScreen
import com.example.rickandmorty.ui.theme.dbtranslator.AppDB
import com.example.rickandmorty.ui.theme.dbtranslator.TranslationEntity
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * MainActivity class that extends ComponentActivity.
 * This class manages the app's lifecycle and UI using Jetpack Compose.
 * It includes features such as requesting runtime permissions, displaying a splash screen,
 * and navigating to the main functionality of the app.
 */
class MainActivityFP : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register an activity result launcher to request audio recording permissions
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (!isGranted) {
                Toast.makeText(this, "Microphone permission is required", Toast.LENGTH_LONG).show()
            }
        }

        // Launch the permission request for RECORD_AUDIO
        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)

        // Set the content of the activity using Jetpack Compose
        setContent {
            MaterialTheme {
                // Set the status bar color to black
                window.statusBarColor = getColor(R.color.black)

                // Define the main surface of the app with full screen size
                Surface(modifier = Modifier.fillMaxSize()) {
                    // State variable to track whether to show the splash screen
                    var showSplash by remember { mutableStateOf(true) }

                    // Initialize NavController
                    val navController = rememberNavController()

                    // Show the splash screen if `showSplash` is true, otherwise show the main app
                    if (showSplash) {
                        SplashScreen(onSplashFinished = { showSplash = false })
                    } else {
                        // Navigation setup
                        NavHost(navController = navController, startDestination = "home") {
                            // Home screen -> TranslationAndSpeechApp
                            composable("home") {
                                TranslationAndSpeechApp(navController) // Tu pantalla principal
                            }

                            // Translations screen
                            composable("translations_records") {
                                val db = AppDB.getInstance(applicationContext)
                                TranslationsRecordsScreen(db, navController) // Pantalla de traducciones
                            }

                        }
                    }
                }
            }
        }
    }
}



/**
 * Composable function that sets the background of the app with a full-screen image.
 */
//@Composable
//fun BakGround() {
//    Image(
//        painter = painterResource(id = R.drawable.background),
//        contentDescription = null, // No content description as this is a decorative image
//        contentScale = ContentScale.Crop, // Crop image to fit the screen
//        modifier = Modifier.fillMaxSize() // Fill the entire screen size
//    )
//}

/**
 * Composable function for the splash screen.
 * It plays a video using a VideoView and navigates to the main app once the video finishes.
 *
 * @param onSplashFinished Callback triggered when the splash video finishes.
 */
@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val context = LocalContext.current
    val videoUri = Uri.parse("android.resource://${context.packageName}/raw/introv7")

    AndroidView(
        factory = { ctx ->
            VideoView(ctx).apply {
                setVideoURI(videoUri) // Set the URI of the splash video
                setOnCompletionListener { onSplashFinished() } // Notify when the video finishes
                start() // Start video playback
            }
        },
        modifier = Modifier.fillMaxSize() // Use full screen for the video
    )
}

/**
 * Composable function representing the main app.
 * It includes functionality for speech-to-text, text-to-speech, translation,
 * and interacting with a local database for saving translations.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationAndSpeechApp(navController: NavHostController) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // State variables for managing input, results, and errors
    var query by remember { mutableStateOf(TextFieldValue("")) }
    var capturedText by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }
    var recognizedText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Select category") }

    // Variable to hold the original input text (before translation)
    var originalText by remember { mutableStateOf("") }

    // Text-to-Speech initialization
    val tts = remember { TextToSpeech(context, null) }
    DisposableEffect(context) {
        tts.language = Locale("en", "MX") // Set TTS language to Spanish (Mexico)
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    // Speech-to-Text initialization
    val speechRecognizer = remember {
        SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {
                    recognizedText = "Error recognizing speech: $error"
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    recognizedText = matches?.firstOrNull() ?: "No audio recognized"
                    query = TextFieldValue(recognizedText)
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    // Intent for speech recognition
    val speechIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
    }

    // Configure audio settings
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 50, 0)

    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(
                    text = "KARAKU",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            colors = TopAppBarDefaults.smallTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )
    },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Save button
                Button(onClick = {
                    coroutineScope.launch {
                        if (resultText.isNotBlank() && originalText.isNotBlank()) {
                            val translation = TranslationEntity(
                                textEnglish = resultText, // Use the original input text
                                textSpanish = originalText,  // Use the translated text
                                category = selectedCategory
                            )
                            val db = AppDB.getInstance(context)
                            db.translationDao.insertTranslation(translation)
                            Toast.makeText(context, "Translation saved", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Both fields must be filled", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.diskette),
                        contentDescription = "Save",
                        modifier = Modifier.size(40.dp)
                    )
                }

                Button(onClick = { navController.navigate("translations_records") }) {
                    Icon(
                        painter = painterResource(id = R.drawable.listing),
                        contentDescription = "List of records",
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Clear button
                Button(onClick = {
                    query = TextFieldValue("")
                    resultText = ""
                    originalText = "" // Reset the original text when clearing
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.clean),
                        contentDescription = "Clean",
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            //



            // Text input field
            item {
                Spacer(modifier = Modifier.height(32.dp))
                OutlinedTextField(
                    value = query.text,
                    onValueChange = {
                        query = TextFieldValue(it)
                        originalText = it // Keep track of the original input text
                    },
                    label = { Text("Text to translate or speak") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Button to swap texts
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    capturedText = query.text
                    val temp = query.text
                    query = TextFieldValue(resultText)
                    resultText = temp
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.swap),
                        contentDescription = "swap",
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            // Read-only translation field
            item {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = resultText,
                    onValueChange = {},
                    label = { Text("Translation") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Buttons for text-to-speech, voice recognition, and translation
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Text-to-Speech button
                        Button(onClick = {
                            if (query.text.isNotBlank()) {
                                tts.speak(query.text, TextToSpeech.QUEUE_FLUSH, null, null)
                            } else {
                                Toast.makeText(
                                    context,
                                    "Write something to convert to speech",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.volume),
                                contentDescription = "Volume",
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        // Translation button
                        Button(onClick = {
                            coroutineScope.launch {
                                try {
                                    val requestBody = listOf(TextRequestBody(query.text))
                                    val response = TranslationApi.service.translateText(text = requestBody)
                                    if (response.isSuccessful) {
                                        response.body()?.let { translationResponse ->
                                            resultText = translationResponse.joinToString {
                                                it.translations.firstOrNull()?.text
                                                    ?: "No translation available"
                                            }
                                        } ?: run {
                                            resultText = "Empty response from server"
                                        }
                                    } else {
                                        resultText = "Error ${response.code()}: ${response.errorBody()?.string()}"
                                    }
                                } catch (e: Exception) {
                                    resultText = "Error: ${e.localizedMessage}"
                                }
                            }
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.trans),
                                contentDescription = "Translate",
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }

                var expanded by remember { mutableStateOf(false) }
                val categories = listOf("Restaurant", "School", "Work", "Church", "Friends")

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Botón que abre el DropdownMenu
                    OutlinedButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = selectedCategory)
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // DropdownMenu Dropdown Menu Options
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}