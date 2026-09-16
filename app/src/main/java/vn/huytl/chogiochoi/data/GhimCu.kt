package vn.huytl.chogiochoi.data

import android.content.Context

/**
 * Nho tin ghim lan truoc, de lan sau go no xuong.
 *
 * Telegram khong thay ghim: ghim tin moi thi tin cu VAN con ghim, danh sach ghim
 * cu the dai ra mai. Moi ngay ba cho mot lan la mot nam sau cho ghim co ba tram
 * sau muoi lam tin, thanh trai bar "Pinned 1/365" tren dau cuoc tro chuyen.
 *
 * Co may doc duoc thi khong sao - getChat.pinned_message tra ve tin co message_id
 * lon nhat trong dam dang ghim, tuc la lenh moi nhat, luon dung. Day thuan tuy la
 * don cho mat nguoi nhin.
 *
 * Go dung tin minh da ghim chu khong goi unpinAllChatMessages: neu hop thu dat
 * trong mot nhom cua ca nha thi lenh do quet sach ca nhung tin nha minh tu ghim.
 */
object GhimCu {

    fun doc(context: Context): Long = sp(context).getLong(K_GHIM, 0L)

    fun ghi(context: Context, messageId: Long) {
        sp(context).edit().putLong(K_GHIM, messageId).apply()
    }

    private fun sp(context: Context) =
        context.getSharedPreferences("chogiochoi", Context.MODE_PRIVATE)

    private const val K_GHIM = "ghim_cu"
}
