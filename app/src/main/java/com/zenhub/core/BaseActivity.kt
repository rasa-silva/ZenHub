package com.zenhub.core

import android.content.Intent
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import com.zenhub.R
import com.zenhub.search.SearchActivity
import com.zenhub.user.REPO_LIST_TYPE
import com.zenhub.user.RepoListActivity
import com.zenhub.user.UserDetailsActivity

abstract class BaseActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout

    protected fun onCreateDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)

        val drawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0)
        drawerLayout.addDrawerListener(drawerToggle)

        drawerToggle.syncState()

        findViewById<NavigationView>(R.id.nav_view)?.setNavigationItemSelectedListener(this)

        findViewById<SwipeRefreshLayout>(R.id.swiperefresh)?.setOnRefreshListener {
            requestDataRefresh()
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> startActivity(Intent(this, UserDetailsActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            R.id.nav_repos -> startActivity(Intent(this, RepoListActivity::class.java).putExtra("LIST_TYPE", REPO_LIST_TYPE.OWN))
            R.id.nav_starred -> startActivity(Intent(this, RepoListActivity::class.java).putExtra("LIST_TYPE", REPO_LIST_TYPE.STARRED))
            R.id.nav_search -> startActivity(Intent(this, SearchActivity::class.java))
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    protected open fun requestDataRefresh() {}
}