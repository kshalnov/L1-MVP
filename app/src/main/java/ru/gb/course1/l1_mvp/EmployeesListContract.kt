package ru.gb.course1.l1_mvp

import android.content.Context
import android.widget.Toast

// это должно лежать где-то в domain/entity пакете
data class EmployeeEntity(
    val name: String,
    val salary: Int
)

/**
 *      1) Возможная утечка view (activity, fragment, context)
 *      2) Восстановление состояния view при detach/attach
 *      3) Одноразовые события
 */
interface EmployeesListContract {
    interface View {
        fun showProgress(progressEnabled: Boolean)
        fun setEmployees(employees: List<EmployeeEntity>)
        fun showError(throwable: Throwable)
    }

    interface Presenter {
        fun onLoadList()

        fun attach(view: View)
        fun detach()
    }
}

class Presenter : EmployeesListContract.Presenter {
    private var view: EmployeesListContract.View? = null

    private var data: List<EmployeeEntity>? = null
        set(value) {
            field = value
            value?.let { view?.setEmployees(it) }
        }

    private var error: Throwable? = null
        set(value) {
            value?.let {
                if (view != null) {
                    view?.showError(it)
                    field = null
                } else {
                    field = value
                }
            }
        }

    private var isLoading: Boolean = false
        set(value) {
            field = value
            view?.showProgress(value)
        }

    override fun onLoadList() {
        isLoading = true
        api.loadUsers( // 30 sec
            onError = { error: Throwable ->
                isLoading = false
                this.error = error
            },
            onSuccess = { data: List<EmployeeEntity> ->
                isLoading = false
                this.data = data
            }
        )
    }

    override fun attach(view: EmployeesListContract.View) {
        this.view = view

        view.showProgress(isLoading)
        data?.let { view.setEmployees(it) }
        error?.let { view.showError(it) }
    }

    override fun detach() {
        view = null
    }

}


class EmployeesView : EmployeesListContract.View {
    private lateinit var context: Context
    private lateinit var presenter: EmployeesListContract.Presenter

    fun onCreate() {
        // ...
        presenter.attach(this)
    }

    override fun showProgress(progressEnabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setEmployees(employees: List<EmployeeEntity>) {
        TODO("Not yet implemented")
    }

    override fun showError(throwable: Throwable) {
        Toast.makeText(context, throwable.message, Toast.LENGTH_SHORT).show()
    }

}