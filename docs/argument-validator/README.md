# Argument validator
Argument validator used in URL argument input parsing and payload parameter parsing.

The validator ensure the data type is match your want to get, if not, ``error(argname, content, dataName)`` method will throw an exception that will be catching by Kora pipeline and convert to a error report to show on browser. 

## Register
Kora only supported useful validators like basic types and BigDecimal or ETC. custom validator need registers manually, a lots way to do this:

### Validator impl
```kotlin
class TypedHttpArgumentXxxValidator : TypedHttpArgumentValidator<Xxx> {
    override operator fun get(argumentName:String, content: String): Xxx {
        try {
            return Xxx()
        } catch (_: Exception) {
            error(argumentName, content, "Byte")
        }
    }
}

// Then register it.
TypedHttpArgument.addValidator(Xxx::class, TypedHttpArgumentXxxValidator())
```

### Lambda register
Use lambda receive two args and return your result.
```kotlin
TypedHttpArgument.addValidator(Xxx::class) { name, content ->
    try {
        Xxx()
    } catch (_: Exception) {
        error(name, content, "Xxx")
    }
}
```

### JSON validator
You can use json object to construct your result.
```kotlin
class TypedHttpArgumentXxxValidator : TypedHttpArgumentJSONObjectValidator<Xxx> {
    override operator fun get(argumentName:String, content: JSONObject): Xxx {
        try {
            return Xxx()
        } catch (_: Exception) {
            error(argumentName, content, "Xxx")
        }
    }
}

// Then register it.
TypedHttpArgument.addValidator(Xxx::class, TypedHttpArgumentXxxValidator())
```

or use json array to construct your result.
```kotlin
class TypedHttpArgumentXxxValidator : TypedHttpArgumentJSONArrayValidator<Xxx> {
    override operator fun get(argumentName:String, content: JSONArray): Xxx {
        try {
            return Xxx()
        } catch (_: Exception) {
            error(argumentName, content, "Xxx")
        }
    }
}

// Then register it.
TypedHttpArgument.addValidator(Xxx::class, TypedHttpArgumentXxxValidator())
```