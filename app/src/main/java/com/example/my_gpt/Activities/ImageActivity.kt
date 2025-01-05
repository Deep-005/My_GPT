package com.example.my_gpt.Activities

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.my_gpt.Adapters.ImageAdapter
import com.example.my_gpt.Adapters.SmmryAdapter
import com.example.my_gpt.Application.Global
import com.example.my_gpt.Interfaces.NewImageRequest
import com.example.my_gpt.Interfaces.NewImageResponse
import com.example.my_gpt.Network.RetrofitClient
import com.example.my_gpt.R
import com.example.my_gpt.databinding.ActivityImageBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageBinding
    private lateinit var imgAdapter: ImageAdapter
    private val historyItems = mutableListOf<String>()
    private val conversationHistory = mutableListOf<String>()  // To maintain context
    private var currentImageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadHistoryItems() // load previous history

        // Initially hide the generate button and the image view
        binding.generate.visibility = View.GONE
        binding.generated.visibility = View.GONE
        binding.loading.visibility =View.GONE

        binding.backToDash.setOnClickListener { finish() }

        // Add a text change listener to EditText
        binding.url.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Show button only when input is not empty
                binding.generate.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                binding.clear.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No operation before text is changed
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No operation on text changed
            }
        })

        // Set click listener for generate button
        binding.generate.setOnClickListener {
            val prompt = binding.url.text.toString().trim()
            if (prompt.isNotEmpty()) {
                binding.generated.visibility = View.GONE
                binding.clear.visibility = View.VISIBLE
                binding.loading.visibility = View.VISIBLE
                fetchImageFromApi(prompt)
                hideKeyboard()
                // Add the question to conversation history
                conversationHistory.add("User: $prompt")
                historyItems.add(prompt) // Also add to history items
                imgAdapter.notifyItemInserted(historyItems.size - 1)
                saveHistoryItems()

            }else{
                binding.imageLoader.visibility = View.VISIBLE
                binding.clear.visibility = View.GONE
            }
        }

        // Initialize download button functionality
        binding.download.setOnClickListener {
            currentImageUrl?.let { imageUrl ->
                downloadImage(imageUrl)
            } ?: run {
                Toast.makeText(this, "No image to download", Toast.LENGTH_SHORT).show()
            }
        }

        // call functions
        options()
        setupHistoryPopup()
        clearButtonListener()

    }

    private fun fetchImageFromApi(prompt: String) {
        val apiService = RetrofitClient.apiService
        val request = NewImageRequest(prompt)

        apiService.generateImage(request).enqueue(object : Callback<NewImageResponse> {
            override fun onResponse(call: Call<NewImageResponse>, response: Response<NewImageResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val imageUrl = response.body()!!.url
                    currentImageUrl = imageUrl // Store the image URL

                    runOnUiThread {
                        // Load the image into the ImageView using Glide
                        Glide.with(this@ImageActivity)
                            .load(imageUrl)
                            .into(binding.generated)

                        // Hide loading animation and show the generated image
                        binding.imageLoader.visibility = View.GONE
                        binding.loading.visibility = View.GONE
                        binding.generated.visibility = View.VISIBLE
                        binding.download.visibility = View.VISIBLE

                        //increment usage
                        val globalVariable = application as? Global
                        if (globalVariable != null) {
                            globalVariable.imgCount += 1
                            globalVariable.saveCounts()
                        } else {
                            Log.e("GlobalVariable", "Application instance is not of type Global")
                        }

                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = "Error: ${response.code()} - ${response.message()} \n $errorBody"
                    runOnUiThread {
                        Toast.makeText(this@ImageActivity, errorMessage, Toast.LENGTH_LONG).show()
                        binding.imageLoader.visibility = View.VISIBLE
                    }
                }
            }

            override fun onFailure(call: Call<NewImageResponse>, t: Throwable) {
                runOnUiThread {
                    val errorMessage = "Failed to fetch image: ${t.localizedMessage}"
                    Toast.makeText(this@ImageActivity, errorMessage, Toast.LENGTH_LONG).show()
                    binding.imageLoader.visibility = View.GONE
                }
            }
        })
    }

    private fun downloadImage(imageUrl: String) {
        val request = DownloadManager.Request(Uri.parse(imageUrl))
            .setTitle("Image Download")
            .setDescription("Downloading image from API")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "downloadedImage.jpg")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
        Toast.makeText(this, "Download started...", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        binding.imageLoader.playAnimation()
    }

    override fun onPause() {
        super.onPause()
        binding.imageLoader.pauseAnimation()
    }

    override fun onStop() {
        super.onStop()
        binding.imageLoader.cancelAnimation()
    }

    @SuppressLint("InflateParams", "MissingInflatedId")
    private fun setupHistoryPopup() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.image_popup, null)
        val imgHistory = popupView.findViewById<RecyclerView>(R.id.recyclerView)

        imgAdapter = ImageAdapter(historyItems,
            itemClickListener = { item ->
                Toast.makeText(this, "Item clicked", Toast.LENGTH_SHORT).show()
            },
            itemDeleteListener = { position ->
                Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show()
            })

        imgHistory.adapter = imgAdapter
        imgHistory.layoutManager = LinearLayoutManager(this)

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
                imgAdapter.removeItem(position)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(imgHistory)

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
            binding.generated.visibility = View.GONE
            binding.url.text.clear()
            binding.options.visibility = View.GONE
            conversationHistory.clear()
        }
    }

    private fun saveHistoryItems() {
        val sharedPreferences = getSharedPreferences("MyAppImage", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putStringSet("historyItems", historyItems.toSet())
        editor.apply()
    }

    private fun loadHistoryItems() {
        val sharedPreferences = getSharedPreferences("MyAppImage", Context.MODE_PRIVATE)
        val savedItems = sharedPreferences.getStringSet("historyItems", setOf())?.toMutableList() ?: mutableListOf()
        historyItems.clear()
        historyItems.addAll(savedItems)
    }

    private fun hideKeyboard() {
        val view = currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

}