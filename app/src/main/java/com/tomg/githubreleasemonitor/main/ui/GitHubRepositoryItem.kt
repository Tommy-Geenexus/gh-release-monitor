/*
 * Copyright (c) 2020-2023, Tom Geiselmann (tomgapplicationsdevelopment@gmail.com)
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

package com.tomg.githubreleasemonitor.main.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.tomg.githubreleasemonitor.R
import com.tomg.githubreleasemonitor.main.data.GitHubRepository
import java.time.ZonedDateTime

@Composable
fun GitHubRepositoryItem(
    modifier: Modifier = Modifier,
    dismissState: DismissState = rememberDismissState(),
    gitHubRepository: GitHubRepository = GitHubRepository(),
    onGitHubUserAvatarSelected: (String) -> Unit = {},
    onGitHubRepositoryReleaseSelected: (String) -> Unit = {}
) {
    SwipeToDismiss(
        state = dismissState,
        background = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.CenterStart
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 28.dp),
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            }
        },
        dismissContent = {
            OutlinedCard(
                modifier = Modifier.clickable {
                    onGitHubRepositoryReleaseSelected(gitHubRepository.latestReleaseHtmlUrl)
                },
                shape = RoundedCornerShape(0.dp),
                border = BorderStroke(0.dp, Color.Transparent)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val painter = rememberAsyncImagePainter(
                        model = gitHubRepository.authorAvatarUrl,
                        imageLoader = LocalContext.current.imageLoader,
                        error = painterResource(id = R.drawable.ic_broken_image)
                    )
                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(16.dp)
                            .size(48.dp)
                            .clip(CircleShape)
                            .placeholder(
                                visible = painter.state is AsyncImagePainter.State.Loading,
                                highlight = PlaceholderHighlight.shimmer()
                            )
                            .clickable {
                                onGitHubUserAvatarSelected(gitHubRepository.authorAvatarUrl)
                            }
                    )
                    Column(
                        modifier = Modifier.padding(
                            start = 24.dp,
                            top = 16.dp,
                            bottom = 16.dp
                        )
                    ) {
                        Text(
                            text = gitHubRepository.owner,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = gitHubRepository.name,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Surface(
                            modifier = Modifier.padding(top = 8.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(
                                text = stringResource(id = R.string.latest),
                                modifier = Modifier.padding(all = 4.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.End,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        Row(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocalOffer,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                if (gitHubRepository.latestReleaseName.isNotEmpty()) {
                                    Text(
                                        text = gitHubRepository.latestReleaseName,
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 1,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Text(
                                    text = TimeAgo.using(
                                        time = ZonedDateTime
                                            .parse(gitHubRepository.latestReleaseTimestamp)
                                            .toInstant()
                                            .toEpochMilli()
                                    ),
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier,
        directions = setOf(DismissDirection.StartToEnd)
    )
}

@Preview(name = "Repository Item Preview")
@Composable
fun GitHubRepositoryItem() {
    GitHubRepositoryItem()
}
