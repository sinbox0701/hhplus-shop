import http from "k6/http";
import { sleep, check, group } from "k6";
import { Counter, Rate } from "k6/metrics";

// 사용자 지정 메트릭
const successfulCouponCreations = new Counter("successful_coupon_creations");
const failedCouponCreations = new Counter("failed_coupon_creations");
const lockFailures = new Counter("lock_failures");
const couponCreationSuccessRate = new Rate("coupon_creation_success_rate");

// 테스트 설정
export const options = {
  scenarios: {
    // 시나리오 1: 동일한 사용자의 동시 쿠폰 생성 (분산락 테스트)
    same_user_coupon_creation: {
      executor: "ramping-arrival-rate",
      startRate: 5, // 초당 시작 요청 수
      timeUnit: "1s", // 시간 단위
      preAllocatedVUs: 20, // 미리 할당할 VU 수
      maxVUs: 50, // 최대 VU 수
      stages: [
        { duration: "20s", target: 20 }, // 20초 동안 초당 요청 20개까지 증가
        { duration: "40s", target: 20 }, // 40초간 초당 요청 20개 유지
        { duration: "20s", target: 0 }, // 20초 동안 초당 요청 0개로 감소
      ],
      tags: { scenario: "same_user_coupon_creation" },
    },

    // 시나리오 2: 여러 사용자의 쿠폰 발급 (분산락 경합 적음)
    multi_user_coupon_creation: {
      executor: "ramping-arrival-rate",
      startRate: 3,
      timeUnit: "1s",
      preAllocatedVUs: 20,
      maxVUs: 40,
      stages: [
        { duration: "20s", target: 15 },
        { duration: "40s", target: 15 },
        { duration: "20s", target: 0 },
      ],
      tags: { scenario: "multi_user_coupon_creation" },
      startTime: "2m", // 첫 번째 시나리오 종료 후 시작
    },

    // 시나리오 3: 쿠폰 사용 테스트
    use_coupon: {
      executor: "ramping-arrival-rate",
      startRate: 2,
      timeUnit: "1s",
      preAllocatedVUs: 15,
      maxVUs: 30,
      stages: [
        { duration: "20s", target: 10 },
        { duration: "40s", target: 10 },
        { duration: "20s", target: 0 },
      ],
      tags: { scenario: "use_coupon" },
      startTime: "4m", // 두 번째 시나리오 종료 후 시작
    },
  },
  thresholds: {
    http_req_duration: ["p(95)<1500"], // 95%의 요청이 1.5초 미만으로 완료되어야 함
    http_req_failed: ["rate<0.3"], // 요청 실패율 30% 미만
    coupon_creation_success_rate: ["rate>0.7"], // 쿠폰 생성 성공률 70% 이상
  },
};

// 헤더 설정
const headers = {
  "Content-Type": "application/json",
  Accept: "application/json",
};

// 1. 동일한 사용자의 쿠폰 생성 시나리오 (분산락 테스트)
function createCouponSameUser() {
  const userId = 1; // 고정된 사용자 ID

  // 현재 시간 기준으로 고유한 쿠폰 코드 생성
  const couponCode = `TESTCOUPON-${Date.now()}-${Math.floor(
    Math.random() * 10000
  )}`;

  // 쿠폰 생성 요청
  const payload = JSON.stringify({
    userId: userId,
    couponCode: couponCode,
    couponType: "DISCOUNT_ORDER",
    discountRate: 10,
    startDate: new Date().toISOString(),
    endDate: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString(), // 30일 후
    quantity: 1,
  });

  const response = http.post("http://app:8080/api/v1/coupons", payload, {
    headers,
  });

  // 응답 확인
  const success = check(response, {
    "status is 200 or acceptable error": (r) =>
      r.status === 200 || r.status === 409 || r.status === 400,
    "lock error correctly identified": (r) => {
      if (r.status !== 200) {
        try {
          const body = JSON.parse(r.body);
          // 락 획득 실패 메시지가 있는지 확인
          return (
            body.message &&
            (body.message.includes("lock") || body.message.includes("Lock"))
          );
        } catch (e) {
          return false;
        }
      }
      return true;
    },
  });

  if (response.status === 200) {
    successfulCouponCreations.add(1);

    try {
      const body = JSON.parse(response.body);
      return {
        userId: userId,
        couponId: body.id,
        userCouponId: body.userCouponId,
      };
    } catch (e) {
      return null;
    }
  } else {
    failedCouponCreations.add(1);

    try {
      if (response.body && JSON.parse(response.body).message.includes("lock")) {
        lockFailures.add(1);
      }
    } catch (e) {
      // 파싱 오류 무시
    }

    return null;
  }

  couponCreationSuccessRate.add(response.status === 200 ? 1 : 0);
}

// 2. 여러 사용자의 쿠폰 생성 (분산락 경합 적음)
function createCouponMultiUser() {
  // 매번 다른 사용자 ID 사용
  const userId = Math.floor(Math.random() * 1000) + 1;

  // 고유한 쿠폰 코드 생성
  const couponCode = `TESTCOUPON-MULTI-${Date.now()}-${Math.floor(
    Math.random() * 10000
  )}`;

  const payload = JSON.stringify({
    userId: userId,
    couponCode: couponCode,
    couponType: "DISCOUNT_PRODUCT",
    discountRate: 15,
    startDate: new Date().toISOString(),
    endDate: new Date(Date.now() + 14 * 24 * 60 * 60 * 1000).toISOString(), // 14일 후
    quantity: 1,
  });

  const response = http.post("http://app:8080/api/v1/coupons", payload, {
    headers,
  });

  check(response, {
    "multi user coupon creation successful": (r) => r.status === 200,
  });

  if (response.status === 200) {
    try {
      const body = JSON.parse(response.body);
      return {
        userId: userId,
        couponId: body.id,
        userCouponId: body.userCouponId,
      };
    } catch (e) {
      return null;
    }
  }

  sleep(0.1);
  return null;
}

// 3. 쿠폰 사용 테스트
function useCoupon() {
  // 먼저 쿠폰 생성
  const couponData = createCouponMultiUser();
  if (!couponData) return;

  const { userId, couponId, userCouponId } = couponData;

  // 쿠폰 발급 (status를 ISSUED로 변경)
  const issuePayload = JSON.stringify({
    userId: userId,
    couponId: couponId,
  });

  sleep(0.5); // 쿠폰 생성 후 약간의 지연

  const issueResponse = http.post(
    "http://app:8080/api/v1/coupons/issue",
    issuePayload,
    { headers }
  );

  check(issueResponse, {
    "coupon issue successful": (r) => r.status === 200,
  });

  if (issueResponse.status !== 200) return;

  sleep(0.5); // 쿠폰 발급 후 약간의 지연

  // 쿠폰 사용
  const usePayload = JSON.stringify({
    userId: userId,
    couponId: couponId,
  });

  const useResponse = http.post(
    "http://app:8080/api/v1/coupons/use",
    usePayload,
    { headers }
  );

  check(useResponse, {
    "coupon use successful": (r) => r.status === 200,
  });

  sleep(0.1);
}

// 각 VU에 대한 기본 함수
export default function () {
  const scenario = __ENV.SCENARIO || "";

  if (
    scenario === "multi_user_coupon_creation" ||
    (!scenario && __VU % 3 === 1)
  ) {
    group("Multiple Users Coupon Creation", () => {
      createCouponMultiUser();
    });
  } else if (scenario === "use_coupon" || (!scenario && __VU % 3 === 2)) {
    group("Coupon Usage", () => {
      useCoupon();
    });
  } else {
    group("Same User Coupon Creation", () => {
      createCouponSameUser();
    });
  }

  // 요청 간 짧은 지연
  sleep(Math.random() * 0.3 + 0.1);
}

// 각 시나리오에 대한 전용 함수들
export function sameUserCouponCreation() {
  group("Same User Coupon Creation", () => {
    createCouponSameUser();
  });
}

export function multiUserCouponCreation() {
  group("Multiple Users Coupon Creation", () => {
    createCouponMultiUser();
  });
}

export function couponUsage() {
  group("Coupon Usage", () => {
    useCoupon();
  });
}
