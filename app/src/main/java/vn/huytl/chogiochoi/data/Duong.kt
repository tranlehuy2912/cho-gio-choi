package vn.huytl.chogiochoi.data

/**
 * Ten duong dan va ten truong trong Firestore.
 *
 * QUAN TRONG: file nay co BA ban sao phai giong het nhau:
 *  - vn.huytl.homeworkgate.dongbo.Duong  (tablet, nop-bai)
 *  - vn.huytl.bangdieukhien.data.Duong   (dien thoai Ba Huy, bang-dieu-khien)
 *  - vn.huytl.chogiochoi.data.Duong      (may ba noi, cho-gio-choi)
 *
 * Doi mot chuoi o mot ben thoi la ba app noi ba thu tieng: lenh gui di khong ai
 * nhan, trang thai doc ve luon rong, va khong co gi bao loi ca - do la kieu hong
 * kho tim nhat. Khong co trinh bien dich nao bat duoc, nen sua o day xong thi
 * chay nop-bai/tools/kiem-duong.sh de so ca ba ban.
 */
object Duong {

    // QUA_CU_MS (30 phut) bo ngay 8/10/2026. Truoc do tablet bo lenh trong lenh/, lenh go
    // qua Telegram va dot viec nha chua tung thay neu cu hon nua tieng (tru TINCO), de "cho
    // 60 phut" bam toi qua khong tu mo gio choi luc sang som. Anh Huy chot: lenh nao cung
    // chay, tre bao lau cung chay; tablet lam dung thu tu bam, bot gio duoc moi luc.

    const val NHA = "nha"

    /**
     * Lich hoc cua mot nha (thoi khoa bieu va ngay nghi), tu 4/10/2026. Collection rieng,
     * document mang dung ma nha, mot truong [F_JSON] la chuoi JSON, khuon xem BanLich ben
     * tablet va Bang dieu khien.
     *
     * Nam ngoai nha/{maNha} vi luat Firestore cho nguoi nha ghi moi thu trong do. O day luat
     * cho nguoi nha DOC, khong may nao ghi: Claude Code ghi qua Firebase console (khong qua
     * luat), tablet cua Le Hoa khong sua duoc lich de khoi bi chan.
     */
    const val LICH_HOC = "lichhoc"
    const val F_JSON = "json"

    /**
     * Laptop xem Netflix cua Le Hoa (7/10/2026). Collection rieng, document mang dung ma nha.
     *
     * Laptop chay Linux Mint, Timekpr-nExt dem gio cua tai khoan Le Hoa. Mot chuong trinh
     * chay ngam tren laptop nghe [CAP] (tu 9/10/2026 giu mot ket noi nghe Firestore, phieu toi
     * trong vai giay; truoc do hoi mot phut mot lan), cong gio cho tung phieu roi xoa phieu, va
     * ghi lai vao document nay luc nao het gio.
     *
     * Nam ngoai nha/{maNha} vi laptop khong phai nguoi nha: cho no vao [F_UIDS] la cho no
     * ghi moi thu trong nha. Laptop chi doc ghi duoc o day, nhan ra bang [F_UID_LAPTOP].
     * Document nay do Claude Code lap qua Firebase console (ghi [F_UID_LAPTOP]), khong app
     * nao lap duoc.
     *
     * Cac truong laptop ghi: [F_KET_THUC_LUC] luc het gio khi con dang dung laptop (0 khi
     * khong ai dung, dien thoai tu dem lui nhu the Gio choi), [F_CON_LAI_MS] so ms con lai
     * khi khong ai dung, [F_DANG_DUNG], [F_CAP_CUOI] { phut, ai, luc } phieu vua cong,
     * [F_CAP_NHAT_LUC] luc ghi, [F_WEB_DANG_MO]. Chi ghi khi co gi doi, khong ghi moi phut.
     */
    const val LAPTOP = "laptop"

    /**
     * Phieu cap gio xem Netflix: laptop/{maNha}/cap/{id}. Su kien nhu [LENH]: laptop cong
     * xong thi xoa.
     *
     * Truong: [F_PHUT] so phut Netflix, [F_PHUT_CHOI] so phut choi tablet da doi ra (0 khi Ba
     * Huy cho them), [F_AI] ([Nguoi.LE_HOA] hay [Nguoi.BA_HUY]), [F_TAO_LUC]. Phieu tao tu hom
     * truoc thi laptop bo: phut Netflix chi dung trong ngay, nhu phieu gio choi tablet.
     *
     * Tu 9/10/2026 [F_PHUT] am la phieu bot (Ba Huy bam "Bớt Netflix", anh Huy chot "cap them thi
     * cung phai bot"): laptop bot chung ay phut, bot qua so dang con thi ve 0. Luat Firestore cho
     * phut tu -600 toi 600, khac 0. Laptop ban truoc 9/10/2026 bo phieu am ma khong bot.
     */
    const val CAP = "cap"
    const val F_UID_LAPTOP = "uidLaptop"
    const val F_PHUT_CHOI = "phutChoi"
    const val F_DANG_DUNG = "dangDung"
    const val F_CAP_CUOI = "capCuoi"

    /**
     * Ba Huy muon mo web tren laptop (7/10/2026): Bang dieu khien ghi, true la go luat chan web
     * cua Firefox, false la dat lai. Khong co han, anh Huy chon. Day la truong duy nhat cua
     * [LAPTOP] ma nguoi nha ghi duoc. Le Hoa dang nhap laptop thi laptop van khoa web.
     */
    const val F_MO_WEB = "moWeb"

    /** Laptop bao web dang mo that hay khong (luat Firefox dang go). Chi laptop ghi. */
    const val F_WEB_DANG_MO = "webDangMo"

    /**
     * Lenh Ba Huy gui laptop tu the Laptop (8/10/2026): laptop/{maNha}/lenh/{id} { [F_KIEU]
     * ([LenhLaptop]), [F_CHU] cau nhan, [F_SO_LAN] so lan doc, [F_AI], [F_TAO_LUC] }. Su kien nhu
     * [LENH] cua tablet: laptop nghe chung ket noi voi [CAP] (truoc 9/10/2026 hoi chung vong mot
     * phut), xoa lenh roi moi lam, xong ghi ket qua vao [F_KET_QUA]. Lenh cho qua [LENH_LAPTOP_HET_HAN_MS] ma laptop chua lam thi bo,
     * ca hai ben cung bo (anh Huy chot 8/10/2026): khong thi bam "Tắt máy" luc laptop dang tat
     * se lam laptop tu tat ngay lan bat sau.
     */
    const val F_SO_LAN = "soLan"
    const val LENH_LAPTOP_HET_HAN_MS = 5 * 60_000L

    /** Cau nhan len tivi dai toi da chung nay ky tu (anh Huy chot 8/10/2026), luat Firestore chan. */
    const val NHAN_TOI_DA = 200

    /**
     * Cac truong laptop ghi them tu 8/10/2026. [F_PHIEN] ten tai khoan dang ngoi man hinh ("lehoa",
     * "huy", rong la dang o man dang nhap), [F_PHIEN_TU] luc tai khoan do dang nhap. [F_BAT_LUC]
     * luc may bat lan nay, [F_TAT_LUC] luc may tat (0 khi dang chay): tatLuc tu batLuc tro di la
     * laptop da tat dung cach. [F_SU_KIEN] cac lan bat, tat, dang nhap trong ngay: [{ [F_KIEU]
     * ([SuKienLaptop]), [F_LUC], [F_AI] ten tai khoan }]. [F_KET_QUA] ket qua nam lenh gan nhat:
     * [{ [F_ID] ma lenh, [F_KIEU], [F_OK], [F_CHU] cau bao, [F_LUC] }].
     */
    const val F_PHIEN = "phien"
    const val F_PHIEN_TU = "phienTu"
    const val F_BAT_LUC = "batLuc"
    const val F_TAT_LUC = "tatLuc"
    const val F_SU_KIEN = "suKien"
    const val F_KET_QUA = "ketQua"
    const val F_ID = "id"
    const val F_OK = "ok"

    /**
     * Anh chup man hinh laptop moi nhat: laptop/{maNha}/anh/moinhat { [F_JPG] anh JPEG, [F_LUC],
     * [F_PHIEN] }. Chi laptop ghi, anh sau de anh truoc (anh Huy chot 8/10/2026).
     */
    const val ANH = "anh"
    const val D_MOI_NHAT = "moinhat"
    const val F_JPG = "jpg"

    /**
     * So tung ngay cua laptop: laptop/{maNha}/ngay/{yyyy-MM-dd} { [F_SU_KIEN], [F_CAP_NHAT_LUC] }
     * (9/10/2026), cho the "Thời gian dùng laptop" o tab Nhat ky. Chi laptop ghi, khi co gi doi;
     * giu hom nay va 6 ngay truoc, cu hon thi laptop xoa. [F_SU_KIEN] cua document laptop chi giu
     * hom nay nen can cho rieng nay. Qua nua dem ma con nguoi ngoi man hinh thi laptop ghi mot
     * dong vao luc 0 gio cua ngay moi.
     */
    const val LAPTOP_NGAY = "ngay"

    // --- cac document trong mot nha ---
    const val HOP = "hop"
    const val D_TRANG_THAI = "trangthai"
    const val D_CAI_DAT = "caidat"
    const val D_DANH_SACH_APP = "danhsachapp"

    /**
     * Dot viec nha dang giao. Mot document, khong phai mot muc trong hang lenh.
     *
     * Vi day la TRANG THAI day du chu khong phai su kien: ca danh sach viec lan
     * viec nao da xong nam gon trong mot ban. Doc lai cung mot ban muoi lan cung
     * khong sinh ra muoi lan cong gio, vi ViecNha.apDung ben tablet so voi ban
     * dang giu roi moi quyet co gi de lam khong.
     *
     * Lenh cho gio thi nguoc lai - mot su kien, lam xong la xoa - nen no nam trong
     * [LENH].
     *
     * HAI DIEN THOAI CUNG GHI: may ba noi va Bang dieu khien. Moi lan bam la mot
     * transaction doc ban tren may chu, sua dung viec vua bam roi moi ghi. Ghi de ca
     * ban tu bo nho rieng cua tung may thi hai nguoi bam gan nhau la cai bam truoc
     * mat, va tablet khoa lai vi mot viec da co nguoi bao xong.
     */
    const val D_VIEC_NHA = "viecnha"

    /**
     * KHONG CON DUNG tu 30/9/2026. Tung giu vo dan do cua buoi vua hoc (cung dang voi
     * [F_DAN_DO], them [F_LUC]), de Bang dieu khien hien the "Nhờ Claude đọc vở" khi may doc
     * khong duoc, roi gui ket qua ve bang lenh [Lenh.DOC_VO]. Gio may doc khong duoc thi con
     * tu go tren tablet, trang da soat nam o [D_NHAC_BAI], va tablet chi xoa document nay.
     * Giu ten de tablet biet xoa, va de khong ai dung lai ten nay cho viec khac.
     */
    const val D_DAN_DO = "dando"

    /**
     * Cac dong dan do chua toi han, gom theo buoi hoc phai xong. Chi tablet ghi.
     *
     * Tu 30/9/2026 vo dan do dung de nhac bai: moi dong han toi tiet sau cua dung mon do,
     * dong khong doc ra mon thi han la buoi hoc ke tiep. Document co [F_CAC_BUOI]: mang
     * { ma, ngay, ten, vaoHoc, cac }, buoi som truoc. ma la khoa buoi "20261003-CHIEU",
     * ngay dang yyyy-MM-dd, ten la "chiều thứ bảy 3/10", vaoHoc la epoch ms luc vao hoc.
     * cac la mang { chu, bai, mon, ngayVo }: bai true la dong con tich la bai tap, mon la
     * ten mon trong thoi khoa bieu (rong la khong doc ra), ngayVo la ngay ghi tren trang
     * vo co dong do.
     *
     * Tablet ghi de ca ban moi khi danh sach doi, ke ca khi mot buoi vua bat dau va cac
     * dong cua no roi ra. Khong con dong nao thi xoa document.
     */
    const val D_NHAC_BAI = "nhacbai"

    /**
     * Danh sach viec de chon khi giao: ten va so phut, o truong [F_VIEC].
     *
     * Bang dieu khien ghi, may ba chi doc. Chua co document nay thi hai may cung dung
     * mot danh sach mac dinh viet san trong app, nen van hien giong nhau.
     */
    const val D_DANH_SACH_VIEC = "danhsachviec"

    /**
     * Danh sach viec dai nhat bay nhieu, cung la so viec nhieu nhat mot dot.
     *
     * Hai man hinh quyet con so nay: man may ba ke het danh sach, ma cuon xuong la
     * thu kho nhat voi ba; man chan tablet ke het viec chua xong, dai hon nam dong thi
     * Le Hoa doc khong vao. Bang dieu khien khong cho luu dai hon, may ba chi hien
     * ngan nay.
     */
    const val TOI_DA_VIEC = 5

    /**
     * So ghi Le Hoa dung app gi, tu may gio den may gio (so NhatKySuDung ben tablet).
     * Chi tablet ghi.
     *
     * Mot document cho ca so, khong phai moi ngay mot document nhu [NHAT_KY]: lan nao
     * tablet cung ghi de ca ban, va ban do chi co [F_GIU_NGAY] ngay gan nhat. Nho vay
     * ngay cu roi khoi Firestore o lan ghi sau, y nhu no roi khoi tablet, khong ai
     * phai di xoa.
     *
     * Tablet chi ghi khi dien thoai go [Lenh.PING]. So tren tablet doi vai phut mot
     * lan trong luc con dung may; day theo tung lan doi thi ton vai tram luot ghi mot
     * ngay ma phan lon khong ai doc. Con mo Bang dieu khien ra la da go PING, nen luc
     * Ba Huy nhin thi so da moi.
     */
    const val D_SU_DUNG = "sudung"

    const val LENH = "lenh"
    const val BAI = "bai"
    const val NHAT_KY = "nhatky"
    const val HOI_AI = "hoiai"
    const val GHEP = "ghep"

    /**
     * So cai cac cau con da lam, moi lan cham mot document.
     *
     * Day len day de no song sot qua lan cai lai app: du lieu trong may mat sach khi
     * go app, ma so cai la thu duy nhat chan viec chup lai bai cu de lay gio lan nua.
     * Mat no thi ca ngan hang cau hoi tro thanh vo nghia.
     */
    const val SO_CAI = "socai"

    // --- truong trong nha/{nhaId} ---
    const val F_UIDS = "uids"

    /**
     * Nguoi nha quyen han: may ba noi.
     *
     * Tach khoi [F_UIDS] vi hai muc quyen khac han. Ai o trong [F_UIDS] thi go duoc
     * moi lenh, ke ca KHOA may hay tat quan tri thiet bi. Ai o day thi chi giao viec
     * nha - luat ben firestore.rules chan tan goc, chu khong trong vao viec app ben
     * may ba khong hien nhung nut kia ra. Tu 26/9/2026 danh sach nay khong tao duoc
     * lenh nao, ke ca [Lenh.CHO].
     */
    const val F_UIDS_PHU = "uidsPhu"

    const val F_TEN_CON = "tenCon"
    const val F_MA_GHEP = "maGhep"
    const val F_MA_GHEP_HET_HAN = "maGhepHetHan"

    // --- truong trong hop/trangthai ---
    const val F_CONG = "cong"
    const val F_KET_THUC_LUC = "ketThucLuc"
    const val F_CON_LAI_MS = "conLaiMs"

    /** Ca phien dai bao nhieu ms. Dung yen suot phien, de ve thanh chay. */
    const val F_TONG_PHIEN_MS = "tongPhienMs"
    const val F_PHUT_DA_DUYET = "phutDaDuyet"
    const val F_PHUT_CON_LAI = "phutConLai"

    /**
     * So ms con da choi that hom nay, tinh toi luc doan dang chay bat dau (1/10/2026).
     *
     * Doan dang chay thi ben dien thoai tu cong them tu [F_DOAN_CHOI_TU], giong cach no tu
     * tru [F_KET_THUC_LUC]: so nay va moc kia chi doi khi phien doi trang thai, nen khong
     * them luot ghi nao. Cong voi phan dang giu ([F_KET_THUC_LUC] luc dang choi, [F_CON_LAI_MS]
     * luc khac) la so "được chơi" cua thanh ngay ben tablet, gom ca gio nguoi lon cho.
     *
     * Khong co truong nay la tablet ban cu: ben dien thoai hien kieu "Đã duyệt hôm nay" nhu
     * truoc.
     */
    const val F_DA_CHOI_MS = "daChoiMs"

    /** Luc doan phien dang chay bat dau, theo gio tablet. 0 la khong co doan nao chay. */
    const val F_DOAN_CHOI_TU = "doanChoiTu"
    const val F_SO_BAI_CHO = "soBaiCho"

    /**
     * So phut trong "Quỹ giờ chơi" cua tablet (tu 29/9/2026): phut lam tren may ma tran rieng
     * cua phan do cat. Bang dieu khien hien so nay va cap tu quy bang lenh [Lenh.CAP_QUY].
     */
    const val F_QUY_GIO = "quyGio"

    /**
     * Ten cac viec nha CHUA xong, dang danh sach chuoi.
     *
     * Co truong nay thi ben dien thoai moi giai thich duoc man hinh dang khoa: thieu
     * no, Bang dieu khien chi thay "dang tam dung" trong khi tablet bi che kin va
     * khong ai biet vi sao.
     *
     * Khac [D_VIEC_NHA]: cho kia la ban hai dien thoai ghi xuong de giao viec, cho
     * nay la tablet noi lai da nhan duoc gi.
     */
    const val F_VIEC_NHA = "viecNha"
    const val F_CHE_DO_BA = "cheDoBa"
    const val F_QUYEN = "quyen"
    const val F_PIN_MAY = "pinMay"
    const val F_DANG_SAC = "dangSac"
    const val F_BAN_APP = "banApp"

    /**
     * Phien ban lich tablet dang dung (so phienBan trong [LICH_HOC]). Ghi lich moi len
     * Firestore xong thi xem so nay de biet tablet da nhan chua.
     */
    const val F_LICH = "lich"
    const val F_CAP_NHAT_LUC = "capNhatLuc"

    /**
     * Ten app dang tren man hinh tablet: "YouTube", hay "YouTube + Zalo" khi chia
     * doi man hinh. Rong la khong mo app nao - dang o man hinh chinh hay man khoa.
     *
     * Tablet ghi lai moi lan doi app, khong doi nhip tim.
     *
     * LUON LA CHUOI, khong duoc doi kieu. Ban Bang dieu khien dau tien doc truong
     * nay bang getString, ma getString gap kieu khac thi nem loi ngay trong luc doc
     * trang thai: app ben dien thoai chet moi lan tablet day len. Can them thong tin
     * thi them truong moi nhu hai truong duoi - ban cu gap truong la chi bo qua.
     */
    const val F_APP_TRUOC_MAT = "appTruocMat"

    /** Luc mo app do, theo gio tablet. 0 la khong co app nao. */
    const val F_APP_TRUOC_MAT_TU = "appTruocMatTu"

    /**
     * Man hinh tablet dang sang hay tat.
     *
     * Vang truong nay nghia la khong biet: tablet ban cu, hoac dich vu canh app dang
     * khong chay nen khong ai nhin thay tren man hinh co gi.
     */
    const val F_MAN_HINH_SANG = "manHinhSang"

    /** Cau tablet noi lai sau khi lam mot lenh: { chu, luc, ai }. */
    const val F_TRA_LOI = "traLoi"

    /**
     * Cac de thi in san trong tablet (30/9/2026), cho hang "Đề thi thử" cua Bang dieu khien.
     *
     * Mang { ma, ten, den, tt, sao, toiDa } theo thu tu trong bo de. ma la ma de ("GK1-1"),
     * gui lai trong [Lenh.MO_DE_THI]. den la Unit cuoi de cham toi: tablet tu mo de khi lop
     * hoc toi Unit do. tt la mot trong KHOA (chua toi pham vi), SAN (mo duoc), MO (dang mo,
     * chua bat dau), DANG (dang lam), XONG (da nop). sao va toiDa la diem lan nop gan nhat,
     * -1 khi chua nop lan nao. Vang truong nay la tablet ban cu, chua co de thi.
     *
     * Tu 1/10/2026 chi con de Tieng Anh: de Toan, KHTN mo theo bai chu khong theo Unit, nen
     * nam o [F_CAC_DE_THI]. Bang dieu khien ban cu chi doc truong nay, va neu de Toan nam o
     * day thi no xep de do vao hang Tieng Anh, kem mot so Unit vo nghia.
     */
    const val F_DE_THI = "deThi"

    /**
     * Cac de thi in san cua moi mon (1/10/2026), cho cac hang "Đề thi thử <môn>" cua Bang
     * dieu khien. Doc truong nay truoc; vang thi tablet la ban cu, doc [F_DE_THI].
     *
     * Mang { ma, mon, ten, phamVi, tt, thieu, sao, toiDa, nopLuc, phut, doRong } theo thu tu cac
     * bo de trong tablet. ma, tt, sao, toiDa nhu [F_DE_THI]. mon la ten mon day du ("Toán",
     * "Khoa học tự nhiên", "Tiếng Anh"). phamVi la chu ta pham vi cua de ("Đại số tới Bài 9,
     * Hình học tới Bài 14", "tới Unit 3"). thieu la chu noi phan nao lop chua hoc toi ("Hình
     * học mới tới Bài 12, đề cần Bài 14"), rong khi tt khac KHOA. nopLuc la luc nop lan
     * gan nhat (ms), 0 khi chua nop lan nao. phut la gio lam bai cua de. doRong la do rong pham
     * vi (so Unit cua de Anh, tong so bai cac phan cua de Toan, KHTN), de dien thoai chon de hep
     * nhat ma khong phai doc chu phamVi.
     */
    const val F_CAC_DE_THI = "cacDeThi"

    // --- truong trong lenh/{id} ---
    const val F_KIEU = "kieu"
    const val F_PHUT = "phut"
    const val F_BAI_ID = "baiId"
    const val F_CHU = "chu"
    const val F_TAO_LUC = "taoLuc"

    /**
     * Ai go lenh nay, xem [Nguoi].
     *
     * Tablet phai biet vi hai nguoi khong cung quyen: lenh cua ba noi chi duoc la cho
     * gio, mot luot moi ngay, Ba Huy thi khong gioi han. Tu 26/9/2026 luat khong cho
     * may ba tao lenh nao nua, nen lenh mang banoi chi con la lenh cu. No con quyet ca
     * cau ghi vao nhat ky, thu ma toi lam Le Hoa doc.
     *
     * Thieu truong nay thi coi la Ba Huy: ban Bang dieu khien cu chua gui gi ca, ma
     * may ba thi luon gui.
     *
     * Trong hop/viecnha truong nay la nguoi giao dot do, va nguoc lai: thieu thi coi
     * la ba noi, vi truoc khi Bang dieu khien giao duoc viec thi chi may ba ghi o do.
     * O day no chi de tablet goi dung nguoi giao, khong mo them quyen gi.
     */
    const val F_AI = "ai"

    // --- truong trong hop/viecnha ---

    /** Ma mot dot giao viec. Doi ma nghia la giao dot moi, khong phai sua dot cu. */
    const val F_MA_PHIEN = "maPhien"

    /**
     * Danh sach viec. Trong hop/viecnha: [{ ten, phut, xong }], rong nghia la da bo
     * het. Trong hop/danhsachviec: [{ ten, phut }].
     */
    const val F_VIEC = "viec"
    const val F_TEN = "ten"
    const val F_XONG = "xong"

    // --- truong trong bai/{id} ---
    const val F_LUC = "luc"
    const val F_TRANG_THAI = "trangThai"
    const val F_SO_PHUT = "soPhut"

    /**
     * Bai cham xong trong gio ngu: luc tablet se cong [F_SO_PHUT] phut, tuc la luc het gio
     * ngu. Di kem trang thai DUYET. Tablet cong xong thi xoa truong nay. Xem CongSang ben
     * nop-bai.
     */
    const val F_CONG_LUC = "congLuc"

    /**
     * Ly do Ba Huy khong duyet bai, di kem trang thai TUCHOI (30/9/2026). Tablet ghi luc
     * nhan [Lenh.TU_CHOI] (chep tu [F_CHU]) hay lenh /tuchoi ben Telegram. Khong co ly do
     * thi khong co truong nay. Man Bai da cham tren tablet hien no cho Le Hoa.
     */
    const val F_LY_DO = "lyDo"
    const val F_ANH = "anh"
    const val F_CHAM = "cham"
    const val F_MESSAGE_ID = "messageId"
    const val F_FILE_ID = "fileId"
    const val F_KHAU = "khau"

    /**
     * Ket qua Claude cham, Ba Huy dan tu app Claude vao Bang dieu khien. Chi Bang dieu khien
     * ghi truong nay.
     *
     * Mot map { luc, chinh, cac }, voi cac = [{ ma, dung, chac, conViet, goiY, de }]; them goi
     * khi ghi cung mot lenh, them xuLuc sau lenh [Lenh.XU_CAU]. Tu 28/9/2026 tablet khong tu
     * cham nua, nen thuong day la ban cham dau tien (chinh la true), ghi cung luc voi lenh
     * [Lenh.CHAM_BAI]; goi la nguyen giaTri gui kem lenh do, de the "Câu cần Ba Huy xem" gui
     * lai qua [Lenh.XU_CAU]. Lenh XU_CAU ghi de goi va cac. Dan lai ket qua Claude la ghi de ca
     * truong.
     *
     * Nam canh [F_CHAM] chu khong ghi de len no: [F_CHAM] la ban tablet ghi sau khi tinh phut.
     * Man Bai da cham tren tablet hien ket luan cua Claude khi co.
     */
    const val F_CHAM_CLAUDE = "chamClaude"

    /**
     * Cac cau con khai truoc khi chup, kem de tung cau.
     *
     * Map { tenNguon, bai, mon, suaBai, cac: [{ ma, cauId, de, dang }] }. Tablet
     * ghi luc con nop. Co no thi loi nho gui Claude co de bai cua tung cau, va ket qua
     * Claude cham ve khop duoc voi dung cau trong sach.
     *
     * suaBai (tu 28/9/2026) chi co o lan con NOP LAI cac cau sai cua mot bai: la ma bai do.
     * Luc do cac chi gom cac cau sai can nop lai, cau ngoai sach co cauId rong va de chep
     * tu ban cham cu. Loi nho dan Claude chi cham cau trong cac, va tablet bo cau ngoai
     * danh sach: con sua de len trang vo cu, cau da dung tu lan truoc khong duoc tra gio
     * lan hai.
     *
     * onTap (lan nop la on lai bai chup anh) bo ngay 2/10/2026: tablet thoi ghi, Bang
     * dieu khien thoi doc. Bai nop truoc ngay do van con truong nay, khong ai doc nua.
     */
    const val F_KHAI = "khai"

    /**
     * Vo dan do con da soat: noi dung cua hop/dando ([D_DAN_DO]), va truong trong bai nop
     * truoc 30/9/2026 (luc do Claude dung vo de tinh tron goi; bai nop sau ngay do khong mang
     * truong nay nua).
     *
     * Map { ngay, cacBai, dongKhac, fileId, chuaDoc, nguon, chupLuc }. ngay la ngay ghi tren
     * vo, dang yyyy-MM-dd. cacBai la cac dong con tich la bai tap, rong la hom do co khong
     * giao bai tap nao. fileId la anh trang vo tren Telegram, vang la chua gui duoc.
     *
     * chuaDoc true la chi co anh, chua ai doc ra chu: cacBai rong nhung chua biet co giao
     * gi, va ngay tam la ngay chup (khong con tu 30/9/2026, xem [D_DAN_DO]). nguon la ai doc ra danh sach: CON (con soat ban may
     * doc), CLAUDE (Claude doc qua lenh [Lenh.DOC_VO]), LUCCHAM (doc o lan cham bai dau
     * tien, chi con o ban cu). chupLuc la luc chup tam anh, giu nguyen khi doc lai hay sua
     * chu.
     */
    const val F_DAN_DO = "danDo"

    // --- truong trong nhatky/{ngay} ---
    const val F_DONG = "dong"

    // --- truong trong hop/nhacbai ---

    /** Cac buoi co dong dan do chua toi han. Xem [D_NHAC_BAI]. */
    const val F_CAC_BUOI = "cacBuoi"

    // --- truong trong hop/sudung ---

    /**
     * Cac khoang con cam may: [{ goi, tu, den }], xep theo tu. goi la ten goi app, tu
     * va den la epoch ms theo gio tablet. Khoang con dang mo luc day thi den la luc day.
     *
     * Chia doi man hinh thi hai app cung tren man hinh, va hai khoang chong len nhau.
     * Cong tong ca ngay thi phai gop phan chong nhau truoc.
     */
    const val F_DOAN = "doan"
    const val F_GOI = "goi"
    const val F_TU = "tu"
    const val F_DEN = "den"

    /**
     * Ten doc duoc cua tung app co trong [F_DOAN]: [{ goi, ten }], cung dang voi danh
     * sach trong hop/danhsachapp.
     *
     * Dien thoai khong cai cac app do nen khong tu tra ten duoc. App da go khoi tablet
     * thi ten la chinh ten goi.
     */
    const val F_APP = "app"

    /** Tablet giu so bao nhieu ngay, tinh ca hom nay. */
    const val F_GIU_NGAY = "giuNgay"

    /**
     * Dich vu canh app tren tablet co dang chay luc day khong.
     *
     * Tat thi tablet khong ghi duoc gi, ma cung khong chan gi. So trong tron luc do
     * nhin giong nhu hom nay khong ai dung may, nen ben doc phai noi ra.
     */
    const val F_DANG_GHI = "dangGhi"
}

/**
 * Ai dang go lenh.
 *
 * Chuoi chu chu khong phai enum, y het [Lenh]: gia tri nay ghi xuong Firestore va
 * nam do cho den khi tablet doc, nen doi ten hang trong ma nguon khong duoc phep
 * lam lech y nghia cua thu da ghi.
 */
object Nguoi {
    const val BA_HUY = "bahuy"
    const val BA_NOI = "banoi"

    /** Le Hoa tu doi phut choi tren tablet, chi dung trong phieu [Duong.CAP] cua laptop. */
    const val LE_HOA = "lehoa"
}

/**
 * Cac kieu lenh dien thoai gui sang tablet.
 *
 * Chuoi chu khong phai enum: enum ghi xuong Firestore thanh ten hang, ma doi ten
 * hang la lenh cu dang nam trong hang doi thanh vo nghia. Chuoi thi nhin thay
 * ngay trong console Firebase luc do loi.
 */
object Lenh {
    /** Duyet mot bai dang cho. Kem [Duong.F_BAI_ID] va so phut. */
    const val DUYET = "DUYET"

    /** Khong duyet. Kem ly do o [Duong.F_CHU] neu co, tablet chep vao [Duong.F_LY_DO] cua bai. */
    const val TU_CHOI = "TUCHOI"

    /**
     * Cho choi ngay, khong tru han muc ngay. Dang choi thi cong them.
     *
     * Truoc 26/9/2026 day la lenh duy nhat may ba noi go duoc. Gio may ba khong go
     * lenh nao, xem [Duong.F_UIDS_PHU].
     */
    const val CHO = "CHO"

    /** Bot phut cua phien dang chay. */
    const val BOT = "BOT"

    const val DUNG = "DUNG"
    const val TIEP = "TIEP"

    /** Khoa tablet ngay, mat so phut con lai. */
    const val KHOA = "KHOA"

    /** Mo toan bo may cho Ba Huy dung. Kem so phut, khong co thi khong dat han. */
    const val MO_MAY = "MOMAY"

    /** Dong che do mo toan bo may. */
    const val DONG_MAY = "DONGMAY"

    /** Tat quan tri thiet bi de go app. */
    const val CHO_GO_APP = "CHOGOAPP"

    const val XOA_PIN = "XOAPIN"

    /** Doi mot muc cau hinh. [Duong.F_CHU] la ten muc, gia tri o truong "giaTri". */
    const val CAI_DAT = "CAIDAT"

    /**
     * Cong gio cho mot dot viec nha ma tablet da bo lo.
     *
     * Ba bam xong het trong luc tablet dang tat, den luc no song lai thi ban da qua nua
     * tieng nen no bo qua - va ba thi khong con nut nao de gui lai. Bang dieu khien nhin
     * thay canh do va go lenh nay thay. Tu 8/10/2026 tablet khong bo dot cu nua, nen canh
     * do khong con xay ra.
     *
     * Tu khi Bang dieu khien cung ghi duoc hop/viecnha, no khong go lenh nay nua: no
     * bam Gui lai nhu may ba, tuc la ghi lai moc luc cua dot do, va tablet cong theo
     * duong thuong. Tablet van nhan lenh nay, de ban Bang dieu khien cu con dung duoc.
     *
     * Khac [CHO] o dung mot cho, ma cho do la ly do no ton tai: nhat ky ghi "Xong
     * viec nha (quet nha, rua chen): +20 phut" chu khong phai "Ba Huy cho 20 phut".
     * Le Hoa doc nhat ky tren man hinh chinh, va con so do la cong con lam ra chu
     * khong phai qua nguoi lon cho.
     *
     * Kem so phut o [Duong.F_PHUT] va danh sach ten viec o [Duong.F_CHU].
     */
    const val CONG_VIEC_NHA = "CONGVIECNHA"

    /**
     * Hoi tablet mot cau duy nhat: con song khong, va trang thai that bay gio la gi.
     *
     * VI SAO CAN. Tablet tu day trang thai moi lan co gi doi, cong them mot nhip
     * tim thua cho nhung luc khong co gi doi. Nhip tim do chay bang Handler, ma
     * Handler dem theo dong ho DUNG LAI khi CPU ngu - nen mot tablet nam im tren
     * ban ca buoi toi co the khong day gi trong nhieu tieng. Khong phai no chet,
     * chi la khong co gi de noi va khong ai hoi.
     *
     * Bang dieu khien mo ra thi go lenh nay. Tablet nghe qua listener nam san trong
     * dich vu tro nang, day mot ban trang thai day du, va man hinh ben kia co so
     * lieu dung cua GIAY NAY thay vi mot dong "so lieu co the cu".
     *
     * Khong tra loi gi vao o traLoi: day khong phai viec Ba Huy bam, khong co gi de
     * bao. Ban trang thai moi chinh la cau tra loi.
     *
     * Tablet day kem so dung app o [Duong.D_SU_DUNG], va day la luc duy nhat no day
     * so do.
     */
    const val PING = "PING"

    /**
     * Sua ban cham: nhung cau may bao sai ma Claude cham lai la dung.
     *
     * Kem [Duong.F_BAI_ID], va danh sach cau o truong "giaTri": [{ ma, de, soDong }].
     * soDong la so dong Claude ghi o lan cham lai (tu 29/9/2026; ban cu khong gui, tablet
     * doc la 0 va lay so dong bai lam trong so). Tablet chi sua cau dang cho sua, tinh phut
     * theo dung luat cong gio nhu luc may tu cham, roi cap gio va ghi so. Cau dung ma van
     * khong co so dong thi tablet khong ghi, cau van cho sua. Cau da duoc tra gio thi bo
     * qua, nen gui lai lenh nay cung khong cong gio hai lan.
     *
     * Chi Ba Huy go duoc.
     */
    const val SUA_CHAM = "SUACHAM"

    /**
     * Cham bai theo ket qua Claude. Tu 28/9/2026 day la duong cham duy nhat: tablet khong
     * tu cham nua. Dung cho bai chua cham, hay da cham ma van cho duyet.
     *
     * Kem [Duong.F_BAI_ID]. "giaTri" la { cac }, voi cac = [{ ma, dung, chac, conViet,
     * goiY, soDong, de, dang, loaiLoi }]. loaiLoi (tu 28/9/2026) la kieu sai cua cau sai,
     * mot trong bay nhan cua LoaiLoi ben tablet; thieu hay la thi tablet tu xu. Tablet tinh
     * phut theo luat, cap gio, ghi so, bao Telegram. Chi lam voi bai dang cho duyet.
     *
     * Truoc 30/9/2026 "giaTri" con mang ngayDanDo, baiDuocGiao, lamHetDanDo, coAnhDanDo va
     * trongDanDo cua tung cau, de tinh tron goi vo dan do. Bo tron goi thi tablet bo qua
     * cac truong do, Bang dieu khien ban cu con gui cung khong sao.
     *
     * Khac [SUA_CHAM]: lenh kia sua mot ban cham da co, lenh nay la ban cham dau tien.
     */
    const val CHAM_BAI = "CHAMBAI"

    /**
     * Dua mot tin cua co giao len man chinh tablet, muc "Tin cua co". Giong het lenh
     * /tinco ben Telegram.
     *
     * Ba Huy chep tin trong nhom lop Zalo roi dan vao Bang dieu khien. Noi dung o
     * [Duong.F_CHU]. Tin vao kho tin cua co, noi ba noi cam tablet len cung doc duoc.
     *
     * Tin gui toi qua ma sang nay tablet moi mo may thi van phai hien, va hien dung gio Ba
     * Huy gui. Truoc 8/10/2026 day la lenh duy nhat khong bi bo khi cu hon nua tieng; tu
     * ngay do khong lenh nao bi bo nua.
     */
    const val TIN_CO = "TINCO"

    /**
     * KHONG CON DUNG tu 30/9/2026: may doc khong duoc thi con tu go tren tablet. Tung la ket
     * qua Claude doc vo dan do cho ban chi co anh, "giaTri" la { chupLuc, ngay, cacDong:
     * [{ chu, bai }] }. Bang dieu khien ban cu con gui thi tablet tra loi la khong nhan nua.
     */
    const val DOC_VO = "DOCVO"

    /**
     * Cap gio tu "Quỹ giờ chơi" (29/9/2026). Kem [Duong.F_PHUT]; thieu hay 0 la cap het quy.
     * Khong tinh vao tran ngay, giong [CHO]: Ba Huy chu dong cho. Quy khong du thi tablet
     * cap phan con. Chi Ba Huy go duoc.
     */
    const val CAP_QUY = "CAPQUY"

    /**
     * Ba Huy tu xu nhung cau Claude doc chua chac hay khong ghi so dong (29/9/2026).
     *
     * Kem [Duong.F_BAI_ID]. "giaTri" giong het [CHAM_BAI]: Bang dieu khien gui lai nguyen
     * ban cham cua Claude, chi doi nhung cau Ba Huy vua xu. Cau Ba Huy bam Dung thi chac la
     * true, dung la true, soDong theo so Ba Huy chon; bam Sai thi chac la true, dung la
     * false; bam Chup lai thi them chupLai la true. Tablet cham lai ca bai theo dung duong
     * cua CHAM_BAI, nen chi co mot luat tinh phut va mot cho ghi so. Cau chup lai khong
     * tinh dung hay sai, con duoc nhan chup lai cau do. Chi Ba Huy go.
     */
    const val XU_CAU = "XUCAU"

    /**
     * Ba Huy bo cau sai khoi danh sach "câu cần sửa" cua tablet, khong cong phut nao
     * (30/9/2026). Dung cho cau ngoai sach con khong sua duoc nua, hay cau Ba Huy thay khong
     * dang bat con chup lai.
     *
     * Kem [Duong.F_BAI_ID]. "giaTri" la danh sach [{ ma, de }], ma va de lay tu ban cham cua
     * bai, ghep voi cau dang cho sua theo dung cach cua [SUA_CHAM]. Tablet nho luc bo: cau do
     * con sai lan nua o lan nop sau thi lai hien. So cai van giu nguyen dong sai, nen cau van
     * chi tra gio mot lan. Chi Ba Huy go.
     */
    const val BO_SUA = "BOSUA"

    /**
     * Mo mot de thi in san cho Le Hoa lam (30/9/2026). Kem ma de ("GK1-1", "TGK1-2") o
     * [Duong.F_CHU], lay tu [Duong.F_CAC_DE_THI] (tablet ban cu: [Duong.F_DE_THI]). Tablet mo
     * ca de lop chua hoc toi pham vi, vi Ba Huy chu dong bam. De dang mo thi tablet giu
     * nguyen, chi tra loi la dang mo. De vua nop trong ngay thi tu hom sau moi lam lai duoc.
     * Chi Ba Huy go.
     */
    const val MO_DE_THI = "MODETHI"
}

/** Trang thai cong, y het GateState ben tablet. */
/**
 * Cac kieu lenh Bang dieu khien gui laptop (8/10/2026), xem [Duong.F_SO_LAN]. Laptop doc cung
 * nhung chuoi nay trong netflix_gio.py (LENH_NHAN ...): doi mot ben phai doi ca ben kia.
 */
object LenhLaptop {
    /** Hien dai chu tren tivi va doc [Duong.F_CHU] [Duong.F_SO_LAN] lan bang giong Google Dich. */
    const val NHAN = "NHAN"

    /** Chup man hinh, laptop ghi vao [Duong.ANH]/[Duong.D_MOI_NHAT]. */
    const val CHUP = "CHUP"

    /** Dang xuat tai khoan dang ngoi man hinh. */
    const val DANG_XUAT = "DANGXUAT"

    const val TAT_MAY = "TATMAY"

    /**
     * Hoi laptop con song khong: laptop ghi lai trang thai, kem [Duong.F_CAP_NHAT_LUC]. Laptop chi
     * ghi khi co gi doi, nen khong co lenh nay thi dien thoai khong biet laptop tat dot ngot.
     */
    const val HOI = "HOI"
}

/** Cac dong trong [Duong.F_SU_KIEN] cua laptop. */
object SuKienLaptop {
    const val BAT = "bat"
    const val TAT = "tat"

    /** May tat dot ngot (mat dien, giu nut nguon): luc la luc cuoi con thay may chay. */
    const val MAT = "mat"
    const val VAO = "vao"
    const val RA = "ra"
}

object Cong {
    const val KHOA = "LOCKED"
    const val CHO_DUYET = "PENDING"
    const val DA_DUYET = "GRANTED"
    const val DANG_CHOI = "ACTIVE"
    const val TAM_DUNG = "PAUSED"
}
