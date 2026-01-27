package com.alaa.ecrdemo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.alaa.ecrdemo.MainActivity

/**
 * BroadcastReceiver that handles ECR payment responses from Zeal POS.
 * 
 * This receiver is registered in the manifest with action "com.alaa.ecrdemo.ECR_RESPONSE"
 * and is triggered when Zeal sends back the payment result via broadcast.
 * 
 * Note: For a real ECR app, you would typically forward this response to your
 * main activity or service for processing. This demo implementation shows
 * a toast and logs the response.
 */
class EcrResponseReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "EcrResponseReceiver"
        
        // Response extras (same as MainActivity)
        const val EXTRA_STATUS = "status"
        const val STATUS_APPROVED = "APPROVED"
        const val EXTRA_TRANSACTION_ID = "transaction_id"
        const val EXTRA_AMOUNT_CENTS = "amount_cents"
        const val EXTRA_ERROR_MESSAGE = "error_message"
        const val EXTRA_ORDER_ID = "order_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "Received ECR response from Zeal")

        val status = intent.getStringExtra(EXTRA_STATUS)
        val success = status == STATUS_APPROVED
        val transactionId = intent.getStringExtra(EXTRA_TRANSACTION_ID)
        val amountCents = intent.getLongExtra(EXTRA_AMOUNT_CENTS, 0)
        val errorMessage = intent.getStringExtra(EXTRA_ERROR_MESSAGE)
        val orderId = intent.getStringExtra(EXTRA_ORDER_ID)

        Log.i(TAG, buildString {
            append("ECR Response: ")
            append("success=$success, ")
            append("transactionId=$transactionId, ")
            append("amountCents=$amountCents, ")
            append("orderId=$orderId, ")
            if (!success) {
                append(", errorMessage=$errorMessage")
            }
        })

        // Show a toast with the result
        val message = if (success) {
            "Payment successful! TxID: $transactionId"
        } else {
            "Payment failed: ${errorMessage ?: "Unknown error"}"
        }
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()

        // Forward the intent to MainActivity if it's running
        // This is done by re-broadcasting with a local action
        val forwardIntent = Intent(MainActivity.Companion.ECR_RESPONSE_ACTION).apply {
            putExtra(EXTRA_STATUS, status)
            putExtra(EXTRA_TRANSACTION_ID, transactionId)
            putExtra(EXTRA_AMOUNT_CENTS, amountCents)
            putExtra(EXTRA_ERROR_MESSAGE, errorMessage)
            putExtra(EXTRA_ORDER_ID, orderId)
        }
        context.sendBroadcast(forwardIntent)
    }
}
