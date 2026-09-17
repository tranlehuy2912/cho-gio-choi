package vn.huytl.chogiochoi.data

/**
 * May con so dong cung, de sua o mot cho.
 *
 * Truoc day file nay con giu token bot, va do la cho yeu nhat cua ca he: token nam
 * trong file APK tren may ba, ai rut duoc ra la nam con bot. Tu khi app di duong
 * Firestore thi may ba khong can token nua - no chi co mot uid an danh, va uid do
 * nam trong uidsPhu nen chi cho gio duoc, khong go duoc lenh nao khac.
 */
internal object Defaults {

    /** Sau nut tren man hinh. Sua o day la sua luon giao dien. */
    val MOC_PHUT = intArrayOf(15, 20, 30, 40, 50, 60)

    /**
     * Khung gio ba bam duoc, tinh theo phut tu nua dem.
     *
     * Dong cung o day vi may ba khong doc duoc cai dat that cua tablet. Ben tablet
     * thi nguoc lai - no cap theo gio chot that trong Prefs. Doi so o day thi nho
     * ngo sang ben do mot cai, va nguoc lai.
     *
     * Chan tren la gio chot cua tablet: qua gio do ben kia tu choi cap, ba bam cung
     * khong duoc gi ma lai tuong da cho roi.
     */
    const val SOM_NHAT = 6 * 60

    const val MUON_NHAT = 22 * 60
}
