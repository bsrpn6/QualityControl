package info.onesandzeros.qualitycontrol.mock

import android.os.Bundle
import android.os.Handler
import android.os.Message
import java.util.Random
import kotlin.math.round


class MockBluetoothService(handler: Handler) {
    private val handler: Handler
    private val random = Random()

    init {
        this.handler = handler
    }

    fun connect(input: Double?) {
        // Simulating a connection to a Bluetooth device...
        // ... and then we read a weight value

        val standardDeviation = 5.0
        val weight = input?.plus(standardDeviation * random.nextGaussian())
        val roundedWeight = round(weight!! * 10) / 10

        // send this weight value using the handler
        val message = Message.obtain()
        val bundle = Bundle()
        bundle.putDouble("weight", roundedWeight)
        message.data = bundle

        handler.sendMessage(message)
    }
}
