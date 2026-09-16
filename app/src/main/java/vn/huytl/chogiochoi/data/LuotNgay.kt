package vn.huytl.chogiochoi.data

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Luat mot lan mot ngay, giu o phia may ba.
 *
 * Ben tablet cung dem lai mot lan nua. Hai dau cung dem la co chu y: may ba go
 * cai dat, hay cai lai app, la so dem o day ve khong - luc do chi con ben tablet
 * chan. Nguoc lai tablet doi ngay luc nua dem con may ba co the lech mui gio,
 * nen de mot minh ben nao cung co ke ho.
 *
 * Moc doi ngay la nua dem theo dong ho may. Khong chong duoc viec chinh dong ho
 * may ba de bam them lan nua, va cung khong can: day la may cua nguoi lon.
 */
object LuotNgay {

    /** Hom nay ba da cho lan nao chua. */
    fun daCho(context: Context): Boolean = sp(context).getString(K_NGAY, "") == homNay()

    /** So phut cua lan da cho hom nay. 0 neu chua cho. */
    fun phutDaCho(context: Context): Int =
        if (daCho(context)) sp(context).getInt(K_PHUT, 0) else 0

    /** Luc bam nut, tinh bang milli giay. 0 neu chua cho. */
    fun lucCho(context: Context): Long =
        if (daCho(context)) sp(context).getLong(K_LUC, 0L) else 0L

    /**
     * Ghi nhan da cho [phut] phut hom nay.
     *
     * Goi TRUOC khi gui len Telegram, khong phai sau. Nghe thi nguoc doi, nhung
     * ghi sau co mot ke ho khong vá duoc bang cach nao khac o day: ba bam xong dong
     * app ngay, hay Android giet tien trinh, trong luc request dang bay. Luc do
     * lenh da len toi Telegram nhung dong ghi nay khong kip chay - mo lai app la
     * thay sau nut nhu chua cho gi. Ba bam lan hai, app bao "da cho 20 phut", ma
     * tablet lam ngo (ben do cung dem mot luot moi ngay) nen khong co gi xay ra ca.
     *
     * Ghi truoc thi tinh huong xau nhat la ghi roi ma lenh khong di - va cai do
     * [traLuot] go lai duoc, vi luc Telegram tu choi thi app van con song de nghe
     * cau tu choi do.
     *
     * Dung commit() chu khong phai apply(): apply() ghi xuong dia o luong khac, ma
     * ca doan nay chay dua voi viec tien trinh bi giet. Ghi xong roi moi goi mang.
     */
    fun ghiNhan(context: Context, phut: Int) {
        sp(context).edit()
            .putString(K_NGAY, homNay())
            .putInt(K_PHUT, phut)
            .putLong(K_LUC, System.currentTimeMillis())
            .commit()
    }

    /**
     * Tra lai luot cua hom nay.
     *
     * Chi goi khi Telegram tu choi ro rang - mat mang, sai ma hop thu, bot bi da
     * khoi nhom. Mat ca ngay vi mot lan rot song thi khong dang.
     */
    fun traLuot(context: Context) {
        sp(context).edit().remove(K_NGAY).remove(K_PHUT).remove(K_LUC).commit()
    }

    private fun homNay(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun sp(context: Context) =
        context.getSharedPreferences("chogiochoi", Context.MODE_PRIVATE)

    private const val K_NGAY = "ngay"
    private const val K_PHUT = "phut"
    private const val K_LUC = "luc"
}
