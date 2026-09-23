package vn.huytl.chogiochoi.ui

import android.os.Bundle
import android.view.View
import android.widget.ScrollView
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
import vn.huytl.chogiochoi.data.Kho
import vn.huytl.chogiochoi.data.LuotNgay
import vn.huytl.chogiochoi.data.Nha
import vn.huytl.chogiochoi.data.ViecNha
import vn.huytl.chogiochoi.databinding.ActivityMainBinding
import vn.huytl.chogiochoi.databinding.DialogCaiDatBinding
import vn.huytl.chogiochoi.databinding.DongSuaViecBinding
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

    /** Dang gui trang thai viec nha. Tach khoi [dangGui] vi hai duong doc lap. */
    private var dangGuiViec = false

    /** Cac viec ba dang tich de giao, khi chua co phien nao chay. */
    private val daChonViec = linkedSetOf<String>()

    private var loiViec = ""

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

    /**
     * Nghe xem dot viec nha con nam tren Firestore khong.
     *
     * Con nghia la chua ai nhan - tablet dang tat, hay no bo qua vi ban qua cu. Luc
     * do ba con nut de gui lai. Tablet nhan va khep xong thi no xoa document, va do
     * moi la luc bo dot viec trong may nay di.
     */
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
        if (!Nha.daGhep(this)) return

        ngheTraLoi = Kho.ngheTraLoi(this) { tra ->
            if (tra.luc <= traLoiLuc) return@ngheTraLoi
            traLoi = tra.chu
            traLoiLuc = tra.luc
            veLai()
        }

        /*
         * Document con nam do nghia la chua ai nhan; mat di nghia la tablet da khep
         * dot viec lai.
         *
         * Bo qua trong luc dang gui: Firestore bao lai ban vua ghi ngay tren may nay
         * truoc khi len toi may chu, va mot ban "khong ton tai" con dang bay ve tu
         * truoc do co the toi sau - luc ay no se xoa mat dot ba vua giao.
         */
        ngheViecNha = Kho.ngheViecNha(this) { conDo ->
            if (dangGuiViec) return@ngheViecNha
            if (!conDo) ViecNha.xoaPhien(this)
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
        val dangGiao = ViecNha.dangGiao(this).isNotEmpty()
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
        val dangGiao = ViecNha.dangGiao(this)
        binding.boxViec.removeAllViews()

        if (dangGiao.isEmpty()) {
            daChonViec.retainAll(ViecNha.danhSach(this).map { it.ten }.toSet())
            binding.txtViecPhuDe.text = getString(R.string.viec_chua_giao)
            ViecNha.danhSach(this).take(ViecNha.TOI_DA_MOI_PHIEN).forEach { v ->
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
            val phut = ViecNha.danhSach(this).filter { it.ten in daChonViec }.sumOf { it.phut }
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

        // Xong het ma dot viec van con trong may nghia la tablet chua nhan: listener
        // ben [batNghe] xoa dot nay ngay khi tablet bao da khep lai.
        val choNhan = dangGiao.all { it.xong }
        binding.txtViecPhuDe.text = when {
            loiViec.isNotEmpty() -> loiViec
            choNhan -> getString(R.string.viec_cho_nhan)
            else -> getString(R.string.viec_dang_giao)
        }
        dangGiao.forEach { v ->
            themDongViec(
                ten = v.ten,
                phu = if (v.xong) getString(R.string.viec_da_xong) else "${v.phut} phút",
                nhanChinh = if (v.xong) getString(R.string.viec_da_xong)
                else getString(R.string.viec_xong),
                chinhMo = !v.xong && !dangGuiViec,
                khiChinh = { xongViec(v.ten) },
                nhanPhu = if (v.xong) null else getString(R.string.viec_bo),
                khiPhu = { boViec(v.ten) }
            )
        }
        binding.btnGiaoViec.visibility = View.VISIBLE
        binding.btnGiaoViec.isEnabled = !dangGuiViec
        binding.btnGiaoViec.text = when {
            dangGuiViec -> getString(R.string.viec_dang_gui)
            loiViec.isNotEmpty() -> getString(R.string.thu_lai)
            choNhan -> getString(R.string.viec_gui_lai)
            else -> getString(R.string.viec_bo_het)
        }
        binding.btnGiaoViec.setOnClickListener {
            if (loiViec.isNotEmpty() || choNhan) dongBoViec() else boHetViec()
        }
    }

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

    /**
     * Ba giao mot dot viec moi.
     *
     * Ghi xuong may TRUOC roi moi gui, y het [bam]: app bi dong giua chung thi cai
     * con lai tren may van la cai ba vua bam, va nut "Thu lai" gui lai duoc. Gui
     * truoc ghi sau thi co canh tablet dang khoa man hinh ma ben nay khong biet
     * minh da giao gi.
     */
    private fun giaoViec() {
        if (dangGuiViec || daChonViec.isEmpty()) return
        if (!Nha.daGhep(this)) return hoiCaiDat()
        ViecNha.giao(this, ViecNha.danhSach(this).filter { it.ten in daChonViec })
        daChonViec.clear()
        dongBoViec()
    }

    private fun xongViec(ten: String) {
        if (dangGuiViec) return
        ViecNha.danhDauXong(this, ten)
        dongBoViec()
    }

    private fun boViec(ten: String) {
        if (dangGuiViec) return
        ViecNha.boViec(this, ten)
        dongBoViec()
    }

    private fun boHetViec() {
        if (dangGuiViec) return
        ViecNha.boHet(this)
        dongBoViec()
    }

    /**
     * Day trang thai viec nha hien tai sang tablet.
     *
     * Gui CA DANH SACH moi lan chu khong gui rieng cai vua doi: mot ban trang thai
     * day du thi tablet doc duoc ban nao cung dung, con gui su kien roi le thi mat
     * mot cai la hai ben lech nhau vinh vien.
     *
     * Xong het thi xoa phien SAU KHI gui duoc, khong phai truoc: xoa truoc ma mang
     * rot thi tablet con khoa man hinh, ma ben nay khong con gi de gui lai.
     */
    private fun dongBoViec() {
        val cac = ViecNha.dangGiao(this)

        dangGuiViec = true
        loiViec = ""
        veLai()

        // Khong gui kem cau mo ta cho nguoi doc nua. Truoc kia phai gui, vi dong do
        // hien thang trong nhom Telegram cho Ba Huy xem; bay gio tablet tu dat cau
        // theo viec no lam duoc - va no moi la ben biet minh da lam duoc gi.
        Kho.datViecNha(
            this,
            maPhien = ViecNha.maPhien(this),
            cac = cac
        ) { kq ->
            dangGuiViec = false
            when (kq) {
                /*
                 * Ghi duoc roi, nhung KHONG xoa dot viec trong may o day.
                 *
                 * Ghi len Firestore xong chi co nghia la du lieu da nam tren may chu,
                 * khong co nghia la tablet da nhan va cong gio - tablet co the dang
                 * tat, hay dang bo qua vi ban qua nua tieng. Xoa o day la vut mat thu
                 * duy nhat con gui lai duoc: ba quay ve man chon viec, khong con nut
                 * nao de bam, va so phut chau lam ra mat luon ma khong ai biet.
                 *
                 * Cho tablet bao da nhan roi moi xoa, xem [batNghe].
                 */
                is Kho.KetQua.Xong -> loiViec = ""

                is Kho.KetQua.Hong -> loiViec = kq.viSao
            }
            veLai()
        }
    }

    /**
     * Sua danh sach viec nha: ten va so phut cua tung viec.
     *
     * Nguoi doc man nay la Ba Huy. Ba noi chi bam cac nut o man chinh, khong bao gio
     * vao day.
     *
     * Moi dong co mot nut xoa. Truoc day khong co, va cach bo mot viec la de trong
     * o ten roi bam Xong - mot quy uoc khong ghi o dau tren man hinh ca.
     */
    private fun hoiCaiDatViec() {
        val cot = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val p = (20 * resources.displayMetrics.density).toInt()
            setPadding(p, p / 2, p, 0)
        }
        val cacDong = mutableListOf<DongSuaViecBinding>()

        fun themHang(ten: String, phut: Int, viTri: Int = -1): DongSuaViecBinding {
            val d = DongSuaViecBinding.inflate(layoutInflater, cot, false)
            d.oTen.setText(ten)
            d.oPhut.setText(phut.toString())
            d.nutXoa.setOnClickListener {
                cot.removeView(d.root)
                cacDong.remove(d)
            }
            cacDong += d
            if (viTri < 0) cot.addView(d.root) else cot.addView(d.root, viTri)
            return d
        }

        ViecNha.danhSach(this).forEach { themHang(it.ten, it.phut) }

        // Nut them nam duoi cung, va hang moi chen vao ngay truoc no.
        val nutThem = MaterialButton(
            this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle
        ).apply {
            text = getString(R.string.viec_them)
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                themHang("", 10, cot.childCount - 1).oTen.requestFocus()
            }
        }
        cot.addView(nutThem)

        val cuon = ScrollView(this).apply { addView(cot) }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.viec_sua_danh_sach)
            .setView(cuon)
            .setPositiveButton(R.string.xong) { _, _ ->
                val moi = cacDong.mapNotNull { d ->
                    val ten = d.oTen.text.toString().trim()
                    if (!ViecNha.tenHopLe(ten)) return@mapNotNull null
                    val phut = d.oPhut.text.toString().trim().toIntOrNull() ?: 10
                    ViecNha.Viec(ten, phut.coerceIn(0, 240))
                }
                if (moi.isNotEmpty()) ViecNha.datDanhSach(this, moi)
                veLai()
            }
            .setNegativeButton(R.string.huy, null)
            .show()
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
        db.btnSuaViec.setOnClickListener { hoiCaiDatViec() }

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
