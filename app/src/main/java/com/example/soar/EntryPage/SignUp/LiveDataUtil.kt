// LiveDataUtil.kt
package com.example.soar.EntryPage.SignUp

import androidx.lifecycle.*

/** 여러 LiveData(List) 결합 — 기존 */
fun <T, R> List<LiveData<T>>.combineLatest(
    block: (List<T>) -> R
): LiveData<R> = MediatorLiveData<R>().apply {
    val buf = MutableList<T?>(size) { null }
    forEachIndexed { i, src ->
        addSource(src) { v ->
            buf[i] = v
            if (buf.none { it == null }) value = block(buf.filterNotNull())
        }
    }
}

/** ★ 2-개 LiveData 전용 오버로드 추가 */
fun <A, B, R> LiveData<A>.combineLatest(
    other: LiveData<B>,
    block: (A, B) -> R
): LiveData<R> = MediatorLiveData<R>().apply {
    var latestA: A? = null
    var latestB: B? = null
    fun dispatch() {
        val a = latestA; val b = latestB
        if (a != null && b != null) value = block(a, b)
    }
    addSource(this@combineLatest) { latestA = it; dispatch() }
    addSource(other)              { latestB = it; dispatch() }
}