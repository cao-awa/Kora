## No content
Add ``noContent`` method to return NoContentResponse instance.

## Response headers
Add response headers.

## Redirect
Add 'movedPermanently', 'permanentlyRedirect', 'temporaryRedirect' method to redirect.

## Request body
Add request body supports for JSON, text, empty, urlencoded form.

## Placeholder route
Add placeholder routes.

You can use placeholder to create routes, this will save you time from writing ``{xxx}`` several times:

```kotlin
object TestEntry {
    @JvmStatic
    fun entry() {
        val usernameHolder = placeholder<String>("username")
        val functionHolder = placeholder<Int>("function")
        val username by usernameHolder
        val function by functionHolder

        val api = http {
            route("/wiki", usernameHolder, functionHolder) {
                get {
                    html {
                        body {
                            p {
                                +"User'{$username}' currently accessing server function '$function'"
                            }
                        }
                    }
                }
            }
        }

        KoraHttpServer(api).start()
    }
}
```

The order of the string and placeholder is arbitrary, you can swap orders it as you like.

Unfortunately, you can't use delegate way to build routes, only raw placeholder can do this, so for future conveniences, you maybe need define a ``val xxx by xxxHolder``, and use ``xxx`` in the next stages, abandon the ``xxxHolder``.

Or maybe you want to use ``xxx(this)`` to get the value, it also ok, the choice is yours.
