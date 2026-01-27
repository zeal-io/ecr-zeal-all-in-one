package com.alaa.ecrdemo

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.alaa.ecrdemo.databinding.ActivityMainBinding

/**
 * Demo ECR Application to test Zeal ECR Integration
 *
 * This app demonstrates two ways to communicate with Zeal POS:
 * 1. Intent-based: Using startActivityForResult
 * 2. Broadcast-based: Using sendBroadcast and receiving response via BroadcastReceiver
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Zeal package and actions
    companion object {
        const val ZEAL_PACKAGE = "com.zealpay.smartpos"
        const val ZEAL_ECR_SALE_ACTION = "com.zealpay.smartpos.ECR_SALE"
        const val ZEAL_ECR_BROADCAST_ACTION = "com.zealpay.smartpos.ECR_PAYMENT_REQUEST"

        // Response action for broadcast mode
        const val ECR_RESPONSE_ACTION = "com.alaa.ecrdemo.ECR_RESPONSE"

        // Request extras
        const val EXTRA_AMOUNT_CENTS = "amount_cents"
        const val EXTRA_ORDER_ID = "order_id"
        const val EXTRA_CALLBACK_ACTION = "callback_action"
        const val EXTRA_CALLER_PACKAGE = "caller_package"

        // Response extras
        const val EXTRA_STATUS = "status"
        const val EXTRA_TRANSACTION_ID = "transaction_id"
        const val EXTRA_CARD_MASKED_PAN = "card_masked_pan"
        const val EXTRA_AUTH_CODE = "auth_code"
        const val EXTRA_ERROR_MESSAGE = "error_message"
    }

    // Activity result launcher for Intent-based communication
    private val zealLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        handleIntentResult(result.resultCode, result.data)
    }

    // Dynamic broadcast receiver for response (Broadcast mode)
    private val responseReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let { handleBroadcastResponse(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        registerResponseReceiver()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(responseReceiver)
        } catch (e: Exception) {
         Log.e("MainActivity", "Failed to unregister receiver", e)
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun registerResponseReceiver() {
        val filter = IntentFilter(ECR_RESPONSE_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(responseReceiver, filter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(responseReceiver, filter)
        }
    }

    private fun setupListeners() {
        binding.btnPayIntent.setOnClickListener {
            payViaIntent()
        }

        binding.btnPayBroadcast.setOnClickListener {
            payViaBroadcast()
        }

        binding.btnClear.setOnClickListener {
            clearResponse()
        }
    }

    /**
     * Pay via Intent (startActivityForResult)
     * This method launches Zeal's EcrActivity via intent action
     */
    private fun payViaIntent() {
        val amount = getAmountCents()
        if (amount <= 0) {
            showToast("Please enter a valid amount")
            return
        }

        val orderId = binding.etOrderId.text?.toString()?.takeIf { it.isNotBlank() }

        val intent = Intent(ZEAL_ECR_SALE_ACTION).apply {
            setPackage(ZEAL_PACKAGE)
            putExtra(EXTRA_AMOUNT_CENTS, amount)
            orderId?.let { putExtra(EXTRA_ORDER_ID, it) }
            putExtra(EXTRA_CALLBACK_ACTION, ECR_RESPONSE_ACTION)
            putExtra(EXTRA_CALLER_PACKAGE, packageName)
        }

        try {
            showWaiting()
            zealLauncher.launch(intent)
        } catch (e: Exception) {
            showError("Failed to launch Zeal: ${e.message}")
        }
    }

    /**
     * Pay via Broadcast
     * This method sends a broadcast to Zeal and receives response via BroadcastReceiver
     */
    private fun payViaBroadcast() {
        val amount = getAmountCents()
        if (amount <= 0) {
            showToast("Please enter a valid amount")
            return
        }

        val orderId = binding.etOrderId.text?.toString()?.takeIf { it.isNotBlank() }

        val intent = Intent(ZEAL_ECR_BROADCAST_ACTION).apply {
            setPackage(ZEAL_PACKAGE)
            putExtra(EXTRA_AMOUNT_CENTS, amount)
            orderId?.let { putExtra(EXTRA_ORDER_ID, it) }
            // Callback info for response
            putExtra(EXTRA_CALLBACK_ACTION, ECR_RESPONSE_ACTION)
            putExtra(EXTRA_CALLER_PACKAGE, packageName)
        }

        try {
            showWaiting()
            sendBroadcast(intent)
            showToast("Payment request sent via broadcast")
        } catch (e: Exception) {
            showError("Failed to send broadcast: ${e.message}")
        }
    }

    /**
     * Handle response from Intent-based communication
     */
    private fun handleIntentResult(resultCode: Int, data: Intent?) {
        if (data == null) {
            showError("No response data received")
            return
        }

        val status = data.getStringExtra(EXTRA_STATUS)
        val transactionId = data.getStringExtra(EXTRA_TRANSACTION_ID)
        val amountCents = data.getLongExtra(EXTRA_AMOUNT_CENTS, 0)
        val cardMaskedPan = data.getStringExtra(EXTRA_CARD_MASKED_PAN)
        val authCode = data.getStringExtra(EXTRA_AUTH_CODE)
        val errorMessage = data.getStringExtra(EXTRA_ERROR_MESSAGE)

        displayResponse(status, transactionId, amountCents, cardMaskedPan, authCode, errorMessage)
    }

    /**
     * Handle response from Broadcast-based communication
     */
    private fun handleBroadcastResponse(intent: Intent) {
        Log.i("MainActivity", "Response received")
        val status = intent.getStringExtra(EXTRA_STATUS)
        val transactionId = intent.getStringExtra(EXTRA_TRANSACTION_ID)
        val amountCents = intent.getLongExtra(EXTRA_AMOUNT_CENTS, 0)
        val cardMaskedPan = intent.getStringExtra(EXTRA_CARD_MASKED_PAN)
        val authCode = intent.getStringExtra(EXTRA_AUTH_CODE)
        val errorMessage = intent.getStringExtra(EXTRA_ERROR_MESSAGE)
        Log.i("MainActivity", "Response received: $status $transactionId $amountCents $cardMaskedPan $authCode $errorMessage")
        runOnUiThread {
            displayResponse(status, transactionId, amountCents, cardMaskedPan, authCode, errorMessage)
        }
    }

    /**
     * Display response in UI
     */
    private fun displayResponse(
        status: String?,
        transactionId: String?,
        amountCents: Long,
        cardMaskedPan: String?,
        authCode: String?,
        errorMessage: String?
    ) {
        binding.tvStatus.text = status
        binding.tvStatus.setTextColor(
            if (status == "APPROVED") getColor(android.R.color.holo_green_dark)
            else getColor(android.R.color.holo_red_dark)
        )

        binding.tvTransactionId.text = transactionId ?: "-"
        binding.tvAmount.text = if (amountCents > 0) formatAmount(amountCents) else "-"
        binding.tvCard.text = cardMaskedPan ?: "-"
        binding.tvAuthCode.text = authCode ?: "-"

        if (status == "ERROR" && !errorMessage.isNullOrBlank()) {
            binding.tvError.text = "Error: $errorMessage"
            binding.tvError.visibility = View.VISIBLE
        } else {
            binding.tvError.visibility = View.GONE
        }
    }

    private fun showWaiting() {
        binding.tvStatus.text = getString(R.string.response_waiting)
        binding.tvStatus.setTextColor(getColor(android.R.color.darker_gray))
        binding.tvTransactionId.text = "-"
        binding.tvAmount.text = "-"
        binding.tvCard.text = "-"
        binding.tvAuthCode.text = "-"
        binding.tvError.visibility = View.GONE
    }

    private fun clearResponse() {
        binding.tvStatus.text = getString(R.string.response_none)
        binding.tvStatus.setTextColor(getColor(android.R.color.darker_gray))
        binding.tvTransactionId.text = "-"
        binding.tvAmount.text = "-"
        binding.tvCard.text = "-"
        binding.tvAuthCode.text = "-"
        binding.tvError.visibility = View.GONE
    }

    private fun showError(message: String) {
        binding.tvStatus.text = "ERROR"
        binding.tvStatus.setTextColor(getColor(android.R.color.holo_red_dark))
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }

    private fun getAmountCents(): Long {
        return try {
            binding.etAmount.text?.toString()?.toLongOrNull() ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun formatAmount(cents: Long): String {
        val dollars = cents / 100.0
        return String.format("$%.2f (%d cents)", dollars, cents)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
