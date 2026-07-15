package com.game.puzzle.desktop

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.key.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.game.puzzle.GameController
import kotlinx.coroutines.delay

private val BgColor = Color(0xFF1E1E2D)
private val BlueBtn = Color(0xFF3498DB)
private val DarkBtn = Color(0xFF34495E)
private val RedBtn = Color(0xFFC0392B)
private val GridBg = Color(0xFF2C3E50)

private val TileColors = listOf(
    Color(0xFFE74C3C), Color(0xFFE67E22), Color(0xFFF1C40F), Color(0xFF2ECC71),
    Color(0xFF1ABC9C), Color(0xFF3498DB), Color(0xFF9B59B6), Color(0xFFE91E63),
    Color(0xFF00BCD4), Color(0xFFFF5722), Color(0xFF795548), Color(0xFF607D8B),
    Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFFFF9800), Color(0xFF673AB7)
)

enum class Screen { SPLASH, MENU, SETTINGS, GAME }

object AppSettings {
    var gridSize = 4
    var shuffleSteps = 50
    var animationSpeed = 200
    var language = "en"
}

object Strings {
    private val en = mapOf(
        "title" to "Puzzle", "subtitle" to "Classic sliding tile puzzle",
        "play" to "Play", "settings" to "Settings", "quit" to "Quit",
        "back" to "Back", "menu" to "Menu", "reset" to "Reset",
        "settings_title" to "Settings", "grid_size" to "Grid Size",
        "shuffle_steps" to "Shuffle Steps", "animation" to "Animation Speed",
        "fast" to "Fast", "normal" to "Normal", "slow" to "Slow",
        "playing" to "Playing", "solved" to "SOLVED!", "language" to "Language",
        "moves" to "Moves: "
    )
    private val ru = mapOf(
        "title" to "Пазл", "subtitle" to "Классический пазл со сдвигающимися плитками",
        "play" to "Играть", "settings" to "Настройки", "quit" to "Выход",
        "back" to "Назад", "menu" to "Меню", "reset" to "Сброс",
        "settings_title" to "Настройки", "grid_size" to "Размер сетки",
        "shuffle_steps" to "Ходы перемешивания", "animation" to "Скорость анимации",
        "fast" to "Быстро", "normal" to "Нормально", "slow" to "Медленно",
        "playing" to "Играем", "solved" to "РЕШЕНО!", "language" to "Язык",
        "moves" to "Ходы: "
    )
    private val de = mapOf(
        "title" to "Puzzle", "subtitle" to "Klassisches Schiebepuzzle",
        "play" to "Spielen", "settings" to "Einstellungen", "quit" to "Beenden",
        "back" to "Zurück", "menu" to "Menü", "reset" to "Zurücksetzen",
        "settings_title" to "Einstellungen", "grid_size" to "Rastergröße",
        "shuffle_steps" to "Mischtritte", "animation" to "Animationstempo",
        "fast" to "Schnell", "normal" to "Normal", "slow" to "Langsam",
        "playing" to "Spiel läuft", "solved" to "GELÖST!", "language" to "Sprache",
        "moves" to "Züge: "
    )

    operator fun get(key: String): String {
        val map = when (AppSettings.language) { "ru" -> ru; "de" -> de; else -> en }
        return map[key] ?: en[key] ?: key
    }
}

fun str(key: String) = Strings[key]

fun main() = application {
    System.setProperty("apple.awt.application.name", "15 Puzzle")
    System.setProperty("apple.awt.application.appearance", "system")

    val exit = ::exitApplication

    Window(
        onCloseRequest = ::exitApplication,
        title = "15 Puzzle",
        state = androidx.compose.ui.window.WindowState(
            size = androidx.compose.ui.unit.DpSize(500.dp, 700.dp)
        ),
        resizable = false
    ) {
        MaterialTheme {
            PuzzleDesktopApp(onExit = exit)
        }
    }
}

@Composable
fun PuzzleDesktopApp(onExit: () -> Unit = {}) {
    var currentScreen by remember { mutableStateOf(Screen.SPLASH) }
    var langVersion by remember { mutableIntStateOf(0) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BgColor
    ) {
        when (currentScreen) {
            Screen.SPLASH -> SplashScreen { currentScreen = Screen.MENU }
            Screen.MENU -> MenuScreen(
                onPlay = { currentScreen = Screen.GAME },
                onSettings = { currentScreen = Screen.SETTINGS },
                onQuit = onExit
            )
            Screen.SETTINGS -> SettingsScreen(
                onLanguageChange = { langVersion++ },
                onBack = { currentScreen = Screen.MENU; langVersion++ }
            )
            Screen.GAME -> GameScreen(
                onMenu = { currentScreen = Screen.MENU }
            )
        }
    }
}

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var showTitle by remember { mutableStateOf(false) }
    var showSubtitle by remember { mutableStateOf(false) }
    var showProgress by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200); showTitle = true
        delay(600); showSubtitle = true
        delay(1000); showProgress = true
        delay(1700); onFinished()
    }

    val titleAlpha by animateFloatAsState(if (showTitle) 1f else 0f, tween(600), label = "t")
    val subtitleAlpha by animateFloatAsState(if (showSubtitle) 1f else 0f, tween(600), label = "s")
    val progress by animateFloatAsState(if (showProgress) 1f else 0f, tween(1200), label = "p")

    Box(Modifier.fillMaxSize().background(BgColor), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("15", fontSize = 72.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = titleAlpha))
            Text(str("title"), fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = titleAlpha))
            Spacer(Modifier.height(12.dp))
            Text(str("subtitle"), fontSize = 20.sp, color = Color(0xFF9696AA).copy(alpha = subtitleAlpha))
            Spacer(Modifier.height(32.dp))
            SplashTileGrid(titleAlpha)
            Spacer(Modifier.height(32.dp))
            if (progress > 0f) {
                Box(Modifier.width(200.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(GridBg)) {
                    Box(Modifier.fillMaxHeight().fillMaxWidth(progress).clip(RoundedCornerShape(2.dp)).background(BlueBtn))
                }
            }
        }
    }
}

@Composable
fun SplashTileGrid(alpha: Float) {
    val tiles = listOf(
        1 to Color(0xFFE74C3C), 2 to Color(0xFFE67E22), 3 to Color(0xFFF1C40F), 4 to Color(0xFF2ECC71),
        5 to Color(0xFF1ABC9C), 6 to Color(0xFF3498DB), 7 to Color(0xFF9B59B6), 8 to Color(0xFFE91E63)
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.alpha(alpha)) {
        for (row in 0..1) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 6.dp)) {
                for (col in 0..3) {
                    val (num, color) = tiles[row * 4 + col]
                    Box(Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)).background(color), contentAlignment = Alignment.Center) {
                        Text("$num", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun MenuScreen(onPlay: () -> Unit, onSettings: () -> Unit, onQuit: () -> Unit) {
    val tiles = listOf(
        1 to Color(0xFFE74C3C), 2 to Color(0xFFE67E22), 3 to Color(0xFFF1C40F), 4 to Color(0xFF2ECC71),
        5 to Color(0xFF1ABC9C), 6 to Color(0xFF3498DB), 7 to Color(0xFF9B59B6), 8 to Color(0xFFE91E63)
    )

    Box(Modifier.fillMaxSize().background(BgColor), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text("15", fontSize = 72.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(str("title"), fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(24.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                for (row in 0..1) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(bottom = 4.dp)) {
                        for (col in 0..3) {
                            val (num, color) = tiles[row * 4 + col]
                            Box(Modifier.size(36.dp).clip(RoundedCornerShape(6.dp)).background(color), contentAlignment = Alignment.Center) {
                                Text("$num", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
            Column(Modifier.fillMaxWidth().padding(horizontal = 40.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                GameButton(str("play"), BlueBtn, onPlay)
                GameButton(str("settings"), DarkBtn, onSettings)
                GameButton(str("quit"), RedBtn, onQuit)
            }
        }
    }
}

@Composable
fun GameButton(text: String, color: Color, onClick: () -> Unit, height: Int = 56) {
    Box(
        Modifier.fillMaxWidth().height(height.dp).clip(RoundedCornerShape(14.dp)).background(color).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SettingsScreen(onLanguageChange: () -> Unit, onBack: () -> Unit) {
    var langIndex by remember { mutableIntStateOf(when (AppSettings.language) { "ru" -> 1; "de" -> 2; else -> 0 }) }
    var gridIndex by remember { mutableIntStateOf(listOf(3, 4, 5).indexOf(AppSettings.gridSize).coerceAtLeast(0)) }
    var shuffleIndex by remember { mutableIntStateOf(listOf(20, 50, 100).indexOf(AppSettings.shuffleSteps).coerceAtLeast(0)) }
    var speedIndex by remember { mutableIntStateOf(listOf(100, 200, 400).indexOf(AppSettings.animationSpeed).coerceAtLeast(0)) }

    val langLabels = listOf("English", "Русский", "Deutsch")
    val gridLabels = listOf("3×3", "4×4", "5×5")
    val shuffleLabels = listOf("20", "50", "100")
    val speedLabels = listOf(str("fast"), str("normal"), str("slow"))

    Column(
        Modifier.fillMaxSize().background(BgColor).padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))
        Text(str("settings_title"), fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(24.dp))

        Text(str("language"), fontSize = 20.sp, color = Color(0xFFB4B4C8))
        Spacer(Modifier.height(8.dp))
        OptionRow(langLabels, langIndex) { i ->
            langIndex = i
            AppSettings.language = arrayOf("en", "ru", "de")[i]
            onLanguageChange()
        }

        Spacer(Modifier.height(20.dp))
        Text(str("grid_size"), fontSize = 20.sp, color = Color(0xFFB4B4C8))
        Spacer(Modifier.height(8.dp))
        OptionRow(gridLabels, gridIndex) { i -> gridIndex = i; AppSettings.gridSize = listOf(3, 4, 5)[i] }

        Spacer(Modifier.height(20.dp))
        Text(str("shuffle_steps"), fontSize = 20.sp, color = Color(0xFFB4B4C8))
        Spacer(Modifier.height(8.dp))
        OptionRow(shuffleLabels, shuffleIndex) { i -> shuffleIndex = i; AppSettings.shuffleSteps = listOf(20, 50, 100)[i] }

        Spacer(Modifier.height(20.dp))
        Text(str("animation"), fontSize = 20.sp, color = Color(0xFFB4B4C8))
        Spacer(Modifier.height(8.dp))
        OptionRow(speedLabels, speedIndex) { i -> speedIndex = i; AppSettings.animationSpeed = listOf(100, 200, 400)[i] }

        Spacer(Modifier.weight(1f))
        GameButton(str("back"), DarkBtn, onBack)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun OptionRow(labels: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        labels.forEachIndexed { index, label ->
            Box(
                Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(10.dp))
                    .background(if (index == selectedIndex) BlueBtn else Color(0xFF373C4B))
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(label, color = Color.White, fontSize = if (label.length > 6) 16.sp else 20.sp,
                    fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GameScreen(onMenu: () -> Unit) {
    val controller = remember(AppSettings.gridSize, AppSettings.shuffleSteps) {
        GameController(AppSettings.gridSize, AppSettings.shuffleSteps).also { it.shuffle() }
    }
    var gridVersion by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) { gridVersion++ }

    fun tryMove(row: Int, col: Int) {
        if (controller.moveTile(row, col)) gridVersion++
    }

    Column(
        Modifier.fillMaxSize().background(BgColor).padding(horizontal = 16.dp)
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown && !controller.isSolved) {
                    val (er, ec) = controller.state.emptyPosition()
                    when (event.key) {
                        Key.DirectionUp -> { tryMove(er + 1, ec); true }
                        Key.DirectionDown -> { tryMove(er - 1, ec); true }
                        Key.DirectionLeft -> { tryMove(er, ec + 1); true }
                        Key.DirectionRight -> { tryMove(er, ec - 1); true }
                        else -> false
                    }
                } else false
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))
        Text("${str("moves")}${controller.movesCount}", fontSize = 28.sp, fontWeight = FontWeight.Bold,
            color = Color.White, modifier = Modifier.align(Alignment.Start))
        Text(if (controller.isSolved) str("solved") else str("playing"), fontSize = 20.sp,
            color = if (controller.isSolved) Color(0xFF2ECC71) else Color.LightGray,
            modifier = Modifier.align(Alignment.Start))

        Spacer(Modifier.height(8.dp))

        GameGrid(controller, gridVersion, ::tryMove, Modifier.fillMaxWidth().aspectRatio(1f))

        Spacer(Modifier.height(8.dp))
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            GameButton(str("menu"), DarkBtn, onMenu, 44)
            GameButton(str("reset"), DarkBtn, { controller.reset(); gridVersion++ }, 44)
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
fun GameGrid(controller: GameController, gridVersion: Int, onTileClick: (Int, Int) -> Unit, modifier: Modifier = Modifier) {
    val state = controller.state
    val size = controller.gridSize

    Column(
        modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(GridBg).padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (r in 0 until size) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (c in 0 until size) {
                    val id = state.grid[r][c]
                    if (id != null && id != 0) {
                        Box(
                            Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(8.dp))
                                .background(TileColors[(id - 1) % TileColors.size])
                                .clickable { onTileClick(r, c) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$id", color = Color.White, fontSize = (28 / (size / 3f)).sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Spacer(Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}
