```kotlin
package com.example.leveldash

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

class GameView(ctx: Context) : View(ctx) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val prefs = ctx.getSharedPreferences("save", 0)

    private var screen = 0
    private var level = 1
    private var character = 0
    private var deaths = 0
    private var won = false

    private var px = 100f
    private var py = 0f
    private var vy = 0f
    private var moving = false
    private var jumpHeld = false
    private var t = 0f

    private val names = arrayOf("NOVA", "BOLT", "MINT", "SHADOW", "ROBO", "FLARE")

    private val colors = intArrayOf(
        Color.rgb(124, 77, 255),
        Color.rgb(0, 229, 255),
        Color.rgb(80, 220, 120),
        Color.rgb(80, 80, 95),
        Color.rgb(255, 190, 50),
        Color.rgb(255, 70, 120)
    )

    private val platforms = mutableListOf<RectF>()
    private val spikes = mutableListOf<RectF>()

    override fun onDraw(c: Canvas) {
        super.onDraw(c)

        p.style = Paint.Style.FILL
        c.drawColor(Color.rgb(8, 8, 18))

        when (screen) {
            0 -> menu(c)
            1 -> levels(c)
            2 -> characters(c)
            3 -> game(c)
        }
    }

    private fun text(
        c: Canvas,
        s: String,
        x: Float,
        y: Float,
        size: Float,
        color: Int,
        center: Boolean = false
    ) {
        p.typeface = Typeface.create("sans", Typeface.BOLD)
        p.textSize = size
        p.color = color
        p.textAlign = if (center) Paint.Align.CENTER else Paint.Align.LEFT
        c.drawText(s, x, y, p)
    }

    private fun round(
        c: Canvas,
        r: RectF,
        color: Int,
        rad: Float = 22f
    ) {
        p.color = color
        c.drawRoundRect(r, rad, rad, p)
    }

    private fun menu(c: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()

        text(c, "LEVEL DASH", w / 2f, 120f, 54f, Color.WHITE, true)
        text(c, "TRAPS AREN'T FAIR.", w / 2f, 158f, 18f, Color.rgb(170, 170, 190), true)

        round(
            c,
            RectF(w / 2f - 180f, 220f, w / 2f + 180f, 300f),
            Color.rgb(124, 77, 255)
        )
        text(c, "PLAY", w / 2f, 272f, 30f, Color.WHITE, true)

        round(
            c,
            RectF(w / 2f - 180f, 325f, w / 2f - 10f, 395f),
            Color.rgb(30, 30, 52)
        )

        round(
            c,
            RectF(w / 2f + 10f, 325f, w / 2f + 180f, 395f),
            Color.rgb(30, 30, 52)
        )

        text(c, "SKINS", w / 2f - 95f, 370f, 21f, Color.WHITE, true)
        text(c, "SETTINGS", w / 2f + 95f, 370f, 18f, Color.WHITE, true)

        text(
            c,
            "100 LEVELS  •  6 HEROES  •  5 WORLDS",
            w / 2f,
            450f,
            15f,
            Color.rgb(120, 120, 145),
            true
        )
    }

    private fun levels(c: Canvas) {
        val w = width.toFloat()

        text(c, "SELECT LEVEL", 40f, 65f, 30f, Color.WHITE)
        text(c, "WORLD 1  —  TRAINING", 40f, 105f, 16f, Color.rgb(160, 160, 180))

        val start = 145f

        for (i in 0 until 25) {
            val col = i % 5
            val row = i / 5

            val x = 40f + col * 145f
            val y = start + row * 72f

            val unlocked = i + 1 <= max(
                1,
                prefs.getInt("unlocked", 1)
            )

            round(
                c,
                RectF(x, y, x + 115f, y + 52f),
                if (unlocked) Color.rgb(28, 28, 48)
                else Color.rgb(17, 17, 28),
                15f
            )

            text(
                c,
                "${i + 1}",
                x + 57f,
                y + 34f,
                20f,
                if (unlocked) Color.WHITE else Color.DKGRAY,
                true
            )
        }

        text(
            c,
            "← BACK",
            40f,
            height.toFloat() - 25f,
            17f,
            Color.rgb(170, 170, 190)
        )
    }

    private fun characters(c: Canvas) {
        val w = width.toFloat()

        text(c, "HEROES", 40f, 65f, 30f, Color.WHITE)

        for (i in names.indices) {
            val x = 45f + (i % 3) * 235f
            val y = 105f + (i / 3) * 155f

            round(
                c,
                RectF(x, y, x + 205f, y + 125f),
                Color.rgb(25, 25, 45),
                18f
            )

            p.color = colors[i]
            c.drawCircle(x + 50f, y + 62f, 27f, p)

            text(c, names[i], x + 92f, y + 55f, 20f, Color.WHITE)

            text(
                c,
                if (i == 0 || prefs.getBoolean("hero$i", false))
                    "AVAILABLE"
                else
                    "LOCKED",
                x + 92f,
                y + 82f,
                13f,
                Color.rgb(150, 150, 170)
            )
        }

        text(
            c,
            "← BACK",
            40f,
            height.toFloat() - 25f,
            17f,
            Color.rgb(170, 170, 190)
        )
    }

    private fun setupLevel() {
        platforms.clear()
        spikes.clear()

        val h = height.toFloat()

        platforms += RectF(
            0f,
            h - 70f,
            width.toFloat(),
            h
        )

        val d = min(level, 100)

        var x = 180f

        val gap = 38f + min(
            55f,
            d * 0.45f
        )

        val count = 8 + min(
            8,
            d / 7
        )

        for (i in 0 until count) {
            val y = h - 115f - (i % 3) * 35f

            // ВАЖНО: 0f вместо 0, чтобы Kotlin получил Float
            val pw = 130f - max(
                0f,
                d * 0.45f
            )

            platforms += RectF(
                x,
                y,
                x + pw,
                y + 22f
            )

            if (i % 2 == 1 || d > 12) {
                spikes += RectF(
                    x + pw / 2f - 15f,
                    y - 20f,
                    x + pw / 2f + 15f,
                    y
                )
            }

            x += pw + gap

            if (x > width.toFloat() + 100f) {
                break
            }
        }

        px = 80f
        py = h - 120f
        vy = 0f
        moving = false
        won = false
    }

    private fun game(c: Canvas) {
        if (!moving) {
            setupLevel()
        }

        moving = true
        t += 1f

        val w = width.toFloat()
        val h = height.toFloat()

        val world = (level - 1) / 20

        p.color = when (world) {
            0 -> Color.rgb(12, 12, 28)
            1 -> Color.rgb(8, 20, 28)
            2 -> Color.rgb(25, 12, 25)
            3 -> Color.rgb(25, 20, 8)
            else -> Color.rgb(10, 8, 20)
        }

        c.drawRect(0f, 0f, w, h, p)

        for (r in platforms) {
            round(c, r, Color.rgb(45, 45, 70), 7f)
        }

        for (s in spikes) {
            p.color = Color.rgb(255, 70, 100)

            val path = Path()

            path.moveTo(s.centerX(), s.bottom)
            path.lineTo(s.left, s.top)
            path.lineTo(s.right, s.top)
            path.close()

            c.drawPath(path, p)
        }

        // goal
        val goal = RectF(
            w - 75f,
            h - 145f,
            w - 30f,
            h - 70f
        )

        round(
            c,
            goal,
            Color.rgb(80, 220, 140),
            8f
        )

        text(
            c,
            "EXIT",
            w - 52f,
            h - 155f,
            11f,
            Color.rgb(80, 220, 140),
            true
        )

        // character
        p.color = colors[character]
        c.drawCircle(px, py, 20f, p)

        text(
            c,
            names[character],
            px,
            py - 28f,
            11f,
            Color.WHITE,
            true
        )

        // physics
        vy += 0.8f
        py += vy

        if (py > h + 80f || px < -50f) {
            lose()
        }

        for (r in platforms) {
            if (
                px > r.left - 18f &&
                px < r.right + 18f &&
                py + 20f >= r.top &&
                py + 20f <= r.top + 28f &&
                vy >= 0f
            ) {
                py = r.top - 20f
                vy = 0f
            }
        }

        for (s in spikes) {
            if (
                abs(px - s.centerX()) < 22f &&
                py > s.top - 25f &&
                py < s.bottom + 15f
            ) {
                lose()
            }
        }

        if (px > w - 80f && py > h - 190f) {
            complete()
        }

        // controls
        round(
            c,
            RectF(
                25f,
                h - 65f,
                145f,
                h - 20f
            ),
            Color.argb(90, 255, 255, 255),
            14f
        )

        text(
            c,
            "MOVE",
            85f,
            h - 35f,
            14f,
            Color.WHITE,
            true
        )

        round(
            c,
            RectF(
                w - 155f,
                h - 85f,
                w - 25f,
                h - 20f
            ),
            Color.argb(90, 124, 77, 255),
            18f
        )

        text(
            c,
            "JUMP",
            w - 90f,
            h - 45f,
            16f,
            Color.WHITE,
            true
        )

        // ВАЖНО: 25f и 35f вместо Int
        text(
            c,
            "LEVEL $level  •  DEATHS $deaths",
            25f,
            35f,
            15f,
            Color.WHITE
        )
    }

    private fun lose() {
        deaths++
        px = 80f
        py = height.toFloat() - 120f
        vy = 0f
    }

    private fun complete() {
        if (won) return

        won = true

        val u = prefs.getInt("unlocked", 1)

        prefs.edit()
            .putInt(
                "unlocked",
                max(u, level + 1)
            )
            .apply()

        if (level < 100) {
            level++
        }

        moving = false
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val x = e
```
