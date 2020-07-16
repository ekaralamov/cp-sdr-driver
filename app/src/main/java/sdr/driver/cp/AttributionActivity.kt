package sdr.driver.cp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView

class AttributionActivity : Activity() {

    private lateinit var urlFormatHtml: String
    private lateinit var licenseFormatHtml: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        actionBar!!.subtitle = getString(R.string.attribution_subtitle)

        urlFormatHtml = getString(R.string.attribution_url)
        licenseFormatHtml = getString(R.string.attribution_license)

        setContentView(R.layout.attribution_activity)
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)

        recyclerView.adapter = object : RecyclerView.Adapter<MyViewHolder>() {

            init {
                setHasStableIds(true)
            }

            override fun getItemId(position: Int) = position.toLong()

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(parent)

            override fun getItemCount() = Dependencies.list.size

            override fun onBindViewHolder(holder: MyViewHolder, position: Int) =
                holder.bind(Dependencies.list[position])
        }
    }

    private inner class MyViewHolder(parent: ViewGroup) :
        RecyclerView.ViewHolder(layoutInflater.inflate(R.layout.attribution_item, parent, false)) {

        private val name = itemView.findViewById<TextView>(R.id.name)
        private val urlView = itemView.findViewById<TextView>(R.id.url).apply {
            setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, url.toUri())) }
        }
        private val licenseView = itemView.findViewById<TextView>(R.id.license).apply {
            setOnClickListener {
                LicenseActivity.start(
                    "file:///android_asset/${getLicenseFileName()}.html",
                    this@AttributionActivity
                )
            }
        }

        private lateinit var url: String
        private lateinit var license: Dependencies.License

        fun bind(dependencyData: Dependencies.DependencyData) {
            name.text = dependencyData.name

            url = dependencyData.url
            urlView.text = Html.fromHtml(String.format(urlFormatHtml, url))

            license = dependencyData.license
            licenseView.text = Html.fromHtml(String.format(licenseFormatHtml, getNameOf(license)))
        }

        private fun getLicenseFileName() = when (license) {
            Dependencies.License.Apache2 -> "apache2"
            Dependencies.License.Gpl3 -> "gpl3"
            Dependencies.License.Lgpl3 -> "lgpl3"
        }
    }

    private fun getNameOf(license: Dependencies.License) = getString(
        when (license) {
            Dependencies.License.Apache2 -> R.string.license_apache2
            Dependencies.License.Gpl3 -> R.string.license_gpl3
            Dependencies.License.Lgpl3 -> R.string.license_lgpl3
        }
    )
}
