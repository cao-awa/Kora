## Allocator

Config ``allocator`` supported ``default``(PooledByteBufAllocator), ``pooled``(same to ``default``), ``unpolled``(
UnpooledByteBufAllocator)

## IO

Config could configure IO modules, supported ``EPOLL``, ``KQUEUE``, ``NIO``, ``LOCAL``, and config system will auto downgrade IO module when that IO doesn't working on your environment, such as, you configured ``EPOLL`` to the IO module, but you are running on Windows system, then Kora will automatically downgrade to ``NIO`` module to run your server.

## Multi entrypoint

You can define multi entrypoint used to load plugins, custom configurations, or connections pool, ETC.

Then you will run the real server startup code in the last entrypoint, you can use previous data and tools to make your server got more enhance features.

## Config
Changed config system, modified config path to '{working_dir}\configs/'.

## Redis
Redis client can be a plugin to load into Kora now, please clone the repo and run ``/src/test/kotlin/Main.kt`` to test this feature.