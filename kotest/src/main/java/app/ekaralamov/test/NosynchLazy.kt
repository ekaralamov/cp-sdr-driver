package app.ekaralamov.test

fun <T> nosynchLazy(initializer: () -> T) = lazy(LazyThreadSafetyMode.NONE, initializer)
