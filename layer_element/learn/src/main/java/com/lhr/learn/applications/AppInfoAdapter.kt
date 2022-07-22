package com.lhr.learn.applications

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.lhr.learn.R

/**
 * @CreateDate: 2022/6/27
 * @Author: mac
 * @Description:
 */
class AppInfoAdapter(
    private val appList: List<AppInfo>,
    private val appClickListener: ItemClickListener
): RecyclerView.Adapter<AppInfoAdapter.AppInfoViewHolder>() {
    class AppInfoViewHolder(view: View): RecyclerView.ViewHolder(view){
        private val iconIv: ImageView = itemView.findViewById(R.id.app_icon)
        private val nameTv: TextView = itemView.findViewById(R.id.app_name)
        private val packageTv: TextView = itemView.findViewById(R.id.app_package)
        private val storageTv: TextView = itemView.findViewById(R.id.storage_usage)
        private val mainContainer: View = itemView.findViewById(R.id.smContentView)

        @SuppressLint("SetTextI18n")
        fun bind(app: AppInfo, appClickListener: ItemClickListener) {
            Glide.with(iconIv.context)
                .load(app.appIconUri)
                .transition(DrawableTransitionOptions.withCrossFade())
                .fitCenter()
                .into(iconIv)

            nameTv.text = app.name
            packageTv.text = app.packageName

            val size =
                if (app.appSize == 0L) "0MB"
                else app.appSize
            storageTv.text = "$size"

            mainContainer.setOnClickListener {
                val pos = this.adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    appClickListener.appOpenClicked(pos)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppInfoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_application, parent, false)
        return AppInfoViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppInfoViewHolder, position: Int) {
        val app = appList[position]
        holder.bind(app, appClickListener)
    }

    override fun getItemCount(): Int = appList.size

    interface ItemClickListener{
        fun appOpenClicked(position: Int)
    }
}