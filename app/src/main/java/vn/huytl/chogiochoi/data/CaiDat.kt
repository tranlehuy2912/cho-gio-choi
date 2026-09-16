package vn.huytl.chogiochoi.data

import android.content.Context

/**
 * Hai thu phai dat mot lan luc cai may: token bot va ma nhom lam hop thu.
 *
 * Tach khoi [LuotNgay] vi hai thu khac ban chat. Cai dat do Ba Huy dat mot lan
 * roi khong dong vao nua; luot ngay thi tu doi moi lan ba bam va tu ve khong moi
 * sang. De chung mot cho thi sau nay xoa luot lai deo theo nguy co xoa mat token.
 *
 * Token co san tu [Defaults] nen thuong khong phai go. O nhap van de day cho
 * truong hop doi bot, hoac ban APK cu con token da thu hoi.
 */
object CaiDat {

    // Hai o duoi ghi bang commit() chu khong phai apply(): chung duoc ghi rat hiem,
    // va mat mot trong hai la app khong gui duoc gi ma khong ai biet vi sao.

    /** Token bot. Chua dat tay thi lay ban nap san trong [Defaults]. */
    fun token(context: Context): String =
        sp(context).getString(K_TOKEN, null)?.takeIf { it.isNotBlank() } ?: Defaults.BOT_TOKEN

    fun datToken(context: Context, token: String) {
        sp(context).edit().putString(K_TOKEN, token.trim()).commit()
    }

    /** Ma so nhom Telegram dung lam hop thu, 0 la chua dat. */
    fun nhom(context: Context): Long {
        val luu = sp(context).getLong(K_NHOM, 0L)
        return if (luu != 0L) luu else Defaults.GROUP_CHAT_ID
    }

    fun datNhom(context: Context, id: Long) {
        sp(context).edit().putLong(K_NHOM, id).commit()
    }

    /** Da du de bam nut chua. */
    fun xong(context: Context): Boolean = nhom(context) != 0L && token(context).isNotBlank()

    private fun sp(context: Context) =
        context.getSharedPreferences("chogiochoi", Context.MODE_PRIVATE)

    private const val K_TOKEN = "token"
    private const val K_NHOM = "nhom"
}
