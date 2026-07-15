package com.game.puzzle.android

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.*
import android.os.Bundle
import android.view.*
import com.game.puzzle.GameController
import java.util.Locale
import kotlin.math.abs

class AndroidLauncher : Activity() {

    companion object {
        const val PREFS_NAME = "puzzle_settings"
        const val KEY_LANGUAGE = "language"
    }

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val langCode = prefs.getString(KEY_LANGUAGE, "system") ?: "system"
        if (langCode != "system") {
            val locale = Locale(langCode)
            Locale.setDefault(locale)
            val config = Configuration(newBase.resources.configuration)
            config.setLocale(locale)
            super.attachBaseContext(newBase.createConfigurationContext(config))
        } else {
            super.attachBaseContext(newBase)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(PuzzleView(this))
    }

    fun setLanguage(langCode: String) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, langCode).apply()
        recreate()
    }
}

class PuzzleView(private val activity: AndroidLauncher) : SurfaceView(activity), SurfaceHolder.Callback, Runnable {

    companion object {
        const val SPLASH = 0
        const val MENU = 1
        const val SETTINGS = 2
        const val GAME = 3

        val TILE_COLORS = intArrayOf(
            0xFFE74C3C.toInt(), 0xFFE67E22.toInt(), 0xFFF1C40F.toInt(), 0xFF2ECC71.toInt(),
            0xFF1ABC9C.toInt(), 0xFF3498DB.toInt(), 0xFF9B59B6.toInt(), 0xFFE91E63.toInt(),
            0xFF00BCD4.toInt(), 0xFFFF5722.toInt(), 0xFF795548.toInt(), 0xFF607D8B.toInt(),
            0xFF4CAF50.toInt(), 0xFF2196F3.toInt(), 0xFFFF9800.toInt(), 0xFF673AB7.toInt()
        )

        private const val PREFS_NAME = "puzzle_settings"
        private const val KEY_LANGUAGE = "language"
    }

    private val ctx: Context = activity
    private var gameThread: Thread? = null
    private var running = false
    private var gridSize = 4
    private var shuffleSteps = 50
    private var animSpeed = 200L
    private var controller = GameController(gridSize, shuffleSteps)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()
    private var tileSize = 0f
    private var gridOffsetX = 0f
    private var gridOffsetY = 0f
    private var isAnimating = false
    private var screenW = 0f
    private var screenH = 0f
    private var currentState = SPLASH

    // Splash
    private var splashStart = 0L
    private var splashTitle = 0f
    private var splashSub = 0f
    private val splashScale = FloatArray(8) { 0f }
    private var splashProgress = 0f
    private var splashDone = false

    // Game anim
    private var animTileId = -1
    private var animSX = 0f; private var animSY = 0f
    private var animEX = 0f; private var animEY = 0f
    private var animST = 0L

    // Buttons
    private var playBtn = RectF(); private var quitBtn = RectF()
    private var settingsBtn = RectF()
    private var menuBtn = RectF(); private var resetBtn = RectF(); private var backBtn = RectF()

    // Settings
    private val gridOptions = intArrayOf(3, 4, 5)
    private val shuffleOptions = intArrayOf(20, 50, 100)
    private val speedLabels = intArrayOf(R.string.speed_fast, R.string.speed_normal, R.string.speed_slow)
    private val speedValues = longArrayOf(100, 200, 400)
    private var selGrid = 1
    private var selShuffle = 1
    private var selSpeed = 1
    private var selLang = 0
    private var settingsGridRects = mutableListOf<RectF>()
    private var settingsShuffleRects = mutableListOf<RectF>()
    private var settingsSpeedRects = mutableListOf<RectF>()
    private var settingsLangRects = mutableListOf<RectF>()

    init {
        holder.addCallback(this)
        isFocusable = true
        loadLangSelection()
    }

    private fun loadLangSelection() {
        val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val langCode = prefs.getString(KEY_LANGUAGE, "system") ?: "system"
        selLang = when (langCode) {
            "en" -> 0
            "ru" -> 1
            "de" -> 2
            else -> 0
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        screenW = width.toFloat(); screenH = height.toFloat()
        setupLayout()
        splashStart = System.currentTimeMillis()
        start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {}
    override fun surfaceDestroyed(holder: SurfaceHolder) { stop() }

    private fun setupLayout() {
        tileSize = (screenW - 80f) / gridSize
        gridOffsetX = (screenW - tileSize * gridSize) / 2f
        gridOffsetY = 220f
        val btnStartX = (screenW - 420f) / 2f
        val btnY = screenH - 140f - 60f
        menuBtn = RectF(btnStartX, btnY, btnStartX + 420f, btnY + 140f)
        resetBtn = RectF(btnStartX, btnY + 170f, btnStartX + 420f, btnY + 310f)

        playBtn = RectF((screenW - 420f) / 2f, screenH / 2f - 120f, (screenW + 420f) / 2f, screenH / 2f - 20f)
        settingsBtn = RectF((screenW - 420f) / 2f, screenH / 2f + 10f, (screenW + 420f) / 2f, screenH / 2f + 110f)
        quitBtn = RectF((screenW - 420f) / 2f, screenH / 2f + 140f, (screenW + 420f) / 2f, screenH / 2f + 240f)
        backBtn = RectF((screenW - 420f) / 2f, screenH - 240f, (screenW + 420f) / 2f, screenH - 140f)

        settingsGridRects.clear(); settingsShuffleRects.clear(); settingsSpeedRects.clear(); settingsLangRects.clear()
        val optW = 150f; val optGap = 15f
        for (i in 0..2) {
            val totalW = 3 * optW + 2 * optGap
            val sx = (screenW - totalW) / 2f + i * (optW + optGap)
            settingsLangRects.add(RectF(sx, 200f, sx + optW, 260f))
            settingsGridRects.add(RectF(sx, 370f, sx + optW, 430f))
            settingsShuffleRects.add(RectF(sx, 540f, sx + optW, 600f))
            settingsSpeedRects.add(RectF(sx, 710f, sx + optW, 770f))
        }
    }

    fun start() { running = true; gameThread = Thread(this); gameThread?.start() }
    fun stop() { running = false; gameThread?.join() }

    override fun run() {
        while (running) {
            val canvas = holder.lockCanvas() ?: continue
            try { drawFrame(canvas) } finally { holder.unlockCanvasAndPost(canvas) }
            Thread.sleep(16)
        }
    }

    private fun drawFrame(canvas: Canvas) {
        canvas.drawColor(Color.rgb(30, 30, 45))
        when (currentState) {
            SPLASH -> drawSplash(canvas)
            MENU -> drawMenu(canvas)
            SETTINGS -> drawSettings(canvas)
            GAME -> drawGame(canvas)
        }
    }

    private fun drawSplash(canvas: Canvas) {
        val elapsed = (System.currentTimeMillis() - splashStart).toFloat()
        splashTitle = ((elapsed - 200f) / 600f).coerceIn(0f, 1f)
        splashSub = ((elapsed - 800f) / 600f).coerceIn(0f, 1f)
        for (i in 0 until 8) {
            val t = ((elapsed - 400f - i * 100f) / 400f).coerceIn(0f, 1f)
            splashScale[i] = t * t * (3f - 2f * t)
        }
        splashProgress = ((elapsed - 1800f) / 1200f).coerceIn(0f, 1f)

        paint.alpha = 12; paint.color = Color.rgb(52, 152, 219)
        canvas.drawCircle(-100f, screenH / 2f, 300f, paint)
        paint.color = Color.rgb(155, 89, 182)
        canvas.drawCircle(screenW + 100f, screenH / 2f - 100f, 250f, paint)
        paint.alpha = 255

        val ps = 100f; val pg = 12f
        val px = (screenW - (ps * 4 + pg * 3)) / 2f; val py = 350f
        for (i in 0 until 8) {
            if (splashScale[i] <= 0f) continue
            val cx = px + (i % 4) * (ps + pg) + ps / 2f
            val cy = py + (i / 4) * (ps + pg) + ps / 2f
            val size = ps * splashScale[i]
            paint.alpha = (splashScale[i] * 255).toInt()
            paint.color = TILE_COLORS[i]; rectF.set(cx - size / 2f, cy - size / 2f, cx + size / 2f, cy + size / 2f)
            canvas.drawRoundRect(rectF, 16f, 16f, paint)
            if (splashScale[i] > 0.6f) {
                paint.color = Color.WHITE; paint.textSize = 36f; paint.textAlign = Paint.Align.CENTER
                val fm = paint.fontMetrics; canvas.drawText("${i + 1}", cx, cy - (fm.ascent + fm.descent) / 2f, paint)
            }
            paint.alpha = 255
        }

        if (splashTitle > 0f) {
            paint.alpha = (splashTitle * 255).toInt(); paint.textAlign = Paint.Align.CENTER
            paint.textSize = 108f; paint.typeface = Typeface.DEFAULT_BOLD; paint.color = Color.WHITE
            canvas.drawText("15", screenW / 2f, 180f, paint)
            paint.textSize = 72f; canvas.drawText(ctx.getString(R.string.title_puzzle), screenW / 2f, 270f, paint)
            paint.alpha = 255
        }
        if (splashSub > 0f) {
            paint.alpha = (splashSub * 255).toInt(); paint.textSize = 32f; paint.typeface = Typeface.DEFAULT
            paint.color = Color.rgb(150, 150, 170); paint.textAlign = Paint.Align.CENTER
            canvas.drawText(ctx.getString(R.string.subtitle), screenW / 2f, 320f, paint)
            paint.alpha = 255
        }

        if (splashProgress > 0f) {
            val bx = (screenW - 500f) / 2f; val by = 600f
            paint.color = Color.rgb(44, 62, 80); rectF.set(bx, by, bx + 500f, by + 10f); canvas.drawRoundRect(rectF, 5f, 5f, paint)
            paint.color = Color.rgb(52, 152, 219); rectF.set(bx, by, bx + 500f * splashProgress, by + 10f); canvas.drawRoundRect(rectF, 5f, 5f, paint)
        }

        if (!splashDone && elapsed > 3500) { splashDone = true; currentState = MENU }
    }

    private fun drawMenu(canvas: Canvas) {
        paint.textAlign = Paint.Align.CENTER; paint.typeface = Typeface.DEFAULT_BOLD
        paint.color = Color.WHITE; paint.textSize = 96f
        canvas.drawText("15", screenW / 2f, 200f, paint)
        paint.textSize = 64f; canvas.drawText(ctx.getString(R.string.title_puzzle), screenW / 2f, 280f, paint)

        val ps = 60f; val pg = 6f; val px = (screenW - (ps * 4 + pg * 3)) / 2f; val py = 360f
        for (i in 0 until 8) {
            paint.color = TILE_COLORS[i]; val x = px + (i % 4) * (ps + pg); val y = py + (i / 4) * (ps + pg)
            rectF.set(x, y, x + ps, y + ps); canvas.drawRoundRect(rectF, 12f, 12f, paint)
            paint.color = Color.WHITE; paint.textSize = 28f; paint.textAlign = Paint.Align.CENTER
            val fm = paint.fontMetrics; canvas.drawText("${i + 1}", x + ps / 2f, y + ps / 2f - (fm.ascent + fm.descent) / 2f, paint)
        }

        paint.textSize = 48f
        drawBtn(canvas, playBtn, ctx.getString(R.string.btn_play), Color.rgb(52, 152, 219))
        drawBtn(canvas, settingsBtn, ctx.getString(R.string.btn_settings), Color.rgb(52, 73, 94))
        drawBtn(canvas, quitBtn, ctx.getString(R.string.btn_quit), Color.rgb(192, 57, 43))
    }

    private fun drawSettings(canvas: Canvas) {
        paint.textAlign = Paint.Align.CENTER; paint.typeface = Typeface.DEFAULT_BOLD
        paint.color = Color.WHITE; paint.textSize = 72f
        canvas.drawText(ctx.getString(R.string.settings_title), screenW / 2f, 100f, paint)

        paint.textSize = 32f; paint.typeface = Typeface.DEFAULT; paint.color = Color.rgb(180, 180, 200)

        canvas.drawText(ctx.getString(R.string.setting_language), screenW / 2f, 175f, paint)
        val langLabels = arrayOf("English", "Русский", "Deutsch")
        for (i in 0..2) {
            paint.color = if (i == selLang) Color.rgb(52, 152, 219) else Color.rgb(55, 60, 75)
            canvas.drawRoundRect(settingsLangRects[i], 12f, 12f, paint)
            paint.color = Color.WHITE; paint.textSize = 26f; paint.textAlign = Paint.Align.CENTER
            val fm = paint.fontMetrics; canvas.drawText(langLabels[i], settingsLangRects[i].centerX(), settingsLangRects[i].centerY() - (fm.ascent + fm.descent) / 2f, paint)
        }

        canvas.drawText(ctx.getString(R.string.setting_grid_size), screenW / 2f, 345f, paint)
        for (i in 0..2) {
            paint.color = if (i == selGrid) Color.rgb(52, 152, 219) else Color.rgb(55, 60, 75)
            canvas.drawRoundRect(settingsGridRects[i], 12f, 12f, paint)
            paint.color = Color.WHITE; paint.textSize = 32f; paint.textAlign = Paint.Align.CENTER
            val fm = paint.fontMetrics; canvas.drawText("${gridOptions[i]}×${gridOptions[i]}", settingsGridRects[i].centerX(), settingsGridRects[i].centerY() - (fm.ascent + fm.descent) / 2f, paint)
        }

        canvas.drawText(ctx.getString(R.string.setting_shuffle_steps), screenW / 2f, 515f, paint)
        for (i in 0..2) {
            paint.color = if (i == selShuffle) Color.rgb(52, 152, 219) else Color.rgb(55, 60, 75)
            canvas.drawRoundRect(settingsShuffleRects[i], 12f, 12f, paint)
            paint.color = Color.WHITE; paint.textSize = 32f; paint.textAlign = Paint.Align.CENTER
            val fm = paint.fontMetrics; canvas.drawText("${shuffleOptions[i]}", settingsShuffleRects[i].centerX(), settingsShuffleRects[i].centerY() - (fm.ascent + fm.descent) / 2f, paint)
        }

        canvas.drawText(ctx.getString(R.string.setting_animation), screenW / 2f, 685f, paint)
        for (i in 0..2) {
            paint.color = if (i == selSpeed) Color.rgb(52, 152, 219) else Color.rgb(55, 60, 75)
            canvas.drawRoundRect(settingsSpeedRects[i], 12f, 12f, paint)
            paint.color = Color.WHITE; paint.textSize = 24f; paint.textAlign = Paint.Align.CENTER
            val fm = paint.fontMetrics; canvas.drawText(ctx.getString(speedLabels[i]), settingsSpeedRects[i].centerX(), settingsSpeedRects[i].centerY() - (fm.ascent + fm.descent) / 2f, paint)
        }

        drawBtn(canvas, backBtn, ctx.getString(R.string.btn_back), Color.rgb(52, 73, 94))
    }

    private fun drawGame(canvas: Canvas) {
        paint.textAlign = Paint.Align.LEFT; paint.typeface = Typeface.DEFAULT_BOLD
        paint.color = Color.WHITE; paint.textSize = 48f
        canvas.drawText(ctx.getString(R.string.moves_format, controller.movesCount), 40f, 100f, paint)
        paint.textSize = 42f
        if (controller.isSolved) {
            paint.color = Color.rgb(46, 204, 113)
            canvas.drawText(ctx.getString(R.string.status_solved), 40f, 155f, paint)
        } else {
            paint.color = Color.LTGRAY
            canvas.drawText(ctx.getString(R.string.status_playing), 40f, 155f, paint)
        }

        paint.color = Color.rgb(44, 62, 80)
        rectF.set(gridOffsetX - 8f, gridOffsetY - 8f, gridOffsetX + tileSize * gridSize + 8f, gridOffsetY + tileSize * gridSize + 8f)
        canvas.drawRoundRect(rectF, 16f, 16f, paint)

        val state = controller.state
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                val id = state.grid[r][c] ?: continue
                if (id == 0) continue
                if (isAnimating && id == animTileId) continue
                val x = gridOffsetX + c * tileSize + 4f; val y = gridOffsetY + r * tileSize + 4f; val s = tileSize - 8f
                paint.color = TILE_COLORS[(id - 1) % TILE_COLORS.size]; rectF.set(x, y, x + s, y + s); canvas.drawRoundRect(rectF, 12f, 12f, paint)
                paint.color = Color.WHITE; paint.textSize = (tileSize * 0.4f).coerceAtMost(60f); paint.textAlign = Paint.Align.CENTER
                val fm = paint.fontMetrics; canvas.drawText(id.toString(), x + s / 2f, y + s / 2f - (fm.ascent + fm.descent) / 2f, paint)
            }
        }

        if (isAnimating) {
            val t = ((System.currentTimeMillis() - animST).toFloat() / animSpeed).coerceIn(0f, 1f)
            val sm = t * t * (3f - 2f * t)
            val cx = animSX + (animEX - animSX) * sm; val cy = animSY + (animEY - animSY) * sm
            val x = cx + 4f; val y = cy + 4f; val s = tileSize - 8f
            paint.color = TILE_COLORS[(animTileId - 1) % TILE_COLORS.size]; rectF.set(x, y, x + s, y + s); canvas.drawRoundRect(rectF, 12f, 12f, paint)
            paint.color = Color.WHITE; paint.textSize = (tileSize * 0.4f).coerceAtMost(60f); paint.textAlign = Paint.Align.CENTER
            val fm = paint.fontMetrics; canvas.drawText(animTileId.toString(), x + s / 2f, y + s / 2f - (fm.ascent + fm.descent) / 2f, paint)
            if (t >= 1f) isAnimating = false
        }

        drawBtn(canvas, menuBtn, ctx.getString(R.string.btn_menu), Color.rgb(52, 73, 94))
        drawBtn(canvas, resetBtn, ctx.getString(R.string.btn_reset), Color.rgb(52, 73, 94))
    }

    private fun drawBtn(canvas: Canvas, rect: RectF, label: String, color: Int) {
        paint.color = color; canvas.drawRoundRect(rect, 18f, 18f, paint)
        paint.color = Color.WHITE; paint.textAlign = Paint.Align.CENTER
        val fm = paint.fontMetrics; canvas.drawText(label, rect.centerX(), rect.centerY() - (fm.ascent + fm.descent) / 2f, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN) return true
        val x = event.x; val y = event.y

        when (currentState) {
            SPLASH -> { splashStart -= 5000; splashDone = true; currentState = MENU }
            MENU -> {
                when {
                    playBtn.contains(x, y) -> {
                        gridSize = gridOptions[selGrid]; shuffleSteps = shuffleOptions[selShuffle]; animSpeed = speedValues[selSpeed]
                        controller = GameController(gridSize, shuffleSteps); setupLayout(); controller.shuffle(); isAnimating = false; currentState = GAME
                    }
                    settingsBtn.contains(x, y) -> currentState = SETTINGS
                    quitBtn.contains(x, y) -> activity.finish()
                }
            }
            SETTINGS -> {
                when {
                    backBtn.contains(x, y) -> currentState = MENU
                    else -> {
                        for (i in 0..2) {
                            if (settingsLangRects[i].contains(x, y)) {
                                val langCodes = arrayOf("en", "ru", "de")
                                selLang = i
                                activity.setLanguage(langCodes[i])
                                return true
                            }
                            if (settingsGridRects[i].contains(x, y)) { selGrid = i; return true }
                            if (settingsShuffleRects[i].contains(x, y)) { selShuffle = i; return true }
                            if (settingsSpeedRects[i].contains(x, y)) { selSpeed = i; return true }
                        }
                    }
                }
            }
            GAME -> {
                if (isAnimating) return true
                when {
                    menuBtn.contains(x, y) -> currentState = MENU
                    resetBtn.contains(x, y) -> { controller.reset(); isAnimating = false }
                    else -> {
                        val col = ((x - gridOffsetX) / tileSize).toInt()
                        val row = ((y - gridOffsetY) / tileSize).toInt()
                        if (row in 0 until gridSize && col in 0 until gridSize) tryMove(row, col)
                    }
                }
            }
        }
        return true
    }

    private fun tryMove(targetRow: Int, targetCol: Int): Boolean {
        if (targetRow !in 0 until gridSize || targetCol !in 0 until gridSize) return false
        if (isAnimating) return false
        val (er, ec) = controller.state.emptyPosition()
        if (abs(targetRow - er) + abs(targetCol - ec) != 1) return false
        val tileId = controller.state.grid[targetRow][targetCol] ?: return false
        if (!controller.moveTile(targetRow, targetCol)) return false
        isAnimating = true; animTileId = tileId
        animSX = gridOffsetX + targetCol * tileSize; animSY = gridOffsetY + targetRow * tileSize
        animEX = gridOffsetX + ec * tileSize; animEY = gridOffsetY + er * tileSize
        animST = System.currentTimeMillis()
        return true
    }
}
