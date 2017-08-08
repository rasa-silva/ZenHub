package com.zenhub

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.zenhub.github.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ZenHub : BaseActivity() {

    val adapter = RepoListRecyclerViewAdapter()
    private val userDetailsCallback = OnUserDetailsResponse(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.zenhub_activity)
        super.onCreateDrawer()

        findViewById<RecyclerView>(R.id.repo_list).let {
            val layoutManager = LinearLayoutManager(it.context)
            it.layoutManager = layoutManager
            it.adapter = adapter
            it.addItemDecoration(DividerItemDecoration(it.context, layoutManager.orientation))
        }

        requestDataRefresh()
    }

    override fun requestDataRefresh() {
        Log.d(Application.LOGTAG, "Refreshing list...")
        gitHubService.userDetails(userDetailsCallback.etag, STUBBED_USER).enqueue(userDetailsCallback)

        val recyclerView = findViewById<RecyclerView>(R.id.repo_list)
        GitHubApi.ownRepos(recyclerView, {response, _ ->
            val top3Repos = response.sortedByDescending { it.pushed_at }.take(3)
            adapter.updateDataSet(top3Repos)
        })
    }
}

class OnUserDetailsResponse(val activity: ZenHub) : Callback<User> {

    var etag: String? = null

    override fun onFailure(call: Call<User>?, t: Throwable?) {
        Log.d(Application.LOGTAG, "Failed: ${t.toString()}")
    }

    override fun onResponse(call: Call<User>?, response: Response<User>) {
        Log.d(Application.LOGTAG, "UserDetails reponse")
        val refreshLayout = activity.findViewById<SwipeRefreshLayout>(R.id.swiperefresh)
        when {
            response.code() == 304 -> Unit
            !response.isSuccessful -> showGitHubApiError(response.errorBody(), refreshLayout)
            else -> {
                etag = response.headers()["ETag"]
                val responseBody = response.body()
                val avatarView = activity.findViewById<ImageView>(R.id.avatar)
                val roundedTransformation = RoundedTransformation()
                Application.picasso.load(responseBody?.avatar_url).transform(roundedTransformation).into(avatarView)
                val navDrawerAvatar = activity.findViewById<ImageView>(R.id.avatarImage)
                Application.picasso.load(responseBody?.avatar_url).transform(roundedTransformation).into(navDrawerAvatar)
                activity.findViewById<TextView>(R.id.userid).text = responseBody?.login
                activity.findViewById<TextView>(R.id.username).text = responseBody?.name
                val created = activity.findViewById<TextView>(R.id.created_at)
                val date_created = dateFormat.parse(responseBody?.created_at)
                created.text = DateUtils.formatDateTime(activity.applicationContext, date_created.time, DateUtils.FORMAT_SHOW_DATE)
                val followers = activity.findViewById<TextView>(R.id.followers)
                followers.text = activity.resources.getString(R.string.numberOfFollowers, responseBody?.followers)
                val following = activity.findViewById<TextView>(R.id.following)
                following.text = activity.resources.getString(R.string.numberOfFollowing, responseBody?.following)
                val gists = activity.findViewById<TextView>(R.id.gists)
                gists.text = activity.resources.getString(R.string.numberOfGists, responseBody?.public_gists)
            }
        }

        refreshLayout.isRefreshing = false
    }
}