package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.*

// Модель данных
data class TodoItem(
    var id: UUID = UUID.randomUUID(),
    var title: String,
    var description: String = "",
    var isCompleted: Boolean = false,
    var category: String = "General",
    var color: Color = Color.LightGray,
    val createdDate: Date = Date()
)

// ViewModel
class TodoViewModel : ViewModel() {
    private val _todoItems = mutableStateListOf<TodoItem>()
    val todoItems: List<TodoItem> get() = _todoItems

    var currentEditingItem: TodoItem? by mutableStateOf(null)
    var showDialog by mutableStateOf(false)
    var searchQuery by mutableStateOf("")

    fun addItem(item: TodoItem) {
        _todoItems.add(0, item)
    }

    fun deleteItem(item: TodoItem) {
        _todoItems.remove(item)
    }

    fun toggleItemCompletion(item: TodoItem) {
        val index = _todoItems.indexOfFirst { it.id == item.id }
        _todoItems[index] = item.copy(isCompleted = !item.isCompleted)
    }

    fun updateItem(updatedItem: TodoItem) {
        val index = _todoItems.indexOfFirst { it.id == updatedItem.id }
        _todoItems[index] = updatedItem
    }

    fun filteredItems(): List<TodoItem> {
        return _todoItems.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true)
        }.sortedByDescending { it.createdDate }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TodoScreen(viewModel: TodoViewModel = viewModel()) {
    val categories = listOf("General", "Work", "Personal", "Shopping")
    val colors = listOf(
        Color(0xFFF8BBD0),
        Color(0xFFB3E5FC),
        Color(0xFFC8E6C9),
        Color(0xFFFFF9C4)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Todo List",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Add Task")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            SearchBar(
                query = viewModel.searchQuery,
                onQueryChange = { viewModel.searchQuery = it },
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.filteredItems(), key = { it.id }) { item ->
                    TodoItemCard(
                        item = item,
                        onEdit = { viewModel.currentEditingItem = item },
                        onDelete = { viewModel.deleteItem(it) },
                        onToggle = { viewModel.toggleItemCompletion(it) },
                        modifier = Modifier.animateItemPlacement()
                    )
                }
            }
        }

        if (viewModel.showDialog) {
            TodoDialog(
                item = viewModel.currentEditingItem,
                categories = categories,
                colors = colors,
                onDismiss = {
                    viewModel.showDialog = false
                    viewModel.currentEditingItem = null
                },
                onConfirm = { newItem ->
                    if (viewModel.currentEditingItem == null) {
                        viewModel.addItem(newItem)
                    } else {
                        viewModel.updateItem(newItem)
                    }
                    viewModel.showDialog = false
                    viewModel.currentEditingItem = null
                }
            )
        }
    }
}

@Composable
fun TodoItemCard(
    item: TodoItem,
    onEdit: () -> Unit,
    onDelete: (TodoItem) -> Unit,
    onToggle: (TodoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = item.color.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isCompleted,
                onCheckedChange = { onToggle(item) },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = item.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.fillMaxWidth()
                )

                if (item.description.isNotEmpty()) {
                    Text(
                        text = item.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.category,
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .background(
                                color = item.color,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = { onDelete(item) }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoDialog(
    item: TodoItem?,
    categories: List<String>,
    colors: List<Color>,
    onDismiss: () -> Unit,
    onConfirm: (TodoItem) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var selectedColor by remember { mutableStateOf(colors[0]) }

    LaunchedEffect(item) {
        title = item?.title ?: ""
        description = item?.description ?: ""
        selectedCategory = item?.category ?: categories[0]
        selectedColor = item?.color ?: colors[0]
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "New Task" else "Edit Task") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )

                Text(
                    "Category:",
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEachIndexed { index, category ->
                        FilterChip(
                            selected = category == selectedCategory,
                            onClick = { selectedCategory = category },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors[index].copy(alpha = 0.2f)
                            )
                        )
                    }
                }

                Text(
                    "Color:",
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    color = color,
                                    shape = CircleShape
                                )
                                .selectable(
                                    selected = color == selectedColor,
                                    onClick = { selectedColor = color }
                                )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newItem = TodoItem(
                        title = title,
                        description = description,
                        category = selectedCategory,
                        color = selectedColor
                    ).apply {
                        if (item != null) id = item.id
                    }
                    onConfirm(newItem)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        placeholder = { Text("Search tasks...") },
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent
        ),
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewTodoScreen() {
    MaterialTheme {
        TodoScreen()
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                TodoScreen()
            }
        }
    }
}