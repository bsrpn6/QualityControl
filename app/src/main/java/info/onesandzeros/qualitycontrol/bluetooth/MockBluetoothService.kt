package info.onesandzeros.qualitycontrol.bluetooth

import java.util.Random
import kotlin.math.round

class MockBluetoothService(private val onWeightReceived: (Double) -> Unit) {
    private val random = Random()

    fun connect(input: Double?) {
        // Simulating a connection to a Bluetooth device...
        // ... and then we read a weight value

        val standardDeviation = 10.0
        val weight = input?.plus(standardDeviation * random.nextGaussian())
        val roundedWeight = round(weight!! * 10) / 10

        // invoke the callback with the weight
        onWeightReceived(roundedWeight)
    }
}
