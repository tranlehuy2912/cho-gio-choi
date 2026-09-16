package vn.huytl.chogiochoi.data

/**
 * Cau hinh nap san khi cai len may ba noi.
 *
 * DANH DOI: token bot nam thang trong file APK, giong het ben homework-gate. Ai
 * lay duoc file APK ra khoi may ba la doc duoc token, ma co token la dieu khien
 * duoc ca con bot. Chap nhan duoc vi may ba la may nguoi lon, khong phai may dang
 * bi khoa de ai do tim cach lach. Neu mot ngay nao do can chat hon, de trong
 * BOT_THAT trong local.properties va bat ba dien tay mot lan khi cai.
 *
 * Token khong con nam trong file nay: no doc tu local.properties qua BuildConfig,
 * ma local.properties thi nam trong .gitignore nen khong len git. Chep
 * local.properties.mau thanh local.properties roi dien vao.
 */
internal object Defaults {

    /**
     * Cung con bot voi ben tablet.
     *
     * Ban go loi dung bot may ao, ban that dung bot cua Ba Huy - dung y nhu ben
     * homework-gate, de cai ban go loi len may that khong ban vao hop thu that.
     *
     * Hai token dien o local.properties: BOT_THAT va BOT_MAY_AO. Dien y het hai
     * dong ben homework-gate, vi hai app phai chung mot con bot.
     */
    val BOT_TOKEN: String = if (vn.huytl.chogiochoi.BuildConfig.DEBUG) {
        vn.huytl.chogiochoi.BuildConfig.BOT_MAY_AO
    } else {
        vn.huytl.chogiochoi.BuildConfig.BOT_THAT
    }

    /**
     * Nhom Telegram dung lam hop thu. 0 la chua dat.
     *
     * De 0 o day thi lan dau mo app se hoi, Ba Huy dan vao mot lan roi thoi - so
     * nay luu lai trong may. Dien san vao day cung duoc neu muon cai xong la chay.
     */
    const val GROUP_CHAT_ID = 0L

    /** Sau nut tren man hinh. Sua o day la sua luon giao dien. */
    val MOC_PHUT = intArrayOf(15, 20, 30, 40, 50, 60)

    /**
     * Khung gio ba bam duoc, tinh theo phut tu nua dem.
     *
     * Dong cung o day vi may ba khong co cach nao hoi tablet dang dat gio chot la
     * may gio. Ben tablet thi nguoc lai - no doc thang cai dat that, xem
     * HopThuBaNoi.nenNgo. Doi so o day thi nho ngo sang ben do mot cai.
     *
     * Chan tren la gio chot cua tablet: qua gio do ben kia tu choi cap, ba bam
     * cung khong duoc gi ma lai tuong da cho roi.
     */
    const val SOM_NHAT = 6 * 60

    const val MUON_NHAT = 22 * 60
}
