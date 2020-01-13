package com.smartcardreader

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast

import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage

import java.util.ArrayList
import java.util.regex.Pattern
import android.app.ProgressDialog
import android.content.Context
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.*


class CardReader internal constructor(
    private val context: Activity,
    bitmapArray: ArrayList<String>
) {
    internal var dd: ProgressDialog? = null
    internal var result = ""
    internal var counter = 0

    init {
        runTextRecognition(bitmapArray, 0)
    }

    private fun runTextRecognition(bitmapArray: ArrayList<String>, index: Int) {

        dd = ProgressDialog(context)
        dd?.setMessage("Running OCR")
        dd?.setCancelable(false)
        dd?.show()

        if (index >= 0 && index < bitmapArray.size) {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(
                    context.contentResolver,
                    Uri.fromFile(File(bitmapArray[index]))
                )


                val image = FirebaseVisionImage.fromBitmap(bitmap)
                val recognizer = FirebaseVision.getInstance().onDeviceTextRecognizer

                recognizer.processImage(image)
                    .addOnSuccessListener { texts ->
                        Log.d(TAG, texts.text)
                        result = result + texts.text + "\n"
                        Log.d(TAG, "Counter :: $counter")

                        runTextRecognition(bitmapArray, ++counter)
                    }
                    .addOnFailureListener { e ->
                        // Task failed with an exception
                        dd?.dismiss()
                        Toast.makeText(context, "Sorry Something went wrong,", Toast.LENGTH_LONG)
                            .show()
                        e.printStackTrace()
                    }


            } catch (e: IOException) {
                e.printStackTrace()
            }

        } else {


            GlobalScope.launch {
                val cardInfo = performNormatlization(result,bitmapArray)
                val intent = Intent(context, Information::class.java)
                intent.putExtra("data", cardInfo)
                intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
                dd?.dismiss()
                context.startActivity(intent)
                context.finish()
            }

        }
    }

    companion object {
        val OCR_REQUEST = 1000
        val OCR_RES = "OCR_RES"

        private val TAG = CardReader::class.java.simpleName
    }

    private fun findNames(fileName: String, inputText: String, context: Context): String {
        var name = ""
        val fileRawText = StringBuilder()

        val inputStream = context.assets.open(fileName.plus(".txt"))

        val reader = BufferedReader(InputStreamReader(inputStream))

        try {
//            read file
            do {
                var line = reader.readLine()
                fileRawText.append("$line ")
            } while (line != null)

            val ocrToken = inputText.split(" ", "\n")
            ocrToken.forEach {

                val word = it.replace(Regex("[1234567890.';/()?@=><,:-]"), "")

                val wordPattern = Pattern.compile(
                    "([ |\\n](?:$word))[ |\\n]",
                    Pattern.CASE_INSENSITIVE
                )
                val wordMatcher = wordPattern.matcher(fileRawText.toString())

                if (wordMatcher.find()) {
                    if (name.length < word.length && word.length > 2)
                        name = word
                }
            }
        } catch (e: IOException) {

        }

        Log.d(TAG, name)
        return name
    }

    fun findDesignation(inputText: String, context: Context): String {

        var designation = ""
        val inputStream = context.assets.open("designation.txt")
        val reader = BufferedReader(InputStreamReader(inputStream))
        val fileRawText = StringBuilder()

        try {

            fileReadLoop@ do {
//              read line
                var line = reader.readLine()
//              create token removing coma and special sign
                val tokens = line?.split(",", "=>")

                tokens?.forEach {
                    val word = it.replace(Regex("[1234567890.';/()?@=><,:-]"), "").trim()
                    val wordPattern = Pattern.compile(
                        "([ |\\n]?(?:$word))[ |\\n]",
                        Pattern.CASE_INSENSITIVE
                    )
                    val wordMatcher = wordPattern.matcher(inputText)
                    if (wordMatcher.find()) {
                        if (designation.length < it.length && it.length > 2)
                            designation = it
                    }
                }

                if (designation.isNotEmpty())
                    break@fileReadLoop

            } while (line != null)
        } catch (e: IOException) {

        }

        Log.d(TAG, "$designation")
        return designation
    }

    fun performNormatlization(text: String, images : ArrayList<String>): CardInfo {
        var result = text
        val cardInfo = CardInfo()
        cardInfo.images = images
        cardInfo.rawString = result

        val emails = ArrayList<String>()
        val websites = ArrayList<String>()
        val phoneNumbers = ArrayList<String>()
        val addresses = ArrayList<String>()
        val company = ArrayList<String>()

        val emailPattern = Pattern.compile("([a-z0-9_.-]+)@([a-z0-9_.-]+[a-z])")

        val phoneNumberPattern =
            Pattern.compile("((\\+)?[0-9]{2,5}[\\s\\,.-]?[0-9]{2,7}[\\s\\,.-]?[0-9]{2,7}[\\s\\,.-]?[0-9]{2,7})")

        val websitePattern = Pattern.compile(
            "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
            Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL
        )

        val addresssPattern = Pattern.compile(
            "((\\d+(?:st|nd|rd|th))|[^0-9][0-9]{2,4}[^0-9])[,| ]?[ ]?(\\w+)[A-Z|a-z]{1,10}[ ]?(.+?),[ ]?((\\w+){1,10})?",
            Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL
        )

//

        val companyPatter = Pattern.compile(
            "([\\w|,]+[ |,](?:company|ltd|solutions|limited|bank|traders|trader|Inc))",
            Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL
        )

        //      looking for all phone numbers
        val phoneMatcher = phoneNumberPattern.matcher(result.trim())
        while (phoneMatcher.find())
            phoneNumbers.add(phoneMatcher.group()).also {
                result = result.replace(phoneMatcher.group(), "")
            }


        //      looking for all email address
        val emailMatcher = emailPattern.matcher(result.trim())
        while (emailMatcher.find())
            emails.add(emailMatcher.group()).also {
                result = result.replace(emailMatcher.group(), "")
            }


        //      looking for all web address
        val websiteMatcher = websitePattern.matcher(result.trim())
        while (websiteMatcher.find())
            websites.add(websiteMatcher.group()).also {
                result = result.replace(websiteMatcher.group(), "")
            }

        //      looking for all address
        val addressMatcher = addresssPattern.matcher(result.trim())
        while (addressMatcher.find())
            addresses.add(addressMatcher.group()).also {
                result = result.replace(addressMatcher.group().trim(), "", true)
            }

        //      looking for all address
        val companyMatcher = companyPatter.matcher(result.trim())
        while (companyMatcher.find())
            company.add(companyMatcher.group()).also {
                result = result.replace(companyMatcher.group(), "")
            }

        val designation = findDesignation(result, context)
        result = result.replace(designation.trim(), "", true)
        val name = findNames("firstnames", result, context)
        result = result.replace(name, "", true)
        val lastname = findNames("surnames", result, context)
        result = result.replace(lastname, "", true)

        cardInfo.emails = emails
        cardInfo.address = addresses.lastOrNull() ?: ""
        cardInfo.company = company.lastOrNull() ?: ""
        cardInfo.phoneNumbers = phoneNumbers
        cardInfo.websites = websites
        cardInfo.miscellaneous = result.trim()
        cardInfo.firstName = name
        cardInfo.lastName = lastname
        cardInfo.designation = designation.makeWordCaps()


        Log.d("OCRManager: ", "" + cardInfo.miscellaneous)

        return cardInfo
    }

}

fun String.makeWordCaps(): String {

    val capBuffer = StringBuffer()
    val capMatcher = Pattern.compile("([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE).matcher(this)

    while (capMatcher.find()) {
        capMatcher.appendReplacement(capBuffer, capMatcher.group(1).toUpperCase() + capMatcher.group(2).toLowerCase())
    }

    return capMatcher.appendTail(capBuffer).toString()
}

