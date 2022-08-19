package com.lhr.learn.applications

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.lhr.learn.R
import com.lhr.common.ui.BaseAdapter

/**
 * @CreateDate: 2022/6/27
 * @Author: mac
 * @Description:
 */
class AppInfoAdapter(
    private val appClickListener: ItemClickListener,
) : BaseAdapter<AppInfo>() {
    override var layout: Int = R.layout.item_app_list

    override fun bind(holder: ViewHolder, position: Int, data: AppInfo) {
        val itemView = holder.itemView
        val iconIv: ImageView = itemView.findViewById(R.id.app_icon)
        val nameTv: TextView = itemView.findViewById(R.id.app_name)
        val packageTv: TextView = itemView.findViewById(R.id.app_package)
        val storageTv: TextView = itemView.findViewById(R.id.storage_usage)
        val mainContainer: View = itemView.findViewById(R.id.smContentView)
        Glide.with(iconIv.context)
            .load(data.appIconUri)
            .transition(DrawableTransitionOptions.withCrossFade())
            .fitCenter()
            .into(iconIv)

        nameTv.text = data.name
        packageTv.text = data.packageName

        val size =
            if (data.appSize == 0L) "0MB"
            else data.appSize
        storageTv.text = "$size"

        mainContainer.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                appClickListener.appOpenClicked(pos)
            }
        }
    }

    interface ItemClickListener {
        fun appOpenClicked(position: Int)
    }
}