package com.game.puzzle.desktop

import com.game.puzzle.GameController
import com.game.puzzle.model.GameState
import java.awt.*
import java.awt.event.*
import java.awt.geom.RoundRectangle2D
import javax.swing.*
import kotlin.math.abs

private data class TileVisual(
    val id: Int, var gridRow: Int, var gridCol: Int,
    var visualX: Float, var visualY: Float,
    var targetX: Float = visualX, var targetY: Float = visualY
)

private data class OptionRow(var label: String, var options: List<String>, var selectedIndex: Int)

private const val WINDOW_WIDTH = 500
private const val WINDOW_HEIGHT = 650
private val BG_COLOR = Color(30, 30, 45)
private val BTN_BG = Color(52, 73, 94)
private val BTN_HOVER = Color(66, 93, 114)
private val TILE_COLORS = arrayOf(
    Color(231, 76, 60), Color(230, 126, 34), Color(241, 196, 15), Color(46, 204, 113),
    Color(26, 188, 156), Color(52, 152, 219), Color(155, 89, 182), Color(233, 30, 99),
    Color(0, 188, 212), Color(255, 87, 34), Color(121, 85, 72), Color(96, 125, 139),
    Color(76, 175, 80), Color(33, 150, 243), Color(255, 152, 0), Color(103, 58, 183)
)

object AppSettings {
    var gridSize = 4
    var shuffleSteps = 50
    var animationSpeed = 200
    var language = "en"
}

object Strings {
    private val en = mapOf(
        "title" to "15 Puzzle", "subtitle" to "Classic sliding tile puzzle",
        "play" to "Play", "settings" to "Settings", "quit" to "Quit",
        "back" to "Back", "menu" to "Menu", "reset" to "Reset",
        "settings_title" to "Settings", "grid_size" to "Grid Size",
        "shuffle_steps" to "Shuffle Steps", "animation" to "Animation Speed",
        "fast" to "Fast", "normal" to "Normal", "slow" to "Slow",
        "playing" to "Playing", "solved" to "SOLVED!", "language" to "Language",
        "moves" to "Moves: "
    )
    private val ru = mapOf(
        "title" to "15 Пазл", "subtitle" to "Классический пазл со сдвигающимися плитками",
        "play" to "Играть", "settings" to "Настройки", "quit" to "Выход",
        "back" to "Назад", "menu" to "Меню", "reset" to "Сброс",
        "settings_title" to "Настройки", "grid_size" to "Размер сетки",
        "shuffle_steps" to "Ходы перемешивания", "animation" to "Скорость анимации",
        "fast" to "Быстро", "normal" to "Нормально", "slow" to "Медленно",
        "playing" to "Играем", "solved" to "РЕШЕНО!", "language" to "Язык",
        "moves" to "Ходы: "
    )
    private val de = mapOf(
        "title" to "15 Puzzle", "subtitle" to "Klassisches Schiebepuzzle",
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

fun main() {
    System.setProperty("apple.awt.application.name", "15 Puzzle")
    System.setProperty("apple.awt.application.appearance", "system")
    SwingUtilities.invokeLater {
        val frame = GameFrame()
        frame.toFront()
        frame.repaint()
    }
}

class GameFrame : JFrame("15 Puzzle") {

    private val splashPanel = SplashPanel()
    private val menuPanel = MenuPanel()
    private val settingsPanel = SettingsPanel()
    private val puzzlePanel = PuzzlePanel()

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        isResizable = false
        contentPane.background = BG_COLOR
        contentPane.layout = null

        val icon16 = createIcon(16)
        val icon32 = createIcon(32)
        val icon64 = createIcon(64)
        val icon128 = createIcon(128)
        iconImages = listOf(icon16, icon32, icon64, icon128)

        listOf(splashPanel, menuPanel, settingsPanel, puzzlePanel).forEach {
            it.setBounds(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT)
            it.isVisible = false
            contentPane.add(it)
        }
        splashPanel.isVisible = true

        addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (puzzlePanel.isVisible) puzzlePanel.handleKey(e.keyCode)
            }
        })

        setSize(WINDOW_WIDTH, WINDOW_HEIGHT)
        setLocationRelativeTo(null)
        isAlwaysOnTop = true
        isVisible = true
        isFocusable = true
        requestFocusInWindow()

        splashPanel.startAnimation()
    }

    private fun createIcon(size: Int): java.awt.Image {
        val img = java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB)
        val g = img.createGraphics()
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON)
        g.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY)

        g.color = BG_COLOR
        g.fillRoundRect(0, 0, size, size, size / 8, size / 8)

        val ps = size * 24 / 128; val pg = size * 4 / 128
        val px = (size - (ps * 4 + pg * 3)) / 2
        val py = (size - (ps * 4 + pg * 3)) / 2
        for (i in 0 until 8) {
            g.color = TILE_COLORS[i]
            g.fillRoundRect(px + (i % 4) * (ps + pg), py + (i / 4) * (ps + pg), ps, ps, 3, 3)
            if (size >= 32) {
                g.color = Color.WHITE
                g.font = Font("SansSerif", Font.BOLD, (size / 10).coerceAtLeast(8))
                val fm = g.fontMetrics
                val num = "${i + 1}"
                val tx = px + (i % 4) * (ps + pg) + (ps - fm.stringWidth(num)) / 2
                val ty = py + (i / 4) * (ps + pg) + (ps + fm.ascent - fm.descent) / 2
                g.drawString(num, tx, ty)
            }
        }

        g.dispose()
        return img
    }

    fun showMenu() {
        listOf(splashPanel, settingsPanel, puzzlePanel).forEach { it.isVisible = false }
        menuPanel.isVisible = true
        menuPanel.requestFocusInWindow()
        menuPanel.repaint()
    }

    fun showSettings() {
        listOf(splashPanel, menuPanel, puzzlePanel).forEach { it.isVisible = false }
        settingsPanel.isVisible = true
        settingsPanel.requestFocusInWindow()
        settingsPanel.repaint()
    }

    fun showGame() {
        listOf(splashPanel, menuPanel, settingsPanel).forEach { it.isVisible = false }
        puzzlePanel.isVisible = true
        puzzlePanel.rebuild()
        puzzlePanel.requestFocusInWindow()
    }

    inner class SplashPanel : JPanel() {
        private var startTime = 0L
        private var titleAlpha = 0f
        private var subtitleAlpha = 0f
        private val tileScale = FloatArray(8) { 0f }
        private var progressWidth = 0f
        private var done = false
        private var timer: Timer? = null

        fun startAnimation() {
            startTime = System.currentTimeMillis()
            timer = Timer(16, ActionListener {
                if (done) return@ActionListener
                val elapsed = (System.currentTimeMillis() - startTime).toFloat()
                titleAlpha = ((elapsed - 200f) / 600f).coerceIn(0f, 1f)
                subtitleAlpha = ((elapsed - 800f) / 600f).coerceIn(0f, 1f)
                for (i in 0 until 8) {
                    val t = ((elapsed - 400f - i * 100f) / 400f).coerceIn(0f, 1f)
                    tileScale[i] = t * t * (3f - 2f * t)
                }
                progressWidth = ((elapsed - 1800f) / 1200f).coerceIn(0f, 1f) * 300f
                repaint()
                if (elapsed > 3500) { timer?.stop(); done = true; showMenu() }
            }).also { it.initialDelay = 0; it.start() }
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            val g2 = g as Graphics2D
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB)
            g2.color = BG_COLOR; g2.fillRect(0, 0, width, height)

            g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.05f)
            g2.color = Color(52, 152, 219); g2.fillOval(-100, 200, 400, 400)
            g2.color = Color(155, 89, 182); g2.fillOval(250, 100, 350, 350)
            g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f)

            val ps = 52; val pg = 6
            val px = (WINDOW_WIDTH - (ps * 4 + pg * 3)) / 2f; val py = 200f
            for (i in 0 until 8) {
                if (tileScale[i] <= 0f) continue
                g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, tileScale[i])
                val cx = px + (i % 4) * (ps + pg) + ps / 2f
                val cy = py + (i / 4) * (ps + pg) + ps / 2f
                val size = ps * tileScale[i]
                g2.color = TILE_COLORS[i]
                g2.fill(RoundRectangle2D.Float(cx - size / 2f, cy - size / 2f, size, size, 10f, 10f))
                if (tileScale[i] > 0.6f) {
                    g2.color = Color.WHITE; g2.font = Font("SansSerif", Font.BOLD, 20)
                    val fm = g2.fontMetrics
                    g2.drawString("${i + 1}", cx - fm.stringWidth("${i + 1}") / 2f, cy + (fm.ascent - fm.descent) / 2f)
                }
            }
            g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f)

            if (titleAlpha > 0f) {
                g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, titleAlpha)
                g2.color = Color.WHITE; g2.font = Font("SansSerif", Font.BOLD, 56)
                val t = "15 Puzzle"; g2.drawString(t, (WINDOW_WIDTH - g2.fontMetrics.stringWidth(t)) / 2f, 110f)
                g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f)
            }
            if (subtitleAlpha > 0f) {
                g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, subtitleAlpha)
                g2.font = Font("SansSerif", Font.PLAIN, 18); g2.color = Color(150, 150, 170)
                val s = "Classic sliding tile puzzle"; g2.drawString(s, (WINDOW_WIDTH - g2.fontMetrics.stringWidth(s)) / 2f, 370f)
                g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f)
            }
            if (progressWidth > 0f) {
                val bx = (WINDOW_WIDTH - 300f) / 2f; val by = 420f
                g2.color = Color(44, 62, 80); g2.fill(RoundRectangle2D.Float(bx, by, 300f, 6f, 3f, 3f))
                g2.color = Color(52, 152, 219); g2.fill(RoundRectangle2D.Float(bx, by, progressWidth, 6f, 3f, 3f))
            }
        }
    }

    inner class MenuPanel : JPanel() {
        private val playBtn = Rectangle(WINDOW_WIDTH / 2 - 140, 340, 280, 55)
        private val settingsBtn = Rectangle(WINDOW_WIDTH / 2 - 140, 410, 280, 55)
        private val quitBtn = Rectangle(WINDOW_WIDTH / 2 - 140, 480, 280, 55)
        private var hovered: String? = null

        init {
            isOpaque = false
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    when {
                        playBtn.contains(e.x, e.y) -> showGame()
                        settingsBtn.contains(e.x, e.y) -> showSettings()
                        quitBtn.contains(e.x, e.y) -> System.exit(0)
                    }
                }
            })
            addMouseMotionListener(object : MouseMotionAdapter() {
                override fun mouseMoved(e: MouseEvent) {
                    val h = when {
                        playBtn.contains(e.x, e.y) -> "play"
                        settingsBtn.contains(e.x, e.y) -> "settings"
                        quitBtn.contains(e.x, e.y) -> "quit"
                        else -> null
                    }
                    if (h != hovered) {
                        hovered = h
                        cursor = if (h != null) Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) else Cursor.getDefaultCursor()
                        repaint()
                    }
                }
            })
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            val g2 = g as Graphics2D
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB)
            g2.color = BG_COLOR; g2.fillRect(0, 0, width, height)

            g2.color = Color.WHITE; g2.font = Font("SansSerif", Font.BOLD, 48)
            val title = "15 Puzzle"
            g2.drawString(title, (WINDOW_WIDTH - g2.fontMetrics.stringWidth(title)) / 2, 80)

            val ps = 40; val pg = 4
            val px = (WINDOW_WIDTH - (ps * 4 + pg * 3)) / 2; val py = 120
            for (i in 0 until 8) {
                g2.color = TILE_COLORS[i]
                g2.fill(RoundRectangle2D.Float((px + (i % 4) * (ps + pg)).toFloat(), (py + (i / 4) * (ps + pg)).toFloat(), ps.toFloat(), ps.toFloat(), 8f, 8f))
                g2.color = Color.WHITE; g2.font = Font("SansSerif", Font.BOLD, 18)
                val fm = g2.fontMetrics
                g2.drawString("${i + 1}", px + (i % 4) * (ps + pg) + (ps - fm.stringWidth("${i + 1}")) / 2, py + (i / 4) * (ps + pg) + (ps + fm.ascent - fm.descent) / 2)
            }

            g2.font = Font("SansSerif", Font.PLAIN, 18); g2.color = Color(150, 150, 170)
            g2.drawString(Strings["subtitle"], (WINDOW_WIDTH - g2.fontMetrics.stringWidth(Strings["subtitle"])) / 2, 290)

            drawMenuBtn(g2, playBtn, Strings["play"], hovered == "play", Color(52, 152, 219), Color(52, 173, 219))
            drawMenuBtn(g2, settingsBtn, Strings["settings"], hovered == "settings", BTN_BG, BTN_HOVER)
            drawMenuBtn(g2, quitBtn, Strings["quit"], hovered == "quit", Color(191, 66, 40), Color(211, 86, 50))
        }

        private fun drawMenuBtn(g2: Graphics2D, rect: Rectangle, label: String, hover: Boolean, bg: Color, hoverBg: Color) {
            g2.color = if (hover) hoverBg else bg
            g2.fill(RoundRectangle2D.Float(rect.x.toFloat(), rect.y.toFloat(), rect.width.toFloat(), rect.height.toFloat(), 14f, 14f))
            g2.color = Color.WHITE; g2.font = Font("SansSerif", Font.BOLD, 22)
            val fm = g2.fontMetrics
            g2.drawString(label, rect.x + (rect.width - fm.stringWidth(label)) / 2, rect.y + (rect.height + fm.ascent - fm.descent) / 2)
        }
    }

    inner class SettingsPanel : JPanel() {

        private val langRow = OptionRow("Language", listOf("English", "Русский", "Deutsch"), 0)
        private val rows = listOf(
            langRow,
            OptionRow("Grid Size", listOf("3×3", "4×4", "5×5"), 1),
            OptionRow("Shuffle Steps", listOf("20", "50", "100"), 1),
            OptionRow("Animation", listOf("Fast", "Normal", "Slow"), 1)
        )
        private val animSpeeds = intArrayOf(100, 200, 400)
        private var hoveredBtn: String? = null
        private val backBtn = Rectangle(WINDOW_WIDTH / 2 - 140, 540, 280, 55)

        init {
            langRow.selectedIndex = when (AppSettings.language) { "ru" -> 1; "de" -> 2; else -> 0 }
            isOpaque = false
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    val x = e.x; val y = e.y
                    if (backBtn.contains(x, y)) {
                        applySettings()
                        showMenu()
                        return
                    }
                    for ((rowIdx, row) in rows.withIndex()) {
                        val rowY = if (rowIdx == 0) 120 else 220 + (rowIdx - 1) * 100
                        val optW = 120; val optGap = 12
                        val totalW = row.options.size * optW + (row.options.size - 1) * optGap
                        val startX = (WINDOW_WIDTH - totalW) / 2
                        for ((optIdx, _) in row.options.withIndex()) {
                            val ox = startX + optIdx * (optW + optGap)
                            val btnY = rowY + 15
                            if (Rectangle(ox, btnY.toInt(), optW, 40).contains(x, y)) {
                                row.selectedIndex = optIdx
                                if (rowIdx == 0) {
                                    AppSettings.language = when (optIdx) { 0 -> "en"; 1 -> "ru"; 2 -> "de"; else -> "en" }
                                    updateLabels()
                                }
                                repaint()
                                return
                            }
                        }
                    }
                }
            })
            addMouseMotionListener(object : MouseMotionAdapter() {
                override fun mouseMoved(e: MouseEvent) {
                    val h = if (backBtn.contains(e.x, e.y)) "back" else null
                    if (h != hoveredBtn) {
                        hoveredBtn = h
                        cursor = if (h != null) Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) else Cursor.getDefaultCursor()
                        repaint()
                    }
                }
            })
        }

        private fun updateLabels() {
            langRow.label = Strings["language"]
            rows[1].label = Strings["grid_size"]
            rows[2].label = Strings["shuffle_steps"]
            rows[3].label = Strings["animation"]
            rows[3].options = listOf(Strings["fast"], Strings["normal"], Strings["slow"])
        }

        private fun applySettings() {
            AppSettings.gridSize = when (rows[1].selectedIndex) { 0 -> 3; 1 -> 4; else -> 5 }
            AppSettings.shuffleSteps = when (rows[2].selectedIndex) { 0 -> 20; 1 -> 50; else -> 100 }
            AppSettings.animationSpeed = animSpeeds[rows[3].selectedIndex]
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            val g2 = g as Graphics2D
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB)
            g2.color = BG_COLOR; g2.fillRect(0, 0, width, height)

            g2.color = Color.WHITE; g2.font = Font("SansSerif", Font.BOLD, 36)
            val title = Strings["settings_title"]
            g2.drawString(title, (WINDOW_WIDTH - g2.fontMetrics.stringWidth(title)) / 2, 70)

            for ((rowIdx, row) in rows.withIndex()) {
                val rowY = if (rowIdx == 0) 120 else 220 + (rowIdx - 1) * 100

                g2.color = Color(180, 180, 200); g2.font = Font("SansSerif", Font.BOLD, 20)
                g2.drawString(row.label, 60, rowY)

                val optW = 120; val optGap = 12
                val totalW = row.options.size * optW + (row.options.size - 1) * optGap
                val startX = (WINDOW_WIDTH - totalW) / 2
                val btnY = rowY + 15
                for ((optIdx, opt) in row.options.withIndex()) {
                    val ox = startX + optIdx * (optW + optGap)
                    val selected = optIdx == row.selectedIndex
                    g2.color = if (selected) Color(52, 152, 219) else Color(55, 60, 75)
                    g2.fill(RoundRectangle2D.Float(ox.toFloat(), btnY.toFloat(), optW.toFloat(), 40f, 8f, 8f))
                    g2.color = Color.WHITE; g2.font = Font("SansSerif", if (selected) Font.BOLD else Font.PLAIN, 16)
                    val fm = g2.fontMetrics
                    g2.drawString(opt, ox + (optW - fm.stringWidth(opt)) / 2, btnY + (40 + fm.ascent - fm.descent) / 2)
                }
            }

            g2.color = if (hoveredBtn == "back") BTN_HOVER else BTN_BG
            g2.fill(RoundRectangle2D.Float(backBtn.x.toFloat(), backBtn.y.toFloat(), backBtn.width.toFloat(), backBtn.height.toFloat(), 14f, 14f))
            g2.color = Color.WHITE; g2.font = Font("SansSerif", Font.BOLD, 22)
            val fm = g2.fontMetrics
            g2.drawString(Strings["back"], backBtn.x + (backBtn.width - fm.stringWidth(Strings["back"])) / 2, backBtn.y + (backBtn.height + fm.ascent - fm.descent) / 2)
        }
    }

    inner class PuzzlePanel : JPanel() {

        var tileSize = 100
        var gridOffsetX = 20
        var gridOffsetY = 80
        var isAnimating = false

        private val tiles = mutableMapOf<Int, TileVisual>()
        private var hoveredBtn: String? = null
        private var controller = GameController(AppSettings.gridSize, AppSettings.shuffleSteps)
        private var gridSize = AppSettings.gridSize
        private val menuBtn get() = Rectangle(WINDOW_WIDTH / 2 - 145, 560, 130, 50)
        private val resetBtn get() = Rectangle(WINDOW_WIDTH / 2 + 15, 560, 130, 50)

        fun rebuild() {
            gridSize = AppSettings.gridSize
            controller = GameController(gridSize, AppSettings.shuffleSteps)
            val area = WINDOW_WIDTH - 40
            tileSize = area / gridSize
            gridOffsetX = (WINDOW_WIDTH - tileSize * gridSize) / 2
            gridOffsetY = 80
            controller.shuffle()
            syncFromController()
        }

        fun syncFromController() {
            isAnimating = false
            tiles.clear()
            val state = controller.state
            for (r in 0 until gridSize) {
                for (c in 0 until gridSize) {
                    val id = state.grid[r][c] ?: continue
                    if (id == 0) continue
                    tiles[id] = TileVisual(id, r, c,
                        gridOffsetX + c * tileSize.toFloat(),
                        gridOffsetY + r * tileSize.toFloat()
                    )
                }
            }
            repaint()
        }

        fun handleKey(keyCode: Int) {
            if (isAnimating) return
            val (er, ec) = controller.state.emptyPosition()
            val (tr, tc) = when (keyCode) {
                KeyEvent.VK_UP -> (er + 1) to ec
                KeyEvent.VK_DOWN -> (er - 1) to ec
                KeyEvent.VK_LEFT -> er to (ec + 1)
                KeyEvent.VK_RIGHT -> er to (ec - 1)
                else -> return
            }
            tryMove(tr, tc)
        }

        private fun tryMove(targetRow: Int, targetCol: Int): Boolean {
            if (targetRow !in 0 until gridSize || targetCol !in 0 until gridSize) return false
            if (isAnimating) return false
            val (er, ec) = controller.state.emptyPosition()
            if (abs(targetRow - er) + abs(targetCol - ec) != 1) return false
            val tileId = controller.state.grid[targetRow][targetCol] ?: return false
            if (!controller.moveTile(targetRow, targetCol)) return false

            isAnimating = true
            val tile = tiles[tileId] ?: return false
            tile.targetX = gridOffsetX + ec * tileSize.toFloat()
            tile.targetY = gridOffsetY + er * tileSize.toFloat()
            val startTime = System.currentTimeMillis()
            val startX = tile.visualX; val startY = tile.visualY

            Timer(16, object : ActionListener {
                override fun actionPerformed(e: ActionEvent) {
                    val t = ((System.currentTimeMillis() - startTime).toFloat() / AppSettings.animationSpeed).coerceIn(0f, 1f)
                    val s = t * t * (3f - 2f * t)
                    tile.visualX = startX + (tile.targetX - startX) * s
                    tile.visualY = startY + (tile.targetY - startY) * s
                    repaint()
                    if (t >= 1f) {
                        tile.visualX = tile.targetX; tile.visualY = tile.targetY
                        tile.gridRow = er; tile.gridCol = ec
                        isAnimating = false; repaint()
                    }
                }
            }).also { it.initialDelay = 0; it.start() }
            return true
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            val g2 = g as Graphics2D
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.color = BG_COLOR; g2.fillRect(0, 0, width, height)

            g2.color = Color.WHITE; g2.font = Font("SansSerif", Font.BOLD, 22)
            g2.drawString("${Strings["moves"]}${controller.movesCount}", 20, 35)
            g2.font = Font("SansSerif", Font.BOLD, 18)
            val status = if (controller.isSolved) Strings["solved"] else Strings["playing"]
            g2.color = if (controller.isSolved) Color(46, 204, 113) else Color.WHITE
            g2.drawString(status, 20, 60)

            g2.color = Color(44, 62, 80)
            g2.fill(RoundRectangle2D.Float(gridOffsetX - 8f, gridOffsetY - 8f,
                (tileSize * gridSize + 16).toFloat(), (tileSize * gridSize + 16).toFloat(), 12f, 12f))

            for ((_, tile) in tiles) {
                val color = TILE_COLORS[(tile.id - 1) % TILE_COLORS.size]
                val x = tile.visualX.toInt() + 3; val y = tile.visualY.toInt() + 3; val size = tileSize - 6
                g2.color = color
                g2.fill(RoundRectangle2D.Float(x.toFloat(), y.toFloat(), size.toFloat(), size.toFloat(), 8f, 8f))
                g2.color = Color.WHITE
                g2.font = Font("SansSerif", Font.BOLD, (tileSize * 0.35f).toInt().coerceAtLeast(14))
                val fm = g2.fontMetrics
                g2.drawString("${tile.id}", x + (size - fm.stringWidth("${tile.id}")) / 2, y + (size + fm.ascent - fm.descent) / 2)
            }

            drawBtn(g2, menuBtn, Strings["menu"], hoveredBtn == "menu")
            drawBtn(g2, resetBtn, Strings["reset"], hoveredBtn == "reset")
        }

        private fun drawBtn(g2: Graphics2D, rect: Rectangle, label: String, hover: Boolean) {
            g2.color = if (hover) BTN_HOVER else BTN_BG
            g2.fill(RoundRectangle2D.Float(rect.x.toFloat(), rect.y.toFloat(), rect.width.toFloat(), rect.height.toFloat(), 10f, 10f))
            g2.color = Color.WHITE; g2.font = Font("SansSerif", Font.BOLD, 16)
            val fm = g2.fontMetrics
            g2.drawString(label, rect.x + (rect.width - fm.stringWidth(label)) / 2, rect.y + (rect.height + fm.ascent - fm.descent) / 2)
        }

        init {
            isOpaque = false
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    requestFocusInWindow()
                    when {
                        menuBtn.contains(e.x, e.y) -> showMenu()
                        resetBtn.contains(e.x, e.y) -> { if (!isAnimating) { controller.reset(); syncFromController() } }
                        else -> {
                            if (!isAnimating) {
                                val col = ((e.x - gridOffsetX) / tileSize).toInt()
                                val row = ((e.y - gridOffsetY) / tileSize).toInt()
                                tryMove(row, col)
                            }
                        }
                    }
                }
            })
            addMouseMotionListener(object : MouseMotionAdapter() {
                override fun mouseMoved(e: MouseEvent) {
                    val h = when {
                        menuBtn.contains(e.x, e.y) -> "menu"
                        resetBtn.contains(e.x, e.y) -> "reset"
                        else -> null
                    }
                    if (h != hoveredBtn) { hoveredBtn = h; cursor = if (h != null) Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) else Cursor.getDefaultCursor(); repaint() }
                }
            })
        }
    }
}
