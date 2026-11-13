package com.tinybakery.rush.data.local

import android.content.Context
import com.tinybakery.rush.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class BakeryPreferences(context: Context) {
    private val preferences = context.getSharedPreferences("tiny_bakery_rush", Context.MODE_PRIVATE)

    suspend fun read(): BakeryProgress? = withContext(Dispatchers.IO) {
        preferences.getString("bakery_save_v1", null)?.let { decode(JSONObject(it)) }
    }

    suspend fun write(progress: BakeryProgress) = withContext(Dispatchers.IO) {
        check(preferences.edit().putString("bakery_save_v1", encode(progress).toString()).commit())
    }

    private fun encode(p: BakeryProgress) = JSONObject().apply {
        put("version", 1)
        put("wallet", p.wallet)
        put("day", p.currentDay)
        put("selected", p.selectedDay)
        put("chapter", p.chapter)
        put("completed", JSONArray(p.completedDays.sorted()))
        put("oven", p.ovenLevel)
        put("counter", p.counterLevel)
        put("best", p.bestCombo)
        put("next", p.nextSessionId)
        put("sound", p.settings.soundEnabled)
        put("music", p.settings.musicEnabled)
        put("session", p.session?.let(::encodeSession) ?: JSONObject.NULL)
        put("results", JSONArray().apply { p.results.forEach { put(encodeResult(it)) } })
    }

    private fun decode(o: JSONObject): BakeryProgress {
        require(o.getInt("version") == 1)
        val p = BakeryProgress(
            wallet = o.getInt("wallet"), currentDay = o.getInt("day"), selectedDay = o.getInt("selected"),
            chapter = o.getInt("chapter"), completedDays = o.getJSONArray("completed").let { a -> (0 until a.length()).map { a.getInt(it) }.toSet() },
            ovenLevel = o.getInt("oven"), counterLevel = o.getInt("counter"), bestCombo = o.getInt("best"),
            nextSessionId = o.getLong("next"), settings = BakerySettings(o.getBoolean("sound"), o.getBoolean("music")),
            session = if (o.isNull("session")) null else decodeSession(o.getJSONObject("session")),
            results = o.getJSONArray("results").objects().map(::decodeResult)
        )
        require(p.wallet >= 0 && p.currentDay in 1..60 && p.selectedDay in 1..p.currentDay && p.chapter in 0..5)
        require(p.ovenLevel in 0..3 && p.counterLevel in 0..2 && p.nextSessionId > 0L && p.completedDays.all { it in 1..60 })
        require(p.results.map { it.sessionId }.distinct().size == p.results.size)
        p.session?.let { s ->
            require(s.day in 1..p.currentDay && s.id < p.nextSessionId && s.shapeSteps in 0..3)
            require(s.ovens.size in 1..2 && s.served in 0..s.target && s.elapsedMs >= 0L)
            require(s.tray.size <= s.trayCapacity && s.orders.size <= s.queueCapacity)
        }
        return p
    }

    private fun encodeSession(s: DaySession) = JSONObject().apply {
        put("id", s.id); put("day", s.day); put("oven", s.ovenLevel); put("counter", s.counterLevel)
        put("elapsed", s.elapsedMs); put("spawnAt", s.nextSpawnMs); put("spawned", s.spawned)
        put("selected", s.selectedRecipe.name); put("dough", s.doughRecipe.name); put("steps", s.shapeSteps)
        put("nextItem", s.nextItemId); put("served", s.served); put("delivery", s.deliveryCoins)
        put("tips", s.tips); put("combo", s.combo); put("best", s.bestCombo)
        put("reason", s.finishReason?.name ?: JSONObject.NULL)
        put("selectedTray", s.selectedTrayId ?: JSONObject.NULL)
        put("orders", JSONArray().apply { s.orders.forEach { order ->
            put(JSONObject().apply {
                put("number", order.number); put("customer", order.customer.name); put("recipe", order.recipe.name)
                put("arrived", order.arrivedMs); put("patience", order.patienceMs)
            })
        } })
        put("ovens", JSONArray().apply { s.ovens.forEach { load ->
            put(load?.let { JSONObject().apply {
                put("item", it.itemId); put("recipe", it.recipe.name); put("age", it.ageMs); put("bake", it.bakeMs)
            } } ?: JSONObject.NULL)
        } })
        put("tray", JSONArray().apply { s.tray.forEach { item ->
            put(JSONObject().apply { put("id", item.id); put("recipe", item.recipe.name) })
        } })
    }

    private fun decodeSession(o: JSONObject) = DaySession(
        id = o.getLong("id"), day = o.getInt("day"), ovenLevel = o.getInt("oven"), counterLevel = o.getInt("counter"),
        elapsedMs = o.getLong("elapsed"), nextSpawnMs = o.getLong("spawnAt"), spawned = o.getInt("spawned"),
        selectedRecipe = RecipeId.valueOf(o.getString("selected")), doughRecipe = RecipeId.valueOf(o.getString("dough")),
        shapeSteps = o.getInt("steps"), nextItemId = o.getInt("nextItem"), served = o.getInt("served"),
        deliveryCoins = o.getInt("delivery"), tips = o.getInt("tips"), combo = o.getInt("combo"), bestCombo = o.getInt("best"),
        finishReason = if (o.isNull("reason")) null else FinishReason.valueOf(o.getString("reason")),
        selectedTrayId = if (o.isNull("selectedTray")) null else o.getInt("selectedTray"),
        orders = o.getJSONArray("orders").objects().map { CustomerOrder(it.getInt("number"), CustomerId.valueOf(it.getString("customer")), RecipeId.valueOf(it.getString("recipe")), it.getLong("arrived"), it.getLong("patience")) },
        ovens = o.getJSONArray("ovens").let { a -> (0 until a.length()).map { index ->
            if (a.isNull(index)) null else a.getJSONObject(index).let { OvenLoad(it.getInt("item"), RecipeId.valueOf(it.getString("recipe")), it.getLong("age"), it.getLong("bake")) }
        } },
        tray = o.getJSONArray("tray").objects().map { TrayItem(it.getInt("id"), RecipeId.valueOf(it.getString("recipe"))) }
    )

    private fun encodeResult(r: DayResult) = JSONObject().apply {
        put("id", r.sessionId); put("day", r.day); put("reason", r.reason.name); put("served", r.served)
        put("delivery", r.deliveryCoins); put("tips", r.tips); put("bonus", r.bonus); put("best", r.bestCombo)
        put("wallet", r.walletAfter); put("unlocked", r.unlockedDay); put("recipe", r.newRecipe?.name ?: JSONObject.NULL)
    }

    private fun decodeResult(o: JSONObject) = DayResult(
        o.getLong("id"), o.getInt("day"), FinishReason.valueOf(o.getString("reason")), o.getInt("served"),
        o.getInt("delivery"), o.getInt("tips"), o.getInt("bonus"), o.getInt("best"),
        o.getInt("wallet"), o.getInt("unlocked"), if (o.isNull("recipe")) null else RecipeId.valueOf(o.getString("recipe"))
    )

    private fun JSONArray.objects(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }
}
