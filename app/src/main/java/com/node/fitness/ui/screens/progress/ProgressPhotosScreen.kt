package com.node.fitness.ui.screens.progress

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.node.fitness.data.database.entity.ProgressPhoto
import com.node.fitness.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProgressPhotosRoute(
    viewModel: ProgressPhotosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Camera & Gallery Logic
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var showAddOptions by remember { mutableStateOf(false) }

    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempPhotoUri != null) {
            viewModel.copyAndAddPhoto(tempPhotoUri!!, context)
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            viewModel.copyAndAddPhoto(it, context)
        }
    }

    fun launchCamera() {
        val file = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        tempPhotoUri = uri
        takePictureLauncher.launch(uri)
        showAddOptions = false
    }

    fun launchGallery() {
        pickImageLauncher.launch("image/*")
        showAddOptions = false
    }

    ProgressPhotosScreen(
        uiState = uiState,
        onAddPhotoClick = { showAddOptions = true },
        onPhotoClick = { photo ->
            if (uiState.isCompareModeActive) {
                viewModel.selectComparePhoto(photo)
            } else {
                viewModel.viewPhoto(photo)
            }
        },
        onDeletePhoto = { viewModel.deletePhoto(it) },
        onToggleCompareMode = { viewModel.toggleCompareMode() },
        onClearCompare = { viewModel.clearCompareSelection() },
        onClosePhotoView = { viewModel.closePhotoView() }
    )

    if (showAddOptions) {
        AlertDialog(
            onDismissRequest = { showAddOptions = false },
            title = { Text("Add Progress Photo", color = TextPrimary) },
            text = { Text("Choose an option:", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { launchCamera() }) {
                    Text("Take Photo", color = AccentOrange)
                }
            },
            dismissButton = {
                TextButton(onClick = { launchGallery() }) {
                    Text("Choose from Gallery", color = AccentOrange)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

@Composable
fun ProgressPhotosScreen(
    uiState: ProgressPhotosUiState,
    onAddPhotoClick: () -> Unit,
    onPhotoClick: (ProgressPhoto) -> Unit,
    onDeletePhoto: (ProgressPhoto) -> Unit,
    onToggleCompareMode: () -> Unit,
    onClearCompare: () -> Unit,
    onClosePhotoView: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.photos.isEmpty()) {
            EmptyState("No photos yet", "Take a photo to track your progress")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.photos) { photo ->
                    PhotoGridItem(
                        photo = photo,
                        isCompareMode = uiState.isCompareModeActive,
                        isSelected = uiState.comparePhoto1?.id == photo.id || uiState.comparePhoto2?.id == photo.id,
                        onClick = { onPhotoClick(photo) }
                    )
                }
            }
        }

        // Compare Mode Overlay
        AnimatedVisibility(
            visible = uiState.isCompareModeActive,
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            CompareOverlay(
                photo1 = uiState.comparePhoto1,
                photo2 = uiState.comparePhoto2,
                onClear = onClearCompare,
                onClose = onToggleCompareMode
            )
        }

        // Buttons
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FloatingActionButton(
                onClick = onToggleCompareMode,
                containerColor = if (uiState.isCompareModeActive) AccentOrange else SurfaceVariantDark,
                contentColor = if (uiState.isCompareModeActive) Color.Black else AccentOrange
            ) {
                Icon(Icons.Filled.CompareArrows, "Compare")
            }

            if (!uiState.isCompareModeActive) {
                FloatingActionButton(
                    onClick = onAddPhotoClick,
                    containerColor = AccentOrange,
                    contentColor = Color.Black
                ) {
                    Icon(Icons.Filled.Add, "Add Photo")
                }
            }
        }

        // Full-screen photo view
        uiState.selectedPhotoForView?.let { photo ->
            PhotoDetailDialog(
                photo = photo,
                onDismiss = onClosePhotoView,
                onDelete = { 
                    onDeletePhoto(photo)
                    onClosePhotoView()
                }
            )
        }
    }
}

@Composable
fun PhotoGridItem(
    photo: ProgressPhoto,
    isCompareMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) AccentOrange else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = photo.filePath),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        
        // Date overlay
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(4.dp)
        ) {
            Text(
                text = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(photo.date)),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontSize = 10.sp
            )
        }

        if (isCompareMode && isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AccentOrange.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
            }
        }
    }
}

@Composable
fun CompareOverlay(
    photo1: ProgressPhoto?,
    photo2: ProgressPhoto?,
    onClear: () -> Unit,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Compare Mode", color = TextPrimary, fontWeight = FontWeight.Bold)
                Row {
                    TextButton(onClick = onClear) { Text("Clear", color = TextSecondary) }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, null, tint = TextPrimary)
                    }
                }
            }

            if (photo1 != null && photo2 != null) {
                BeforeAfterSlider(
                    beforePhoto = photo1,
                    afterPhoto = photo2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                // Date labels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Before: ${SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(photo1.date))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        "After: ${SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(photo2.date))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Select 2 photos to compare",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun BeforeAfterSlider(
    beforePhoto: ProgressPhoto,
    afterPhoto: ProgressPhoto,
    modifier: Modifier = Modifier
) {
    var sliderPosition by remember { mutableFloatStateOf(0.5f) }

    Box(
        modifier = modifier.clip(RoundedCornerShape(12.dp))
    ) {
        // After photo (full width underneath)
        Image(
            painter = rememberAsyncImagePainter(model = afterPhoto.filePath),
            contentDescription = "After",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Before photo (clipped to slider position)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(sliderPosition)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = beforePhoto.filePath),
                    contentDescription = "Before",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(
                            with(androidx.compose.ui.platform.LocalDensity.current) {
                                // Use the parent's full width for the image, but clip it
                                // This ensures both images are aligned
                                1000.dp // Will be constrained by parent
                            }
                        ),
                    alignment = Alignment.TopStart
                )
            }
        }

        // Slider handle
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(3.dp)
                .align(Alignment.CenterStart)
                .offset(
                    x = with(androidx.compose.ui.platform.LocalDensity.current) {
                        // Approximate position
                        (sliderPosition * 300).dp
                    }
                )
                .background(Color.White)
        )

        // Invisible slider on top for gesture control
        Slider(
            value = sliderPosition,
            onValueChange = { sliderPosition = it },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White.copy(alpha = 0.5f),
                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
            )
        )

        // Labels
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text("Before", color = Color.White, fontSize = 10.sp)
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text("After", color = Color.White, fontSize = 10.sp)
        }
    }
}

@Composable
private fun EmptyState(title: String, subtitle: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.PhotoCamera,
                contentDescription = null,
                tint = TextDisabled,
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextDisabled)
        }
    }
}

@Composable
fun PhotoDetailDialog(
    photo: ProgressPhoto,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {},
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = photo.filePath),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(photo.date)),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                if (photo.notes.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        photo.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Filled.Delete, null, tint = ErrorRed, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Delete", color = ErrorRed)
                }
                TextButton(onClick = onDismiss) {
                    Text("Close", color = AccentOrange)
                }
            }
        },
        containerColor = SurfaceDark,
        modifier = Modifier.fillMaxWidth()
    )
    
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Photo?", color = TextPrimary) },
            text = { Text("This action cannot be undone.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = onDelete) {
                    Text("Delete", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }
}
