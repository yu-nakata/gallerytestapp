package marutune.jp.myapplication
import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v7.widget.GridLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.Toast
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import com.facebook.drawee.backends.pipeline.Fresco
import java.io.File
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), RecyclerViewHolder.ItemClickListener, RecyclerViewHolder.ItemLongClickListener {
    private val REQUEST_EXTERNAL_STORAGE = 1
    val mPhotos = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Fresco
        Fresco.initialize(this)

        setContentView(R.layout.activity_main)

        // Android 6, API 23以上でパーミッシンの確認
        if (Build.VERSION.SDK_INT >= 23) {
            checkPermission()
        } else {
            setUpReadExternalStorage()
        }
        recyclerView.addItemDecoration(CustomItemDecoration(context = applicationContext))
        recyclerView.layoutManager = GridLayoutManager(applicationContext, 4)
    }

    override fun onItemClick(view: View, position: Int) {
        val viewHolder = recyclerView.getChildViewHolder(view) as RecyclerViewHolder
        val adapter = recyclerView.adapter as RecyclerAdapter
        val isSelected = viewHolder.checkboxImageView.isSelected

        // アニメーションを実施
        val scaleX = PropertyValuesHolder.ofFloat("scaleX", 1.0f, 0.95f)
        val scaleY = PropertyValuesHolder.ofFloat("scaleY", 1.0f, 0.95f)
        val objectAnimator = ObjectAnimator.ofPropertyValuesHolder(viewHolder.recyclerViewCell, scaleX, scaleY)
        objectAnimator.duration = 100
        objectAnimator.start()
        // 戻すアニメーションを実施
        val reScaleX = PropertyValuesHolder.ofFloat("scaleX", 0.95f, 1.0f)
        val reScaleY = PropertyValuesHolder.ofFloat("scaleY", 0.95f, 1.0f)
        val objectAnimator2 = ObjectAnimator.ofPropertyValuesHolder(viewHolder.recyclerViewCell, reScaleX, reScaleY)
        objectAnimator2.startDelay = 100
        objectAnimator2.duration = 100
        objectAnimator2.start()

        // 選択状態を変更
        if (isSelected) {
            viewHolder.blurView.visibility = View.INVISIBLE
            viewHolder.checkboxImageView.isSelected = false
            adapter.itemStateList.delete(position)
        } else {
            viewHolder.blurView.visibility = View.VISIBLE
            viewHolder.checkboxImageView.isSelected = true
            adapter.itemStateList.put(position, true)
        }
    }

    override fun onItemLongClick(view: View, position: Int) {
        // 保持しているByteArrayを取得しセット
        val adapter = recyclerView.adapter as RecyclerAdapter
        val byteArray = adapter.itemByteList[position]
        val bundle = Bundle()
        bundle.putByteArray("image", byteArray.readBytes())

        // Fragment生成
        val fragment = ImageViewerFragment()
        fragment.arguments = bundle

        // transaction生成
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.container, fragment, fragment.tag)
        transaction.addToBackStack(null)
        // transactionを反映
        transaction.commit()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.size != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                //拒否された時の動作
            } else {
                // 許可された時の動作
                setImages()
            }
        }
    }

    private fun setUpReadExternalStorage() {
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state) {
            setImages()
        }
    }

    // permissionの確認
    private fun checkPermission() {
        // 既に許可している
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            setUpReadExternalStorage()
        } else {
            requestLocationPermission()
        }// 拒否していた場合
    }

    // 許可を求める
    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_EXTERNAL_STORAGE
            )

        } else {
            val toast = Toast.makeText(this, "アプリ実行に許可が必要です", Toast.LENGTH_SHORT)
            toast.show()

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_EXTERNAL_STORAGE
            )
        }
    }

    private fun setImages() {
        val file = Environment.getExternalStorageDirectory()
        val storagePath = file.path
        searchImageFiles(storagePath)

        recyclerView.adapter = RecyclerAdapter(this, this, this, mPhotos)
    }

    private fun searchImageFiles(path: String) {
        val listDirectory = ArrayList<String>()
        listDirectory.add(path)

        var m = 0
        var n = 0
        var fileName: Array<String>
        var imgPath: String?
        val regex = """(?i:.*\.(jpg|jpeg))""".toRegex()
        val deSortPhotos = ArrayList<String>()

        // dirList.size() は動的変化あり注意
        while (listDirectory.size > m) {

            // get(m) リスト内の指定された位置 m にある要素を返す
            val directory = File(listDirectory[m])
            // java.io.File クラスのメソッド list()
            // 指定したディレクトリに含まれるファイル、ディレクトリの一覧を String 型の配列で返す。
            fileName = directory.list()

            n = 0
            while (fileName.size > n) {

                val subFile = File(directory.path + "/" + fileName[n])
                if (subFile.isHidden) {
                    // 隠しフォルダや隠しファイルは無視する
                } else if (subFile.isDirectory) {
                    listDirectory.add(directory.path + "/" + fileName[n])
                } else if (subFile.name.matches(regex)) {
                    imgPath = directory.path + "/" + fileName[n]
                    deSortPhotos.add(imgPath)
                } else {
                }
                n++
            }
            m++
        }

        // 日付順でソート
        val sortPhotos = deSortPhotos.sortedWith(compareByDescending {
            val file = File(it)
            file.lastModified()
        })
        mPhotos.addAll(sortPhotos)
    }
}
