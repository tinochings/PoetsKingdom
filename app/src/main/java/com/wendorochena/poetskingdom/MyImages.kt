package com.wendorochena.poetskingdom

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.wendorochena.poetskingdom.databinding.FragmentMyImagesBinding
import com.wendorochena.poetskingdom.recyclerViews.ImageRecyclerViewAdapter
import com.wendorochena.poetskingdom.recyclerViews.MyPoemsRecyclerViewAdapter
import com.wendorochena.poetskingdom.utils.UriUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


/**
 * A simple [Fragment] subclass.
 * create an instance of this fragment.
 */
class MyImages : Fragment() {

    private var _binding: FragmentMyImagesBinding? = null
    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null
    private var permissionsResultLauncher: ActivityResultLauncher<String>? = null
    private var localImageFolder: File? = File(context?.filesDir, "myImages")
    private var recyclerView: RecyclerView? = null
    private var myPoemsRecyclerView: RecyclerView? = null
    private var selectedImages = ArrayList<Pair<File, Int>>()
    private var isLongClicked = false

    private lateinit var recyclerViewAdapter: ImageRecyclerViewAdapter
    private lateinit var myPoemsRecyclerViewAdapter: MyPoemsRecyclerViewAdapter

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityResultLauncher = launchGalleryIntent()
        permissionsResultLauncher = permissionsActivityResult()
        localImageFolder = requireContext().getDir("myImages", Context.MODE_PRIVATE)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMyImagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = binding.RVImages
        myPoemsRecyclerView = binding.myPoemsRV
        recyclerViewAdapter =
            localImageFolder?.listFiles()?.let {
                ImageRecyclerViewAdapter(
                    this.javaClass.name,
                    it.toCollection(ArrayList()),
                    requireContext()
                )
            }!!
        setupLongClickImages()
        myPoemsRecyclerViewAdapter = MyPoemsRecyclerViewAdapter(requireContext())
        prepareRecyclerView()
        setupListeners()
        setupOnBackPressed()
        val sharedPreferences =
            requireContext().getSharedPreferences("my_shared_pref", Context.MODE_PRIVATE)
        if (sharedPreferences != null) {
            if (!sharedPreferences.getBoolean("myImagesFirstUse", false)) {
                onFirstUse()
                sharedPreferences.edit().putBoolean("myImagesFirstUse", true).apply()
            }
        }
    }

    private fun onFirstUse() {
        val alertDialogBuilder = context?.let { androidx.appcompat.app.AlertDialog.Builder(it) }
        alertDialogBuilder?.setTitle(R.string.guide_title)
            ?.setPositiveButton(R.string.builder_understood) { dialog, _ ->
                dialog.dismiss()
            }?.setMessage(R.string.guide_my_images)?.show()
    }


    private fun setupOnBackPressed() {
        val callBack = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isLongClicked) {
                    clearDeleteButton()
                } else {
                    Navigation.findNavController(requireView()).popBackStack(R.id.myImages, true)
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callBack)
    }

    /**
     *
     */
    private fun setupLongClickImages() {

        var counter = 0
        while (counter < recyclerViewAdapter.itemCount) {
            val imageView = ImageView(requireContext())
            imageView.tag = "circle"
            imageView.visibility = View.GONE
            recyclerViewAdapter.addElement(imageView, counter)
            counter++
        }
    }

    /**
     *
     */
    private fun initiateSavedImagesData() {
        val savedImagesFolder =
            requireContext().getDir(
                getString(R.string.saved_images_folder_name),
                Context.MODE_PRIVATE
            )
        try {
            val savedImageFiles = savedImagesFolder.listFiles()?.toMutableList()
            if (savedImageFiles != null) {
                savedImageFiles.sortByDescending { it.lastModified() }
                for (file in savedImageFiles) {
                    val frameLayout = createFrameLayout(file.name)
                    myPoemsRecyclerViewAdapter.addItem(frameLayout)
                }
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    /**
     *
     */
    private fun createFrameLayout(fileName: String): FrameLayout {
        val frameToRet = layoutInflater.inflate(R.layout.list_view_layout, null) as FrameLayout
        frameToRet.id = View.generateViewId()
        val shapeableImageView = frameToRet.getChildAt(1) as ShapeableImageView
        val textView = frameToRet.getChildAt(2) as TextView
        val thumbnailsFolder =
            context?.getDir(getString(R.string.thumbnails_folder_name), Context.MODE_PRIVATE)
        try {
            val thumbnailFile = File(
                thumbnailsFolder?.absolutePath + File.separator + fileName + ".png"
            )
            if (thumbnailFile.exists()) {
                shapeableImageView.setImageBitmap(BitmapFactory.decodeFile(thumbnailFile.absolutePath))
                shapeableImageView.tag = thumbnailFile.absolutePath
            } else {
                Log.e("No Such Thumbnail", fileName)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        textView.text = fileName.replace('_', ' ')

        return frameToRet
    }

    private fun setupMyPoemsRVOnClick() {
        myPoemsRecyclerViewAdapter.onItemClick = { frameLayout, i ->
            if (!isLongClicked) {
                val imageIntent = Intent(context, ImageViewer::class.java)
                val poemTitleTextView = frameLayout.getChildAt(2) as TextView
                imageIntent.putExtra("imageLoadType", "poem saved image")
                imageIntent.putExtra("poemName", poemTitleTextView.text.toString())
                startActivity(imageIntent)
            } else {
                val shapeableImageView = frameLayout.getChildAt(1) as ShapeableImageView
                val file = File(shapeableImageView.tag.toString())
                if (file.exists())
                    selectedImages.add(Pair(file, i))
                myPoemsRecyclerViewAdapter.updateLongImage(i, "check")
            }
        }

        myPoemsRecyclerViewAdapter.onItemLongClick = { frameLayout, i ->
            if (binding.addNewImageButton.isVisible)
                binding.addNewImageButton.visibility = View.GONE

            if (!binding.deleteImageButton.isVisible)
                binding.deleteImageButton.visibility = View.VISIBLE
//            if (!isLongClicked) {
//                myPoemsRecyclerViewAdapter.initiateOnLongClickImage(i)
//            }
            val shapeableImageView = frameLayout.getChildAt(1) as ShapeableImageView
            val file = File(shapeableImageView.tag.toString())
            if (file.exists())
                selectedImages.add(Pair(file, i))

            isLongClicked = true
        }
    }

    /**
     * This function sets two on click instances for the recycler view
     * The first onClick event is when the PersonalisationActivity is looking for an image path result
     * When a confirmation happens we return the path and control resumes in Personalisation Activity
     *
     * The second onClick is used to display an image when it is clicked TODO
     */
    private fun prepareRecyclerView() {

        recyclerView?.layoutManager = GridLayoutManager(context, 3)
        recyclerView?.adapter = recyclerViewAdapter

        recyclerViewAdapter.onItemClick = { file, index ->
            if (!isLongClicked) {
                val imageIntent = Intent(context, ImageViewer::class.java)
                imageIntent.putExtra("imageLoadType", "image")
                imageIntent.putExtra("imagePath", file.absolutePath)
                startActivity(imageIntent)
            } else {
                selectedImages.add(Pair(file, index))
                recyclerViewAdapter.updateLongImage(index, "check")
            }
        }

        recyclerViewAdapter.onItemLongClick = { file, index ->
            if (binding.addNewImageButton.isVisible)
                binding.addNewImageButton.visibility = View.GONE

            if (!binding.deleteImageButton.isVisible)
                binding.deleteImageButton.visibility = View.VISIBLE

            recyclerViewAdapter.updateLongImage(index, "check")
            if (!isLongClicked) {
                recyclerViewAdapter.initiateOnLongClickImage(index)
            }
            if (selectedImages.isNotEmpty()) {
                val lastElem = selectedImages[selectedImages.size - 1]
                if (lastElem.second > index) {
                    var counter = selectedImages.size - 1

                    while (counter >= 0) {
                        val lastElement = selectedImages[counter]
                        if (lastElement.second < index) {
                            selectedImages.add(counter + 1, Pair(file, index))
                            break
                        }
                        counter--
                    }
                    if (counter == -1)
                        selectedImages.add(0, Pair(file, index))
                }
            } else
                selectedImages.add(Pair(file, index))
            isLongClicked = true
        }

        myPoemsRecyclerView?.layoutManager = GridLayoutManager(context, 2)
        myPoemsRecyclerView?.adapter = myPoemsRecyclerViewAdapter

        initiateSavedImagesData()
        setupMyPoemsRVOnClick()
    }

    /**
     * This function sets up an onclick listener for the button to add images
     * Permission is always checked and if allowed any chosen image will be added to our local folder
     */
    private fun setupListeners() {

        binding.addNewImageButton.setOnClickListener {
            if (Build.VERSION.SDK_INT < 33 && requireContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT >= 33 && requireContext().checkSelfPermission(
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val galleryIntent = Intent()
                galleryIntent.type = "image/*"
                galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                galleryIntent.action = Intent.ACTION_GET_CONTENT
                activityResultLauncher?.launch(galleryIntent)
            } else {
                activity?.let {
                    AlertDialog.Builder(it)
                }?.setTitle(R.string.permission_request)
                    ?.setMessage(R.string.permission_request_message)
                    ?.setPositiveButton(R.string.permission_agreement) { _, _ ->
                        if (Build.VERSION.SDK_INT >= 33)
                            permissionsResultLauncher?.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        else
                            permissionsResultLauncher?.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }?.setNegativeButton(R.string.permission_denial, null)?.show()
            }
        }

        binding.myImagesText.setOnClickListener {
            val tag = it.tag as String
            if (tag == "inactive" && !isLongClicked) {
                myPoemsRecyclerView?.visibility = View.GONE
                recyclerView?.visibility = View.VISIBLE
                val selectedDrawable =
                    ResourcesCompat.getDrawable(resources, R.drawable.selected, null)
                val textView = it as TextView
                selectedDrawable?.setBounds(0, 0, 60, 60)
                textView.setCompoundDrawables(selectedDrawable, null, null, null)
                textView.tag = "active"
                val myPoemsTextView = binding.myPoemsText
                myPoemsTextView.tag = "inactive"
                myPoemsTextView.setCompoundDrawables(null, null, null, null)
            }
        }

        binding.myPoemsText.setOnClickListener {
            val tag = it.tag as String
            if (tag == "inactive" && !isLongClicked) {
                recyclerView?.visibility = View.GONE
                myPoemsRecyclerView?.visibility = View.VISIBLE
                val selectedDrawable =
                    ResourcesCompat.getDrawable(resources, R.drawable.selected, null)
                val textView = it as TextView
                selectedDrawable?.setBounds(0, 0, 60, 60)
                textView.setCompoundDrawables(selectedDrawable, null, null, null)
                textView.tag = "active"
                val myPoemsTextView = binding.myImagesText
                myPoemsTextView.tag = "inactive"
                myPoemsTextView.setCompoundDrawables(null, null, null, null)

            }
        }
        setupDeleteImages()
    }

    /**
     *
     */
    private fun clearDeleteButton() {
        if (recyclerView?.isVisible == true)
            recyclerViewAdapter.turnOffLongClick()
        else
            myPoemsRecyclerViewAdapter.turnOffLongClick()
        selectedImages.clear()
        binding.addNewImageButton.visibility = View.VISIBLE
        binding.deleteImageButton.visibility = View.GONE
        isLongClicked = false
    }

    /**
     * Deletes selected images
     */
    private fun setupDeleteImages() {
        binding.deleteImageButton.setOnClickListener {
            if (selectedImages.isNotEmpty()) {
                try {
                    for (pair in selectedImages) {
                        if (pair.first.delete()) {
                            if (recyclerView?.isVisible == true) {
                                recyclerViewAdapter.deleteElem(pair.second)
                                recyclerViewAdapter.notifyItemRemoved(pair.second)
                            } else {
                                try {
                                    val file = pair.first
                                    val splitString = file.name.split(File.separator)
                                    val poemName = splitString[splitString.size - 1]
                                    val savedImagesFilePath = requireContext().getDir(
                                        getString(R.string.saved_images_folder_name),
                                        Context.MODE_PRIVATE
                                    )
                                    val fullPathToDelete = File(
                                        savedImagesFilePath.absolutePath + File.separator + poemName.replace(
                                            ".png",
                                            ""
                                        )
                                    )
                                    if (fullPathToDelete.exists())
                                        fullPathToDelete.deleteRecursively()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                myPoemsRecyclerViewAdapter.deleteElem(pair.second)
                                myPoemsRecyclerViewAdapter.notifyItemRemoved(pair.second)
                            }
                        } else {
                            Log.e("Failed to remove file: ", pair.first.name)
                        }
                    }
                    clearDeleteButton()
                } catch (e: Exception) {
                    e.printStackTrace()
                    clearDeleteButton()
                }
            }
        }
    }

    /**
     *
     */
    private fun permissionsActivityResult(): ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                val galleryIntent = Intent()
                galleryIntent.type = "image/*"
                galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                galleryIntent.action = Intent.ACTION_GET_CONTENT
                activityResultLauncher?.launch(galleryIntent)
            }
        }

    /**
     *
     */
    private fun launchGalleryIntent(): ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data?.clipData != null) {
                //handle multiple selected
                var counter = 0

                while (counter < data.clipData!!.itemCount) {
                    val uriUtils =
                        context?.let { UriUtils(it, data.clipData!!.getItemAt(counter).uri) }
                    if (uriUtils != null) {
                        copyToLocalFolder(uriUtils.getRealPathFromURI())
                    }
                    counter++
                }
            }
            // do your operation from here....
            else if (data != null) {
                if (data.data != null) {
                    val uriUtils = context?.let { UriUtils(it, data.data!!) }
                    if (uriUtils != null) {
                        copyToLocalFolder(uriUtils.getRealPathFromURI())
                    }

                }
            }
        }
    }

    /**
     * @param imagePath the path to the image that we want to copy
     *
     * This function copies any selected image or images to our own local folder
     */
    private fun copyToLocalFolder(imagePath: String?) {
        try {
            var newFileName = ""
            if (imagePath != null) {
                val splitString = imagePath.split("/")
                newFileName = splitString[splitString.size - 1]
            }
            if (newFileName != "") {
                val imageToAdd = File(localImageFolder?.path + File.separator + newFileName)

                if (!imageToAdd.exists()) {
                    if (imageToAdd.createNewFile()) {
                        val imageFile = imagePath?.let { File(it) }
                        val inputStream = FileInputStream(imagePath)
                        val outputStream = FileOutputStream(imageToAdd)

                        if (imageFile != null) {
                            outputStream.channel.transferFrom(
                                inputStream.channel,
                                0,
                                imageFile.totalSpace
                            )
                        }
                        outputStream.close()
                        inputStream.close()
                        val updateNum = recyclerViewAdapter.itemCount
                        val imageView = ImageView(requireContext())
                        imageView.tag = "circle"
                        imageView.visibility = View.GONE
                        recyclerViewAdapter.addElement(
                            imageView,
                            recyclerViewAdapter.getLongClickImages().size
                        )
                        recyclerViewAdapter.addElement(imageToAdd)
                        recyclerViewAdapter.notifyItemInserted(updateNum)
                    }
                } else {
                    val builder: AlertDialog.Builder? = activity?.let {
                        AlertDialog.Builder(it)
                    }
                    builder?.setMessage(R.string.builder_message)?.setTitle(R.string.builder_title)
                    builder?.setNegativeButton(
                        R.string.builder_understood
                    ) { _, _ ->
                    }

                    builder?.create()?.show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}