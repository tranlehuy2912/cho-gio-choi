package vn.huytl.chogiochoi.ui

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.ListenerRegistration
import vn.huytl.chogiochoi.R
import vn.huytl.chogiochoi.data.Defaults
import vn.huytl.chogiochoi.data.Duong
import vn.huytl.chogiochoi.data.Kho
import vn.huytl.chogiochoi.data.LuotNgay
import vn.huytl.chogiochoi.data.Nguoi
import vn.huytl.chogiochoi.data.Nha
import vn.huytl.chogiochoi.data.ViecNha
import vn.huytl.chogiochoi.databinding.ActivityMainBinding
import vn.huytl.chogiochoi.databinding.DialogCaiDatBinding
import vn.huytl.chogiochoi.databinding.ItemNutBinding
import vn.huytl.chogiochoi.databinding.ItemViecBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Man hinh duy nhat cua app: may nut gio, bam mot cai la xong.
 *
 * Khong PIN, khong hoi lai, khong huy - do la y muon cua nguoi dat hang. Doi lai
 * phai chac hai viec: nut chi an duoc mot lan moi ngay, va bam xong phai biet
 * ngay la lenh da di hay chua. Bam vao khoang khong roi ba tuong da cho la hong
 * nhat, vi luc do khong ai biet de sua.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var dangGui = false

    /** Dang gui mot lan bam viec nha. Tach khoi [dangGui] vi hai duong doc lap. */
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
     * nhip sau doi sang dot dang chay va ca khoi nhay len tren nut gio - dung luc ba
     * dang dua tay toi mot nut.
     */
    private var daCoDot = false

    /** Con so vua bam, giu lai de nut "Thu lai" biet gui lai bao nhieu phut. */
    private var phutVuaBam = 0
    private var loiVuaRoi = ""

    /**
     * Cau may cua chau noi lai, va luc no noi.
     *
     * Duong hop thu Telegram cu khong co cho nay: ba bam xong khong biet may ben kia
     * co nhan duoc khong, co cap duoc gio khong, hay dang trong gio ngu. Bay gio
     * tablet ghi cau tra loi xuong Firestore va may nay nghe duoc.
     */
    private var traLoi = ""
    private var traLoiLuc = 0L
    private var ngheTraLoi: ListenerRegistration? = null

    private var ngheViecNha: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        chuaThanhBar()
        dungNut()

        binding.btnCaiDat.setOnClickListener { hoiCaiDat() }

        // Chua cai gi thi hoi luon, khoi bat Ba Huy mo ra roi doan phai lam gi.
        if (!Nha.daGhep(this)) hoiCaiDat()
    }

    override fun onResume() {
        super.onResume()
        // Ve lai o day chu khong chi o onCreate: may de mo qua dem thi hom sau mo
        // ra phai thay nut, va 22:00 di qua thi nut phai tat.
        veLai()
        batNghe()
    }

    override fun onPause() {
        // Go lang nghe khi ba dong app lai. App nay khong co gi chay nen: dong la
        // het, khong ton mot giot pin nao cua may ba.
        ngheTraLoi?.remove()
        ngheTraLoi = null
        ngheViecNha?.remove()
        ngheViecNha = null
        ngheDanhSach?.remove()
        ngheDanhSach = null
        super.onPause()
    }

    /**
     * Nghe cau may cua chau noi lai.
     *
     * Chi nhan cau moi hon luc ba vua bam. Cau cu con nam do tren Firestore, va mo
     * app ra sau nua ngay ma thay "Da cho 30 phut" nhay len thi ba tuong minh vua
     * bam cai gi.
     */
    private fun batNghe() {
        ngheTraLoi?.remove()
        ngheViecNha?.remove()
        ngheDanhSach?.remove()
        if (!Nha.daGhep(this)) {
            daCoDot = true
            return
        }

        ngheTraLoi = Kho.ngheTraLoi(this) { tra ->
            if (tra.luc <= traLoiLuc) return@ngheTraLoi
            traLoi = tra.chu
            traLoiLuc = tra.luc
            veLai()
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

    /**
     * Do sau nut vao man hinh, hai cai mot hang.
     *
     * Dung tu [Defaults.MOC_PHUT] chu khong viet tay trong XML: sua danh sach phut
     * o mot cho la ca man hinh theo ngay, khong co chuyen XML mot dang ma so phut
     * gui di mot neo.
     */
    private fun dungNut() {
        val moc = Defaults.MOC_PHUT
        val cachNhau = (8 * resources.displayMetrics.density).toInt()
        binding.boxNut.removeAllViews()

        var i = 0
        while (i < moc.size) {
            val hang = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { if (i > 0) topMargin = cachNhau }
            }
            for (cot in 0 until 2) {
                val vt = i + cot
                if (vt >= moc.size) break
                val phut = moc[vt]
                val nut = ItemNutBinding.inflate(layoutInflater, hang, false).root
                nut.text = getString(R.string.nut_phut, phut)
                (nut.layoutParams as LinearLayout.LayoutParams).apply {
                    weight = 1f
                    if (cot > 0) marginStart = cachNhau
                }
                nut.setOnClickListener { bam(phut) }
                hang.addView(nut)
            }
            binding.boxNut.addView(hang)
            i += 2
        }
    }

    /** Ve lai toan bo man hinh theo tinh hinh hien tai. */
    private fun veLai() {
        veViecNha()
        xepLaiKhoi()
        val the = binding
        when {
            dangGui -> hienThe(
                tieuDe = getString(R.string.dang_gui),
                chiTiet = getString(R.string.nut_phut, phutVuaBam),
                mau = R.color.chu_nhat,
                conNut = true
            )

            LuotNgay.daCho(this) -> {
                val luc = SimpleDateFormat("HH:mm", Locale.forLanguageTag("vi-VN"))
                    .format(Date(LuotNgay.lucCho(this)))
                hienThe(
                    tieuDe = getString(R.string.xong_tieu_de),
                    chiTiet = getString(R.string.xong_chi_tiet, LuotNgay.phutDaCho(this), luc) +
                        // Cau cua chinh may ben kia, neu no da kip noi. Dong nay moi
                        // la cai bao that: o tren chi noi may nay da gui di.
                        (if (traLoi.isBlank()) "" else "\n$traLoi") +
                        "\n" + getString(R.string.xong_mai),
                    mau = R.color.xong,
                    conNut = false
                )
            }

            !Nha.daGhep(this) -> hienThe(
                tieuDe = getString(R.string.chua_noi_tieu_de),
                chiTiet = getString(R.string.chua_noi_chi_tiet),
                mau = R.color.hong,
                conNut = false,
                nhanNut = getString(R.string.cai_dat),
                khiBam = { hoiCaiDat() }
            )

            ngoaiGio() -> hienThe(
                tieuDe = getString(R.string.ngoai_gio_tieu_de),
                chiTiet = getString(
                    R.string.ngoai_gio_chi_tiet,
                    gioBayGio(), gioChuoi(Defaults.SOM_NHAT), gioChuoi(Defaults.MUON_NHAT)
                ),
                mau = R.color.hong,
                conNut = false
            )

            loiVuaRoi.isNotEmpty() -> hienThe(
                tieuDe = getString(R.string.hong_tieu_de),
                chiTiet = loiVuaRoi,
                mau = R.color.hong,
                conNut = true,
                nhanNut = getString(R.string.thu_lai),
                khiBam = { bam(phutVuaBam) }
            )

            else -> {
                the.boxThe.visibility = View.GONE
                the.boxNut.visibility = View.VISIBLE
                batNut(true)
            }
        }
    }

    /**
     * Hien the thong bao.
     *
     * [conNut] la con cho bam nut gio nua hay khong. Gui hong thi van con - chua
     * tru luot nen ba bam lai duoc ngay. Da cho xong roi thi khong.
     */
    private fun hienThe(
        tieuDe: String,
        chiTiet: String,
        mau: Int,
        conNut: Boolean,
        nhanNut: String? = null,
        khiBam: (() -> Unit)? = null
    ) {
        binding.boxNut.visibility = if (conNut) View.VISIBLE else View.GONE
        batNut(conNut && !dangGui)

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

    /**
     * Dang co dot viec chay thi day khoi viec nha len tren cac nut gio.
     *
     * Binh thuong nut gio o tren vi do la viec moi ngay cua ba, con viec nha thi
     * thinh thoang. Nhung tu luc giao mot dot, cai ba can bam lai nam trong khoi
     * viec - ma no o duoi day, phai cuon xuong moi toi, va cuon la thu kho nhat
     * voi nguoi khong quen dung dien thoai.
     */
    private fun xepLaiKhoi() {
        val dangGiao = dot?.cac?.isNotEmpty() == true
        val cot = binding.boxNut.parent as? LinearLayout ?: return
        val viTriNut = cot.indexOfChild(binding.boxNut)
        val viTriTieuDe = cot.indexOfChild(binding.txtViecTieuDe)
        if (viTriNut < 0 || viTriTieuDe < 0) return
        // Dang dung thu tu can roi thi thoi: doi cho view moi lan ve lai la moi lan
        // ban phim va con tro nhay mot cai.
        if (dangGiao == (viTriTieuDe < viTriNut)) return

        val khoiViec = listOf(
            binding.txtViecTieuDe, binding.txtViecPhuDe, binding.boxViec, binding.btnGiaoViec
        )
        khoiViec.forEach { cot.removeView(it) }
        val dat = if (dangGiao) cot.indexOfChild(binding.boxNut) else cot.indexOfChild(binding.boxThe)
        khoiViec.forEachIndexed { i, v -> cot.addView(v, dat + i) }
    }

    private fun batNut(bat: Boolean) {
        binding.boxNut.forEachNut { it.isEnabled = bat }
    }

    private inline fun LinearLayout.forEachNut(f: (MaterialButton) -> Unit) {
        for (h in 0 until childCount) {
            val hang = getChildAt(h) as? LinearLayout ?: continue
            for (c in 0 until hang.childCount) {
                (hang.getChildAt(c) as? MaterialButton)?.let(f)
            }
        }
    }

    /**
     * Bam mot nut gio.
     *
     * Tru luot NGAY khi bam, roi tra lai neu Telegram tu choi. Ly do chon thu tu
     * do nam o [LuotNgay.ghiNhan]: ghi sau thi mot lan app bi dong giua chung la
     * so dem ben nay lech han voi thuc te ben tablet.
     */
    private fun bam(phut: Int) {
        if (dangGui || phut <= 0) return
        if (!Nha.daGhep(this)) {
            hoiCaiDat()
            return
        }

        dangGui = true
        phutVuaBam = phut
        loiVuaRoi = ""
        traLoi = ""
        // Ghi xong roi moi goi mang. Trong luc [dangGui] thi man hinh ve the
        // "Dang gui..." chu khong ve the "Hom nay xong roi", nen ghi som o day
        // khong lam ba tuong da gui xong.
        LuotNgay.ghiNhan(this, phut)
        veLai()

        // Khong can luong nen: Firestore tu lo phan mang, ham nay tra ve ngay va
        // goi lai [xong] tren luong chinh. Do la ly do ca doan withContext(IO) cu
        // bien mat.
        Kho.choGio(this, phut) { kq ->
            dangGui = false
            if (kq is Kho.KetQua.Hong) {
                LuotNgay.traLuot(this)
                loiVuaRoi = kq.viSao
            }
            veLai()
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
     * la thu quyet dinh: uidsPhu chi cho gio va giao viec nha duoc, con uids thi go
     * duoc ca lenh khoa tablet va tat quan tri thiet bi.
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

    /** Ngoai khung gio tablet con cap gio duoc. */
    private fun ngoaiGio(): Boolean {
        val c = Calendar.getInstance()
        val phut = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)
        return phut < Defaults.SOM_NHAT || phut >= Defaults.MUON_NHAT
    }

    private fun gioBayGio(): String =
        SimpleDateFormat("HH:mm", Locale.forLanguageTag("vi-VN")).format(Date())

    private fun gioChuoi(phutTrongNgay: Int): String =
        "%02d:%02d".format(phutTrongNgay / 60, phutTrongNgay % 60)

    /** Tu Android 15 app tran ra sau thanh trang thai, phai tu chua lai. */
    private fun chuaThanhBar() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.scroll) { v, insets ->
            val bar = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = bar.top, bottom = bar.bottom)
            insets
        }
    }
}
