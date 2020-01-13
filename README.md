# OCRSDK

A custom lib which have ability to extract information from buisness card, Buisness card can be scanned using built in `Camera` or by selecting image from `Gallery` 

Please check Demo project for a basic example on how to use OCRSDK.

## Note

- Add  `google-services.json` which can be downloaded from Google Firebase console.
- Add google services plugin in your app

## Usage

#### In your Activity Class
```kotlin
const val OCR_REQUEST: Int = 1000
const val OCR_RES: String = "OCR_RES"

class MainActivity : AppCompatActivity() {
  
  override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
      
      //stating library intent
        btn.setOnClickListener {
            startActivityForResult(Intent(this, CardReaderActivity::class.java), OCR_REQUEST)
        }
    }

   //Returns scan results 
   override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == OCR_REQUEST) {
            val _cardinfo = data?.getSerializableExtra(OCR_RES) as CardInfo
        }
    }
}

```

## Installation

**OCRSDK** is available through [Jitpack](https://jitpack.io/#ocrsdkapp/ocr_sdk). To install
it, simply add the following line to your build.gradle:

**MAVEN** 
```gradle

	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories
	
	<dependency>
	    <groupId>com.github.ocrsdkapp</groupId>
	    <artifactId>ocr_sdk</artifactId>
	    <version>Tag</version>
	</dependency>
```
  
  **GRADLE** 

```gradle
  
  allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
	
  dependencies {
	        implementation 'com.github.ocrsdkapp:ocr_sdk:1.0.1'
	}
```
## License

**OCRSDK** is available under the MIT license. See the LICENSE file for more info.
