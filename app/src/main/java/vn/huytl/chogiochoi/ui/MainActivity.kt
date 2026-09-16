package vn.huytl.chogiochoi.ui

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vn.huytl.chogiochoi.R
import vn.huytl.chogiochoi.data.CaiDat
import vn.huytl.chogiochoi.data.Defaults
import vn.huytl.chogiochoi.data.GhimCu
import vn.huytl.chogiochoi.data.LuotNgay
import vn.huytl.chogiochoi.databinding.ActivityMainBinding
import vn.huytl.chogiochoi.databinding.DialogCaiDatBinding
import vn.huytl.chogiochoi.databinding.ItemNutBinding
import vn.huytl.chogiochoi.telegram.Bot
import vn.huytl.chogiochoi.telegram.BotLoi
import vn.huytl.chogiochoi.telegram.HopThu
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

    /**
     * Dung moi lan mot con, khong giu lai.
     *
     * Token doi duoc trong Cai dat, ma mot lan bam chi ton vai request - giu san
     * mot con bot cu chi de roi vao canh doi token xong van goi bang token cu.
     */
    private fun bot() = Bot(CaiDat.token(this))

    private var dangGui = false

    /** Con so vua bam, giu lai de nut "Thu lai" biet gui lai bao nhieu phut. */
    private var phutVuaBam = 0
    private var loiVuaRoi = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        chuaThanhBar()
        dungNut()

        binding.btnCaiDat.setOnClickListener { hoiCaiDat() }

        // Chua cai gi thi hoi luon, khoi bat Ba Huy mo ra roi doan phai lam gi.
        if (!CaiDat.xong(this)) hoiCaiDat()
    }

    override fun onResume() {
        super.onResume()
        // Ve lai o day chu khong chi o onCreate: may de mo qua dem thi hom sau mo
        // ra phai thay nut, va 22:00 di qua thi nut phai tat.
        veLai()
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
                        "\n" + getString(R.string.xong_mai),
                    mau = R.color.xong,
                    conNut = false
                )
            }

            !CaiDat.xong(this) -> hienThe(
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
        if (!CaiDat.xong(this)) {
            hoiCaiDat()
            return
        }
        val nhom = CaiDat.nhom(this)

        dangGui = true
        phutVuaBam = phut
        loiVuaRoi = ""
        // Ghi xong roi moi goi mang. Trong luc [dangGui] thi man hinh ve the
        // "Dang gui..." chu khong ve the "Hom nay xong roi", nen ghi som o day
        // khong lam ba tuong da gui xong.
        LuotNgay.ghiNhan(this, phut)
        veLai()

        lifecycleScope.launch {
            val ketQua = withContext(Dispatchers.IO) {
                runCatching {
                    HopThu.datLenh(
                        bot(), nhom, phut, getString(R.string.child_name),
                        ghimCu = GhimCu.doc(this@MainActivity)
                    )
                }.getOrElse { HopThu.KetQua.Hong((it as? BotLoi)?.viSao ?: "Lỗi lạ: ${it.message}") }
            }
            dangGui = false
            when (ketQua) {
                is HopThu.KetQua.Xong ->
                    if (ketQua.idGhim != 0L) GhimCu.ghi(this@MainActivity, ketQua.idGhim)

                is HopThu.KetQua.Hong -> {
                    LuotNgay.traLuot(this@MainActivity)
                    loiVuaRoi = ketQua.viSao
                }
            }
            veLai()
        }
    }

    /**
     * Hoi token bot va ma nhom. Nguoi doc man nay la Ba Huy, khong phai ba noi.
     *
     * Kiem tra ca hai truoc khi luu, bang hai lan goi that: getMe xem token con
     * song, getChat xem bot vao duoc nhom chua. Go nham mot chu so roi de do thi
     * den luc ba bam that moi vo - ma luc do khong ai o day de doc cau bao loi.
     *
     * Kiem xong moi luu, va luu ca hai cung mot luc: luu token truoc roi nhom hong
     * thi may nam o trang thai nua voi, lan sau mo ra khong biet cai nao da dung.
     */
    private fun hoiCaiDat() {
        val db = DialogCaiDatBinding.inflate(layoutInflater)
        db.edToken.setText(CaiDat.token(this))
        CaiDat.nhom(this).takeIf { it != 0L }?.let { db.edNhom.setText(it.toString()) }

        val hop = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.cai_tieu_de)
            .setView(db.root)
            .setPositiveButton(R.string.cai_kiem_tra, null)
            .setNegativeButton(R.string.huy, null)
            .create()

        hop.setOnShowListener {
            // Gan tay thay vi qua setPositiveButton de hop khong tu dong dong khi
            // go sai - dong roi thi Ba Huy phai mo lai tu dau de sua mot chu so.
            val nut = hop.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            nut.setOnClickListener {
                val token = db.edToken.text.toString().trim()
                val id = db.edNhom.text.toString().trim().toLongOrNull()
                if (token.isEmpty()) {
                    db.edToken.error = getString(R.string.cai_thieu_token)
                    return@setOnClickListener
                }
                if (id == null || id == 0L) {
                    db.edNhom.error = getString(R.string.cai_sai_so)
                    return@setOnClickListener
                }

                nut.isEnabled = false
                nut.text = getString(R.string.cai_dang_kiem)
                lifecycleScope.launch {
                    val thu = Bot(token)
                    val ketQua = withContext(Dispatchers.IO) {
                        runCatching { thu.xemBot() to thu.xemNhom(id).optString("title") }
                    }
                    nut.isEnabled = true
                    nut.text = getString(R.string.cai_kiem_tra)
                    ketQua.onSuccess { (tenBot, tenNhom) ->
                        CaiDat.datToken(this@MainActivity, token)
                        CaiDat.datNhom(this@MainActivity, id)
                        hop.dismiss()
                        veLai()
                        MaterialAlertDialogBuilder(this@MainActivity)
                            .setMessage(getString(R.string.cai_xong, tenBot, tenNhom))
                            .setPositiveButton(R.string.xong, null)
                            .show()
                    }.onFailure { loi ->
                        val cau = (loi as? BotLoi)?.viSao ?: loi.message
                        // Loi token thi bao o o token, con lai bao o o nhom: dat cau
                        // bao ngay duoi cho phai sua, khong bat doc roi tu doan.
                        if (cau.orEmpty().contains("Token", true)) {
                            db.edToken.error = cau
                        } else {
                            db.edNhom.error = cau
                        }
                    }
                }
            }
        }
        hop.show()
    }

    /** Ngoai khung gio tablet con doc duoc hop thu. */
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
