package marutune.jp.myapplication

import android.content.Context
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.facebook.drawee.backends.pipeline.Fresco
import java.io.File

class RecyclerAdapter(
    private val context: Context,
    private val itemClickListener: RecyclerViewHolder.ItemClickListener,
    private val itemLongClickListener: RecyclerViewHolder.ItemLongClickListener,
    private val itemList:List<String>) : RecyclerView.Adapter<RecyclerViewHolder>() {

    private var mRecyclerView : RecyclerView? = null
    var itemStateList: SparseBooleanArray = SparseBooleanArray()
    var itemByteList: SparseArray<ByteArray> = SparseArray()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        mRecyclerView = null
    }

    override fun onBindViewHolder(p0: RecyclerViewHolder, p1: Int) {
        val file = File(itemList[p1])

        // ポジションをキーとしてByteArrayを保持する
        itemByteList.put(p1, file.readBytes())

        // 画像読み込み失敗時はタップで4回までリトライ出来るように設定
        p0.photoImageView.controller = Fresco.newDraweeControllerBuilder()
            .setTapToRetryEnabled(true)
            .setUri(Uri.fromFile(file))
            .build()

        // 状態配列に保持されているか判断
        if (itemStateList.get(p1, false)) {
            p0.checkboxImageView.isSelected = true
            p0.blurView.visibility = View.VISIBLE
        } else {
            p0.checkboxImageView.isSelected = false
            p0.blurView.visibility = View.INVISIBLE
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val mView = layoutInflater.inflate(R.layout.custom_cell, p0, false)

        mView.setOnClickListener { view ->
            mRecyclerView?.let {
                itemClickListener.onItemClick(view, it.getChildAdapterPosition(view))
            }
        }

        mView.setOnLongClickListener { view ->
            mRecyclerView?.let {
                itemLongClickListener.onItemLongClick(view, it.getChildAdapterPosition(view))
            }
            true
        }

        // 本来88dpだが3.5x対応のため4で割って計算
        val lp = mView.layoutParams
        // マージン6dp分は引いておく
        lp.width = ((p0.measuredWidth - (6 * context.resources.displayMetrics.density)) / 4).toInt()
        lp.height = lp.width
        mView.layoutParams = lp

        return RecyclerViewHolder(mView)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}