package com.example.widgets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SweepGradient
import android.graphics.Typeface
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

object WidgetArt {
    val GOLD = Color.parseColor("#F5C542")
    val GOLD_LIGHT = Color.parseColor("#FFE082")
    val CORAL = Color.parseColor("#D9534F")
    val CORAL_LIGHT = Color.parseColor("#FF8A80")
    val CARD_TOP = Color.parseColor("#5A2430")
    val CARD_BOT = Color.parseColor("#3B1620")
    val GREEN = Color.parseColor("#4CAF50")
    val INK = Color.parseColor("#2B0503")

    fun dp(ctx: Context, v: Float) = v * ctx.resources.displayMetrics.density

    private fun bmp(w: Int, h: Int): Pair<Bitmap, Canvas> {
        val b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        return b to Canvas(b)
    }

    private fun card(c: Canvas, w: Int, h: Int, r: Float) {
        val p = Paint().apply {
            isAntiAlias = true
            shader = LinearGradient(0f, 0f, 0f, h.toFloat(), intArrayOf(CARD_TOP, CARD_BOT), null, Shader.TileMode.CLAMP)
        }
        c.drawRoundRect(0f, 0f, w.toFloat(), h.toFloat(), r, r, p)
    }

    private fun text(c: Canvas, s: String, x: Float, y: Float, size: Float, color: Int, align: Paint.Align = Paint.Align.LEFT, bold: Boolean = true) {
        val p = Paint().apply {
            isAntiAlias = true
            this.color = color
            textSize = size
            textAlign = align
            typeface = if (bold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.DEFAULT
        }
        c.drawText(s, x, y, p)
    }

    private fun miniMascot(c: Canvas, cx: Float, cy: Float, s: Float, frame: Int, typing: Boolean) {
        val body = Paint().apply { isAntiAlias = true; color = Color.parseColor("#FFF3E4") }
        val r = s
        c.drawRoundRect(RectF(cx - r, cy - r * 0.9f, cx + r, cy + r), r * 0.9f, r, body)
        c.drawCircle(cx - r * 0.15f, cy - r * 0.85f, r * 0.28f, body)
        val leaf = Paint().apply { isAntiAlias = true; color = Color.parseColor("#66BB6A") }
        c.save(); c.translate(cx + r * 0.1f, cy - r * 1.05f); c.rotate(-30f)
        c.drawOval(RectF(-r * 0.3f, -r * 0.12f, r * 0.3f, r * 0.12f), leaf)
        c.restore()
        val ink = Paint().apply { isAntiAlias = true; color = Color.parseColor("#3E2723") }
        c.drawCircle(cx - r * 0.32f, cy - r * 0.15f, r * 0.09f, ink)
        c.drawCircle(cx + r * 0.32f, cy - r * 0.15f, r * 0.09f, ink)
        val blush = Paint().apply { isAntiAlias = true; color = Color.parseColor("#F48FB1") }
        c.drawCircle(cx - r * 0.5f, cy + r * 0.05f, r * 0.1f, blush)
        c.drawCircle(cx + r * 0.5f, cy + r * 0.05f, r * 0.1f, blush)
        val sm = Paint().apply { isAntiAlias = true; color = Color.parseColor("#3E2723"); style = Paint.Style.STROKE; strokeWidth = r * 0.07f; strokeCap = Paint.Cap.ROUND }
        c.drawArc(RectF(cx - r * 0.18f, cy - r * 0.05f, cx + r * 0.18f, cy + r * 0.25f), 20f, 140f, false, sm)
        if (typing) {
            val kb = Paint().apply { isAntiAlias = true; color = Color.parseColor("#B0BEC5") }
            val kr = RectF(cx - r * 0.95f, cy + r * 0.55f, cx + r * 0.95f, cy + r * 1.05f)
            c.drawRoundRect(kr, r * 0.12f, r * 0.12f, kb)
            val key = Paint().apply { color = Color.parseColor("#ECEFF1") }
            for (row in 0..1) for (col in 0..6) {
                val cw = (kr.width() - 8f) / 7f
                c.drawRect(kr.left + 4f + col * cw, kr.top + 3f + row * (kr.height() / 2f), kr.left + 4f + col * cw + cw - 2f, kr.top + 3f + row * (kr.height() / 2f) + kr.height() / 2f - 3f, key)
            }
            val up = if (frame % 2 == 0) 0f else r * 0.08f
            val dn = if (frame % 2 == 0) r * 0.08f else 0f
            c.drawCircle(cx - r * 0.4f, cy + r * 0.6f + up, r * 0.16f, body)
            c.drawCircle(cx + r * 0.4f, cy + r * 0.6f + dn, r * 0.16f, body)
        }
    }

    private fun flame(c: Canvas, x: Float, y: Float, s: Float, v: Int) {
        val wob = v * 0.3f
        val outer = Paint().apply {
            isAntiAlias = true
            shader = LinearGradient(x, y - s, x, y + s, intArrayOf(Color.parseColor("#FFDD2C00"), Color.parseColor("#FF7043")), null, Shader.TileMode.CLAMP)
        }
        val p = Path().apply {
            moveTo(x - s * 0.5f, y + s * 0.6f)
            cubicTo(x - s * 0.6f, y - s * 0.1f, x - s * 0.2f, y - s * (0.5f + wob * 0.2f), x + wob * 2f, y - s)
            cubicTo(x + s * 0.35f, y - s * 0.4f, x + s * 0.55f, y, x + s * 0.5f, y + s * 0.6f)
            close()
        }
        c.drawPath(p, outer)
        val core = Paint().apply { isAntiAlias = true; color = Color.parseColor("#FFEB3B") }
        val p2 = Path().apply {
            moveTo(x - s * 0.22f, y + s * 0.5f)
            cubicTo(x - s * 0.25f, y + s * 0.1f, x - s * 0.1f, y - s * 0.1f, x, y - s * 0.35f)
            cubicTo(x + s * 0.12f, y - s * 0.1f, x + s * 0.25f, y + s * 0.1f, x + s * 0.22f, y + s * 0.5f)
            close()
        }
        c.drawPath(p2, core)
    }

    fun renderMascot(ctx: Context, w: Int, h: Int, frame: Int, day: Int, quote: String): Bitmap {
        val (b, c) = bmp(w, h)
        card(c, w, h, dp(ctx, 24f))
        miniMascot(c, w * 0.26f, h * 0.40f, dp(ctx, 30f), frame, true)
        val pill = Paint().apply { isAntiAlias = true; shader = LinearGradient(0f, 0f, 0f, dp(ctx, 22f), intArrayOf(GOLD_LIGHT, GOLD), null, Shader.TileMode.CLAMP) }
        val pr = RectF(w * 0.60f, dp(ctx, 10f), w * 0.94f, dp(ctx, 32f))
        c.drawRoundRect(pr, dp(ctx, 11f), dp(ctx, 11f), pill)
        text(c, "Day $day", pr.centerX(), pr.centerY() + dp(ctx, 5f), dp(ctx, 12f), INK, Paint.Align.CENTER)
        val bub = Paint().apply { isAntiAlias = true; color = Color.parseColor("#6E3A47") }
        val br = RectF(w * 0.50f, h * 0.44f, w * 0.96f, h * 0.88f)
        c.drawRoundRect(br, dp(ctx, 12f), dp(ctx, 12f), bub)
        val tail = Path().apply { moveTo(w * 0.50f, h * 0.60f); lineTo(w * 0.44f, h * 0.65f); lineTo(w * 0.50f, h * 0.72f); close() }
        c.drawPath(tail, bub)
        val words = quote.split(" ")
        var l1 = ""; var l2 = ""
        for (wd in words) { if ((l1 + wd).length < 18) l1 += "$wd " else l2 += "$wd " }
        text(c, l1.trim(), br.centerX(), br.top + dp(ctx, 17f), dp(ctx, 10f), Color.WHITE, Paint.Align.CENTER, false)
        text(c, l2.trim(), br.centerX(), br.top + dp(ctx, 30f), dp(ctx, 10f), Color.WHITE, Paint.Align.CENTER, false)
        return b
    }

    fun renderTicker(ctx: Context, w: Int, h: Int, data: List<Pair<String, Float>>): Bitmap {
        val (b, c) = bmp(w, h)
        card(c, w, h, dp(ctx, 24f))
        var y = dp(ctx, 30f)
        for ((sym, ch) in data.take(2)) {
            text(c, sym, dp(ctx, 18f), y, dp(ctx, 16f), GOLD)
            val col = if (ch >= 0) GREEN else CORAL_LIGHT
            val sign = if (ch >= 0) "+" else ""
            text(c, "$sign%.1f%%".format(ch), dp(ctx, 82f), y, dp(ctx, 16f), col)
            y += dp(ctx, 26f)
        }
        val pts = floatArrayOf(0.7f, 0.55f, 0.62f, 0.4f, 0.5f, 0.28f, 0.35f, 0.12f, 0.05f)
        val path = Path()
        val x0 = w * 0.55f; val x1 = w * 0.94f; val y0 = h * 0.8f; val y1 = h * 0.18f
        pts.forEachIndexed { i, v ->
            val x = x0 + (x1 - x0) * i / (pts.size - 1)
            val yy = y1 + (y0 - y1) * v
            if (i == 0) path.moveTo(x, yy) else path.lineTo(x, yy)
        }
        val glow = Paint().apply { isAntiAlias = true; style = Paint.Style.STROKE; strokeWidth = dp(ctx, 7f); color = Color.argb(80, 245, 197, 66); strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND }
        c.drawPath(path, glow)
        val line = Paint().apply { isAntiAlias = true; style = Paint.Style.STROKE; strokeWidth = dp(ctx, 3f); shader = LinearGradient(x0, 0f, x1, 0f, intArrayOf(GOLD, GOLD_LIGHT), null, Shader.TileMode.CLAMP); strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND }
        c.drawPath(path, line)
        text(c, "Knowledge Market", w * 0.5f, h - dp(ctx, 12f), dp(ctx, 10f), Color.argb(160, 255, 255, 255), Paint.Align.CENTER, false)
        return b
    }

    fun renderDanger(ctx: Context, w: Int, h: Int, frame: Int, label: String): Bitmap {
        val (b, c) = bmp(w, h)
        card(c, w, h, dp(ctx, 24f))
        val cx = w / 2f; val cy = h / 2f; val r = min(w, h) * 0.30f
        val tick = Paint().apply { isAntiAlias = true; color = Color.argb(120, 255, 224, 130); strokeWidth = dp(ctx, 2f) }
        for (i in 0 until 12) {
            val a = i * 30f * PI.toFloat() / 180
            c.drawLine(cx + cos(a.toDouble()).toFloat() * r * 0.78f, cy + sin(a.toDouble()).toFloat() * r * 0.78f, cx + cos(a.toDouble()).toFloat() * r * 0.9f, cy + sin(a.toDouble()).toFloat() * r * 0.9f, tick)
        }
        for (i in 0 until 14) {
            val a = (i * 25.7f + frame * 9f) * PI.toFloat() / 180
            val fx = cx + cos(a) * r
            val fy = cy + sin(a) * r
            flame(c, fx, fy, dp(ctx, 5f + ((i + frame) % 3) * 2f), (i + frame) % 4)
        }
        text(c, label, cx, cy + dp(ctx, 6f), dp(ctx, 17f), GOLD_LIGHT, Paint.Align.CENTER)
        return b
    }

    fun renderFocus(ctx: Context, w: Int, h: Int, secs: Int, total: Int, running: Boolean, frame: Int): Bitmap {
        val (b, c) = bmp(w, h)
        card(c, w, h, dp(ctx, 24f))
        val t = String.format("%02d:%02d", secs / 60, secs % 60)
        text(c, t, dp(ctx, 20f), h * 0.48f, dp(ctx, 30f), GOLD_LIGHT)
        val pill = Paint().apply { isAntiAlias = true; shader = LinearGradient(0f, h * 0.58f, 0f, h * 0.58f + dp(ctx, 26f), intArrayOf(GOLD_LIGHT, GOLD), null, Shader.TileMode.CLAMP) }
        val pr = RectF(dp(ctx, 20f), h * 0.58f, dp(ctx, 20f) + dp(ctx, 118f), h * 0.58f + dp(ctx, 26f))
        c.drawRoundRect(pr, dp(ctx, 13f), dp(ctx, 13f), pill)
        text(c, if (running) "LOCKED IN" else "TAP TO START", pr.centerX(), pr.centerY() + dp(ctx, 5f), dp(ctx, 11f), INK, Paint.Align.CENTER)
        val cx = w * 0.76f; val cy = h * 0.5f; val r = h * 0.33f
        val bg = Paint().apply { isAntiAlias = true; style = Paint.Style.STROKE; strokeWidth = dp(ctx, 7f); color = Color.argb(60, 255, 255, 255) }
        c.drawCircle(cx, cy, r, bg)
        val prog = Paint().apply { isAntiAlias = true; style = Paint.Style.STROKE; strokeWidth = dp(ctx, 7f); strokeCap = Paint.Cap.ROUND; shader = SweepGradient(cx, cy, intArrayOf(CORAL, GOLD, CORAL), null) }
        val progress = if (total > 0) 1f - secs.toFloat() / total else 0f
        c.drawArc(RectF(cx - r, cy - r, cx + r, cy + r), -90f, 360f * progress.coerceIn(0f, 1f), false, prog)
        miniMascot(c, cx, cy - r * 0.1f, r * 0.5f, frame, true)
        return b
    }

    fun renderStreak(ctx: Context, w: Int, h: Int, frame: Int, days: Int): Bitmap {
        val (b, c) = bmp(w, h)
        val cx = w / 2f; val cy = h * 0.40f; val s = min(w, h) * 0.30f
        val spark = Paint().apply { isAntiAlias = true; color = GOLD_LIGHT }
        for (i in 0 until 7) {
            val a = (i * 51f + frame * 23f) * PI / 180
            val d = s * (1.25f + ((i + frame) % 3) * 0.18f)
            c.drawCircle(cx + cos(a) * d, cy + sin(a) * d * 0.8f, dp(ctx, 1.6f), spark)
        }
        flame(c, cx, cy, s * (1f + (frame % 2) * 0.05f), frame % 4)
        text(c, "$days Day Streak", cx, h - dp(ctx, 10f), dp(ctx, 13f), GOLD, Paint.Align.CENTER)
        return b
    }

    fun renderQuests(ctx: Context, w: Int, h: Int, quests: List<Triple<String, Int, Int>>): Bitmap {
        val (b, c) = bmp(w, h)
        card(c, w, h, dp(ctx, 24f))
        var y = dp(ctx, 30f)
        for ((title, cur, target) in quests.take(3)) {
            val pct = if (target > 0) (cur * 100 / target).coerceIn(0, 100) else 0
            text(c, title, dp(ctx, 18f), y, dp(ctx, 12f), Color.WHITE, Paint.Align.LEFT, false)
            val bx = w * 0.56f; val bw = w * 0.28f
            val bgp = Paint().apply { isAntiAlias = true; color = Color.argb(50, 255, 255, 255) }
            c.drawRoundRect(RectF(bx, y - dp(ctx, 11f), bx + bw, y - dp(ctx, 5f)), dp(ctx, 3f), dp(ctx, 3f), bgp)
            val fp = Paint().apply { isAntiAlias = true; shader = LinearGradient(bx, 0f, bx + bw, 0f, intArrayOf(CORAL, GOLD), null, Shader.TileMode.CLAMP) }
            c.drawRoundRect(RectF(bx, y - dp(ctx, 11f), bx + bw * pct / 100f, y - dp(ctx, 5f)), dp(ctx, 3f), dp(ctx, 3f), fp)
            text(c, "$pct%", w - dp(ctx, 16f), y, dp(ctx, 12f), Color.WHITE, Paint.Align.RIGHT)
            y += dp(ctx, 26f)
        }
        return b
    }
}
