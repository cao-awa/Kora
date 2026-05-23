# Plugin standard
Plugin can add a file to describe itself to ``META-INF/plugin.json``, like: 
```json
{
    "name": "kora-external",
    "entrypoint": "com.github.cao.awa.kora.external.SampleEntrypoint#entry",
    "depends_on": [
        "kora-redis"
    ]
}
```

The ``name`` is the name of this plugin, cannot duplicate to other plugin.

The ``entrypoint`` is the entrypoint method

The ``depends_on`` is dependencies of this plugin, Kora cannot be startup if required plugin not loaded or got wrong orders. 