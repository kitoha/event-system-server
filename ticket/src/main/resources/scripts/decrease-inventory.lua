local key = KEYS[1]
local stock = redis.call('get', key)

if stock and tonumber(stock) > 0 then
    redis.call('decr', key)
    return 1
else
    return 0
end
