package vn.huytl.chogiochoi.data

import android.content.Context
import org.json.JSONArray
import kotlin.random.Random

/**
 * Viec nha giao cho chau, va cai khoa di kem ben tablet.
 *
 * CACH CHAY: ba bam mot hay nhieu viec. Tablet khoa man hinh cho den khi ba bam
 * "Xong" cho tung viec. Xong het thi may mo va cong so phut cua cac viec do.
 *
 * GIAO BAO NHIEU LAN CUNG DUOC, khong gioi han moi ngay nhu sau nut cho gio cua ban
 * app truoc: do la viec that trong nha, khong phai mot suat uu dai.
 *
 * TEN VIEC GUI KEM SANG TABLET chu khong dat ma cung o hai dau: Ba Huy sua danh
 * sach viec, ma sua xong thi tablet phai goi dung ten moi ngay chu khong phai cai
 * lai ca hai may.
 *
 * BA HUY CUNG GIAO VA BAM XONG DUOC, tren app Bang dieu khien. Nen dot viec khong
 * con nam trong may nay nua: ban truoc giu dot viec o day roi ghi de ca ban len
 * Firestore moi lan ba bam, va chi nhin Firestore de biet document con hay mat.
 * Hai may cung ghi theo kieu do thi may nay khong thay viec Ba Huy giao, va cai
 * bam tiep theo cua ba xoa mat viec Ba Huy vua bao xong. Bay gio man hinh ve theo
 * document hop/viecnha, moi lan bam la mot transaction, xem [Kho.giaoViec].
 *
 * Danh sach viec de chon cung vay: nam o hop/danhsachviec, Ba Huy sua tren Bang
 * dieu khien. Danh sach trong may chi con de dung tam, xem [danhSachTrongMay].
 */
object ViecNha {

    /** Mot dau viec trong danh sach de chon. */
    data class Viec(val ten: String, val phut: Int)

    /** Mot dau viec dang giao, kem trang thai. */
    data class DangLam(val ten: String, val phut: Int, val xong: Boolean)

    /**
     * Mot dot viec dang giao, doc tu hop/viecnha.
     *
     * @param ai nguoi giao dot nay, [Nguoi.BA_NOI] hay [Nguoi.BA_HUY]
     * @param luc lan cuoi co nguoi bam, theo gio cua may bam. Tablet lay moc nay de
     *   bo mot dot giao tu hon nua tieng truoc ma no chua tung thay.
     */
    data class Dot(val maPhien: String, val luc: Long, val ai: String, val cac: List<DangLam>) {

        /** Xong het ma document con do nghia la tablet chua nhan. */
        val xongHet: Boolean get() = cac.isNotEmpty() && cac.all { it.xong }

        fun xong(ten: String, bayGio: Long) =
            copy(luc = bayGio, cac = cac.map { if (it.ten == ten) it.copy(xong = true) else it })

        fun bo(ten: String, bayGio: Long) = copy(luc = bayGio, cac = cac.filterNot { it.ten == ten })

        /** Danh sach rong la cach noi "bo het" voi tablet. Tablet thay thi xoa document. */
        fun boHet(bayGio: Long) = copy(luc = bayGio, cac = emptyList())

        /** Chi doi moc luc, de tablet dang bo qua vi qua cu thi nhan lai. */
        fun guiLai(bayGio: Long) = copy(luc = bayGio)
    }

    /** Danh sach mac dinh, khi chua co danh sach nao khac. */
    val MAC_DINH = listOf(
        Viec("Quét nhà lau nhà", 10),
        Viec("Rửa chén", 10),
        Viec("Tập thể dục", 10),
        Viec("Tắm rửa", 10)
    )

    // ------------------------------------------------------------- danh sach

    /**
     * Danh sach dang nam trong may nay.
     *
     * Ban app truoc cho Ba Huy sua danh sach ngay tren may ba, va danh sach do nam o
     * day. Gio no con hai viec: dung tam khi chua doc duoc danh sach chung (chua dan
     * luat Firestore moi, hay may chua tung co mang); va la ban may nay gui len lan
     * dau khi Firestore chua co danh sach nao, de nhung viec Ba Huy da sua khong mat.
     */
    fun danhSachTrongMay(context: Context): List<Viec> {
        val chu = sp(context).getString(K_DANH_SACH, null) ?: return MAC_DINH
        return runCatching {
            val a = JSONArray(chu)
            (0 until a.length()).map { i ->
                val o = a.getJSONObject(i)
                Viec(o.getString("ten"), o.optInt("phut", 10))
            }
        }.getOrDefault(MAC_DINH)
    }

    /** Doc truong [Duong.F_VIEC] cua hop/danhsachviec. Muc thieu ten thi bo. */
    fun docDanhSach(cac: List<*>?): List<Viec> =
        cac.orEmpty().filterIsInstance<Map<*, *>>().mapNotNull { o ->
            val ten = (o[Duong.F_TEN] as? String)?.trim()?.takeIf { it.isNotEmpty() }
                ?: return@mapNotNull null
            Viec(ten, docPhut(o[Duong.F_PHUT]))
        }

    fun banDanhSach(cac: List<Viec>): List<Map<String, Any>> =
        cac.map { mapOf(Duong.F_TEN to it.ten, Duong.F_PHUT to it.phut) }

    // ---------------------------------------------------------------- dot viec

    /**
     * Doc document hop/viecnha. null la khong co dot nao.
     *
     * Document con ma danh sach rong thi van tra ve mot dot: do la luc da bo het va
     * tablet chua kip xoa. Ben ve man hinh coi no nhu khong co gi, con ben giao dot
     * moi thi duoc ghi de len.
     */
    fun docDot(du: Map<String, Any?>?): Dot? {
        if (du == null) return null
        val ma = (du[Duong.F_MA_PHIEN] as? String).orEmpty()
        if (ma.isBlank()) return null
        val cac = (du[Duong.F_VIEC] as? List<*>).orEmpty().filterIsInstance<Map<*, *>>()
            .mapNotNull { o ->
                val ten = (o[Duong.F_TEN] as? String)?.trim()?.takeIf { it.isNotEmpty() }
                    ?: return@mapNotNull null
                DangLam(ten, docPhut(o[Duong.F_PHUT]), o[Duong.F_XONG] == true)
            }
        return Dot(
            maPhien = ma,
            luc = (du[Duong.F_LUC] as? Number)?.toLong() ?: 0L,
            // Thieu la ban cu cua chinh app nay ghi: truoc do chi may ba giao viec.
            ai = du[Duong.F_AI] as? String ?: Nguoi.BA_NOI,
            cac = cac
        )
    }

    /** Ban ghi xuong hop/viecnha. Luon ghi ca ban, khong merge. */
    fun banGhi(d: Dot): Map<String, Any> = mapOf(
        Duong.F_MA_PHIEN to d.maPhien,
        Duong.F_LUC to d.luc,
        Duong.F_AI to d.ai,
        Duong.F_VIEC to d.cac.map {
            mapOf(Duong.F_TEN to it.ten, Duong.F_PHUT to it.phut, Duong.F_XONG to it.xong)
        }
    )

    /**
     * Mot dot moi, voi ma phien moi de tablet biet day khong phai dot cu.
     *
     * Tam chu so hex chu khong phai bon: tablet nho ma cua dot vua khep de khong cong
     * gio hai lan, nen mot dot moi trung ma dot cu se bi xoa ngay ma khong khoa may.
     */
    fun dotMoi(cac: List<Viec>, ai: String, bayGio: Long) = Dot(
        maPhien = "%08x".format(Random.nextInt()),
        luc = bayGio,
        ai = ai,
        cac = cac.map { DangLam(it.ten, it.phut, false) }
    )

    /** Tablet cung keo so phut ve khoang nay, xem ViecNha.tuBan ben nop-bai. */
    private fun docPhut(v: Any?): Int = ((v as? Number)?.toInt() ?: 0).coerceIn(0, 240)

    private fun sp(context: Context) =
        context.getSharedPreferences("chogiochoi", Context.MODE_PRIVATE)

    private const val K_DANH_SACH = "viec_danh_sach"
}
