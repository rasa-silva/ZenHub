package com.zenhub.repo.commits

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import com.zenhub.*
import com.zenhub.core.PagedRecyclerViewAdapter
import com.zenhub.core.asFuzzyDate
import com.zenhub.github.extractPaginationInfo
import com.zenhub.github.gitHubService
import com.zenhub.github.mappings.Commit
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import ru.gildor.coroutines.retrofit.Result
import ru.gildor.coroutines.retrofit.awaitResult


class CommitsFragment : Fragment() {

    private val fullRepoName by lazy { arguments.getString("REPO_NAME") }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.repo_content_commits, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.list)
        val recyclerViewAdapter = CommitsRecyclerViewAdapter(fullRepoName, recyclerView.context, recyclerView)
        val refreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.commits_swiperefresh)
        refreshLayout?.setOnRefreshListener { requestDataRefresh(refreshLayout, recyclerViewAdapter) }

        recyclerView.let {
            val layoutManager = LinearLayoutManager(it.context)
            it.layoutManager = layoutManager
            it.adapter = recyclerViewAdapter
            it.addItemDecoration(DividerItemDecoration(it.context, layoutManager.orientation))
        }

        requestDataRefresh(refreshLayout, recyclerViewAdapter)

        return view
    }

    private fun requestDataRefresh(container: ViewGroup, adapter: CommitsRecyclerViewAdapter) {
        launch(UI) {
            Log.d(Application.LOGTAG, "Refreshing repo branches...")
            val branches = fetchBranches(container)
            val branchesSpinner = container.findViewById<Spinner>(R.id.branches_list)
            branchesSpinner.adapter = ArrayAdapter<String>(container.context, R.layout.support_simple_spinner_dropdown_item, branches)
            //TODO The contents tab also needs to switch between branches...

            Log.d(Application.LOGTAG, "Refreshing repo commits...")
            val result = gitHubService.commits(fullRepoName, null).awaitResult()
            when (result) {
                is Result.Ok -> adapter.updateDataSet(result)
                is Result.Error -> showErrorOnSnackbar(container, result.response.message())
                is Result.Exception -> showExceptionOnSnackbar(container, result.exception)
            }

            container.findViewById<SwipeRefreshLayout>(R.id.commits_swiperefresh)?.isRefreshing = false
        }
    }

    class CommitsRecyclerViewAdapter(private val fullRepoName: String,
                                     private val ctx: Context,
                                     private val recyclerView: RecyclerView) : PagedRecyclerViewAdapter<Commit?>(ctx, R.layout.repo_content_commits_item) {
        override fun bindData(itemView: View, model: Commit?) {
            val commit = model ?: return

            val avatarView = itemView.findViewById<ImageView>(R.id.avatar)
            commit.committer?.let {
                Application.picasso.load(it.avatar_url)
                        .transform(RoundedTransformation).into(avatarView)
            }

            itemView.findViewById<TextView>(R.id.commit_message).text = commit.commit.message
            itemView.findViewById<TextView>(R.id.committer).text = commit.commit.committer.name
            itemView.findViewById<TextView>(R.id.pushed_time).text = commit.commit.committer.date.asFuzzyDate()

            itemView.setOnClickListener {
                val intent = Intent(ctx, RepoCommitDetails::class.java)
                intent.putExtra("REPO_FULL_NAME", fullRepoName)
                intent.putExtra("COMMIT_SHA", commit.sha)
                ContextCompat.startActivity(ctx, intent, null)
            }
        }

        override fun doPageRequest(url: String) {
            launch(UI) {
                Log.d(Application.LOGTAG, "Paging own repo list with $url...")
                val result = gitHubService.commitsPaginate(url).awaitResult()
                when (result) {
                    is Result.Ok -> appendDataSet(result.value)
                    is Result.Error -> showErrorOnSnackbar(recyclerView, result.exception.localizedMessage)
                    is Result.Exception -> showErrorOnSnackbar(recyclerView, result.exception.localizedMessage)
                }
            }
        }
    }

    private suspend fun fetchBranches(rootView: View): MutableList<String> {
        val names = mutableListOf("<default>")

        val branches = gitHubService.branches(fullRepoName).awaitResult() as? Result.Ok ?: return names
        names.addAll(branches.value.map { it.name })
        val (lastPage, templateUrl) = extractPaginationInfo(branches.response)

        if (lastPage == 1) return names

        var currentPage = 2
        while (lastPage >= currentPage) {
            val url = templateUrl.replaceAfterLast("?page=", currentPage.toString())
            val paging = gitHubService.branchesPaginate(url).awaitResult()
            when (paging) {
                is Result.Ok -> names.addAll(paging.value.map { it.name })
                is Result.Error -> showErrorOnSnackbar(rootView, paging.response.message())
                is Result.Exception -> showExceptionOnSnackbar(rootView, paging.exception)
            }

            currentPage++
        }

        return names
    }
}