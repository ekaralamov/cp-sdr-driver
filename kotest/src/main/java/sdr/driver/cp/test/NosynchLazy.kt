package sdr.driver.cp.test

fun <T> nosynchLazy(initializer: () -> T) = lazy(LazyThreadSafetyMode.NONE, initializer)
