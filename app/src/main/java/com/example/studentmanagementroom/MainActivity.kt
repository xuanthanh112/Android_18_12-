package com.example.studentmanagementroom

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StudentAdapter
    private var studentList = mutableListOf<Student>()
    private lateinit var appDatabase: AppDatabase
    private lateinit var searchEditText: EditText

    private val activityScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        appDatabase = AppDatabase.getInstance(this)

        adapter = StudentAdapter(this, studentList) { student ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("studentId", student.id)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Thêm dữ liệu mẫu nếu cần
        activityScope.launch {
            withContext(Dispatchers.IO) {
                    addSampleData()
            }
            loadStudents() // Load dữ liệu sinh viên sau khi thêm dữ liệu mẫu
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                searchStudents(charSequence.toString())
            }

            override fun afterTextChanged(editable: Editable?) {}
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_student -> {
                val intent = Intent(this, AddStudentActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.action_delete -> {
                deleteSelectedStudents() // Gọi hàm xóa nhiều sinh viên
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    // Hàm xóa các sinh viên đã chọn
    private fun deleteSelectedStudents() {
        CoroutineScope(Dispatchers.Main).launch {
            val selectedStudents = adapter.getSelectedStudents()
            if (selectedStudents.isNotEmpty()) {
                withContext(Dispatchers.IO) {
                    appDatabase.studentDao().deleteStudents(selectedStudents) // Xóa trong cơ sở dữ liệu
                }
                loadStudents() // Cập nhật lại danh sách
            } else {
                Toast.makeText(this@MainActivity, "Không có sinh viên nào được chọn!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadStudents() // Làm mới danh sách sinh viên mỗi khi quay lại màn hình chính
    }

    private fun loadStudents() {
        activityScope.launch {
            val students = withContext(Dispatchers.IO) {
                appDatabase.studentDao().getAllStudents()
            }
            studentList.clear()
            studentList.addAll(students)
            adapter.notifyDataSetChanged()
        }
    }

    private fun searchStudents(keyword: String) {
        activityScope.launch {
            val students = withContext(Dispatchers.IO) {
                appDatabase.studentDao().searchStudents("%$keyword%")
            }
            studentList.clear()
            studentList.addAll(students)
            adapter.notifyDataSetChanged()
        }
    }

    private fun addSampleData() {
        val sampleStudents = listOf(
            Student(mssv = "SV011", hoten = "Nguyen Thi Lan", ngaysinh = "2001-03-14", email = "lan.nguyen@example.com"),
            Student(mssv = "SV012", hoten = "Tran Van Minh", ngaysinh = "2002-07-22", email = "minh.tran@example.com"),
            Student(mssv = "SV013", hoten = "Le Thi Huong", ngaysinh = "2003-05-18", email = "huong.le@example.com"),
            Student(mssv = "SV014", hoten = "Pham Van Tai", ngaysinh = "2004-11-09", email = "tai.pham@example.com"),
            Student(mssv = "SV015", hoten = "Hoang Thi Phuong", ngaysinh = "2005-02-25", email = "phuong.hoang@example.com"),
            Student(mssv = "SV016", hoten = "Nguyen Van Hoa", ngaysinh = "2006-08-30", email = "hoa.nguyen@example.com"),
            Student(mssv = "SV017", hoten = "Tran Thi Ngoc", ngaysinh = "2001-12-15", email = "ngoc.tran@example.com"),
            Student(mssv = "SV018", hoten = "Le Van Thanh", ngaysinh = "2002-06-05", email = "thanh.le@example.com"),
            Student(mssv = "SV019", hoten = "Pham Thi Mai", ngaysinh = "2003-10-12", email = "mai.pham@example.com"),
            Student(mssv = "SV020", hoten = "Hoang Van Khoa", ngaysinh = "2004-03-08", email = "khoa.hoang@example.com")
            // Thêm các sinh viên khác...
        )
        appDatabase.studentDao().insertAll(sampleStudents)
    }


    override fun onDestroy() {
        super.onDestroy()
        activityScope.cancel() // Hủy phạm vi hoạt động khi Activity bị hủy
    }
}
