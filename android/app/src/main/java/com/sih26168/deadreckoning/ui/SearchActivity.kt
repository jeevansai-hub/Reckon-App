package com.sih26168.deadreckoning.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sih26168.deadreckoning.R

/**
 * Full-screen search. There's no real places/routing backend in this
 * prototype -- picking a row just returns its label to MainActivity to show
 * in the search bar, which is an honest stand-in rather than a fake route.
 */
class SearchActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_QUERY = "query"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_ReckonAI_Light)
        setContentView(R.layout.activity_search)

        val etQuery = findViewById<EditText>(R.id.etSearchQuery)
        val btnClear = findViewById<ImageButton>(R.id.btnClear)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        etQuery.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btnClear.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        btnClear.setOnClickListener { etQuery.text.clear() }
        etQuery.setOnEditorActionListener { _, _, _ ->
            returnResult(etQuery.text.toString().ifBlank { "Search" })
            true
        }

        findViewById<android.view.ViewGroup>(R.id.rowRecent1).setOnClickListener { returnResult("Yerba Buena Tunnel") }
        findViewById<android.view.ViewGroup>(R.id.rowRecent2).setOnClickListener { returnResult("Salesforce Tower") }
        findViewById<android.view.ViewGroup>(R.id.rowRecent3).setOnClickListener { returnResult("San Francisco Intl. Airport (SFO)") }

        findViewById<android.view.ViewGroup>(R.id.rowSetHome).setOnClickListener {
            Toast.makeText(this, "Home address saved (prototype -- not used in routing yet)", Toast.LENGTH_SHORT).show()
        }
        findViewById<android.view.ViewGroup>(R.id.rowSetWork).setOnClickListener {
            Toast.makeText(this, "Work address saved (prototype -- not used in routing yet)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun returnResult(label: String) {
        setResult(RESULT_OK, Intent().putExtra(EXTRA_QUERY, label))
        finish()
    }
}
