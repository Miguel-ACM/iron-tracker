package com.daykon.irontracker.composable

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.daykon.irontracker.viewModels.events.ExerciseRecordEvent
import com.daykon.irontracker.viewModels.state.ExerciseState

@Composable
fun ExpandableSearchView(
    state: ExerciseState,
    onEvent: (ExerciseRecordEvent) -> Unit,
    modifier: Modifier = Modifier,
    onSearchDisplayClosed: () -> Unit = {},
    expandedInitially: Boolean = false,
    //tint: Color = MaterialTheme.colors.onPrimary
) {
    val (expanded, onExpandedChanged) = remember {
        mutableStateOf(expandedInitially)
    }


    Crossfade(targetState = expanded) { isSearchFieldVisible ->
        when (isSearchFieldVisible) {
            true -> ExpandedSearchView(
                state = state,
                onEvent = onEvent,
                onSearchDisplayClosed = onSearchDisplayClosed,
                onExpandedChanged = onExpandedChanged,
                modifier = modifier,
                //tint = tint
            )

            false -> CollapsedSearchView(
                onExpandedChanged = onExpandedChanged,
                modifier = modifier,
                //tint = tint
            )
        }
    }
}

@Composable
fun SearchIcon() {
    Icon(
        Icons.Default.Search,
        contentDescription = "search icon",
        //tint = iconTint
    )
}

@Composable
fun CollapsedSearchView(
    onExpandedChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Iron tracker",
            //style = MaterialTheme.typography.h6,
            modifier = Modifier
                .padding(start = 16.dp)
        )
        IconButton(onClick = { onExpandedChanged(true) }) {
            SearchIcon()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedSearchView(
    state: ExerciseState,
    onEvent: (ExerciseRecordEvent) -> Unit,
    onSearchDisplayClosed: () -> Unit,
    onExpandedChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    //tint: Color = MaterialTheme.colors.onPrimary,
) {
    val textFieldFocusRequester = remember { FocusRequester() }

    SideEffect {
        textFieldFocusRequester.requestFocus()
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {
            onExpandedChanged(false)
            onSearchDisplayClosed()
        }) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "back icon"
                //tint = tint
            )
        }

        BasicTextField(
            modifier = Modifier.fillMaxWidth()
                .focusRequester(textFieldFocusRequester)
                .background(color = MaterialTheme.colorScheme.onPrimaryContainer, shape = RoundedCornerShape(size = 16.dp))
                .padding(PaddingValues(15.dp,5.dp)),
            value = state.searchTerm,
            onValueChange = {
                onEvent(ExerciseRecordEvent.UpdateSearch(it))
            },
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onPrimary
            ),

            decorationBox = { innerTextField ->
                Row(modifier = Modifier.fillMaxWidth()) {

                }
                innerTextField()
            }
        )

    }
}