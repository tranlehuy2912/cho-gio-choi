package vn.huytl.chogiochoi.telegram

import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/** Loi goi len Telegram, da doi sang cau tieng Viet doc duoc. */
class BotLoi(val viSao: String) : IOException(viSao)

/**
 * Phan goi mang, mong nhat co the.
 *
 * App nay khong nghe Telegram, chi noi: mot lan bam la vai request roi thoi. Nen
 * o day khong co long-poll, khong co service, khong co gi chay nen - dong app la
 * het, may ba khong ton giot pin nao.
 */
class Bot(private val token: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    /** Dat mo ta nhom. Day moi la hop thu that, tablet doc o day truoc tien. */
    fun datMoTaNhom(chatId: Long, moTa: String) {
        goi("setChatDescription", mapOf("chat_id" to chatId.toString(), "description" to moTa))
    }

    /** Gui tin vao nhom, tra ve message_id. */
    fun guiTin(chatId: Long, text: String): Long =
        goi("sendMessage", mapOf("chat_id" to chatId.toString(), "text" to text))
            .optLong("message_id", 0L)

    /**
     * Ghim tin vua gui.
     *
     * disable_notification de khong hu con ca nha moi lan ba cho gio. Tin ghim la
     * duong du phong: neu quyen sua mo ta nhom bi tat thi tablet con cho nay de doc.
     */
    fun ghimTin(chatId: Long, messageId: Long) {
        goi(
            "pinChatMessage",
            mapOf(
                "chat_id" to chatId.toString(),
                "message_id" to messageId.toString(),
                "disable_notification" to "true"
            )
        )
    }

    /** Go ghim mot tin cu. Xem [vn.huytl.chogiochoi.data.GhimCu] de biet vi sao can. */
    fun goGhim(chatId: Long, messageId: Long) {
        goi(
            "unpinChatMessage",
            mapOf("chat_id" to chatId.toString(), "message_id" to messageId.toString())
        )
    }

    /** Hoi ten con bot. Dung luc cai dat de biet token con song hay da bi thu hoi. */
    fun xemBot(): String = goi("getMe", emptyMap()).optString("username")

    /** Hoi thong tin nhom, dung luc cai dat de biet ma nhom go dung chua. */
    fun xemNhom(chatId: Long): JSONObject =
        goi("getChat", mapOf("chat_id" to chatId.toString()))

    private fun goi(method: String, params: Map<String, String>): JSONObject {
        val body = FormBody.Builder().apply {
            params.forEach { (k, v) -> add(k, v) }
        }.build()
        val req = Request.Builder()
            .url("https://api.telegram.org/bot$token/$method")
            .post(body)
            .build()

        val raw = try {
            client.newCall(req).execute().use { it.body?.string().orEmpty() }
        } catch (e: IOException) {
            throw BotLoi("Máy không vào được mạng. Kiểm tra wifi hoặc 4G rồi thử lại.")
        }

        val json = runCatching { JSONObject(raw) }.getOrNull()
            ?: throw BotLoi("Telegram trả về câu lạ, thử lại sau một lúc.")
        if (json.optBoolean("ok")) return json.optJSONObject("result") ?: JSONObject()
        throw BotLoi(dichLoi(json.optString("description")))
    }

    /**
     * Doi cau loi cua Telegram sang cau chi duoc viec phai lam.
     *
     * Nguoi doc may cau nay la Ba Huy luc dang cai, khong phai ba noi - nhung cau
     * tieng Anh nguyen ban ("Bad Request: not enough rights") thi doc xong van
     * khong biet phai bam vao dau.
     */
    private fun dichLoi(goc: String): String = when {
        goc.contains("not enough rights", true) ||
            goc.contains("CHAT_ADMIN_REQUIRED", true) ->
            "Bot chưa đủ quyền trong nhóm. Vào nhóm, đặt bot làm quản trị viên, " +
                "bật \"Đổi thông tin nhóm\" và \"Ghim tin nhắn\"."

        goc.contains("chat not found", true) ->
            "Không tìm thấy nhóm. Mã nhóm gõ sai, hoặc bot chưa được thêm vào nhóm."

        goc.contains("bot was kicked", true) || goc.contains("bot is not a member", true) ->
            "Bot đã bị đá khỏi nhóm. Thêm lại bot vào nhóm rồi đặt làm quản trị viên."

        goc.contains("unauthorized", true) ->
            "Token bot sai hoặc đã bị thu hồi."

        goc.isBlank() -> "Telegram từ chối, không nói lý do. Thử lại sau một lúc."

        else -> "Telegram báo: $goc"
    }
}
