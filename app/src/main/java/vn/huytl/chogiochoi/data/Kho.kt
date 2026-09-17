package vn.huytl.chogiochoi.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/**
 * Cua duy nhat di ra Firestore.
 *
 * THAY CHO CAI GI: truoc day app nay dat lenh vao mo ta mot nhom Telegram, roi
 * tablet ghe qua doc moi mot den ba phut. Duong do chay duoc nhung co ba cho dau:
 * ba bam "da xong" ma chau ngoi cho ca phut truoc man hinh khoa; mo ta nhom chi
 * co mot o nen lenh cho gio va lenh viec nha de len nhau; va may ba phai cam token
 * bot. Ghi thang xuong Firestore bo duoc ca ba.
 *
 * MAY NAY KHONG PHAI NGUOI NHA DAY DU. Uid cua no nam trong uidsPhu, va luat ben
 * firestore.rules chi cho danh sach do lam ba viec: tao lenh kieu CHO, ghi
 * hop/viecnha, va doc hop/trangthai. Bam nham cai gi khac thi Firestore tu choi,
 * khong phai trong vao viec man hinh nay khong hien nut do ra.
 *
 * Khong dung Task.await() vi nhu the phai keo them kotlinx-coroutines-play-services
 * chi de cho vai lan goi. Callback la du.
 */
object Kho {

    private const val TAG = "ChoGioChoi"

    /** Ket qua mot viec co the hong, de man hinh biet noi gi voi ba. */
    sealed interface KetQua {
        data object Xong : KetQua
        data class Hong(val viSao: String) : KetQua
    }

    /**
     * Da khai bao Firebase chua.
     *
     * Thieu google-services.json luc build thi initializeApp tra ve null. Luc do app
     * van mo duoc, chi la man nao cung bao "chua noi" thay vi tat ngang.
     */
    fun san(context: Context): Boolean = app(context) != null

    // ---------------------------------------------------------------- ghep doi

    /**
     * Xin vao nha.
     *
     * May nay chua phai nguoi nha nen chua ghi duoc gi ngoai mot cho:
     * nha/{ma nha}/ghep/{uid cua chinh minh}. Dat ma sau so vao do roi cho tablet
     * ket nap.
     *
     * Gui kem [Nguoi.BA_NOI]: do la cho tablet biet xep uid nay vao uidsPhu chu
     * khong phai uids. Nham cho do la may ba go duoc ca lenh khoa tablet.
     *
     * Tra ve Xong nghia la da dat duoc loi xin, chua phai da vao duoc nha. Ben goi
     * phai nghe tiep bang [ngheKetNap].
     */
    fun xinVaoNha(context: Context, maNha: String, maGhep: String, xong: (KetQua) -> Unit) {
        dangNhap(context) { kq ->
            if (kq is KetQua.Hong) return@dangNhap xong(kq)
            val d = db(context) ?: return@dangNhap xong(KetQua.Hong(THIEU_FIREBASE))
            val uid = uid(context)
            if (uid.isEmpty()) return@dangNhap xong(KetQua.Hong("Chưa có danh tính máy."))

            d.collection(Duong.NHA).document(maNha.trim())
                .collection(Duong.GHEP).document(uid)
                .set(
                    mapOf(
                        "ma" to maGhep.trim(),
                        "luc" to System.currentTimeMillis(),
                        Duong.F_AI to Nguoi.BA_NOI
                    )
                )
                .addOnSuccessListener { xong(KetQua.Xong) }
                .addOnFailureListener {
                    Log.w(TAG, "xin vao nha hong", it)
                    xong(KetQua.Hong("Không gửi được lời xin. Mã nhà gõ sai, hoặc mất mạng."))
                }
        }
    }

    /**
     * Nghe xem tablet da ket nap chua.
     *
     * Tablet doc ma, so voi ma dang hien tren man hinh no, dung thi ghi
     * trangThai = "OK" vao chinh cai o xin nay. Khong dung thi ghi "SAI" - va do la
     * loi duy nhat ben nay phan biet duoc voi mat mang.
     */
    fun ngheKetNap(
        context: Context,
        maNha: String,
        khi: (trangThai: String) -> Unit
    ): ListenerRegistration? {
        val d = db(context) ?: return null
        val uid = uid(context)
        if (uid.isEmpty()) return null
        return d.collection(Duong.NHA).document(maNha.trim())
            .collection(Duong.GHEP).document(uid)
            .addSnapshotListener { snap, loi ->
                if (loi != null) return@addSnapshotListener
                khi(snap?.getString("trangThai").orEmpty())
            }
    }

    // ------------------------------------------------------------------- ghi

    /**
     * Ba bam cho gio.
     *
     * Dat mot lenh vao hang doi cua tablet. Tablet nghe hang nay, lam xong thi xoa
     * document di va ghi cau tra loi vao hop/trangthai - xem [ngheTraLoi].
     *
     * Khong tu quyet dinh duoc gio hay khong: con han muc ngay khong, co dang gio
     * ngu khong, hom nay ba cho lan nao chua - chi tablet biet. Cho nay chi go lenh.
     */
    fun choGio(context: Context, phut: Int, xong: (KetQua) -> Unit) {
        val n = nha(context) ?: return xong(KetQua.Hong(CHUA_GHEP))
        n.collection(Duong.LENH).add(
            mapOf(
                Duong.F_KIEU to Lenh.CHO,
                Duong.F_PHUT to phut,
                Duong.F_AI to Nguoi.BA_NOI,
                // Gio may chu gui kem gio may chu: tablet lay cai nay de bo lenh go
                // tu hom qua, con dong ho hai may thi khong bao gio khop tuyet doi.
                "tao" to System.currentTimeMillis()
            )
        )
            .addOnSuccessListener { xong(KetQua.Xong) }
            .addOnFailureListener {
                Log.w(TAG, "cho gio hong", it)
                xong(KetQua.Hong(loiNguoiDoc(it)))
            }
    }

    /**
     * Dat trang thai viec nha.
     *
     * Ghi CA DANH SACH moi lan chu khong gui rieng cai vua doi: mot ban trang thai
     * day du thi tablet doc duoc ban nao cung dung, con gui su kien roi le thi mat
     * mot cai la hai ben lech nhau vinh vien.
     *
     * Ghi de luon, khong merge: danh sach ngan di - ba bo mot viec - ma merge thi
     * viec do van nam lai trong mang cu.
     */
    fun datViecNha(
        context: Context,
        maPhien: String,
        cac: List<ViecNha.DangLam>,
        xong: (KetQua) -> Unit
    ) {
        val h = hop(context, Duong.D_VIEC_NHA) ?: return xong(KetQua.Hong(CHUA_GHEP))
        h.set(
            mapOf(
                Duong.F_MA_PHIEN to maPhien,
                Duong.F_LUC to System.currentTimeMillis(),
                Duong.F_VIEC to cac.map {
                    mapOf(
                        Duong.F_TEN to it.ten,
                        Duong.F_PHUT to it.phut,
                        Duong.F_XONG to it.xong
                    )
                }
            )
        )
            .addOnSuccessListener { xong(KetQua.Xong) }
            .addOnFailureListener {
                Log.w(TAG, "dat viec nha hong", it)
                xong(KetQua.Hong(loiNguoiDoc(it)))
            }
    }

    // ------------------------------------------------------------------ nghe

    /** Mot cau tablet noi lai, va luc no noi. */
    data class TraLoi(val chu: String, val luc: Long)

    /**
     * Nghe cau tablet noi lai sau khi lam lenh cua may NAY.
     *
     * Duong hop thu Telegram cu khong co cho nay: ba bam xong khong biet tablet co
     * nhan duoc khong, co cap duoc gio khong, hay dang trong gio ngu. Tablet bao ve
     * chat cua Ba Huy chu khong bao cho ba.
     *
     * Loc theo [Duong.F_AI]: o traLoi chi co mot cho, ma ca Ba Huy lan ba cung ghi
     * vao do. Khong loc thi ba thay cau tra loi cho cai nut Ba Huy vua bam ben kia.
     */
    fun ngheTraLoi(context: Context, khi: (TraLoi) -> Unit): ListenerRegistration? =
        hop(context, Duong.D_TRANG_THAI)?.addSnapshotListener { snap, loi ->
            if (loi != null) return@addSnapshotListener
            val o = snap?.get(Duong.F_TRA_LOI) as? Map<*, *> ?: return@addSnapshotListener
            if (o[Duong.F_AI] != Nguoi.BA_NOI) return@addSnapshotListener
            val chu = o["chu"] as? String ?: return@addSnapshotListener
            khi(TraLoi(chu, (o["luc"] as? Number)?.toLong() ?: 0L))
        }

    /**
     * Nghe xem dot viec nha con nam tren Firestore khong.
     *
     * DAY LA CAU TRA LOI, khong phai ban sao du lieu. Ghi len Firestore xong khong co
     * nghia la tablet da nhan: tablet co the dang tat, hay dang bo qua vi ban qua cu.
     * Tablet nhan va khep xong mot dot thi no XOA document di - nen document con nam
     * do nghia la chua ai nhan, va ba con nut de gui lai.
     *
     * [khi] nhan true khi document con, false khi da mat.
     */
    fun ngheViecNha(context: Context, khi: (conDo: Boolean) -> Unit): ListenerRegistration? =
        hop(context, Duong.D_VIEC_NHA)?.addSnapshotListener { snap, loi ->
            if (loi != null) return@addSnapshotListener
            khi(snap != null && snap.exists())
        }

    // ---------------------------------------------------------------- rieng tu

    private fun app(context: Context): FirebaseApp? =
        runCatching { FirebaseApp.initializeApp(context.applicationContext) }.getOrNull()
            ?: runCatching { FirebaseApp.getInstance() }.getOrNull()

    private fun db(context: Context): FirebaseFirestore? =
        app(context)?.let { FirebaseFirestore.getInstance(it) }

    private fun auth(context: Context): FirebaseAuth? =
        app(context)?.let { FirebaseAuth.getInstance(it) }

    /** Uid cua may nay. Rong la chua dang nhap xong. */
    fun uid(context: Context): String = auth(context)?.currentUser?.uid.orEmpty()

    /**
     * Dang nhap an danh roi goi [xong].
     *
     * An danh: ba khong phai nho mat khau nao, nhung may van co mot uid rieng de
     * luat truy cap ben Firestore bam vao. Uid do nam lai tren may; go app roi cai
     * lai la ra uid moi va phai ghep doi lai.
     */
    fun dangNhap(context: Context, xong: (KetQua) -> Unit) {
        val a = auth(context) ?: return xong(KetQua.Hong(THIEU_FIREBASE))
        if (a.currentUser != null) return xong(KetQua.Xong)
        a.signInAnonymously()
            .addOnSuccessListener { xong(KetQua.Xong) }
            .addOnFailureListener {
                Log.w(TAG, "dang nhap hong", it)
                xong(
                    KetQua.Hong(
                        "Chưa đăng nhập được Firebase. Kiểm tra mạng, và xem đã bật " +
                            "Anonymous trong phần Authentication chưa."
                    )
                )
            }
    }

    private fun nha(context: Context): DocumentReference? {
        val ma = Nha.maNha(context)
        if (ma.isEmpty()) return null
        return db(context)?.collection(Duong.NHA)?.document(ma)
    }

    private fun hop(context: Context, ten: String): DocumentReference? =
        nha(context)?.collection(Duong.HOP)?.document(ten)

    /**
     * Doi loi cua Firestore sang cau noi duoc viec phai lam.
     *
     * Nguoi doc may cau nay la ba noi, nen khong duoc de nguyen cau tieng Anh. Rieng
     * PERMISSION_DENIED thi gan nhu luc nao cung mot nghia: may nay chua duoc ket
     * nap, hoac da bi go ra khoi nha.
     */
    private fun loiNguoiDoc(loi: Exception): String {
        val chu = loi.message.orEmpty()
        return when {
            chu.contains("PERMISSION_DENIED", true) || chu.contains("permission", true) ->
                "Máy này chưa được nối với máy của cháu. Nhờ Ba Huy mở Cài đặt nối lại."
            chu.contains("UNAVAILABLE", true) || chu.contains("network", true) ->
                "Chưa có mạng. Máy sẽ tự gửi lại khi có mạng."
            else -> "Không gửi được: $chu"
        }
    }

    private const val THIEU_FIREBASE =
        "Bản app này chưa nối Firebase (thiếu google-services.json lúc build)."

    private const val CHUA_GHEP = "Máy này chưa nối với máy của cháu."
}
