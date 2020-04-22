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

    //所得した画像の配列入れる用
    val imageArrayList = arrayListOf<Uri>()
    //画像を何晩目かを指定するための変数
    var count = 0
    //タイマーが動いてる時に時間数える変数
    private var mTimer: Timer? = null
    //不明　ハンドラーを入れてる？
    private var mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

            //進むボタンが押された時
            buttonNext.setOnClickListener {
                //画像がある時
                if(imageArrayList.isNotEmpty()) {
                    //タイマーが動いてない時
                    if (mTimer == null) {
                    //進むボタンが押された時に次のスライドに進む
                    count += 1
                    //画像の数よりカウントが小さい時
                    if (count < imageArrayList.size) {
                    //カウント番目の画像を表示
                        imageView.setImageURI(imageArrayList[count])
                    //画像の数よりカウントが大きくなった場合
                    } else if (count == imageArrayList.size) {
                        //カウントを0にする
                        count = 0
                        //１番目の画像を表示
                        imageView.setImageURI(imageArrayList[count])
                    }
                }
            }
        }

            buttonBack.setOnClickListener {
                //画像がある時
                if(imageArrayList.isNotEmpty()) {

                    if(mTimer == null) {
                        //前のスライドに戻る
                        count -= 1
                        if ((0 <= count) && (count < imageArrayList.size)) {
                            imageView.setImageURI(imageArrayList[count])
                        //カウントが0より小さい(１番目よりまえ)
                        } else if (count == -1) {
                            //count⓪①②=size１２３　count②=３-1　で数字を合わせて最後の画像表示
                            count = imageArrayList.size - 1
                            imageView.setImageURI(imageArrayList[count])
                        }
                    }
            }
        }

        // タイマーの作成


        // タイマーの始動
        buttonStartStop.setOnClickListener {
            //画像がある時
            if(imageArrayList.isNotEmpty()) {
                //タイマーが動いてない時
                if (mTimer == null) {
                    //ボタンに停止を表示
                    buttonStartStop.text = "停止"
                    //mTimer変数を作成　＊倍速対策
                    mTimer = Timer()
                    //何かある時強制的に実行的な？　！！はisNotEmptyがあって、nullの時の条件文で実行可能になってるらしい
                    mTimer!!.schedule(object : TimerTask() {
                        //多分runメソッド？が動いてる時
                        override fun run() {
                            //何がしたいか ＊今回は自動めくり
                            mHandler.post {
                                //上と同じ
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
                  //動いている時
                } else if (mTimer != null) {
                    mTimer!!.cancel()
                    //ヌルにする
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

    //パーミッションが拒否か許可が押された時
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
        //許可してない時の対策　許可してボタンが押されてない時の最初の画面から１枚目の画像を表示
        if(imageArrayList.isNotEmpty()) {
            imageView.setImageURI(imageArrayList[0])
        }
    }
}
