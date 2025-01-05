package com.example.my_gpt.Activities

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.PopupWindow
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.my_gpt.Adapters.SmmryAdapter
import com.example.my_gpt.Application.Global
import com.example.my_gpt.DataClass.SummaryResponse
import com.example.my_gpt.Interfaces.ApiService
import com.example.my_gpt.R
import com.example.my_gpt.databinding.ActivitySummarizerBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SummarizerActivity : AppCompatActivity() {

    lateinit var binding: ActivitySummarizerBinding
    private lateinit var summryAdapter: SmmryAdapter
    private val historyItems = mutableListOf<String>()
    private val conversationHistory = mutableListOf<String>()  // To maintain context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySummarizerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadHistoryItems() // Load history items from SharedPreferences
        options()
        clearButtonListener()
        setupHistoryPopup()

        binding.backToDash.setOnClickListener { finish() }
        binding.url.setOnClickListener { hideKeyboard() }

        // summarize article logic
        urlBoxCheck()
        summarizeButton()

    }

    @SuppressLint("InflateParams", "MissingInflatedId")
    private fun setupHistoryPopup() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.summ_history, null)
        val summHistory = popupView.findViewById<RecyclerView>(R.id.recyclerView)

        summryAdapter = SmmryAdapter(historyItems,
            itemClickListener = { item ->
                Toast.makeText(this, "Item clicked", Toast.LENGTH_SHORT).show()
            },
            itemDeleteListener = { position ->
                Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show()
            })

        summHistory.adapter = summryAdapter
        summHistory.layoutManager = LinearLayoutManager(this)

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
                summryAdapter.removeItem(position)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(summHistory)

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

    private fun options() {
        binding.dots.setOnClickListener {
            binding.options.visibility = if (binding.options.visibility == View.GONE) View.VISIBLE else View.GONE
        }
    }

    private fun clearButtonListener() {
        binding.clear.setOnClickListener {
            binding.summary.text = ""
            binding.summary.visibility = View.GONE
            binding.url.text.clear()
            binding.options.visibility = View.GONE
            conversationHistory.clear()
        }
    }

    private fun saveHistoryItems() {
        val sharedPreferences = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putStringSet("historyItems", historyItems.toSet())
        editor.apply()
    }

    private fun loadHistoryItems() {
        val sharedPreferences = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
        val savedItems = sharedPreferences.getStringSet("historyItems", setOf())?.toMutableList() ?: mutableListOf()
        historyItems.clear()
        historyItems.addAll(savedItems)
    }

    private fun fetchSummary(url: String) {
        // Create an anonymous Retrofit object
        val retrofit = Retrofit.Builder()
            .baseUrl("https://article-extractor-and-summarizer.p.rapidapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        val call = apiService.getSummary(
            url = url,
            apiKey = "6199e24496msh4ba9d031fa24763p12cd8ejsn665804ca23a4"
        )

        call.enqueue(object : Callback<SummaryResponse> {
            override fun onResponse(call: Call<SummaryResponse>, response: Response<SummaryResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val summaryResponse = response.body()
                    binding.smmry.text = summaryResponse?.summary

                    //increment usage
                    val globalVariable = application as? Global
                    if (globalVariable != null) {
                        globalVariable.summCount += 1
                        globalVariable.saveCounts()
                    } else {
                        Log.e("GlobalVariable", "Application instance is not of type Global")
                    }

                    saveHistoryItems() // Save to history if needed
                } else {
                    Toast.makeText(baseContext,"Failed to fetch",Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SummaryResponse>, t: Throwable) {
                Toast.makeText(this@SummarizerActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Toast.makeText(baseContext,t.message,Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun hideKeyboard() {
        val view = currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun urlBoxCheck(){
        // Initially hide the submit and clear button
        binding.summary.visibility = View.GONE
        binding.clear.visibility = View.GONE

        binding.url.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed before text is changed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.summary.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                binding.clear.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {
                // No action needed after text is changed
            }
        })

    }

    override fun onResume() {
        super.onResume()
        binding.animation.playAnimation()
    }

    override fun onPause() {
        super.onPause()
        binding.animation.pauseAnimation()
    }

    override fun onStop() {
        super.onStop()
        binding.animation.cancelAnimation()
    }

    private fun summarizeButton(){

        binding.summary.setOnClickListener {
            val userInputUrl = binding.url.text.toString() // Get the user input URL from EditText
            if (userInputUrl.isNotEmpty()) {
                if (binding.smmry.visibility == View.GONE){
                    binding.smmry.visibility = View.VISIBLE
                    binding.animation.visibility = View.GONE
                    fetchSummary(userInputUrl)
                    // Add the question to conversation history
                    conversationHistory.add("User: $userInputUrl")
                    historyItems.add(userInputUrl) // Also add to history items
                    summryAdapter.notifyItemInserted(historyItems.size - 1)
                    saveHistoryItems()
                }else{
                    binding.smmry.visibility = View.GONE
                    binding.animation.visibility = View.VISIBLE
                }

            } else {
                Toast.makeText(baseContext,"Something went wrong.",Toast.LENGTH_SHORT).show()
            }
        }

    }

}
