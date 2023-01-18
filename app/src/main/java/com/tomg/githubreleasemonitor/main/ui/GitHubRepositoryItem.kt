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
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.SwipeableState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
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
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import kotlin.math.roundToInt

@Composable
fun GitHubRepositoryItem(
    modifier: Modifier = Modifier,
    swipeableState: SwipeableState<Float> = rememberSwipeableState(initialValue = 0f),
    gitHubRepository: GitHubRepository = GitHubRepository(),
    onGitHubUserAvatarSelected: (String) -> Unit = {},
    onGitHubRepositoryReleaseSelected: (String) -> Unit = {},
    onDeleteGitHubRepository: (GitHubRepository) -> Unit = {}
) {
    var fraction by rememberSaveable {
        mutableStateOf(0f)
    }
    val anchorEnd = with(LocalDensity.current) {
        96.dp.toPx()
    }
    val fractionEnd = 36f
    val anchors = mapOf(
        0f to 0f,
        anchorEnd to 1f
    )
    Box(
        modifier = modifier
            .swipeable(
                state = swipeableState,
                anchors = anchors,
                orientation = Orientation.Horizontal
            )
            .fillMaxSize()
            .background(
                color = lerp(
                    start = MaterialTheme.colorScheme.surface,
                    stop = MaterialTheme.colorScheme.error,
                    fraction = fraction
                )
            ),
        propagateMinConstraints = true
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize()
        ) {
            val scope = rememberCoroutineScope()
            IconButton(
                onClick = {
                    scope.launch {
                        swipeableState.snapTo(0f)
                    }
                    onDeleteGitHubRepository(gitHubRepository)
                },
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
        ElevatedCard(
            modifier = Modifier
                .offset {
                    if (swipeableState.overflow.value == 0f) {
                        val value = minOf(swipeableState.offset.value, fractionEnd)
                        fraction = value / fractionEnd
                    }
                    IntOffset(
                        x = swipeableState.offset.value.roundToInt(),
                        y = 0
                    )
                }
                .clickable {
                    onGitHubRepositoryReleaseSelected(gitHubRepository.latestReleaseHtmlUrl)
                },
            shape = RoundedCornerShape(0.dp),
            elevation = CardDefaults.outlinedCardElevation()
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
                        Column(
                            modifier = Modifier.weight(
                                weight = 0.65f,
                                fill = false
                            )
                        ) {
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
                        Row(
                            modifier = Modifier.weight(
                                weight = 0.35f,
                                fill = false
                            )
                        ) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
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
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Repository Item Preview")
@Composable
fun GitHubRepositoryItem() {
    GitHubRepositoryItem()
}
