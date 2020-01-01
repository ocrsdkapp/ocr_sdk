package com.cardreader

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.smartcardreader.CardReaderActivity
import com.smartcardreader.CardInfo
import kotlinx.android.synthetic.main.activity_main.*

const val OCR_REQUEST: Int = 1000
const val OCR_RES: String = "OCR_RES"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        btn.setOnClickListener {
            startActivityForResult(Intent(this, CardReaderActivity::class.java), OCR_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == OCR_REQUEST) {
            val data1 = data?.getSerializableExtra(OCR_RES) as CardInfo
            resultText.text =  data1.rawString?: "No Result Found"
        }
    }
}
