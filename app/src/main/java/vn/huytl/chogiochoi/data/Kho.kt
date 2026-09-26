package vn.huytl.chogiochoi.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges

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
 * firestore.rules chi cho danh sach do lam may viec: doc va ghi hop/viecnha, doc
 * hop/danhsachviec va tao no mot lan khi chua co, doc hop/trangthai, tao lenh kieu
 * CHO. Lenh CHO la cua sau nut cho gio da bo ngay 26/9/2026, app nay khong con go.
 * Bam nham cai gi khac thi Firestore tu choi, khong phai trong vao viec man hinh nay
 * khong hien nut do ra.
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

    // -------------------------------------------------------------- viec nha

    /** Ba giao mot dot moi tu danh sach da chon. */
    fun giaoViec(context: Context, cac: List<ViecNha.Viec>, xong: (KetQua) -> Unit) =
        doiViecNha(context, xong) { cu ->
            // Man hinh ba dang la danh sach de chon, ma tren may chu da co dot khac:
            // Ba Huy vua giao, va ban do chua kip ve toi day. Ghi de la xoa dot cua
            // Ba Huy ma khong ai hay.
            if (cu != null && cu.cac.isNotEmpty()) throw LoiViec(DA_CO_DOT)
            ViecNha.dotMoi(cac, Nguoi.BA_NOI, System.currentTimeMillis())
        }

    /**
     * Ba bam xong mot viec.
     *
     * Viec do da co nguoi bam xong roi (Ba Huy bam truoc mot nhip) thi thoi, khong
     * ghi gi va khong bao loi: dieu ba muon da dung roi.
     */
    fun xongViec(context: Context, maPhien: String, ten: String, xong: (KetQua) -> Unit) =
        doiViecNha(context, xong) { cu ->
            val dot = cungDot(cu, maPhien)
            if (dot.cac.none { it.ten == ten && !it.xong }) null
            else dot.xong(ten, System.currentTimeMillis())
        }

    /** Ba bo mot viec da giao, vi bam nham hay thoi khong bat lam nua. */
    fun boViec(context: Context, maPhien: String, ten: String, xong: (KetQua) -> Unit) =
        doiViecNha(context, xong) { cu ->
            val dot = cungDot(cu, maPhien)
            if (dot.cac.none { it.ten == ten }) null
            else dot.bo(ten, System.currentTimeMillis())
        }

    fun boHetViec(context: Context, maPhien: String, xong: (KetQua) -> Unit) =
        doiViecNha(context, xong) { cu ->
            val dot = cungDot(cu, maPhien)
            if (dot.cac.isEmpty()) null else dot.boHet(System.currentTimeMillis())
        }

    /**
     * Gui lai mot dot da xong het ma tablet chua nhan.
     *
     * Document da mat, hay da la dot khac, thi thoi: tablet nhan roi, hoac co nguoi
     * da giao dot moi. Khong co gi de gui lai.
     */
    fun guiLaiViec(context: Context, maPhien: String, xong: (KetQua) -> Unit) =
        doiViecNha(context, xong) { cu ->
            if (cu == null || cu.maPhien != maPhien) null
            else cu.guiLai(System.currentTimeMillis())
        }

    /**
     * Doi dot viec nha bang mot transaction.
     *
     * VI SAO TRANSACTION: Bang dieu khien cung ghi vao day. Ghi de ca ban ma man
     * hinh nay dang giu thi mot cai bam ben kia ngay truoc do mat luon - viec Ba Huy
     * vua bao xong quay ve chua xong, va tablet khoa lai vi no. Transaction doc ban
     * tren may chu, [doi] sua dung viec vua bam tren ban do roi moi ghi. Hai may bam
     * cung luc thi Firestore bat mot ben lam lai tren ban moi.
     *
     * DOI LAI, LUC BAM PHAI CO MANG. set() thi xep hang cho co mang roi tu gui, con
     * transaction mat mang la hong ngay va ba bam lai. Chap nhan duoc: tablet cung
     * phai co mang moi nhan duoc gi, va hong thi man hinh noi ra chu khong lang im.
     *
     * [doi] nhan dot dang nam tren may chu, null la khong co dot nao. Tra ve null la
     * khong can ghi gi; nem [LoiViec] la dung lai va bao cho ba.
     */
    private fun doiViecNha(
        context: Context,
        xong: (KetQua) -> Unit,
        doi: (ViecNha.Dot?) -> ViecNha.Dot?
    ) {
        val h = hop(context, Duong.D_VIEC_NHA) ?: return xong(KetQua.Hong(CHUA_GHEP))
        h.firestore.runTransaction { tr ->
            val moi = doi(ViecNha.docDot(tr.get(h).data))
            if (moi != null) tr.set(h, ViecNha.banGhi(moi))
            null
        }
            .addOnSuccessListener { xong(KetQua.Xong) }
            .addOnFailureListener {
                Log.w(TAG, "doi viec nha hong", it)
                val loiViec = it as? LoiViec ?: it.cause as? LoiViec
                xong(KetQua.Hong(loiViec?.message ?: loiGiaoDich(it)))
            }
    }

    /** Dot tren may chu phai dung la dot ba dang nhin, khong thi dung lai. */
    private fun cungDot(cu: ViecNha.Dot?, maPhien: String): ViecNha.Dot {
        if (cu == null || cu.maPhien != maPhien) throw LoiViec(DOT_DA_DOI)
        return cu
    }

    /** Loi do chinh ben nay dung transaction lai, kem cau noi cho ba. */
    private class LoiViec(chu: String) : Exception(chu)

    /**
     * Nghe danh sach viec chung o hop/danhsachviec.
     *
     * [khi] nhan null khi chua co danh sach nao doc duoc: document chua co, hay luat
     * tren Firestore chua cho may nay doc (chua dan firestore.rules moi). Luc do man
     * hinh dung danh sach trong may.
     *
     * Document chua co, va chinh may chu noi vay chu khong phai bo nho dem luc mat
     * mang, thi gui danh sach trong may len. Ban app truoc cho Ba Huy sua danh sach
     * tren may ba; khong gui len thi nhung viec do mat khi hai may dung chung mot
     * danh sach. Luat chi cho may ba TAO document nay, nen neu Bang dieu khien da
     * luu truoc thi lan gui nay bi tu choi va danh sach cua Ba Huy giu nguyen.
     */
    fun ngheDanhSachViec(
        context: Context,
        khi: (List<ViecNha.Viec>?) -> Unit
    ): ListenerRegistration? {
        val h = hop(context, Duong.D_DANH_SACH_VIEC) ?: return null
        // Nghe ca doi metadata: bo nho dem luc mat mang bao "chua co", roi may chu bao
        // lai dung cau do. Khong nghe thi lan thu hai khong toi, va may nay khong bao
        // gio biet la phai gui danh sach len.
        return h.addSnapshotListener(MetadataChanges.INCLUDE) { snap, loi ->
            if (loi != null) {
                Log.w(TAG, "nghe danh sach viec hong", loi)
                return@addSnapshotListener khi(null)
            }
            if (snap == null) return@addSnapshotListener
            if (!snap.exists()) {
                if (!snap.metadata.isFromCache && !daGuiDanhSach) {
                    daGuiDanhSach = true
                    h.set(
                        mapOf(
                            Duong.F_VIEC to ViecNha.banDanhSach(ViecNha.danhSachTrongMay(context)),
                            Duong.F_LUC to System.currentTimeMillis(),
                            Duong.F_AI to Nguoi.BA_NOI
                        )
                    ).addOnFailureListener { Log.w(TAG, "gui danh sach viec hong", it) }
                }
                return@addSnapshotListener khi(null)
            }
            khi(ViecNha.docDanhSach(snap.get(Duong.F_VIEC) as? List<*>).takeIf { it.isNotEmpty() })
        }
    }

    /** Da gui danh sach trong may len chua, trong lan chay nay. Mot lan la du. */
    private var daGuiDanhSach = false

    // ------------------------------------------------------------------ nghe

    /**
     * Nghe dot viec nha dang giao. null la khong co dot nao.
     *
     * Day la thu man hinh ve theo, ke ca viec Ba Huy giao hay bam xong ben Bang dieu
     * khien. Va no cung la cau tra loi cua tablet: tablet nhan va khep xong mot dot
     * thi no XOA document di, nen dot xong het ma con nam do nghia la tablet chua
     * nhan, va ba con nut de gui lai.
     */
    fun ngheViecNha(context: Context, khi: (ViecNha.Dot?) -> Unit): ListenerRegistration? =
        hop(context, Duong.D_VIEC_NHA)?.addSnapshotListener { snap, loi ->
            if (loi != null) return@addSnapshotListener
            khi(ViecNha.docDot(snap?.data))
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

    /**
     * Cau bao hong cho mot lan bam viec nha.
     *
     * Khac [loiNguoiDoc] o cau mat mang: transaction khong tu gui lai khi co mang,
     * nen noi "may se tu gui lai" la noi sai.
     */
    private fun loiGiaoDich(loi: Exception): String {
        val matMang = (loi as? FirebaseFirestoreException)?.code ==
            FirebaseFirestoreException.Code.UNAVAILABLE ||
            loi.message.orEmpty().contains("offline", true)
        return if (matMang) "Chưa có mạng nên chưa gửi được. Có mạng rồi bà bấm lại."
        else loiNguoiDoc(loi)
    }

    private const val DA_CO_DOT = "Đang có một đợt việc khác. Bà xem lại danh sách ở trên."

    private const val DOT_DA_DOI = "Danh sách việc vừa đổi. Bà xem lại rồi bấm lại."

    private const val THIEU_FIREBASE =
        "Bản app này chưa nối Firebase (thiếu google-services.json lúc build)."

    private const val CHUA_GHEP = "Máy này chưa nối với máy của cháu."
}
