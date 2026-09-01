package org.example.rickandmorti.presentation.screens.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Heart
import org.example.rickandmorti.domain.model.Episode

@Composable
fun ItemEpisode(
    episode: Episode,
    isFavorite: Boolean,
    onToggleFavorite: (Episode) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .wrapContentWidth()
            .height(148.dp)
            .padding(8.dp)
            .clickable{ onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
//        Row(
//            modifier = Modifier.padding(16.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {


            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = episode.name,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = episode.episode,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center)
                IconButton(
                    modifier = Modifier.wrapContentWidth().padding(8.dp),
                    onClick = { onToggleFavorite(episode) }
                ) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        imageVector = if (isFavorite) TablerIcons.Heart else TablerIcons.Heart,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) Color.Red else LocalContentColor.current.copy(alpha = 0.5f)
                    )
                }
            }



//            IconButton(onClick = { onToggleFavorite(episode) }) {
//                Icon(
//                    imageVector = if (isFavorite) TablerIcons.Heart else TablerIcons.Heart,
//                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
//                    tint = if (isFavorite) Color.Red else LocalContentColor.current.copy(alpha = 0.5f)
//                )
//            }
//        }
    }
}