package com.game.puzzle.android

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

data class AppSettings(
    val gridIndex: Int = 1,
    val shuffleIndex: Int = 1,
    val speedIndex: Int = 1,
    val langIndex: Int = 0
) {
    val gridSize get() = listOf(3, 4, 5)[gridIndex]
    val shuffleSteps get() = listOf(20, 50, 100)[shuffleIndex]
    val animSpeed get() = listOf(100L, 200L, 400L)[speedIndex]
}

@Composable
fun PuzzleApp() {
    var currentScreen by remember { mutableStateOf(Screen.SPLASH) }
    var settings by remember { mutableStateOf(AppSettings()) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BgColor
    ) {
        when (currentScreen) {
            Screen.SPLASH -> SplashScreen {
                currentScreen = Screen.MENU
            }
            Screen.MENU -> MenuScreen(
                onPlay = { currentScreen = Screen.GAME },
                onSettings = { currentScreen = Screen.SETTINGS },
                onQuit = {}
            )
            Screen.SETTINGS -> SettingsScreen(
                settings = settings,
                onSettingsChange = { settings = it },
                onBack = { currentScreen = Screen.MENU }
            )
            Screen.GAME -> GameScreen(
                settings = settings,
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
        delay(200)
        showTitle = true
        delay(600)
        showSubtitle = true
        delay(1000)
        showProgress = true
        delay(1700)
        onFinished()
    }

    val titleAlpha by animateFloatAsState(
        targetValue = if (showTitle) 1f else 0f,
        animationSpec = tween(600),
        label = "title"
    )
    val subtitleAlpha by animateFloatAsState(
        targetValue = if (showSubtitle) 1f else 0f,
        animationSpec = tween(600),
        label = "subtitle"
    )
    val progress by animateFloatAsState(
        targetValue = if (showProgress) 1f else 0f,
        animationSpec = tween(1200),
        label = "progress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "15",
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = titleAlpha)
            )
            Text(
                text = stringResource(R.string.title_puzzle),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = titleAlpha)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.subtitle),
                fontSize = 20.sp,
                color = Color(0xFF9696AA).copy(alpha = subtitleAlpha)
            )
            Spacer(modifier = Modifier.height(32.dp))

            SplashTileGrid(titleAlpha)

            Spacer(modifier = Modifier.height(32.dp))

            if (progress > 0f) {
                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(GridBg)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .clip(RoundedCornerShape(2.dp))
                            .background(BlueBtn)
                    )
                }
            }
        }
    }
}

@Composable
fun SplashTileGrid(alpha: Float) {
    val tiles = remember {
        listOf(
            1 to Color(0xFFE74C3C), 2 to Color(0xFFE67E22), 3 to Color(0xFFF1C40F), 4 to Color(0xFF2ECC71),
            5 to Color(0xFF1ABC9C), 6 to Color(0xFF3498DB), 7 to Color(0xFF9B59B6), 8 to Color(0xFFE91E63)
        )
    }
    val tileSize = 50.dp
    val gap = 6.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.alpha(alpha)
    ) {
        for (row in 0..1) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(gap),
                modifier = Modifier.padding(bottom = gap)
            ) {
                for (col in 0..3) {
                    val idx = row * 4 + col
                    val (num, color) = tiles[idx]
                    Box(
                        modifier = Modifier
                            .size(tileSize)
                            .clip(RoundedCornerShape(8.dp))
                            .background(color),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$num",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MenuScreen(onPlay: () -> Unit, onSettings: () -> Unit, onQuit: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("15", fontSize = 72.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(
                stringResource(R.string.title_puzzle),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(24.dp))

            MenuTilePreview()

            Spacer(modifier = Modifier.height(80.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GameButton(stringResource(R.string.btn_play), BlueBtn) { onPlay() }
                GameButton(stringResource(R.string.btn_settings), DarkBtn) { onSettings() }
                GameButton(stringResource(R.string.btn_quit), RedBtn) { onQuit() }
            }
        }
    }
}

@Composable
fun MenuTilePreview() {
    val tiles = remember {
        listOf(
            1 to Color(0xFFE74C3C), 2 to Color(0xFFE67E22), 3 to Color(0xFFF1C40F), 4 to Color(0xFF2ECC71),
            5 to Color(0xFF1ABC9C), 6 to Color(0xFF3498DB), 7 to Color(0xFF9B59B6), 8 to Color(0xFFE91E63)
        )
    }
    val tileSize = 36.dp
    val gap = 4.dp

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        for (row in 0..1) {
            Row(horizontalArrangement = Arrangement.spacedBy(gap), modifier = Modifier.padding(bottom = gap)) {
                for (col in 0..3) {
                    val idx = row * 4 + col
                    val (num, color) = tiles[idx]
                    Box(
                        modifier = Modifier
                            .size(tileSize)
                            .clip(RoundedCornerShape(6.dp))
                            .background(color),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("$num", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun GameButton(text: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(color)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SettingsScreen(settings: AppSettings, onSettingsChange: (AppSettings) -> Unit, onBack: () -> Unit) {
    val gridOptions = listOf(3, 4, 5)
    val shuffleOptions = listOf(20, 50, 100)
    val speedLabels = listOf(
        stringResource(R.string.speed_fast),
        stringResource(R.string.speed_normal),
        stringResource(R.string.speed_slow)
    )
    val langLabels = listOf("English", "Русский", "Deutsch")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            stringResource(R.string.settings_title),
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(stringResource(R.string.setting_language), fontSize = 20.sp, color = Color(0xFFB4B4C8))
        Spacer(modifier = Modifier.height(8.dp))
        OptionRow(langLabels, settings.langIndex) { onSettingsChange(settings.copy(langIndex = it)) }

        Spacer(modifier = Modifier.height(20.dp))

        Text(stringResource(R.string.setting_grid_size), fontSize = 20.sp, color = Color(0xFFB4B4C8))
        Spacer(modifier = Modifier.height(8.dp))
        OptionRow(gridOptions.map { "${it}×${it}" }, settings.gridIndex) { onSettingsChange(settings.copy(gridIndex = it)) }

        Spacer(modifier = Modifier.height(20.dp))

        Text(stringResource(R.string.setting_shuffle_steps), fontSize = 20.sp, color = Color(0xFFB4B4C8))
        Spacer(modifier = Modifier.height(8.dp))
        OptionRow(shuffleOptions.map { "$it" }, settings.shuffleIndex) { onSettingsChange(settings.copy(shuffleIndex = it)) }

        Spacer(modifier = Modifier.height(20.dp))

        Text(stringResource(R.string.setting_animation), fontSize = 20.sp, color = Color(0xFFB4B4C8))
        Spacer(modifier = Modifier.height(8.dp))
        OptionRow(speedLabels, settings.speedIndex) { onSettingsChange(settings.copy(speedIndex = it)) }

        Spacer(modifier = Modifier.weight(1f))

        GameButton(stringResource(R.string.btn_back), DarkBtn) { onBack() }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun OptionRow(labels: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        labels.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) BlueBtn else Color(0xFF373C4B))
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = Color.White,
                    fontSize = if (label.length > 6) 16.sp else 20.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun GameScreen(settings: AppSettings, onMenu: () -> Unit) {
    val controller = remember(settings.gridSize, settings.shuffleSteps) {
        GameController(settings.gridSize, settings.shuffleSteps).also { it.shuffle() }
    }
    var gridVersion by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        gridVersion++
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            stringResource(R.string.moves_format, controller.movesCount),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.Start)
        )
        Text(
            if (controller.isSolved) stringResource(R.string.status_solved)
            else stringResource(R.string.status_playing),
            fontSize = 24.sp,
            color = if (controller.isSolved) Color(0xFF2ECC71) else Color.LightGray,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(16.dp))

        GameGrid(
            controller = controller,
            gridVersion = gridVersion,
            onTileClick = { row, col ->
                if (controller.moveTile(row, col)) {
                    gridVersion++
                }
            },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GameButton(stringResource(R.string.btn_menu), DarkBtn) { onMenu() }
            GameButton(stringResource(R.string.btn_reset), DarkBtn) {
                controller.reset()
                gridVersion++
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun GameGrid(
    controller: GameController,
    gridVersion: Int,
    onTileClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val state = controller.state
    val size = controller.gridSize

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(GridBg)
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (r in 0 until size) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (c in 0 until size) {
                    val id = state.grid[r][c]

                    if (id != null && id != 0) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(TileColors[(id - 1) % TileColors.size])
                                .clickable { onTileClick(r, c) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "$id",
                                color = Color.White,
                                fontSize = (28 / (size / 3f)).sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                }
            }
        }
    }
}
