package vn.huytl.chogiochoi.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.ListenerRegistration
import vn.huytl.chogiochoi.R
import vn.huytl.chogiochoi.data.Duong
import vn.huytl.chogiochoi.data.Kho
import vn.huytl.chogiochoi.data.Nguoi
import vn.huytl.chogiochoi.data.Nha
import vn.huytl.chogiochoi.data.ViecNha
import vn.huytl.chogiochoi.databinding.ActivityMainBinding
import vn.huytl.chogiochoi.databinding.DialogCaiDatBinding
import vn.huytl.chogiochoi.databinding.ItemViecBinding

/**
 * Man hinh duy nhat cua app: giao viec nha cho Le Hoa, roi bam Xong tung viec.
 *
 * Truoc day tren cung con sau nut cho gio, tu 15 den 60 phut, moi ngay mot lan.
 * Ngay 26/9/2026 Huy bo sau nut do: may ba chi con giao viec nha.
 *
 * Khong PIN, khong hoi lai - nguoi bam la ba. Doi lai bam xong phai biet ngay la da
 * gui duoc hay chua. Bam vao khoang khong roi ba tuong da xong la hong nhat, vi luc
 * do khong ai biet de sua.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    /** Dang gui mot lan bam viec nha, de khong bam chong len nhau. */
    private var dangGuiViec = false

    /** Cac viec ba dang tich de giao, khi chua co dot nao chay. */
    private val daChonViec = linkedSetOf<String>()

    private var loiViec = ""

    /**
     * Dot viec dang giao, doc tu Firestore. null la khong co dot nao.
     *
     * Man hinh ve theo cai nay chu khong theo mot ban giu trong may: Ba Huy cung giao
     * va bam xong duoc ben Bang dieu khien, va ba phai thay ngay cai ben do vua bam.
     */
    private var dot: ViecNha.Dot? = null

    /** Danh sach viec chung tren Firestore. null la chua doc duoc, dung ban trong may. */
    private var danhSachChung: List<ViecNha.Viec>? = null
    private var ngheDanhSach: ListenerRegistration? = null

    /**
     * Firestore da tra ban dau tien cua dot viec chua, tu luc mo app.
     *
     * Chua thi khoi viec nha chua hien gi: ve truoc la ra danh sach de chon, roi mot
     * nhip sau doi sang dot dang chay - dung luc ba dang dua tay toi mot nut.
     */
    private var daCoDot = false

    private var ngheViecNha: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        chuaThanhBar()

        binding.btnCaiDat.setOnClickListener { hoiCaiDat() }

        // Chua cai gi thi hoi luon, khoi bat Ba Huy mo ra roi doan phai lam gi.
        if (!Nha.daGhep(this)) hoiCaiDat()
    }

    override fun onResume() {
        super.onResume()
        // Ve ngay ban dang giu, roi listener mang ban moi ve: ba quay lai app sau mot
        // luc thi dot viec co the da doi ben Bang dieu khien hay ben tablet.
        veLai()
        batNghe()
    }

    override fun onPause() {
        // Go lang nghe khi ba dong app lai. App nay khong co gi chay nen: dong la
        // het, khong ton mot giot pin nao cua may ba.
        ngheViecNha?.remove()
        ngheViecNha = null
        ngheDanhSach?.remove()
        ngheDanhSach = null
        super.onPause()
    }

    /** Nghe dot viec nha dang giao va danh sach viec chung. */
    private fun batNghe() {
        ngheViecNha?.remove()
        ngheDanhSach?.remove()
        if (!Nha.daGhep(this)) {
            daCoDot = true
            return
        }

        // Dot viec con nam do nghia la chua ai nhan; mat di nghia la tablet da khep
        // no lai. Ca nhung gi Ba Huy vua giao hay vua bam cung ve qua day.
        ngheViecNha = Kho.ngheViecNha(this) { moi ->
            // Sang dot khac thi cau bao hong cua lan bam truoc khong con noi ve cai gi
            // tren man hinh nua.
            if (moi?.maPhien != dot?.maPhien && !dangGuiViec) loiViec = ""
            dot = moi
            daCoDot = true
            veLai()
        }
        if (ngheViecNha == null) daCoDot = true
        ngheDanhSach = Kho.ngheDanhSachViec(this) { ds ->
            danhSachChung = ds
            veLai()
        }
    }

    /** Ve lai toan bo man hinh theo tinh hinh hien tai. */
    private fun veLai() {
        veViecNha()
        // Chua noi may thi noi ra, kem nut sang Cai dat. Da noi roi thi the thong bao
        // an di: moi chuyen cua viec nha da co dong chu ngay duoi tieu de.
        if (Nha.daGhep(this)) {
            binding.boxThe.visibility = View.GONE
        } else {
            hienThe(
                tieuDe = getString(R.string.chua_noi_tieu_de),
                chiTiet = getString(R.string.chua_noi_chi_tiet),
                mau = R.color.hong,
                nhanNut = getString(R.string.cai_dat),
                khiBam = { hoiCaiDat() }
            )
        }
    }

    private fun hienThe(
        tieuDe: String,
        chiTiet: String,
        mau: Int,
        nhanNut: String? = null,
        khiBam: (() -> Unit)? = null
    ) {
        binding.boxThe.visibility = View.VISIBLE
        binding.txtTheTieuDe.text = tieuDe
        binding.txtTheTieuDe.setTextColor(ContextCompat.getColor(this, mau))
        binding.txtTheChiTiet.text = chiTiet

        if (nhanNut == null || khiBam == null) {
            binding.btnThe.visibility = View.GONE
        } else {
            binding.btnThe.visibility = View.VISIBLE
            binding.btnThe.text = nhanNut
            binding.btnThe.setOnClickListener { khiBam() }
        }
    }

    // ------------------------------------------------------------------ viec nha

    /**
     * Ve khu viec nha.
     *
     * Hai canh, va chi hai: chua giao gi thi day la danh sach de tich; dang giao roi
     * thi day la danh sach de bam xong. Khong tron hai canh vao nhau - ba khong phai
     * doan cai nut minh sap bam la giao them hay bao xong.
     */
    private fun veViecNha() {
        binding.boxViec.removeAllViews()
        if (!daCoDot) {
            binding.txtViecPhuDe.text = ""
            binding.btnGiaoViec.visibility = View.GONE
            return
        }
        val d = dot?.takeIf { it.cac.isNotEmpty() }

        if (d == null) {
            val ds = danhSach()
            daChonViec.retainAll(ds.map { it.ten }.toSet())
            binding.txtViecPhuDe.text = loiViec.ifEmpty { getString(R.string.viec_chua_giao) }
            ds.forEach { v ->
                themDongViec(
                    ten = v.ten,
                    phu = "${v.phut} phút",
                    nhanChinh = if (v.ten in daChonViec) "Bỏ chọn" else "Chọn",
                    chinhMo = !dangGuiViec,
                    khiChinh = {
                        if (!daChonViec.add(v.ten)) daChonViec.remove(v.ten)
                        veLai()
                    }
                )
            }
            val phut = ds.filter { it.ten in daChonViec }.sumOf { it.phut }
            binding.btnGiaoViec.visibility =
                if (daChonViec.isEmpty()) View.GONE else View.VISIBLE
            binding.btnGiaoViec.isEnabled = !dangGuiViec
            binding.btnGiaoViec.text = if (dangGuiViec) {
                getString(R.string.viec_dang_gui)
            } else {
                getString(R.string.viec_giao_nut, daChonViec.size, phut)
            }
            // Gan lai moi lan ve: nhanh duoi day cung dung chinh nut nay cho viec
            // khac han, va no da doi listener di roi.
            binding.btnGiaoViec.setOnClickListener { giaoViec() }
            return
        }

        // Xong het ma dot viec van con tren Firestore nghia la tablet chua nhan:
        // tablet khep dot nao thi xoa document cua dot do.
        val choNhan = d.xongHet
        binding.txtViecPhuDe.text = when {
            loiViec.isNotEmpty() -> loiViec
            choNhan -> getString(R.string.viec_cho_nhan)
            d.ai == Nguoi.BA_HUY -> getString(R.string.viec_ba_huy_giao)
            else -> getString(R.string.viec_dang_giao)
        }
        d.cac.forEach { v ->
            themDongViec(
                ten = v.ten,
                phu = if (v.xong) getString(R.string.viec_da_xong) else "${v.phut} phút",
                nhanChinh = if (v.xong) getString(R.string.viec_da_xong)
                else getString(R.string.viec_xong),
                chinhMo = !v.xong && !dangGuiViec,
                khiChinh = { xongViec(d.maPhien, v.ten) },
                nhanPhu = if (v.xong) null else getString(R.string.viec_bo),
                khiPhu = { boViec(d.maPhien, v.ten) }
            )
        }
        binding.btnGiaoViec.visibility = View.VISIBLE
        binding.btnGiaoViec.isEnabled = !dangGuiViec
        binding.btnGiaoViec.text = when {
            dangGuiViec -> getString(R.string.viec_dang_gui)
            choNhan -> getString(R.string.viec_gui_lai)
            else -> getString(R.string.viec_bo_het)
        }
        binding.btnGiaoViec.setOnClickListener {
            if (choNhan) guiLaiViec(d.maPhien) else boHetViec(d.maPhien)
        }
    }

    /**
     * Danh sach de chon: ban chung tren Firestore, chua doc duoc thi ban trong may.
     *
     * Cat o [Duong.TOI_DA_VIEC] du Bang dieu khien da khong cho luu dai hon: ban
     * trong may la do ban app truoc de lai, luc do chua co tran nay.
     */
    private fun danhSach(): List<ViecNha.Viec> =
        (danhSachChung ?: ViecNha.danhSachTrongMay(this)).take(Duong.TOI_DA_VIEC)

    private fun themDongViec(
        ten: String,
        phu: String,
        nhanChinh: String,
        chinhMo: Boolean,
        khiChinh: () -> Unit,
        nhanPhu: String? = null,
        khiPhu: (() -> Unit)? = null
    ) {
        val d = ItemViecBinding.inflate(layoutInflater, binding.boxViec, false)
        d.tenViec.text = ten
        d.phutViec.text = phu
        d.nutChinh.text = nhanChinh
        d.nutChinh.isEnabled = chinhMo
        d.nutChinh.setOnClickListener { khiChinh() }
        if (nhanPhu != null && khiPhu != null) {
            d.nutPhu.visibility = View.VISIBLE
            d.nutPhu.text = nhanPhu
            d.nutPhu.isEnabled = !dangGuiViec
            d.nutPhu.setOnClickListener { khiPhu() }
        }
        binding.boxViec.addView(d.root)
    }

    /** Ba giao mot dot viec moi tu nhung viec da chon. */
    private fun giaoViec() {
        if (dangGuiViec || daChonViec.isEmpty()) return
        if (!Nha.daGhep(this)) return hoiCaiDat()
        val cac = danhSach().filter { it.ten in daChonViec }
        // Giao hong thi giu nguyen cac viec da chon, de ba chi viec bam lai.
        guiViec({ xong -> Kho.giaoViec(this, cac, xong) }) { daChonViec.clear() }
    }

    private fun xongViec(maPhien: String, ten: String) =
        guiViec({ xong -> Kho.xongViec(this, maPhien, ten, xong) })

    private fun boViec(maPhien: String, ten: String) =
        guiViec({ xong -> Kho.boViec(this, maPhien, ten, xong) })

    private fun boHetViec(maPhien: String) =
        guiViec({ xong -> Kho.boHetViec(this, maPhien, xong) })

    private fun guiLaiViec(maPhien: String) =
        guiViec({ xong -> Kho.guiLaiViec(this, maPhien, xong) })

    /**
     * Gui mot lan bam viec nha, va giu man hinh o canh "dang gui" cho toi luc xong.
     *
     * KHONG SUA [dot] O DAY, ke ca khi gui duoc: listener se mang ban moi ve. Tu sua
     * theo cai vua gui thi co luc sai - bam xong viec cuoi la tablet khep dot va xoa
     * document ngay, va tin "da xoa" do co the ve truoc cau tra loi cua transaction.
     * Luc ay man hinh giu mai mot dot khong con nua, ma khong con gi bao no ve lai.
     */
    private fun guiViec(gui: ((Kho.KetQua) -> Unit) -> Unit, khiXong: () -> Unit = {}) {
        if (dangGuiViec) return
        dangGuiViec = true
        loiViec = ""
        veLai()
        gui { kq ->
            dangGuiViec = false
            when (kq) {
                is Kho.KetQua.Xong -> khiXong()
                is Kho.KetQua.Hong -> loiViec = kq.viSao
            }
            veLai()
        }
    }

    /**
     * Ghep may nay voi may cua chau. Nguoi doc man nay la Ba Huy, khong phai ba noi.
     *
     * Duong di: tren tablet mo man ghep doi, no hien ma nha va mot ma sau so. Go ca
     * hai vao day -> may nay dat mot loi xin vao nha/{ma nha}/ghep/{uid cua no} ->
     * tablet doc, so ma, dung thi them uid nay vao uidsPhu roi ghi lai "OK" -> man
     * nay thay va dong lai.
     *
     * Vi sao hai ma chu khong phai mot: ma nha la ten mot cho tren Firestore, no
     * khong doi bao gio. Ma sau so thi het han sau muoi phut, nen mot anh chup man
     * hinh tablet tu tuan truoc khong dung lai duoc.
     *
     * VAO UIDSPHU CHU KHONG PHAI UIDS. May nay gui kem Nguoi.BA_NOI luc xin, va do
     * la thu quyet dinh: uidsPhu chi giao viec nha duoc, con uids thi go duoc ca lenh
     * khoa tablet va tat quan tri thiet bi.
     */
    private fun hoiCaiDat() {
        val db = DialogCaiDatBinding.inflate(layoutInflater)
        db.edMaNha.setText(Nha.maNha(this))

        if (!Kho.san(this)) {
            db.chuTinhHinh.text = getString(R.string.chua_noi_firebase)
            db.chuTinhHinh.setTextColor(ContextCompat.getColor(this, R.color.hong))
        }

        val hop = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.cai_tieu_de)
            .setView(db.root)
            .setPositiveButton(R.string.cai_noi, null)
            .setNegativeButton(R.string.huy, null)
            .create()

        var nghe: ListenerRegistration? = null
        hop.setOnDismissListener { nghe?.remove() }

        hop.setOnShowListener {
            // Gan tay thay vi qua setPositiveButton de hop khong tu dong dong khi go
            // sai - dong roi thi Ba Huy phai mo lai tu dau de sua mot chu so.
            val nut = hop.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            nut.setOnClickListener {
                val maNha = db.edMaNha.text.toString().trim()
                val maGhep = db.edMaGhep.text.toString().trim()
                if (maNha.isEmpty()) {
                    db.edMaNha.error = getString(R.string.cai_thieu_ma_nha)
                    return@setOnClickListener
                }
                if (maGhep.length != 6) {
                    db.edMaGhep.error = getString(R.string.cai_sai_ma_ghep)
                    return@setOnClickListener
                }

                nut.isEnabled = false
                db.chuTinhHinh.text = getString(R.string.cai_dang_cho)
                db.chuTinhHinh.setTextColor(ContextCompat.getColor(this, R.color.chu_nhat))

                Kho.xinVaoNha(this, maNha, maGhep) { kq ->
                    if (kq is Kho.KetQua.Hong) {
                        nut.isEnabled = true
                        db.chuTinhHinh.text = kq.viSao
                        db.chuTinhHinh.setTextColor(ContextCompat.getColor(this, R.color.hong))
                        return@xinVaoNha
                    }
                    // Dat duoc loi xin roi thi ngoi cho tablet. Chua luu ma nha luc
                    // nay: chua duoc ket nap ma da coi la xong thi lan sau mo app se
                    // bo qua man nay va bam vao khoang khong.
                    nghe?.remove()
                    nghe = Kho.ngheKetNap(this, maNha) { trangThai ->
                        when (trangThai) {
                            "OK" -> {
                                Nha.datMaNha(this, maNha)
                                hop.dismiss()
                                veLai()
                                batNghe()
                            }

                            "SAI" -> {
                                nut.isEnabled = true
                                db.chuTinhHinh.text = getString(R.string.cai_sai_ma)
                                db.chuTinhHinh.setTextColor(
                                    ContextCompat.getColor(this, R.color.hong)
                                )
                            }
                        }
                    }
                }
            }
        }
        hop.show()
    }

    /** Tu Android 15 app tran ra sau thanh trang thai, phai tu chua lai. */
    private fun chuaThanhBar() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.scroll) { v, insets ->
            val bar = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = bar.top, bottom = bar.bottom)
            insets
        }
    }
}
