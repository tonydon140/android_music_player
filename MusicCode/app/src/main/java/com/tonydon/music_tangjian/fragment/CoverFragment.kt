package com.tonydon.music_tangjian.fragment

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.tonydon.music_tangjian.R
import com.tonydon.music_tangjian.service.PlayerManager
import com.tonydon.music_tangjian.vm.MusicViewModel


class CoverFragment(
    val coverUrl: String
) : Fragment() {

    lateinit var imageView: ImageView
    lateinit var rotateAnim: ObjectAnimator

    // 缓存外部请求的 URL
    private var pendingCoverUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cover, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        imageView = view.findViewById<ImageView>(R.id.iv_cover)
        // 创建一个从 0° 转到 360° 的动画
        rotateAnim = ObjectAnimator.ofFloat(imageView, View.ROTATION, 0f, 360f).apply {
            duration = 8000L                 // 完整转一圈用时 5 秒，你可以根据需求调整
            repeatCount = ValueAnimator.INFINITE  // 无限循环
            interpolator = LinearInterpolator()   // 匀速插值，保持匀速旋转
        }
        if (PlayerManager.binder.isPlaying()) {
            rotateAnim.start()
        }
        // 如果外部在 onViewCreated 之前调用过 updateImage，就在这里真正加载一次
        pendingCoverUrl?.let { doLoadCover(it) }
        pendingCoverUrl = null
        doLoadCover(coverUrl)
    }

    /**
     * Activity / Adapter / Service 统一调用这个方法来更新封面
     */
    fun updateImage(coverUrl: String) {
        // Fragment 尚未 attach，或者 view 还没初始化
        if (!this::imageView.isInitialized) {
            pendingCoverUrl = coverUrl
            return
        }
        doLoadCover(coverUrl)
    }

    private fun doLoadCover(url: String) {
        Glide.with(this)
            .load(url)
            .circleCrop()
            .into(imageView)
    }

    fun stopAnim() {
        rotateAnim.pause()
    }

    fun resumeAnim() {
        if (!rotateAnim.isStarted) {
            rotateAnim.start()
        } else {
            rotateAnim.resume()
        }
    }

}