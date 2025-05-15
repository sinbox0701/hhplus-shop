-- 쿠폰 발급 스크립트
local couponCode = KEYS[1]
local userId = ARGV[1]

-- 중복 발급 확인
if redis.call('SISMEMBER', 'coupon:issued:' .. couponCode, userId) == 1 then
    return {err = "ALREADY_ISSUED"}
end

-- 재고 확인
if redis.call('LLEN', 'coupon:stock:' .. couponCode) == 0 then
    return {err = "OUT_OF_STOCK"}
end

-- 재고 감소
local couponId = redis.call('LPOP', 'coupon:stock:' .. couponCode)

-- 발급 처리
redis.call('SADD', 'coupon:issued:' .. couponCode, userId)
redis.call('HINCRBY', 'coupon:info:' .. couponCode, 'remaining_quantity', -1)

return {ok = couponId} 