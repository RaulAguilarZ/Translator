package com.example.rickandmorty.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.rickandmorty.ui.theme.dbtranslator.AppDB
import com.example.rickandmorty.R
import com.example.rickandmorty.ui.theme.dbtranslator.TranslationEntity
import com.example.rickandmorty.ui.theme.dbtranslator.TranslationViewModel
import com.example.rickandmorty.ui.theme.dbtranslator.TranslationViewModelFactory

/**
 * Composable function that displays the translations records screen.
 * It shows a list of translations with options to update their category or delete them.
 *
 * @param db The database instance used for accessing translation data.
 * @param navController The navigation controller for navigating between screens.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationsRecordsScreen(db: AppDB, navController: NavController) {
    // Get the DAO (Data Access Object) from the database
    val dao = db.translationDao

    // Create the ViewModel using a custom factory
    val viewModel: TranslationViewModel = viewModel(
        factory = TranslationViewModelFactory(dao)
    )

    // Observe the list of translations from the ViewModel
    val translations = viewModel.translations.collectAsState(initial = emptyList())

    // User Interface with top and bottom navigation bars
    Scaffold(
        topBar = {
            // Displays a TopAppBar with the title of the screen
            TopAppBar(
                title = {
                    Text(
                        text = "Translations Records", // Title of the top bar
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
            // BottomAppBar with a navigation button
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Button(
                    onClick = { navController.navigate("home") }, // Navigates to the home screen
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Home, // Home icon
                        contentDescription = "Go to Home",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    ) { padding ->
        // Main content displaying a list of translations
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Iterates over the translations list and displays each one as a card
            items(translations.value) { translation ->
                TranslationCard(translation, viewModel) // Displays each translation as a card
            }
        }
    }
}
/**
 * Composable function that displays a card for each translation.
 * It shows the translation text and allows the user to select a category or delete the translation.
 *
 * @param translation The translation entity to be displayed.
 * @param viewModel The ViewModel used to interact with the translation data.
 */
@Composable
fun TranslationCard(
    translation: TranslationEntity,
    viewModel: TranslationViewModel
) {
    // Creates a card displaying the details of each translation
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp), // Card padding
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = MaterialTheme.shapes.medium // Rounded corners
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Display translation data
            Text("English: ${translation.textEnglish}", style = MaterialTheme.typography.bodyLarge)
            Text("Spanish: ${translation.textSpanish}", style = MaterialTheme.typography.bodyLarge)
            Text("Language: ${translation.language}, Country: ${translation.country}", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(8.dp))

            // DropdownMenu for selecting a category
            var expanded by remember { mutableStateOf(false) }
            var selectedCategory by remember { mutableStateOf(translation.category) } // Initialize with the category from the database
            val categories = listOf("Restaurant", "School", "Work", "Church", "Friends")

            // Column for the DropdownMenu
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Button that opens the DropdownMenu
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
                        Text(text = selectedCategory) // Display the selected category
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // DropdownMenu options for categories
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category // Update selected category
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Action buttons (Save and Delete)
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Save button (updates the category in the database)
                Button(
                    onClick = {
                        // Call the ViewModel to update the category
                        viewModel.updateTranslationCategory(translation.copy(category = selectedCategory))
                    },
                    //colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.diskette),
                        contentDescription = "Save",
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Delete button (calls the ViewModel to delete the translation)
                Button(
                    onClick = {
                        viewModel.deleteTranslation(translation) // Delete the translation
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.delete),
                        contentDescription = "Delete",
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}