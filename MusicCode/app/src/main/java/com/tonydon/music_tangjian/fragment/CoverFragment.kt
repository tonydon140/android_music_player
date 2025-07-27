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
import com.bumptech.glide.Glide
import com.tonydon.music_tangjian.R


class CoverFragment(
    val coverUrl: String
) : Fragment() {

    lateinit var imageView: ImageView
    lateinit var rotateAnim: ObjectAnimator

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
        rotateAnim.start()
        updateImage(coverUrl)
    }


    fun updateImage(coverUrl: String) {
        Glide.with(imageView)
            .load(coverUrl)
            .circleCrop()
            .into(imageView)
    }

    fun stopAnim() {
        rotateAnim.pause()
    }

    fun resumeAnim() {
        rotateAnim.resume()
    }

}