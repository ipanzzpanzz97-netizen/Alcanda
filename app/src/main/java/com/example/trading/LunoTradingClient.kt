package com.example.trading

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class LunoTicker(
    val pair: String,
    val baseAsset: String,
    val lastTrade: Double,
    val bid: Double,
    val ask: Double,
    val volume24h: Double,
    val status: String,
    val timestamp: Long
)

data class LunoCandle(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double
)

data class LunoOrderBookEntry(
    val price: Double,
    val volume: Double
)

data class LunoOrderBookTop(
    val bids: List<LunoOrderBookEntry>,
    val asks: List<LunoOrderBookEntry>
)

data class LunoWalletBalance(
    val asset: String,
    val balance: Double,
    val reserved: Double,
    val unconfirmed: Double
)

class LunoTradingClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://api.luno.com"

    suspend fun fetchMyrTickers(): List<LunoTicker> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/api/1/tickers")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Luno Tickers HTTP ${response.code}: ${extractLunoError(raw)}")
            }
            val json = JSONObject(raw)
            val arr = json.optJSONArray("tickers") ?: return@withContext emptyList()
            val out = ArrayList<LunoTicker>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val pair = obj.optString("pair", "")
                if (!pair.endsWith("MYR", ignoreCase = true)) continue
                val status = obj.optString("status", "ACTIVE")
                if (!status.equals("ACTIVE", ignoreCase = true)) continue
                val last = obj.optString("last_trade", "0").toDoubleOrNull() ?: 0.0
                val bid = obj.optString("bid", "0").toDoubleOrNull() ?: last
                val ask = obj.optString("ask", "0").toDoubleOrNull() ?: last
                val vol = obj.optString("rolling_24_hour_volume", "0").toDoubleOrNull() ?: 0.0
                val ts = obj.optLong("timestamp", 0L)
                val base = pair.removeSuffix("MYR").let {
                    if (it == "XBT") "BTC" else it
                }
                if (last > 0.0) {
                    out.add(
                        LunoTicker(
                            pair = pair,
                            baseAsset = base,
                            lastTrade = last,
                            bid = bid,
                            ask = ask,
                            volume24h = vol,
                            status = status,
                            timestamp = ts
                        )
                    )
                }
            }
            val priorityOrder = listOf("XBTMYR", "ETHMYR", "SOLMYR", "XRPMYR", "ADAMYR", "DOGEMYR", "APEMYR", "RENDERMYR", "BNBMYR")
            out.sortedWith(
                compareBy<LunoTicker> { ticker ->
                    val idx = priorityOrder.indexOf(ticker.pair)
                    if (idx >= 0) idx else 100
                }.thenBy { it.lastTrade }
            )
        }
    }

    suspend fun fetchCandles(
        pair: String,
        durationSec: Int = 3600,
        limit: Int = 60
    ): List<LunoCandle> = withContext(Dispatchers.IO) {
        val sinceMs = System.currentTimeMillis() - (limit.toLong() * durationSec.toLong() * 1000L)
        val url = "$baseUrl/api/1/candles?pair=$pair&duration=$durationSec&since=$sinceMs"
        val request = Request.Builder().url(url).get().build()

        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Luno Candles HTTP ${response.code}: ${extractLunoError(raw)}")
            }
            val json = JSONObject(raw)
            val arr = json.optJSONArray("candles") ?: return@withContext emptyList()
            val out = ArrayList<LunoCandle>(arr.length())
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val close = obj.optString("close", "0").toDoubleOrNull() ?: continue
                val open = obj.optString("open", "0").toDoubleOrNull() ?: close
                val high = obj.optString("high", "0").toDoubleOrNull() ?: close
                val low = obj.optString("low", "0").toDoubleOrNull() ?: close
                val vol = obj.optString("volume", "0").toDoubleOrNull() ?: 0.0
                val ts = obj.optLong("timestamp", 0L)
                out.add(LunoCandle(timestamp = ts, open = open, high = high, low = low, close = close, volume = vol))
            }
            out.sortedBy { it.timestamp }.takeLast(limit)
        }
    }

    suspend fun fetchOrderBookTop(pair: String): LunoOrderBookTop = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/api/1/orderbook_top?pair=$pair")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Luno OrderBook HTTP ${response.code}: ${extractLunoError(raw)}")
            }
            val json = JSONObject(raw)
            val bidsArr = json.optJSONArray("bids")
            val asksArr = json.optJSONArray("asks")
            val bids = ArrayList<LunoOrderBookEntry>()
            val asks = ArrayList<LunoOrderBookEntry>()
            if (bidsArr != null) {
                for (i in 0 until minOf(bidsArr.length(), 20)) {
                    val o = bidsArr.optJSONObject(i) ?: continue
                    val p = o.optString("price", "0").toDoubleOrNull() ?: continue
                    val v = o.optString("volume", "0").toDoubleOrNull() ?: 0.0
                    bids.add(LunoOrderBookEntry(p, v))
                }
            }
            if (asksArr != null) {
                for (i in 0 until minOf(asksArr.length(), 20)) {
                    val o = asksArr.optJSONObject(i) ?: continue
                    val p = o.optString("price", "0").toDoubleOrNull() ?: continue
                    val v = o.optString("volume", "0").toDoubleOrNull() ?: 0.0
                    asks.add(LunoOrderBookEntry(p, v))
                }
            }
            LunoOrderBookTop(bids = bids, asks = asks)
        }
    }

    suspend fun fetchPrivateBalances(
        apiKeyId: String,
        apiKeySecret: String
    ): List<LunoWalletBalance> = withContext(Dispatchers.IO) {
        if (apiKeyId.isBlank() || apiKeySecret.isBlank()) {
            error("Sila masukkan Luno API Key ID dan Secret dahulu.")
        }
        val request = Request.Builder()
            .url("$baseUrl/api/1/balance")
            .header("Authorization", Credentials.basic(apiKeyId.trim(), apiKeySecret.trim()))
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Luno Auth HTTP ${response.code}: ${extractLunoError(raw)}")
            }
            val json = JSONObject(raw)
            val arr = json.optJSONArray("balance") ?: return@withContext emptyList()
            val out = ArrayList<LunoWalletBalance>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val asset = obj.optString("asset", "")
                val bal = obj.optString("balance", "0").toDoubleOrNull() ?: 0.0
                val res = obj.optString("reserved", "0").toDoubleOrNull() ?: 0.0
                val unc = obj.optString("unconfirmed", "0").toDoubleOrNull() ?: 0.0
                if (asset.isNotBlank() && (bal > 0.0 || res > 0.0 || asset == "MYR")) {
                    out.add(LunoWalletBalance(asset, bal, res, unc))
                }
            }
            out.sortedByDescending { it.asset == "MYR" }
        }
    }

    private fun extractLunoError(raw: String): String {
        if (raw.isBlank()) return "Tiada maklum balas pelayan Luno."
        return runCatching {
            val obj = JSONObject(raw)
            obj.optString("error").ifBlank { raw.take(180) }
        }.getOrDefault(raw.take(180))
    }
}
