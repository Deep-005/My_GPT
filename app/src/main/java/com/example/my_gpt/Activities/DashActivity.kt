package com.example.my_gpt.Activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.airbnb.lottie.LottieAnimationView
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.my_gpt.Application.Global
import com.example.my_gpt.R
import com.example.my_gpt.databinding.ActivityDashBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlin.system.exitProcess

class DashActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var animation: LottieAnimationView
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val userName = sharedPreferences.getString("USER_NAME", "Guest")
        supportActionBar?.title = "Welcome, $userName"

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()

    // Setup ActionBarDrawerToggle
        drawerToggle = ActionBarDrawerToggle(this, binding.drawerLayout, R.string.open, R.string.close)
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize the Lottie animation in the drawer header
        val headerView = binding.drawerNavigation.getHeaderView(0)
        animation = headerView.findViewById(R.id.anime)

        // Control Lottie animation based on drawer state
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                // No action needed here for Lottie animation control
            }

            override fun onDrawerOpened(drawerView: View) {
                animation.playAnimation()
            }

            override fun onDrawerClosed(drawerView: View) {
                animation.pauseAnimation()
            }

            override fun onDrawerStateChanged(newState: Int) {

            }
        })
        // user API usage panel
        updateUsageCounts()
        // Handle navigation item clicks
        binding.drawerNavigation.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.item1 -> startActivity(Intent(this@DashActivity, TextActivity::class.java))
                R.id.item2 -> startActivity(Intent(this@DashActivity, ImageActivity::class.java))
                R.id.item3 -> startActivity(Intent(this@DashActivity, SummarizerActivity::class.java))
                R.id.item4 -> startActivity(Intent(this@DashActivity, FAQActivity::class.java))
                R.id.item5 -> startActivity(Intent(this@DashActivity, FeedbackActivity::class.java))
                R.id.item6 -> showExitDialog()
                R.id.item7 -> startActivity(Intent(this@DashActivity, LogoutActivity::class.java))

            }
            true
        }

        //image Slider
        val imageList = ArrayList<SlideModel>()

        imageList.add(SlideModel(R.drawable.one))
        imageList.add(SlideModel(R.drawable.two))
        imageList.add(SlideModel(R.drawable.three))
        imageList.add(SlideModel(R.drawable.four))
        imageList.add(SlideModel(R.drawable.five))
        imageList.add(SlideModel(R.drawable.six))
        imageList.add(SlideModel(R.drawable.seven))
        imageList.add(SlideModel(R.drawable.eight))
        imageList.add(SlideModel(R.drawable.nine))


        val imageSlider = findViewById<ImageSlider>(R.id.imageSlider)
        imageSlider.setImageList(imageList, ScaleTypes.CENTER_CROP)

        //about text
        binding.abtText.text = Html.fromHtml(about())

    }

    private fun showExitDialog() {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle("Exit My-GPT")
            .setMessage("Do you really want to exit?")
            .setPositiveButton("Yes") { _, _ ->
                exitProcess(1)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        val customDialog = builder.create()
        customDialog.show()
        customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE)
        customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.GREEN)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (drawerToggle.onOptionsItemSelected(item)) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun about():String{
        return "<b><h2>About Our App</h2><b><br/>" +
                "Welcome to our innovative app, designed to " +
                "enhance your creativity and productivity with ease! " +
                "This app is your ultimate tool for " +
                "generating high-quality text content, " +
                "creating stunning images from text prompts," +
                " and summarizing articles—all through a " +
                "user-friendly interface that is both" +
                " intuitive and efficient.<br/>" +
                "<br/>" +
                "Whether you're looking to generate engaging " +
                "content, visualize your ideas, or quickly" +
                " digest information, our app provides a " +
                "seamless experience tailored to your needs." +
                " Built with advanced APIs, it ensures you get " +
                "the most out of every interaction, transforming" +
                " your inputs into meaningful outputs in just " +
                "a few clicks.<br/>" +
                "<br/>" +
                "<b><h2>Key Features</h2></b><br/>"+
                "<b><h4>1. AI Text Generation:</h4></b> Effortlessly generate " +
                        "high-quality, engaging text for your content " +
                        "needs. Whether it's for blogs, social media," +
                        " or marketing, our app provides a variety" +
                        " of writing styles and tones to choose from.<br/>"+
                "<b><h4>2. Image Creation from Text:</h4></b> Turn your words into " +
                        "visuals! Simply input a text prompt, and our" +
                        " app will generate a unique image based on your" +
                        " description. Perfect for creating custom graphics," +
                        " memes, or visual content for social media.<br/>"+
                "<b><h4>3. Article Summarization:</h4></b> Save time by quickly " +
                        "summarizing lengthy articles. Our app" +
                        " extracts key points and presents them " +
                        "in an easy-to-understand format, ensuring " +
                        "you get the essential information without" +
                        " the extra reading.<br/>"+
                "<b><h4>4. User-Friendly Interface:</h4></b> Designed with simplicity " +
                        "and usability in mind, our app ensures a" +
                        " smooth and intuitive experience for all" +
                        " users, regardless of their tech skills.<br/>"+
                 "<b><h4>5. Fast and Efficient:</h4></b> Built with advanced technology " +
                         "to ensure fast processing times, so you" +
                         " get the results you need without delay.<br/>"+
                "<b><h2>Meet the Developer</h2><b>" +
                "<br/>" +
                "Hello there, myself<u> <b>Deepak</b></u>, and I am currently an" +
                " intern at <u><b>FebTech Pvt. Ltd.</b></u> This app " +
                "is the result of my internship project, " +
                "where I've combined cutting-edge technology " +
                "with a commitment to providing users like you" +
                " with an exceptional digital experience. " +
                "I hope you enjoy using the app as much" +
                " as I enjoyed creating it!<br/>" +
                "<br/>" +
                "Thank you for choosing our app." +
                " We look forward to supporting your " +
                "creative and productive endeavors!"
    }

    override fun onResume() {
        super.onResume()
        updateUsageCounts()
    }

    private fun updateUsageCounts() {
        val global = application as Global  // Access the Global class instance

        // Set the text views with the latest usage counts
        binding.tCount.text = global.textCount.toString()   // Set text usage
        binding.iCount.text = global.imgCount.toString()    // Set img usage
        binding.sCount.text = global.summCount.toString()   // Set summary usage
    }

}
