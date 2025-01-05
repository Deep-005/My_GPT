package com.example.my_gpt.Activities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.my_gpt.Adapters.HistoryAdapter
import com.example.my_gpt.Application.Global
import com.example.my_gpt.R
import com.example.my_gpt.databinding.ActivityTextBinding
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class TextActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTextBinding
    private lateinit var historyAdapter: HistoryAdapter
    private val historyItems = mutableListOf<String>()
    val ApiKey = "AI#############################VA"
    private var responseText: String? = null
    private val conversationHistory = mutableListOf<String>()  // To maintain context

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backToDash.setOnClickListener {finish() }

        loadHistoryItems() // Load history items from SharedPreferences
        // Set up listeners for search bar, send button, card views, and clear button
        options()
        searchBarListener()
        sendButtonListener()
        cardClickListeners()
        clearButtonListener()
        setupHistoryPopup()
    }

    @SuppressLint("InflateParams")
    private fun setupHistoryPopup() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_history, null)
        val recyclerViewHistory = popupView.findViewById<RecyclerView>(R.id.recyclerViewHistory)

        historyAdapter = HistoryAdapter(historyItems,
            itemClickListener = { item ->
                addQuestionToDataBox(item)
            },
            itemDeleteListener = { position ->
                Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show()
            })

        recyclerViewHistory.adapter = historyAdapter
        recyclerViewHistory.layoutManager = LinearLayoutManager(this)

        // Set up swipe-to-delete
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                historyAdapter.removeItem(position)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerViewHistory)

        // Show the popup when the "history" button is clicked
        binding.history.setOnClickListener {
            val popupWindow = PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            popupWindow.isFocusable = true
            popupWindow.showAtLocation(binding.root, Gravity.CENTER, 0, 0)
        }
    }

    // Method to clean the response text
    private fun cleanResponseText(text: String?): String {
        if (text == null) return ""
        return text.replace("[*#]".toRegex(), "").trim()
    }

    private fun addQuestionToDataBox(questionText: String) {
        val questionTextView = TextView(this).apply {
            text = questionText
            textSize = 16f
            setTextColor(resources.getColor(R.color.white, null))
            setPadding(15, 10, 15, 10)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(120, 10, 10, 5) // Margins for styling
                gravity = Gravity.END
            }
            textAlignment = View.TEXT_ALIGNMENT_VIEW_END
            setTextIsSelectable(true)
            setBackgroundResource(R.drawable.input)
        }

        // Create a new TextView for the answer (initially empty)
        val answerTextView = TextView(this).apply {
            textSize = 16f
            setTextColor(resources.getColor(R.color.white, null))
            setPadding(15, 10, 15, 10)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(10, 5, 120, 10) // Margins for spacing
                gravity = Gravity.START
            }
            setTextIsSelectable(true)
            setBackgroundResource(R.drawable.output)
        }

        binding.dataBox.addView(questionTextView)
        binding.dataBox.addView(answerTextView)

        // Show the Clear button because we now have content
        binding.clear.visibility = View.VISIBLE

        if (binding.scroll.visibility == View.GONE) {
            binding.scroll.visibility = View.VISIBLE
            binding.grid.visibility = View.GONE
        }

        // Add the question to conversation history
        conversationHistory.add("User: $questionText")
        historyItems.add(questionText) // Also add to history items
        historyAdapter.notifyItemInserted(historyItems.size - 1)
        saveHistoryItems()
    }

    private fun sendButtonListener() {
        binding.send.setOnClickListener {
            val userInput = binding.search.text.toString().trim()
            if (userInput.isBlank()) return@setOnClickListener

            // Show the question in the UI first
            addQuestionToDataBox(userInput)
            binding.search.text.clear()
            hideKeyboard()

            // Send the question to the API
            sendQuestionToApi(userInput)
        }
    }

    private fun searchBarListener() {
        binding.search.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus || binding.scroll.visibility == View.VISIBLE) {
                binding.grid.visibility = View.GONE
            } else {
                binding.grid.visibility = View.VISIBLE
                hideKeyboard()
            }
        }
    }

    private fun cardClickListeners() {
        // Simplify click listeners for cards
        listOf(binding.card1, binding.card2, binding.card3, binding.card4).forEachIndexed { index, card ->
            card.setOnClickListener {
                val question = when (index) {
                    0 -> binding.text1.text.toString()
                    1 -> binding.text2.text.toString()
                    2 -> binding.text3.text.toString()
                    else -> binding.text4.text.toString()
                }
                addQuestionToDataBox(question)
                sendQuestionToApi(question)
            }
        }
    }

    private fun sendQuestionToApi(question: String) {
        hideKeyboard()
        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = ApiKey
        )

        // Include conversation history for context
        val fullContext = (conversationHistory + "User: $question").joinToString("\n")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = generativeModel.generateContent(fullContext)
                val cleanedResponse = cleanResponseText(response.text)  // Clean the response text
                withContext(Dispatchers.Main) {
                    responseText = cleanedResponse
                    updateAnswerInDataBox(responseText)
                    conversationHistory.add("Bot: $responseText")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    responseText = "Error: ${e.message}"
                    updateAnswerInDataBox(responseText)
                }
            }
        }
    }

    private fun updateAnswerInDataBox(answer: String?) {
        (binding.dataBox.getChildAt(binding.dataBox.childCount - 1) as? TextView)?.text = answer
        //increment usage
        val globalVariable = application as? Global
        if (globalVariable != null) {
            globalVariable.textCount += 1
            globalVariable.saveCounts()
        } else {
            Log.e("GlobalVariable", "Application instance is not of type Global")
        }
    }

    private fun options() {
        binding.dots.setOnClickListener {
            binding.options.visibility = if (binding.options.visibility == View.GONE) View.VISIBLE else View.GONE
        }
    }

    private fun clearButtonListener() {
        binding.clear.setOnClickListener {
            binding.dataBox.removeAllViews()
            binding.options.visibility = View.GONE
            conversationHistory.clear()
        }
    }

    private fun hideKeyboard() {
        val view = currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun saveHistoryItems() {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putStringSet("historyItems", historyItems.toSet())
        editor.apply()
    }

    private fun loadHistoryItems() {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val savedItems = sharedPreferences.getStringSet("historyItems", setOf())?.toMutableList() ?: mutableListOf()
        historyItems.clear()
        historyItems.addAll(savedItems)
    }
}



