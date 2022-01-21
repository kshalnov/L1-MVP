package ru.gb.course1.l1_mvp

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

// это должно лежать где-то в domain/entity пакете
data class EmployeeEntity(
    val name: String,
    val salary: Int
)

/**
 *      x`   1) Возможная утечка view (activity, fragment, context)
 *      x    2) Восстановление состояния view при detach/attach
 *      3) Одноразовые события
 */

data class OneTimeError(
    private val error: Throwable
) {
    private var wasUsed = false
    fun getValue(): Throwable? = if (wasUsed) {
        null
    } else {
        wasUsed = true
        error
    }
}

interface EmployeesListContract {

    interface ViewModel {
        val progress: LiveData<Boolean>
        val employeesList: LiveData<List<EmployeeEntity>?>
        val error: LiveData<Throwable>

        fun onLoadList()
    }
}

class ViewModel : EmployeesListContract.ViewModel {
    private fun <T> LiveData<T>.mutable(): MutableLiveData<T> {
        return this as MutableLiveData<T>
    }

    override val progress: LiveData<Boolean> = MutableLiveData<Boolean>()

    private val _employeesList = MutableLiveData<List<EmployeeEntity>?>()
    override val employeesList = _employeesList as LiveData<List<EmployeeEntity>?>

    private val _error = MutableLiveData<Throwable>()
    override val error = _error as LiveData<Throwable>

    override fun onLoadList() {
        progress.mutable().postValue(true)

        api.loadUsers( // 30 sec
            onError = { error: Throwable ->
                _progress.postValue(false)
                _error.postValue(error)
            },
            onSuccess = { data: List<EmployeeEntity> ->
                _progress.postValue(false)
                _employeesList.postValue(data)
            }
        )
    }

}


class EmployeesView {
    private lateinit var context: Context
    private lateinit var viewModel: EmployeesListContract.ViewModel

    fun onCreate() {
        // ...

        viewModel.progress.observe(this) {
            binding.progressBar.isVisible = it
        }

        viewModel.error.observe(this) {
            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
        }

    }

}