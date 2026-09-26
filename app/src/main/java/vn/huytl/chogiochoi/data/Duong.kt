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

    /**
     * Lenh hay ban trang thai go tu lau hon chung nay thi tablet khong chay nua.
     *
     * Nua tieng: du dai de om nhung luc tablet mat mang, du ngan de khong co chuyen
     * lenh cua toi hom truoc chay vao sang hom sau - "cho 60 phut" bam toi qua tu
     * dung mo gio choi luc sang som ma khong ai bam gi.
     *
     * De o day chu khong de rieng mot ben, vi Bang dieu khien cung phai biet: no chi
     * chia ra thay giup khi mot dot viec nha da qua moc nay, tuc la tablet chac chan
     * se tu choi.
     */
    const val QUA_CU_MS = 30 * 60_000L

    const val NHA = "nha"

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
     * Vo dan do cua ngay, ban dang con hieu luc tren tablet. Chi tablet ghi.
     *
     * Cung dang voi [F_DAN_DO], them [F_LUC] la luc luu. Khong co ban nao con hieu luc
     * thi tablet xoa document nay. Bang dieu khien doc no de hien the "Nhờ Claude đọc
     * vở" khi ban do chi co anh (chuaDoc), roi gui ket qua ve bang lenh [Lenh.DOC_VO].
     */
    const val D_DAN_DO = "dando"

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
    const val CHAT = "chat"
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
    const val F_SO_BAI_CHO = "soBaiCho"

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
    const val F_ANH = "anh"
    const val F_CHAM = "cham"
    const val F_MESSAGE_ID = "messageId"
    const val F_FILE_ID = "fileId"
    const val F_KHAU = "khau"

    /**
     * Ket qua Claude cham lai, Ba Huy dan tu app Claude vao Bang dieu khien.
     *
     * Mot map { luc, cac: [{ ma, dung, chac, conViet, goiY }] }. Nam canh [F_CHAM]
     * chu khong ghi de len no: ban cham cua may van giu nguyen de doi chieu, con man
     * ket qua tren tablet thi hien ket luan cua Claude khi co.
     */
    const val F_CHAM_CLAUDE = "chamClaude"

    /**
     * Cac cau con khai truoc khi chup, kem de tung cau.
     *
     * Map { tenNguon, bai, mon, onTap, cac: [{ ma, cauId, de, dang }] }. Tablet ghi luc
     * con nop. Co no thi loi nho gui Claude co de bai ngay ca khi may khong cham, va ket
     * qua Claude cham ve khop duoc voi dung cau trong sach.
     */
    const val F_KHAI = "khai"

    /**
     * Vo dan do con da soat tu dau buoi, ma lan nop nay dung thay cho trang vo.
     *
     * Map { ngay, cacBai, dongKhac, fileId, chuaDoc, nguon, chupLuc }. Tablet chep luc con
     * nop, khi lan nop do khong chup trang vo. ngay la ngay ghi tren vo, dang yyyy-MM-dd.
     * cacBai la cac dong con tich la bai tap, rong la hom do co khong giao bai tap nao.
     * fileId la anh trang vo tren Telegram, vang la chua gui duoc; co thi anh do cung nam
     * trong [F_ANH] voi khau DAN_DO, de Bang dieu khien ban cu van gui trang vo cho Claude
     * doc.
     *
     * chuaDoc true la chi co anh, chua ai doc ra chu: cacBai rong nhung chua biet co giao
     * gi, va ngay tam la ngay chup. nguon la ai doc ra danh sach: CON (con soat ban may
     * doc), CLAUDE (Claude doc qua lenh [Lenh.DOC_VO]), LUCCHAM (doc o lan cham bai dau
     * tien). chupLuc la luc chup tam anh, giu nguyen khi doc lai hay sua chu.
     *
     * Co truong nay thi loi nho gui Claude chep san ngay va danh sach bai, y nhu cau
     * lenh cua may cham, va Claude chi con noi con lam het chua, cau nao thuoc bai co
     * giao.
     */
    const val F_DAN_DO = "danDo"

    // --- truong trong chat/{id} ---
    const val F_TU = "tu"
    const val F_DA_DOC = "daDoc"

    // --- truong trong nhatky/{ngay} ---
    const val F_DONG = "dong"

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

    /** Khong duyet. Kem ly do o [Duong.F_CHU] neu co. */
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

    /** Nhan mot cau cho con, hien thanh thong bao co tieng tren tablet. */
    const val NHAN = "NHAN"

    /**
     * Cong gio cho mot dot viec nha ma tablet da bo lo.
     *
     * Ba bam xong het trong luc tablet dang tat, den luc no song lai thi ban da qua
     * [Duong.QUA_CU_MS] nen no bo qua - va ba thi khong con nut nao de gui lai. Bang
     * dieu khien nhin thay canh do va go lenh nay thay.
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
     * Kem [Duong.F_BAI_ID], va danh sach cau o truong "giaTri": [{ ma, de }]. Tablet
     * chi sua cau dang cho sua, tinh phut theo dung luat cong gio nhu luc may tu cham,
     * roi cap gio va ghi so. Cau da duoc tra gio thi bo qua, nen gui lai lenh nay cung
     * khong cong gio hai lan.
     *
     * Chi Ba Huy go duoc.
     */
    const val SUA_CHAM = "SUACHAM"

    /**
     * Cham bai theo ket qua Claude, dung khi may chua cham bai do: tablet tat cham AI,
     * hay AI hong luc con nop.
     *
     * Kem [Duong.F_BAI_ID]. "giaTri" la { cac, ngayDanDo, baiDuocGiao, lamHetDanDo,
     * coAnhDanDo }, voi cac = [{ ma, dung, chac, conViet, goiY, soDong, mucDo, de, dang,
     * trongDanDo }]. Tablet chay dung cac buoc nhu luc AI cham xong: tinh phut theo luat,
     * ke ca tron goi vo dan do, cap gio, ghi so, bao Telegram. Chi lam voi bai dang cho
     * duyet.
     *
     * Bai co [Duong.F_DAN_DO] thi coAnhDanDo la true, va tablet lay ngay cung danh sach
     * bai tu ban no giu luc con nop. Tu Claude chi lay lamHetDanDo va trongDanDo. Ban do
     * chi co anh (chuaDoc) thi khong co danh sach: tablet lay ca ngay lan bai tu Claude,
     * roi giu lai lam vo dan do cua ngay cho cac bai sau.
     *
     * Khac [SUA_CHAM]: lenh kia sua mot ban cham da co, lenh nay la ban cham dau tien.
     */
    const val CHAM_BAI = "CHAMBAI"

    /**
     * Dua mot tin cua co giao len man chinh tablet, muc "Tin cua co". Giong het lenh
     * /tinco ben Telegram.
     *
     * Ba Huy chep tin trong nhom lop Zalo roi dan vao Bang dieu khien. Noi dung o
     * [Duong.F_CHU]. Khac [NHAN]: tin khong vao khung chat cua con ma vao kho tin cua
     * co, noi ba noi cam tablet len cung doc duoc.
     *
     * Khong bi bo vi qua [Duong.QUA_CU_MS] nhu cac lenh khac: tin gui toi qua ma sang
     * nay tablet moi mo may thi van phai hien, va hien dung gio Ba Huy gui.
     */
    const val TIN_CO = "TINCO"

    /**
     * Ket qua Claude doc vo dan do, cho ban chi co anh (may doc khong duoc).
     *
     * "giaTri" la { chupLuc, ngay, cacDong: [{ chu, bai }] }. chupLuc lay tu
     * [Duong.D_DAN_DO], de tablet chi ghi vao dung tam anh do: con chup tam khac, hay da
     * tu doc lai va soat xong, thi tablet bo ket qua nay. Chi Ba Huy go duoc.
     */
    const val DOC_VO = "DOCVO"
}

/** Trang thai cong, y het GateState ben tablet. */
object Cong {
    const val KHOA = "LOCKED"
    const val CHO_DUYET = "PENDING"
    const val DA_DUYET = "GRANTED"
    const val DANG_CHOI = "ACTIVE"
    const val TAM_DUNG = "PAUSED"
}
