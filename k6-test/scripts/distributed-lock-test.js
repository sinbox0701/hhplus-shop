import http from "k6/http";
import { sleep, check, group } from "k6";
import { Counter, Rate, Trend } from "k6/metrics";

// 사용자 지정 메트릭
const successfulOrders = new Counter("successful_orders");
const failedOrders = new Counter("failed_orders");
const lockFailures = new Counter("lock_failures");
const successRate = new Rate("success_rate");
const orderCreationTime = new Trend("order_creation_time");

// 테스트 설정
export const options = {
  scenarios: {
    // 시나리오 1: 동일한 사용자의 동시 주문 생성
    same_user_orders: {
      executor: "ramping-arrival-rate",
      startRate: 10, // 초당 시작 요청 수
      timeUnit: "1s", // 시간 단위
      preAllocatedVUs: 50, // 미리 할당할 VU 수
      maxVUs: 100, // 최대 VU 수
      stages: [
        { duration: "30s", target: 50 }, // 30초 동안 초당 요청 50개까지 증가
        { duration: "1m", target: 50 }, // 1분간 초당 요청 50개 유지
        { duration: "30s", target: 0 }, // 30초 동안 초당 요청 0개로 감소
      ],
      tags: { scenario: "same_user_orders" },
    },

    // 시나리오 2: 서로 다른 사용자의 주문 생성 (분산락 경합이 적은 경우)
    different_user_orders: {
      executor: "ramping-arrival-rate",
      startRate: 5,
      timeUnit: "1s",
      preAllocatedVUs: 50,
      maxVUs: 100,
      stages: [
        { duration: "30s", target: 30 },
        { duration: "1m", target: 30 },
        { duration: "30s", target: 0 },
      ],
      tags: { scenario: "different_user_orders" },
      startTime: "3m", // 첫 번째 시나리오 종료 후 시작
    },

    // 시나리오 3: 주문 결제 처리
    order_payment: {
      executor: "ramping-arrival-rate",
      startRate: 5,
      timeUnit: "1s",
      preAllocatedVUs: 30,
      maxVUs: 60,
      stages: [
        { duration: "30s", target: 20 },
        { duration: "1m", target: 20 },
        { duration: "30s", target: 0 },
      ],
      tags: { scenario: "order_payment" },
      startTime: "6m", // 두 번째 시나리오 종료 후 시작
    },
  },
  thresholds: {
    http_req_duration: ["p(95)<2000"], // 95%의 요청이 2초 미만으로 완료되어야 함
    http_req_failed: ["rate<0.3"], // 요청 실패율 30% 미만
    success_rate: ["rate>0.7"], // 성공률 70% 이상
  },
};

// 헤더 설정
const headers = {
  "Content-Type": "application/json",
  Accept: "application/json",
};

// 동일한 사용자의 주문 생성 시나리오
function createOrderSameUser() {
  const userId = 1; // 고정된 사용자 ID

  // 주문 생성 요청
  const payload = JSON.stringify({
    userId: userId,
    orderItems: [
      {
        productId: Math.floor(Math.random() * 10) + 1,
        productOptionId: Math.floor(Math.random() * 5) + 1,
        quantity: Math.floor(Math.random() * 3) + 1,
      },
      {
        productId: Math.floor(Math.random() * 10) + 11,
        productOptionId: Math.floor(Math.random() * 5) + 6,
        quantity: Math.floor(Math.random() * 3) + 1,
      },
    ],
  });

  const startTime = new Date().getTime();
  const response = http.post("http://app:8080/api/v1/orders", payload, {
    headers,
  });
  const endTime = new Date().getTime();

  orderCreationTime.add(endTime - startTime);

  // 응답 확인
  const success = check(response, {
    "status is 200 or acceptable error": (r) =>
      r.status === 200 || r.status === 409 || r.status === 400,
    "lock error correctly identified": (r) => {
      if (r.status !== 200) {
        const body = JSON.parse(r.body);
        // 락 획득 실패 메시지가 있는지 확인
        return (
          body.message &&
          (body.message.includes("lock") || body.message.includes("Lock"))
        );
      }
      return true;
    },
  });

  if (response.status === 200) {
    successfulOrders.add(1);
  } else {
    failedOrders.add(1);
    if (response.body && JSON.parse(response.body).message.includes("lock")) {
      lockFailures.add(1);
    }
  }

  successRate.add(response.status === 200 ? 1 : 0);

  // k6가 너무 빠르게 요청을 보내지 않도록 짧은 휴식
  sleep(0.1);
}

// 다른 사용자의 주문 생성 시나리오
function createOrderDifferentUsers() {
  // 매번 다른 사용자 ID 사용
  const userId = Math.floor(Math.random() * 1000) + 1;

  const payload = JSON.stringify({
    userId: userId,
    orderItems: [
      {
        productId: Math.floor(Math.random() * 10) + 1,
        productOptionId: Math.floor(Math.random() * 5) + 1,
        quantity: Math.floor(Math.random() * 3) + 1,
      },
    ],
  });

  const response = http.post("http://app:8080/api/v1/orders", payload, {
    headers,
  });

  check(response, {
    "status is 200": (r) => r.status === 200,
  });

  if (response.status === 200) {
    const orderId = JSON.parse(response.body).id;
    return { userId, orderId };
  }

  sleep(0.1);
  return null;
}

// 주문 결제 테스트
function processOrderPayment() {
  // 먼저 주문 생성
  const orderData = createOrderDifferentUsers();
  if (!orderData) return;

  const { userId, orderId } = orderData;

  // 결제 요청
  const paymentPayload = JSON.stringify({
    orderId: orderId,
    userId: userId,
  });

  sleep(0.5); // 주문 생성 후 약간의 지연

  const response = http.post(
    "http://app:8080/api/v1/orders/payment",
    paymentPayload,
    { headers }
  );

  check(response, {
    "payment status is 200": (r) => r.status === 200,
    "payment processed successfully": (r) => {
      if (r.status === 200) {
        const body = JSON.parse(r.body);
        return body.status === "COMPLETED";
      }
      return false;
    },
  });

  sleep(0.1);
}

// 각 VU에 대한 기본 함수
export default function () {
  const scenario = __ENV.SCENARIO || "";

  if (scenario === "different_user_orders" || (!scenario && __VU % 3 === 1)) {
    group("Different Users Order Creation", () => {
      createOrderDifferentUsers();
    });
  } else if (scenario === "order_payment" || (!scenario && __VU % 3 === 2)) {
    group("Order Payment Processing", () => {
      processOrderPayment();
    });
  } else {
    group("Same User Order Creation", () => {
      createOrderSameUser();
    });
  }
}

// 각 시나리오에 대한 전용 함수들
export function sameUserOrders() {
  group("Same User Order Creation", () => {
    createOrderSameUser();
  });
}

export function differentUserOrders() {
  group("Different Users Order Creation", () => {
    createOrderDifferentUsers();
  });
}

export function orderPayment() {
  group("Order Payment Processing", () => {
    processOrderPayment();
  });
}
