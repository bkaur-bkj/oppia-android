package org.oppia.android.domain.exploration

import org.oppia.android.app.model.Exploration
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.ProfileId
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationCheckpointController
import org.oppia.android.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import javax.inject.Inject

private const val GET_EXPLORATION_BY_ID_PROVIDER_ID =
  "get_exploration_by_id_provider_id"

/**
 * Controller for loading explorations by ID, or beginning to play an exploration. This controller
 * is also responsible for controlling the saved checkpoints when the exploration is started or
 * stopped.
 *
 * At most one exploration may be played at a given time, and its state will be managed by
 * [ExplorationProgressController].
 */
class ExplorationDataController @Inject constructor(
  private val explorationProgressController: ExplorationProgressController,
  private val explorationRetriever: ExplorationRetriever,
  private val dataProviders: DataProviders,
  private val exceptionsController: ExceptionsController,
  private val explorationCheckpointController: ExplorationCheckpointController
) {
  /** Returns an [Exploration] given an ID. */
  fun getExplorationById(id: String): DataProvider<Exploration> {
    return dataProviders.createInMemoryDataProviderAsync(
      GET_EXPLORATION_BY_ID_PROVIDER_ID
    ) {
      retrieveExplorationById(id)
    }
  }

  /**
   * Begins playing an exploration of the specified ID.
   *
   * [ExplorationProgressController] should be used to manage the play state, and monitor the load
   * success/failure of the exploration.
   *
   * This can be called even if a session is currently active as it will force initiate a new play
   * session, resetting any data from the previous session (though any pending unsaved checkpoint
   * progress is guaranteed to be saved from the previous session, first).
   *
   * [stopPlayingExploration] may be optionally called to clean up the session--see the
   * documentation for that method for details.
   *
   * @param internalProfileId the ID corresponding to the profile for which exploration has to be
   *     played
   * @param topicId the ID corresponding to the topic for which exploration has to be played
   * @param storyId the ID corresponding to the story for which exploration has to be played
   * @param explorationId the ID of the exploration which has to be played
   * @param shouldSavePartialProgress indicates if partial progress should be saved for the new play
   *     session
   * @param explorationCheckpoint the checkpoint which may be used to resume the exploration
   * @return a [DataProvider] to observe whether initiating the play request, or future play
   *     requests, succeeded
   */
  fun startPlayingExploration(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    shouldSavePartialProgress: Boolean,
    explorationCheckpoint: ExplorationCheckpoint
  ): DataProvider<Any?> {
    return explorationProgressController.beginExplorationAsync(
      ProfileId.newBuilder().apply { internalId = internalProfileId }.build(),
      topicId,
      storyId,
      explorationId,
      shouldSavePartialProgress,
      explorationCheckpoint
    )
  }

  /**
   * Finishes the most recent exploration started by [startPlayingExploration], and returns a
   * [DataProvider] indicating whether the operation succeeded.
   *
   * This method should only be called if an active exploration is being played, otherwise the
   * resulting provider will fail. Note that this doesn't actually need to be called between
   * sessions unless the caller wants to ensure other providers monitored from
   * [ExplorationProgressController] are reset to a proper out-of-session state.
   *
   * Note that the returned provider monitors the long-term stopping state of exploration sessions
   * and will be reset to 'pending' when a session is currently active, or before any session has
   * started.
   */
  fun stopPlayingExploration(): DataProvider<Any?> =
    explorationProgressController.finishExplorationAsync()

  /**
   * Fetches the details of the oldest saved exploration for a specified profileId.
   *
   * @param profileId the ID corresponding to the profile for which the oldest checkpoint details
   *     has to be retrieved
   * @return a [DataProvider] that indicates the success or failure of the retrieve operation
   */
  fun getOldestExplorationDetailsDataProvider(profileId: ProfileId) =
    explorationCheckpointController.retrieveOldestSavedExplorationCheckpointDetails(profileId)

  /**
   * Kicks off the operation to delete the saved progress for the exploration specified by the
   * exploration id and profile id.
   *
   * @param profileId the ID corresponding to the profile for which the oldest checkpoint details
   *     has to be retrieved
   * @param explorationId the ID of the exploration whose checkpoint has to be deleted
   */
  fun deleteExplorationProgressById(profileId: ProfileId, explorationId: String) {
    explorationCheckpointController.deleteSavedExplorationCheckpoint(
      profileId,
      explorationId
    )
  }

  // DataProviders expects this function to be a suspend function.
  @Suppress("RedundantSuspendModifier")
  private suspend fun retrieveExplorationById(explorationId: String): AsyncResult<Exploration> {
    return try {
      AsyncResult.Success(explorationRetriever.loadExploration(explorationId))
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e)
      AsyncResult.Failure(e)
    }
  }
}
