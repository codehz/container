package one.codehz.container.base

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import one.codehz.container.R
import one.codehz.container.ext.get

abstract class BaseActivity(private val layoutId: Int) : AppCompatActivity() {
    val toolbar by lazy<Toolbar> { this[R.id.toolbar] }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(layoutId)
        setSupportActionBar(toolbar)
    }
}