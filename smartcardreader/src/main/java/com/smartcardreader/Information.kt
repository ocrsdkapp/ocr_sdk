package com.smartcardreader

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_information.*
import kotlinx.android.synthetic.main.text_chooser.view.*
import android.content.Intent
import android.app.Activity
import com.smartcardreader.CardReader.Companion.OCR_RES


class Information : AppCompatActivity() {
    lateinit var cardInfo: CardInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_information)
        cardInfo = intent.getSerializableExtra("data") as CardInfo

        initViews()
    }

    private fun initViews() {
        first_name.editText?.setText(cardInfo.firstName)
        last_name.editText?.setText(cardInfo.lastName)
        designation.editText?.setText(cardInfo.designation)
        email.editText?.setText(cardInfo.emails.joinToString())
        phone.editText?.setText(cardInfo.phoneNumbers.joinToString())
        website.editText?.setText(cardInfo.websites.joinToString())
        miscellaneous.editText?.setText(cardInfo.rawString)
        address.editText?.setText(cardInfo.address)
        company.editText?.setText(cardInfo.company)

        first_name_info.setOnClickListener {
            showSelectTextDialog(this@Information, first_name.editText!!, cardInfo.miscellaneous)
        }

        last_name.setOnClickListener {
            showSelectTextDialog(this@Information, last_name.editText!!, cardInfo.miscellaneous)
        }

        designationInfo.setOnClickListener {
            showSelectTextDialog(this@Information, designation.editText!!, cardInfo.miscellaneous)
        }

        phoneInfo.setOnClickListener {

            showSelectTextDialog(this@Information, phone.editText!!, cardInfo.miscellaneous)
        }

        emailInfo.setOnClickListener {

            showSelectTextDialog(this@Information, email.editText!!, cardInfo.miscellaneous)
        }

        websiteInfo.setOnClickListener {

            showSelectTextDialog(this@Information, website.editText!!, cardInfo.miscellaneous)
        }

        addressInfo.setOnClickListener {
            showSelectTextDialog(this@Information, address.editText!!, cardInfo.miscellaneous)
        }

        companyInfo.setOnClickListener {
            showSelectTextDialog(this@Information, company.editText!!, cardInfo.miscellaneous)
        }

        save.setOnClickListener {
            cardInfo.emails = email.editText?.text.toString().CommaSeperatedElementsToList()
            cardInfo.phoneNumbers = phone.editText?.text.toString().CommaSeperatedElementsToList()
            cardInfo.websites = website.editText?.text.toString().CommaSeperatedElementsToList()

            cardInfo.website =
                website.editText?.text.toString().CommaSeperatedElementsToList().getIfExist(0) ?: ""
            cardInfo.phoneNumber =
                phone.editText?.text.toString().CommaSeperatedElementsToList().getIfExist(0) ?: ""
            cardInfo.email =
                email.editText?.text.toString().CommaSeperatedElementsToList().getIfExist(0) ?: ""

            cardInfo.company = company.editText?.text.toString()
            cardInfo.address = address.editText?.text.toString()
            cardInfo.firstName = first_name.editText?.text.toString()
            cardInfo.lastName = last_name.editText?.text.toString()

            cardInfo.designation = designation.editText?.text.toString()
            cardInfo.miscellaneous = miscellaneous.editText?.text.toString()


            setResult(Activity.RESULT_OK, Intent().apply { putExtra(OCR_RES, cardInfo) })
            finish()
        }
    }

    private fun showSelectTextDialog(context: Context, editText: EditText, raw: String) {
        val builder = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.text_chooser, null)
        builder.setView(view)

        val dialog = builder.create()

        view.raw.setText(raw)
        view.apply.setOnClickListener {
            editText.setText(view.raw.text.toString())
            dialog.dismiss()
        }
        dialog.show()
    }
}

fun <T> List<T>.ListToCommaSeperatedElements(): String {
    var stream = ""
    if (!isNullOrEmpty()) {
        for (i in this.indices) {
            stream = stream.plus("${this[i]},")
        }
    }
//    else {
//        throw NoSuchElementException("List is empty or null.")
//    }
    //deleting that last annoying ","
    return stream
}

fun <T> List<T>.getIfExist(index: Int): T? {
    if (index >= 0 && index < this.size) {
        return this.get(index)
    } else return null
}

fun String.CommaSeperatedElementsToList(): List<String> {
    var list = emptyList<String>()

    if (!equals("")) {
        list = this.split(",")
    }

    return list
}