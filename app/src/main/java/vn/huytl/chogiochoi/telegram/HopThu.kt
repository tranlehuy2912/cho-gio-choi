package vn.huytl.chogiochoi.telegram

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

/**
 * Cach may ba dat lenh vao cho tablet nhat.
 *
 * Tai sao phai vong veo the nay: tablet nghe lenh bang cach hoi Telegram
 * "co ai nhan gi cho bot khong" (getUpdates). Nhung mot con bot khong bao gio
 * thay tin nhan cua chinh no trong hang do - Telegram co tinh khong tra ve, de
 * hai con bot khong noi chuyen vong tron voi nhau. Nghia la app nay cam token
 * cung khong gia lam Ba Huy go lenh duoc.
 *
 * Nen doi huong: app nay khong gui lenh, no *dat* lenh xuong mot cho co dinh,
 * roi tablet ghe qua doc. Cho co dinh do la phan mo ta cua mot nhom Telegram
 * rieng - bot sua duoc bang setChatDescription va doc lai duoc bang getChat.
 *
 * Dat hai cho cho chac:
 *  - mo ta nhom: hop thu chinh, mot lan goi la xong, khong sinh tin nhan;
 *  - tin ghim: duong du phong, phong khi quyen doi thong tin nhom bi tat.
 *
 * Tin gui vao nhom con mot cong dung nua: nhom do thanh so ghi, Ba Huy mo ra la
 * thay ba da cho choi nhung hom nao, bao nhieu phut.
 */
object HopThu {

    /** Ket qua mot lan bam nut. */
    sealed interface KetQua {
        /**
         * Lenh da nam trong hop thu.
         *
         * [idGhim] la tin vua ghim, 0 la khong ghim duoc (luc do chi con duong mo
         * ta nhom). Ben goi giu lai so nay de lan sau go dung tin do xuong.
         */
        data class Xong(val ghimDuoc: Boolean, val idGhim: Long) : KetQua

        data class Hong(val viSao: String) : KetQua
    }

    /**
     * Dat lenh cho [phut] phut vao hop thu cua [nhom].
     *
     * Chay tren luong nen. Tra ve Hong khi lenh chac chan chua toi noi.
     */
    fun datLenh(bot: Bot, nhom: Long, phut: Int, tenCon: String, ghimCu: Long): KetQua {
        val giay = System.currentTimeMillis() / 1000L
        val ma = "%04x".format(Random.nextInt(0x10000))
        val dau = "$TU_KHOA|$phut|$giay|$ma"
        val luc = SimpleDateFormat("HH:mm 'ngày' dd/MM", Locale.forLanguageTag("vi-VN"))
            .format(Date())
        val loi = "Bà nội cho $tenCon chơi $phut phút — $luc."

        // Thu ca hai duong, thong duong nao cung duoc - tablet doc ca hai cho.
        //
        // Phai roi ra the nay vi hai kieu cho khac nhau: nhom thi sua duoc mo ta,
        // chat rieng thi khong (setChatDescription chi danh cho nhom va kenh) nhung
        // lai ghim duoc. Truoc day mo ta hong la bao hong luon, tuc la dat hop thu
        // vao chat rieng thi bam nut nao cung ra "chua gui duoc", trong khi tin ghim
        // van toi noi binh thuong.
        val loiMoTa = runCatching { bot.datMoTaNhom(nhom, "$dau $loi") }.exceptionOrNull()

        var idMoi = 0L
        val loiGhim = runCatching {
            val id = bot.guiTin(nhom, "$dau\n$loi")
            if (id == 0L) error("Telegram không trả về message_id.")
            bot.ghimTin(nhom, id)
            idMoi = id
            // Go ghim cu SAU khi ghim moi da xong, khong phai truoc. Go truoc ma ghim
            // moi hong thi hop thu rong tron mot luc - tablet ghe qua dung luc do la
            // khong thay gi. Go hong thi thoi, chi la thua mot dong ghim.
            if (ghimCu != 0L && ghimCu != id) runCatching { bot.goGhim(nhom, ghimCu) }
        }.exceptionOrNull()

        return when {
            loiMoTa == null || loiGhim == null ->
                KetQua.Xong(ghimDuoc = loiGhim == null, idGhim = idMoi)

            // Ca hai cung hong. Bao cau cua duong ghim: no la duong chay duoc o ca
            // hai kieu cho, nen loi cua no sat voi viec phai sua hon.
            else -> KetQua.Hong(viCauNguoiDoc(loiGhim))
        }
    }

    private fun viCauNguoiDoc(loi: Throwable): String =
        (loi as? BotLoi)?.viSao ?: loi.message ?: "Không rõ lỗi."

    /** Chu dau dong de tablet biet day la lenh chu khong phai mo ta nhom ai do vua go. */
    const val TU_KHOA = "CHOGIO"
}
