package com.example.android.mlkitimagerecognition

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        open_camera.setOnClickListener {
            if(checkPhotoPermission()){
                startActivity(Intent(this,CameraActivity::class.java))
            }
            else{
                requestPermissions(arrayOf(android.Manifest.permission.CAMERA), PermissionRequestCode.PERMISSION_REQUEST_CODE_CAMERA)
            }
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == PermissionRequestCode.PERMISSION_REQUEST_CODE_CAMERA){
            if(!checkPhotoPermission()){
                Toast.makeText(this,"Выдайте, пожалуйста, разрешение",Toast.LENGTH_SHORT).show()
            }
            else {
                startActivity(Intent(this,CameraActivity::class.java))
            }
        }
    }

    private fun checkPhotoPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }


}