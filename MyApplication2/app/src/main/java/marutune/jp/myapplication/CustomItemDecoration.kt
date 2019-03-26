package marutune.jp.myapplication

import android.content.Context
import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

class CustomItemDecoration(context: Context) : RecyclerView.ItemDecoration() {

    private val extraMargin: Int

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        // ---- マージンを設定する -----
        val position = parent.getChildAdapterPosition(view)

        // 位置によってマージンの位置も変える
        when(position % 4) {
            0 -> { // 1列目の場合は右にマージンを追加
                outRect.right = extraMargin
            }
            1 -> { // 2列目の場合は右にマージンを追加
                outRect.right = extraMargin
            }
            // それ以外は何もしない
        }

        // 1行目以外は上側にマージンを追加
        if (parent.childCount > 4) {
            outRect.top = extraMargin
        }
    }

    init {
        // マージンは2dpとする
        extraMargin = (2 * context.resources.displayMetrics.density).toInt()
    }
}