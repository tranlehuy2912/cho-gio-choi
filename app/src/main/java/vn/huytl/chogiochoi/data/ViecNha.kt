package vn.huytl.chogiochoi.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

/**
 * Viec nha ba giao cho chau, va cai khoa di kem ben tablet.
 *
 * CACH CHAY: ba bam mot hay nhieu viec. Tablet khoa man hinh cho den khi ba bam
 * "Xong" cho tung viec. Xong het thi may mo va cong so phut cua cac viec do.
 *
 * KHAC NUT CHO GIO O HAI CHO. Nut cho gio chi mot lan moi ngay, con viec nha thi
 * ba giao bao nhieu lan cung duoc - do la viec that trong nha, khong phai mot suat
 * uu dai. Va nut cho gio bam xong la het viec, con viec nha thi phai theo den luc
 * chau lam xong.
 *
 * TEN VIEC GUI KEM SANG TABLET chu khong dat ma cung o hai dau: Ba Huy sua danh
 * sach viec o day, ma sua xong thi tablet phai goi dung ten moi ngay chu khong phai
 * cai lai ca hai may.
 *
 * GUI DI DAU: document hop/viecnha tren Firestore, xem [Kho.datViecNha]. Ca ban
 * trang thai ghi de moi lan ba bam, khong phai gui tung su kien.
 */
object ViecNha {

    /** Mot dau viec trong danh sach. */
    data class Viec(val ten: String, val phut: Int)

    /** Mot dau viec dang giao, kem trang thai. */
    data class DangLam(val ten: String, val phut: Int, val xong: Boolean)

    /** Danh sach mac dinh, sua duoc trong Cai dat. */
    val MAC_DINH = listOf(
        Viec("Quét nhà lau nhà", 10),
        Viec("Rửa chén", 10),
        Viec("Tập thể dục", 10),
        Viec("Tắm rửa", 10)
    )

    /**
     * Toi da bay nhieu viec mot phien.
     *
     * Khong con la gioi han ky thuat - document tren Firestore chua bao nhieu viec
     * cung duoc, khac han cho 255 ky tu cua mo ta nhom Telegram ngay truoc. Giu lai
     * vi mot ly do khac: man chan tren tablet ke ra het cac viec chua xong, va mot
     * danh sach dai hon nam dong thi chau doc khong vao.
     */
    const val TOI_DA_MOI_PHIEN = 5

    // ------------------------------------------------------------- danh sach

    fun danhSach(context: Context): List<Viec> {
        val chu = sp(context).getString(K_DANH_SACH, null) ?: return MAC_DINH
        return runCatching {
            val a = JSONArray(chu)
            (0 until a.length()).map { i ->
                val o = a.getJSONObject(i)
                Viec(o.getString("ten"), o.optInt("phut", 10))
            }
        }.getOrDefault(MAC_DINH)
    }

    fun datDanhSach(context: Context, cac: List<Viec>) {
        val a = JSONArray()
        cac.forEach { a.put(JSONObject().put("ten", it.ten).put("phut", it.phut)) }
        sp(context).edit().putString(K_DANH_SACH, a.toString()).commit()
    }

    /**
     * Ten viec dung duoc hay khong.
     *
     * Truoc kia con cam dau hai cham, dau phay va gach dung, vi ca danh sach phai
     * nen thanh mot dong chu de nhet vao mo ta nhom Telegram. Document tren Firestore
     * giu tung ten trong mot o rieng, nen khong con dau nao pha duoc khuon nua - ba
     * dat ten viec la "Rửa chén, lau bàn" cung khong sao.
     */
    fun tenHopLe(ten: String): Boolean = ten.isNotBlank()

    // ----------------------------------------------------------------- phien

    /** Phien dang giao, rong la khong co viec nao. */
    fun dangGiao(context: Context): List<DangLam> {
        val chu = sp(context).getString(K_PHIEN, null) ?: return emptyList()
        return runCatching {
            val a = JSONArray(chu)
            (0 until a.length()).map { i ->
                val o = a.getJSONObject(i)
                DangLam(o.getString("ten"), o.optInt("phut"), o.optBoolean("xong"))
            }
        }.getOrDefault(emptyList())
    }

    /** Ma phien hien tai. Doi ma nghia la mot dot giao viec moi. */
    fun maPhien(context: Context): String = sp(context).getString(K_MA, "").orEmpty()

    /** Ba giao mot dot viec moi. Sinh ma moi de tablet biet day khong phai dot cu. */
    fun giao(context: Context, cac: List<Viec>) {
        val ma = "%04x".format(Random.nextInt(0x10000))
        luu(context, ma, cac.map { DangLam(it.ten, it.phut, false) })
    }

    /** Ba bam xong mot viec. */
    fun danhDauXong(context: Context, ten: String) {
        val moi = dangGiao(context).map { if (it.ten == ten) it.copy(xong = true) else it }
        luu(context, maPhien(context), moi)
    }

    /** Ba bo mot viec da giao, vi bam nham hoac thoi khong bat lam nua. */
    fun boViec(context: Context, ten: String) {
        luu(context, maPhien(context), dangGiao(context).filterNot { it.ten == ten })
    }

    fun boHet(context: Context) {
        luu(context, maPhien(context), emptyList())
    }

    /** Xong het roi thi khong con gi de theo doi nua. */
    fun xoaPhien(context: Context) {
        sp(context).edit().remove(K_PHIEN).remove(K_MA).commit()
    }

    private fun luu(context: Context, ma: String, cac: List<DangLam>) {
        val a = JSONArray()
        cac.forEach {
            a.put(JSONObject().put("ten", it.ten).put("phut", it.phut).put("xong", it.xong))
        }
        sp(context).edit().putString(K_MA, ma).putString(K_PHIEN, a.toString()).commit()
    }

    private fun sp(context: Context) =
        context.getSharedPreferences("chogiochoi", Context.MODE_PRIVATE)

    private const val K_DANH_SACH = "viec_danh_sach"
    private const val K_PHIEN = "viec_phien"
    private const val K_MA = "viec_ma_phien"
}
