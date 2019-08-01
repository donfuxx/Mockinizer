package com.appham.mockinizer

import androidx.lifecycle.ViewModel
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class MainViewModel : ViewModel() {

    private val demoRepository: DemoRepository = DemoRepository()

    fun runDemo() {

        demoRepository.getMocked()
            .subscribeOn(Schedulers.io())
            .subscribe()

        demoRepository.getPosts()
            .subscribeOn(Schedulers.io())
            .subscribe()

    }
}