package xyz.enterkey.linkpreviewdemo

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import xyz.enterkey.linkpreview.LinkPreview
import xyz.enterkey.linkpreview.SearchUrls
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        submit.setOnClickListener {
            val rawUrl =url.text.toString()
            if(rawUrl.isNullOrEmpty()){
                Toast.makeText(applicationContext,"Enter url",Toast.LENGTH_SHORT).show()
            }else{
                makePreview(rawUrl)
                loadPreview(rawUrl)
            }

        }

        randomColor.setOnClickListener {
            previewClassic.setTitleColor(getRandomTitle())
            previewClassic.setDescriptionColor(getRandomDescription())
            previewMaterial.setTitleColor(getRandomTitle())
            previewMaterial.setDescriptionColor(getRandomDescription())
            previewPotrait.setTitleColor(getRandomTitle())
            previewPotrait.setDescriptionColor(getRandomDescription())
        }


    }

    fun makePreview(url: String?) {
        val tempUrl: ArrayList<String> = SearchUrls.matches(url)
        if (tempUrl.isNullOrEmpty()) {
            Toast.makeText(applicationContext,"No url found",Toast.LENGTH_SHORT).show()

        }else{
            val trimUrl: String = LinkPreview.extendedTrim(tempUrl[0])
            preview.text = trimUrl
        }
    }
    fun loadPreview(text:String){
        previewClassic.makePreview(text)
        previewMaterial.makePreview(text)
        previewPotrait.makePreview(text)
    }

    private val RANDOM_TITLE = arrayOf(
        Color.RED, Color.BLUE,
        Color.BLACK, Color.GREEN, Color.YELLOW, Color.CYAN, Color.DKGRAY, Color.MAGENTA )
    private val RANDOM_DESCRIPTION = arrayOf(
        Color.RED, Color.BLUE,
        Color.BLACK, Color.GREEN, Color.YELLOW, Color.CYAN, Color.DKGRAY, Color.MAGENTA )


    private fun getRandomDescription(): Int {
        val random = Random().nextInt(RANDOM_DESCRIPTION.size)
        return RANDOM_DESCRIPTION[random]
    }

    private fun getRandomTitle(): Int {
        val random = Random().nextInt(RANDOM_TITLE.size)
        return RANDOM_TITLE[random]
    }


}
