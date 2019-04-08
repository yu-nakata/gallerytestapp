package marutune.jp.myapplication

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import com.facebook.drawee.view.SimpleDraweeView
import kotlinx.android.synthetic.main.custom_cell.view.*

class RecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    // アイテムクリック時Listener
    interface ItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    // アイテムロングクリック時Listener
    interface ItemLongClickListener {
        fun onItemLongClick(view: View, position: Int)
    }

    val recyclerViewCell = view.recyclerViewCell
    val photoImageView: SimpleDraweeView = view.photo
    val blurView: View = view.blur_view
    val checkboxImageView: ImageView = view.checkbox_image

    init {
        // layoutの初期設定するときはココ
    }
}