package com.pegasus.cardbattlerpg.ui.story

import androidx.compose.runtime.Composable
import com.pegasus.cardbattlerpg.repository.GameRepository
import com.pegasus.cardbattlerpg.ui.battle.BattleScreen

@Composable
fun StoryBattleScreen(
    repository: GameRepository,
    stageId: Int,
    onBack: () -> Unit
) {
    BattleScreen(
        repository = repository,
        storyStageId = stageId,
        onBack = onBack
    )
}