package jp.techacademy.takanari.autoslideshowapp

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.net.Uri
import android.os.Handler
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100

    val imageArrayList = arrayListOf<Uri>()
    var count = 0
    private var mTimer: Timer? = null
    private var mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

            buttonNext.setOnClickListener {
                if(imageArrayList.isNotEmpty()) {
                    if (mTimer == null) {

                    //次のスライドに進む
                    count += 1
                    if (count < imageArrayList.size) {
                        imageView.setImageURI(imageArrayList[count])
                    } else if (count == imageArrayList.size) {
                        count = 0
                        imageView.setImageURI(imageArrayList[count])
                    }
                }
            }
        }

            buttonBack.setOnClickListener {
                if(imageArrayList.isNotEmpty()) {

                    if(mTimer == null) {
                        //次のスライドに進む
                        count -= 1
                        if ((0 <= count) && (count < imageArrayList.size)) {
                            imageView.setImageURI(imageArrayList[count])
                        } else if (count == -1) {
                            count = imageArrayList.size - 1
                            imageView.setImageURI(imageArrayList[count])
                        }
                    }
            }
        }

        // タイマーの作成


        // タイマーの始動
        buttonStartStop.setOnClickListener {
            if(imageArrayList.isNotEmpty()) {
                if (mTimer == null) {
                    buttonStartStop.text = "停止"
                    mTimer = Timer()
                    mTimer!!.schedule(object : TimerTask() {
                        override fun run() {
                            mHandler.post {
                                count += 1
                                if (count < imageArrayList.size ) {
                                    imageView.setImageURI(imageArrayList[count])
                                } else if (count == imageArrayList.size) {
                                    count = 0
                                    imageView.setImageURI(imageArrayList[count])
                                }
                            }
                        }
                    }, 100, 2000) // 最初に始動させるまで 100ミリ秒、ループの間隔を 2000ミリ秒 に設定
                } else if (mTimer != null) {
                    mTimer!!.cancel()
                    mTimer = null
                    buttonStartStop.text = "再生"
                }
            }
        }


        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目(null = 全項目)
            null, // フィルタ条件(null = フィルタなし)
            null, // フィルタ用パラメータ
            null // ソート (null ソートなし)
        )

        if (cursor!!.moveToFirst()) {
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID) //フォトを選んでる
                val id = cursor.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                imageArrayList.add(imageUri)

                Log.d("ANDROID", "URI : " + imageUri.toString())


            } while (cursor.moveToNext())
        }
        cursor.close()
        if(imageArrayList.isNotEmpty()) {
            imageView.setImageURI(imageArrayList[0])
        }
    }
}
