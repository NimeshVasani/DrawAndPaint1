package com.example.drawandpaint

import android.content.ContentValues
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.EditText
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.drawandpaint.widget.DrawView
import kotlinx.android.synthetic.main.activity_drawing.*
import kotlinx.android.synthetic.main.color_palette_view.*
import top.defaults.colorpicker.ColorPickerPopup
import java.io.File
import java.io.File.separator
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

class DrawingActivity : AppCompatActivity() {

    private val sizeEditor = arrayOf<Int>(56)
    private val hideBars = arrayOf<Int>(56)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawing)

        colorSelector()

        val paint = findViewById<DrawView>(R.id.draw_view)

        image_draw_undo.setOnClickListener {
            paint.undo()
        }
        image_draw_redo.setOnClickListener {
            paint.redo()
        }
        fab_send_drawing.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dialog_save, null)
            alertDialog.setView(dialogView)
            val fileNameEditText: EditText = dialogView.findViewById(R.id.editText_file_name)
            val filename = UUID.randomUUID().toString()
            fileNameEditText.setSelectAllOnFocus(true)
            fileNameEditText.setText(filename)
            alertDialog.setTitle("Save Drawing")
                .setPositiveButton("ok") { _, _ ->
                    saveImage(
                        paint.save()!!,
                        this,
                        fileNameEditText.text.toString()
                    )
                }
                .setNegativeButton("Cancel") { _, _ -> }
            val dialog = alertDialog.create()
            dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            dialog.show()
        }
        image_close_drawing.setOnClickListener {
            finish()
        }
        hide_bars.setOnClickListener {
            when (toolbar.translationY) {
                -(56).toPx -> {
                    toolbar.animate().translationY((0).toPx)
                    hide_bars.animate().translationY(-(28).toPx)
                    draw_tools.animate().translationY((56).toPx)
                }
                (0).toPx -> {
                    toolbar.animate().translationY(-(56).toPx)
                    hide_bars.animate().translationY(-(10).toPx)
                    draw_tools.animate().translationY(((sizeEditor[0]) * 2).toPx)
                }
                -(80).toPx -> {
                    toolbar.animate().translationY((0).toPx)
                    hide_bars.animate().translationY(-(20).toPx)
                    draw_tools.animate().translationY((80).toPx)
                }
            }
        }
        image_draw_color.setOnClickListener {
            if (draw_tools.translationY == (56).toPx) {
                sizeEditor[0] = 56
                toggleDrawTools(draw_tools, true, 0)
            }
            if (draw_tools.translationY == (0).toPx && draw_color_palette.visibility == View.VISIBLE) {
                toggleDrawTools(draw_tools, false, sizeEditor[0])
            }
            if (draw_tools.translationY == (80).toPx) {
                sizeEditor[0] = 80
                toggleDrawTools(draw_tools, true, 0)
            }
            if (draw_view.isEraserOn[0])
                draw_view.toggleEraser(true)

            circle_view_width.visibility = View.GONE
            circle_view_opacity.visibility = View.GONE
            seekBar_width.visibility = View.GONE
            seekBar_opacity.visibility = View.GONE
            draw_color_palette.visibility = View.VISIBLE

        }

        circle_view_opacity.setCircleRadius(100f)

        image_draw_eraser.setOnClickListener {
            draw_view.setAlpha(100)
            draw_view.toggleEraser(true)
            if (draw_tools.translationY == (0).toPx && seekBar_width.visibility == View.GONE) {
                toggleDrawTools(draw_tools, false, sizeEditor[0])
            }
            scaleColorView(image_color_green)
        }
        image_draw_eraser.setOnLongClickListener {
            draw_view.clearCanvas()
            if (draw_tools.translationY == (0).toPx && draw_color_palette.visibility == View.VISIBLE) {
                toggleDrawTools(draw_tools, false, sizeEditor[0])
            }
            true
        }
        image_draw_opacity.setOnClickListener {
            if (draw_tools.translationY == (56).toPx) {
                sizeEditor[0] = 56
                toggleDrawTools(draw_tools, true, 0)
            }
            if (draw_tools.translationY == (0).toPx && seekBar_opacity.visibility == View.VISIBLE) {
                toggleDrawTools(draw_tools, false, sizeEditor[0])
            }
            if (draw_tools.translationY == (80).toPx) {
                sizeEditor[0] = 80
                toggleDrawTools(draw_tools, true, 0)
            }
            if (draw_view.isEraserOn[0])
                draw_view.toggleEraser(true)
            circle_view_width.visibility = View.GONE
            circle_view_opacity.visibility = View.VISIBLE
            seekBar_width.visibility = View.GONE
            seekBar_opacity.visibility = View.VISIBLE
            draw_color_palette.visibility = View.GONE
        }
        image_draw_width.setOnClickListener {
            if (draw_tools.translationY == (56).toPx) {
                sizeEditor[0] = 56
                toggleDrawTools(draw_tools, true, 0)
            }
            if (draw_tools.translationY == (0).toPx && seekBar_width.visibility == View.VISIBLE) {
                toggleDrawTools(draw_tools, false, sizeEditor[0])
            }
            if (draw_tools.translationY == (80).toPx) {
                sizeEditor[0] = 80
                toggleDrawTools(draw_tools, true, 0)
            }
            circle_view_width.visibility = View.VISIBLE
            circle_view_opacity.visibility = View.GONE
            seekBar_width.visibility = View.VISIBLE
            seekBar_opacity.visibility = View.GONE
            draw_color_palette.visibility = View.GONE
        }
        seekBar_width.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                draw_view.setStrokeWidth(progress)
                circle_view_width.setCircleRadius(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBar_opacity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                draw_view.setAlpha(progress)
                circle_view_opacity.setAlpha(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        val vto: ViewTreeObserver = paint.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                paint.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val width: Int = paint.measuredWidth
                val height: Int = paint.measuredHeight
                paint.init(height, width)
            }
        })
    }

    private fun toggleDrawTools(view: View, showView: Boolean = true, size: Int) {
        view.animate().translationY(size.toPx)
    }

    private val Int.toPx: Float
        get() = (this * Resources.getSystem().displayMetrics.density)

    private fun colorSelector() {
        image_color_black.setOnClickListener {
            val color = ResourcesCompat.getColor(resources, R.color.color_black, null)
            draw_view.setColor(color)
            circle_view_opacity.setColor(color)
            circle_view_width.setColor(color)
            scaleColorView(image_color_black)
        }
        image_color_red.setOnClickListener {
            val color = ResourcesCompat.getColor(resources, R.color.color_red, null)
            draw_view.setColor(color)
            circle_view_opacity.setColor(color)
            circle_view_width.setColor(color)
            scaleColorView(image_color_red)
        }
        image_color_yellow.setOnClickListener {
            val color = ResourcesCompat.getColor(resources, R.color.color_yellow, null)
            draw_view.setColor(color)
            circle_view_opacity.setColor(color)
            circle_view_width.setColor(color)
            scaleColorView(image_color_yellow)
        }
        image_color_green.setOnClickListener {
            val color = ResourcesCompat.getColor(resources, R.color.color_green, null)
            draw_view.setColor(color)
            circle_view_opacity.setColor(color)
            circle_view_width.setColor(color)
            scaleColorView(image_color_green)
        }
        image_color_blue.setOnClickListener {
            val color = ResourcesCompat.getColor(resources, R.color.color_blue, null)
            draw_view.setColor(color)
            circle_view_opacity.setColor(color)
            circle_view_width.setColor(color)
            scaleColorView(image_color_blue)
        }
        image_color_pink.setOnClickListener {
            val color = ResourcesCompat.getColor(resources, R.color.color_pink, null)
            draw_view.setColor(color)
            circle_view_opacity.setColor(color)
            circle_view_width.setColor(color)
            scaleColorView(image_color_pink)
        }
        image_color_picker.setOnClickListener {
            ColorPickerPopup.Builder(this)
                .initialColor(Color.RED) // Set initial color
                .enableBrightness(true) // Enable brightness slider or not
                .enableAlpha(true) // Enable alpha slider or not
                .okTitle("Choose")
                .cancelTitle("Cancel")
                .showIndicator(true)
                .showValue(true)
                .build()
                .show(draw_view, object : ColorPickerPopup.ColorPickerObserver() {
                    override fun onColorPicked(color: Int) {
                        draw_view.setColor(color)
                        circle_view_opacity.setColor(color)
                        circle_view_width.setColor(color)
                    }
                })
            scaleColorView(image_color_picker)
        }
    }

    private fun scaleColorView(view: View) {
        //reset scale of all views

        image_color_black.scaleX = 1f
        image_color_black.scaleY = 1f

        image_color_red.scaleX = 1f
        image_color_red.scaleY = 1f

        image_color_yellow.scaleX = 1f
        image_color_yellow.scaleY = 1f

        image_color_green.scaleX = 1f
        image_color_green.scaleY = 1f

        image_color_blue.scaleX = 1f
        image_color_blue.scaleY = 1f

        image_color_pink.scaleX = 1f
        image_color_pink.scaleY = 1f

        image_color_picker.scaleX = 1f
        image_color_picker.scaleY = 1f

        //set scale of selected view
        view.scaleX = 1.5f
        view.scaleY = 1.5f
    }

    private fun saveImage(bitmap: Bitmap, context: Context, fileName: String) {
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            val values = contentValues()
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/DrawAndPaint")
            values.put(MediaStore.Images.Media.IS_PENDING, true)

            val uri: Uri? =
                context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )
            if (uri != null) {
                saveImageToStream(bitmap, context.contentResolver.openOutputStream(uri))
                values.put(MediaStore.Images.Media.IS_PENDING, false)
                context.contentResolver.update(uri, values, null, null)
            }
        } else {
            val directory =
                File(
                    Environment.getExternalStorageDirectory()
                        .toString() + separator + "DrawAndPaint"
                )

            if (!directory.exists()) {
                directory.mkdirs()
            }
            val fileName = "$fileName.png"
            val file = File(directory, fileName)
            saveImageToStream(bitmap, FileOutputStream(file))
            if (file.absolutePath != null) {
                val values = contentValues()
                values.put(MediaStore.Images.Media.DATA, file.absolutePath)
                context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )
            }
        }
    }

    private fun contentValues(): ContentValues {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        return values
    }

    private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}
