package com.pnt.data_layer_wear_os

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {

    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val messageClient by lazy { Wearable.getMessageClient(this) }
    private val capabilityClient by lazy { Wearable.getCapabilityClient(this) }

    private val clientDataViewModel by viewModels<ClientDataViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.e(TAG, "onCreate")

        setContent {
            MainApp(
                events = clientDataViewModel.events,
                image = clientDataViewModel.image,
                onQueryOtherDevicesClicked = ::onQueryOtherDevicesClicked,
                onQueryMobileCameraClicked = ::onQueryMobileCameraClicked
            )
        }
    }

    private fun onQueryOtherDevicesClicked() {
        lifecycleScope.launch {
            try {
                val nodes =
                    getCapabilitiesForReachableNodes().filterValues { MOBILE_CAPABILITY in it || WEAR_CAPABILITY in it }.keys

                displayNodes(nodes)
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (exception: Exception) {
                Log.d(TAG, "Querying nodes failed: $exception")
                throw exception
            }
        }
    }

    private fun onQueryMobileCameraClicked() {
        lifecycleScope.launch {
            try {
                val nodes =
                    getCapabilitiesForReachableNodes().filterValues { MOBILE_CAPABILITY in it && CAMERA_CAPABILITY in it }.keys
                displayNodes(nodes)
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (exception: Exception) {
                Log.d(TAG, "Querying nodes failed: $exception")
            }
        }
    }

    /**
     * Collects the capabilities for all nodes that are reachable using the [CapabilityClient].
     *
     * [CapabilityClient.getAllCapabilities] returns this information as a [Map] from capabilities
     * to nodes, while this function inverts the map so we have a map of [Node]s to capabilities.
     *
     * This form is easier to work with when trying to operate upon all [Node]s.
     */
    private suspend fun getCapabilitiesForReachableNodes(): Map<Node, Set<String>> =
        capabilityClient.getAllCapabilities(CapabilityClient.FILTER_REACHABLE).await()
            // Pair the list of all reachable nodes with their capabilities
            .flatMap { (capability, capabilityInfo) ->
                capabilityInfo.nodes.map { it to capability }
            }
            // Group the pairs by the nodes
            .groupBy(keySelector = { it.first }, valueTransform = { it.second })
            // Transform the capability list for each node into a set
            .mapValues { it.value.toSet() }

    private fun displayNodes(nodes: Set<Node>) {
        val message = if (nodes.isEmpty()) {
            getString(R.string.no_device)
        } else {
            getString(R.string.connected_nodes, nodes.joinToString(", ") { it.displayName })
        }

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()

        Log.e(TAG, "onResume")

        try {
            dataClient.addListener(clientDataViewModel)
            Log.e(TAG, "onResume: ${clientDataViewModel.image}")

            messageClient.addListener(clientDataViewModel)
            Log.e(TAG, "onResume: ${clientDataViewModel.image}")

            capabilityClient.addListener(
                clientDataViewModel, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE
            )
            Log.e(TAG, "onResume: ${clientDataViewModel.image}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()

        Log.e(TAG, "onPause")

        try {
            dataClient.removeListener(clientDataViewModel)
            Log.e(TAG, "onPause: ${clientDataViewModel.image}")

            messageClient.removeListener(clientDataViewModel)
            Log.e(TAG, "onPause: ${clientDataViewModel.image}")

            capabilityClient.removeListener(clientDataViewModel)
            Log.e(TAG, "onPause: ${clientDataViewModel.image}")

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val TAG = "MainActivity"

        private const val CAMERA_CAPABILITY = "camera"
        private const val WEAR_CAPABILITY = "wear"
        private const val MOBILE_CAPABILITY = "mobile"
    }
}