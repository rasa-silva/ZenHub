package com.zenhub.github.mappings

import java.io.Serializable

class TokenRequest(val client_id: String, val client_secret: String, val scopes: List<String>, val note: String)
class TokenResponse(val token: String)

class Repository(val name: String, val full_name: String,
                 val description: String, val pushed_at: String,
                 val stargazers_count: Int, val language: String?)

class RepositoryDetails(val name: String, val description: String, val stargazers_count: Int,
                        val homepage: String?, val html_url: String,
                        val pushed_at: String, val language: String, val size: Long)

class User(val login: String, val avatar_url: String, val name: String,
           val public_repos: Int, val total_private_repos: Int,
           val public_gists: Int, val private_gists: Int?,
           val followers: Int, val following: Int,
           val created_at: String)

class Branch(val name: String)

class Committer(val avatar_url: String)
class CommitInfo(val message: String, val committer: CommitCommitter)
class CommitCommitter(val name: String, val date: String)
class Commit(val sha: String, val commit: CommitInfo, val committer: Committer?)

class CommitFile(val filename: String, val patch: String)
class CommitDetails(val commit: CommitInfo, val files: List<CommitFile>)

class RepoContentEntry(val name: String, val path: String, val size: Long, val type: String, val download_url: String)

class ErrorMessage(val message: String)

//Search
class RepositorySearch(val total_count: Int, val items: List<Repository>)
class UserSearch(val total_count: Int, val items: List<User>)

//Gists
class Gist(val description: String?, val id: String,
           val updated_at: String, val public: Boolean,
           val url: String, val files: Map<String, GistFile>) : Serializable
class GistFile(val filename: String, val language: String, val size: Long, val raw_url: String): Serializable
class NewGist(val description: String, val public: Boolean, val files: Map<String, Map<String, String>>)