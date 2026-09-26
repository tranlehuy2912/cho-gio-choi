package vn.huytl.chogiochoi.data

import android.content.Context

/**
 * Thu duy nhat app nay phai nho giua cac lan mo: nha nao tren Firestore.
 *
 * Ma nha lay duoc luc ghep doi, sau do khong doi nua. Khong con token bot nao o
 * day ca - truoc kia app phai cam token de sua mo ta nhom Telegram, va do la cho
 * yeu nhat cua ca he: ai rut duoc file APK ra khoi may ba la nam con bot, tuc la
 * doc duoc chat gia dinh va go duoc moi lenh sang tablet. Bay gio may ba chi co
 * mot uid an danh, ma uid do nam trong uidsPhu: khong khoa may, khong go app, khong
 * doc duoc chat.
 */
object Nha {

    fun maNha(context: Context): String = sp(context).getString(K_NHA, "").orEmpty()

    fun datMaNha(context: Context, ma: String) {
        sp(context).edit().putString(K_NHA, ma.trim()).commit()
    }

    fun daGhep(context: Context): Boolean = maNha(context).isNotEmpty()

    private fun sp(context: Context) =
        context.getSharedPreferences("chogiochoi", Context.MODE_PRIVATE)

    private const val K_NHA = "ma_nha"
}
