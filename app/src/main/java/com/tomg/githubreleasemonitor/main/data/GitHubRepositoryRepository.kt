/*
 * Copyright (c) 2020-2022, Tom Geiselmann (tomgapplicationsdevelopment@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY,WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tomg.githubreleasemonitor.main.data

import androidx.paging.PagingSource
import androidx.sqlite.db.SimpleSQLiteQuery
import com.tomg.githubreleasemonitor.main.SortOrder
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
class GitHubRepositoryRepository @Inject constructor(
    private val repositoryDao: GitHubRepositoryDao
) {

    fun getRepositories() = repositoryDao.getRepositories()

    fun getRepositories(sortOrder: SortOrder): PagingSource<Int, GitHubRepository> {
        val sqlSortColumn = if (
            sortOrder == SortOrder.Asc.RepositoryOwner ||
            sortOrder == SortOrder.Desc.RepositoryOwner
        ) "owner" else "name"
        val sqlSortOrder = if (sortOrder is SortOrder.Asc) "ASC" else "DESC"
        val statement = "SELECT * FROM GitHubRepository ORDER BY $sqlSortColumn $sqlSortOrder"
        return repositoryDao.getRepositories(SimpleSQLiteQuery(statement))
    }

    suspend fun insertRepositories(vararg gitHubRepository: GitHubRepository): Boolean {
        return runCatching {
            repositoryDao.insert(*gitHubRepository)
            true
        }.getOrElse { exception ->
            Timber.e(exception)
            false
        }
    }

    suspend fun deleteRepository(gitHubRepository: GitHubRepository): Boolean {
        return runCatching {
            repositoryDao.delete(gitHubRepository)
            true
        }.getOrElse { exception ->
            Timber.e(exception)
            false
        }
    }

    suspend fun updateRepositories(vararg gitHubRepositories: GitHubRepository): Boolean {
        return runCatching {
            repositoryDao.update(*gitHubRepositories)
            true
        }.getOrElse { exception ->
            Timber.e(exception)
            false
        }
    }
}
