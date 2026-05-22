FROM redis/redis-stack

WORKDIR /usr/local/etc/redis

COPY redis.conf .

EXPOSE 6379

CMD ["redis-server", "/usr/local/etc/redis/redis.conf"]