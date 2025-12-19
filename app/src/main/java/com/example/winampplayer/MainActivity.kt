package com.example.winampplayer

// Android імпорти
import android.Manifest
import android.content.Context
import android.os.Bundle
import android.provider.MediaStore

// Compose імпорти
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ExoPlayer імпорти
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

// ----------------------
// Головна Activity
// ----------------------
class MainActivity : ComponentActivity() {

    private lateinit var player: ExoPlayer

    // Запит дозволу на читання музики
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Запит дозволу
        permissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)

        // Створюємо плеєр
        player = ExoPlayer.Builder(this).build()

        // Скануємо всю музику на пристрої і додаємо до плеєра
        val playlist = scanDeviceMusic(this)
        playlist.forEach { player.addMediaItem(it) }
        player.prepare()

        // Встановлюємо UI Compose
        setContent {
            WinampUI(player)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}

// ----------------------
// UI Winamp-style плеєра
// ----------------------
@Composable
fun WinampUI(player: ExoPlayer) {
    var isPlaying by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF4B145F))
            .padding(12.dp)
    ) {

        // --- Дисплей ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color(0xFF7E3F98), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "WINAMP ANDROID",
                fontFamily = FontFamily.Monospace,
                fontSize = 18.sp,
                color = Color.Green,
                modifier = Modifier.padding(12.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // --- Кнопки керування ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ControlButton("⏮") { player.seekToPrevious() }

            ControlButton(if (isPlaying) "⏸" else "▶") {
                if (player.isPlaying) player.pause() else player.play()
                isPlaying = player.isPlaying
            }

            ControlButton("⏹") {
                player.stop()
                player.prepare()
            }

            ControlButton("⏭") { player.seekToNext() }
        }

        Spacer(Modifier.height(16.dp))

        // --- Регулятор гучності ---
        Text("Volume", color = Color.White)
        Slider(
            value = player.volume,
            onValueChange = { player.volume = it }
        )
    }
}

// ----------------------
// Кнопка керування
// ----------------------
@Composable
fun ControlButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
        modifier = Modifier.size(56.dp)
    ) {
        Text(label, fontSize = 22.sp)
    }
}

// ----------------------
// Функція автосканування музики
// ----------------------
fun scanDeviceMusic(context: Context): List<MediaItem> {
    val items = mutableListOf<MediaItem>()

    val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    val projection = arrayOf(MediaStore.Audio.Media.DATA)

    context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        val column = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
        while (cursor.moveToNext()) {
            val path = cursor.getString(column)
            items.add(MediaItem.fromUri(path))
        }
    }
    return items
}
