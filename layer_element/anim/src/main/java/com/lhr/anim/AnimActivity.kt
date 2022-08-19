package com.lhr.anim

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.lhr.centre.annotation.CElement

@CElement(name = "动画组件")
class AnimActivity : AppCompatActivity(),View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anim)
        this.findViewById<View>(R.id.start).setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.start -> {
                val track = this.findViewById<TrackBetweenView>(R.id.guide_track)
                if (track.currentSelection == TrackBetweenView.Select.LEFT){
                    track.select(TrackBetweenView.Select.RIGHT)
                }else{
                    track.select(TrackBetweenView.Select.LEFT)
                }
            }
        }
    }
}