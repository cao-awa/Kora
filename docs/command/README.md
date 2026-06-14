# Command
## help / ?
Show all commands and usage.

## exit / stop
Stop all services and exit Kora server.

## reload
Stop all services and reload Kora server. 

## memory / mem
Show memory stats, includes: heap, non-heap, direct, metadata

## threads
Show all threads and status information.

## thread
Show thread information of target thread id, need an integer input <thread_id>.

## deadlock
Show deadlock information.

## uptime
Show the formatted time that Kora running time.

## pid
Show the Kora process PID.

## jvmargs
Show all jvm arguments.

## env
Show all environment variables.

## version
Show versions information.

## osinfo
Show operating system information.

## gc
Try call System.gc() to start once garbage collection.

## status
Show basic runtime information for Kora.

## services
Show  all registered services name and type

## threaddump / thread_dump
Dump all thread status and stacktrace to <dump_file_name>.

## heapdump / heap_dump
Dump all heap memory data to <dump_file_name>.

## loglevel
Get or change loglevel, change loglevel need a string input <loglevel>.

## plugins
Show all plugins load status.

## plugin
Show a plugin details information, need an input <plugin_name>.

## config
Show a config content, need an input <config_name>.
