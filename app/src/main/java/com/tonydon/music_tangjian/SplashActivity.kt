package com.tonydon.music_tangjian

import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tonydon.music_tangjian.utils.ConfigUtils

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 判断是否显示隐私弹窗
//        ConfigUtils.setAgreed(false)
        if (!ConfigUtils.isAgreed()) {
            showPrivacyDialog()
        } else {
            goHomeSmooth()
        }
    }

    private fun showPrivacyDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_privacy, null)
        val tv = view.findViewById<TextView>(R.id.tv_privacy_content)
        val html = """
          欢迎使用音乐社区，我们将严格遵守相关法律和隐私政策保护您的个人隐私，
          请您阅读并同意 
          <a href="https://www.mi.com">《用户协议》</a> 
          与 
          <a href="https://xiaomiev.com">《隐私政策》</a>。
        """.trimIndent()

        // 3. 设置到 TextView，并开启点击
        tv.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
        tv.movementMethod = LinkMovementMethod.getInstance()


        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawable(
            ContextCompat.getDrawable(this, R.drawable.bg_pri)
        )
        dialog.show()
        val params = dialog.window?.attributes
        params?.width = (resources.displayMetrics.widthPixels * 0.72).toInt()
        dialog.window?.attributes = params

        view.findViewById<TextView>(R.id.tv_cancel).setOnClickListener {
            finish()
        }
        view.findViewById<TextView>(R.id.tv_ok).setOnClickListener {
            ConfigUtils.setAgreed(true)
            dialog.dismiss()
            goHomeSmooth()
        }
    }

    private fun goHomeSmooth() {
        startActivity(Intent(this, MainActivity::class.java))
        val enterAnim = R.anim.fade_in
        val exitAnim = R.anim.fade_out
        @Suppress("DEPRECATION")
        overridePendingTransition(enterAnim, exitAnim)
        finish()
    }
}