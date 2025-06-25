package com.wendorochena.poetskingdom.utils.files.images

import android.content.Context
import com.wendorochena.poetskingdom.R
import java.io.File

class ImagesFolderOperations(private val imageSortType: ImageSortType) {

    private fun retrieveImagesFolder(context: Context): File {
        return context.getDir(
            context.getString(R.string.my_images_folder_name),
            Context.MODE_PRIVATE
        )
    }

    private fun retrievePoemThumbnailImagesFolder(context: Context): File {
        return context.getDir(
            context.getString(R.string.thumbnails_folder_name),
            Context.MODE_PRIVATE
        )
    }

    /**
     * @return a list of sorted files as per [imageSortType]
     */
    private fun sortedFiles(imageFolder: File): List<File> {
        return when (imageSortType) {
            ImageSortType.ASCENDING -> {
                imageFolder.listFiles()?.sortedBy { it.lastModified() } ?: emptyList()
            }

            ImageSortType.DESCENDING -> {
                imageFolder.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
            }

            ImageSortType.NONE -> {
                imageFolder.listFiles()?.toList() ?: emptyList()
            }
        }
    }

    /**
     *
     * @return a list of sorted image files as per [imageSortType]
     */
    fun retrieveImagesFolderFiles(context: Context): List<File> {
        return sortedFiles(retrieveImagesFolder(context))
    }

    /**
     *
     * @return a list of sorted poem thumbnail files as per [imageSortType]
     */
    fun retrievePoemThumbnailFolderFiles(context: Context): List<File> {
        return sortedFiles(retrievePoemThumbnailImagesFolder(context))
    }

    /**
     * In order to make the time complexity O(n) instead of O(n^2) for deleting a file, we populate
     * a hashMap containing the file name without extension as key and the corresponding value being
     * its file
     */
    private fun filesAsMap(context: Context): Map<String, File> {
        val filesAsMap = hashMapOf<String, File>()
        val imagesFolder = retrieveImagesFolder(context)
        imagesFolder.listFiles()?.forEach {
            filesAsMap[it.nameWithoutExtension] = it
        }
        return filesAsMap
    }

    /**
     * Find the file to delete given a file name without an extension
     */
    fun findImageFilePathFromFileName(context: Context, fileNameWithoutExtension: String): File? {
        val imageFiles = filesAsMap(context)

        return imageFiles[fileNameWithoutExtension]
    }
}