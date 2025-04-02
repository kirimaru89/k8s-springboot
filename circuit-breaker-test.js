import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = 'http://localhost:9999/api/test';

// # Chạy test Circuit Breaker
// k6 run --env PATTERN=circuitBreaker circuit-breaker-test.js
//
// # Chạy test Bulkhead
// k6 run --env PATTERN=bulkhead circuit-breaker-test.js
//
// # Chạy test Rate Limiter
// k6 run --env PATTERN=rateLimiter circuit-breaker-test.js
//
// # Chạy test Timeout
// k6 run --env PATTERN=timeout circuit-breaker-test.js

// Cấu hình cho từng pattern test
const patternConfigs = {
  // Circuit Breaker Pattern:
  // - Mục đích: Test khả năng ngắt mạch khi có nhiều lỗi liên tiếp
  // - Cấu hình trong ExampleService.java với annotation @CircuitBreaker:
  //   + slidingWindowSize: 10 (số request để tính failure rate)
  //   + failureRateThreshold: 50 (tỉ lệ % lỗi để chuyển sang OPEN)
  //   + waitDurationInOpenState: 10s (thời gian ở trạng thái OPEN)
  circuitBreaker: {
    executor: 'ramping-vus',
    startVUs: 0,
    stages: [
      { duration: '10s', target: 5 },    // Khởi động nhẹ nhàng với 5 users
      { duration: '20s', target: 20 },   // Tăng nhanh để tạo nhiều lỗi -> OPEN
      { duration: '10s', target: 0 },    // Dừng để chờ HALF_OPEN
      { duration: '10s', target: 2 },    // Test phục hồi với ít users
      { duration: '10s', target: 0 },    // Kết thúc test
    ]
  },

  // Bulkhead Pattern:
  // - Mục đích: Test giới hạn concurrent calls
  // - Cấu hình trong ExampleService.java với annotation @Bulkhead
  // - Response codes:
  //   + 200: Success
  //   + 503: BULKHEAD_FULL
  bulkhead: {
    executor: 'constant-arrival-rate',
    rate: 20,
    timeUnit: '1s',
    preAllocatedVUs: 25,
    duration: '1m'
  },

  // Rate Limiter Pattern:
  // - Mục đích: Test giới hạn số request/giây
  // - Cấu hình trong ExampleService.java với annotation @RateLimiter
  // - Response codes:
  //   + 200: Success
  //   + 429: RATE_LIMIT_EXCEEDED
  rateLimiter: {
    executor: 'constant-vus',
    vus: 10,
    duration: '1m'
  },

  // Timeout Pattern:
  // - Mục đích: Test xử lý timeout (cấu hình 2s)
  // - Cấu hình trong ExampleService.java với annotation @TimeLimiter
  // - Response codes:
  //   + 200: Success
  //   + 504: TIMEOUT_ERROR
  timeout: {
    executor: 'constant-vus',
    vus: 5,
    duration: '30s'
  }
};

// Lấy pattern từ environment variable
const pattern = __ENV.PATTERN || 'circuitBreaker';

export const options = {
  scenarios: {
    [pattern]: patternConfigs[pattern]
  }
};

// Các hàm test pattern
const patternTests = {
  // Test Circuit Breaker Pattern
  circuitBreaker: function() {
    let failureCount = 0;
    const currentState = getCurrentCircuitBreakerState();
    let response;
    if (currentState === 'HALF_OPEN') {
      // Khi ở trạng thái HALF_OPEN, gửi request success để chuyển sang CLOSED
      console.log('HALF_OPEN state detected - Sending success request to recover');
      response = http.get(`${BASE_URL}/test-circuit-breaker?success=true`);
    } else if (currentState === 'CLOSED' && __VU <= 5) {
      // Khi đã CLOSED và số VU thấp, tiếp tục gửi success để duy trì trạng thái
      console.log('CLOSED state - Maintaining healthy state with success requests');
      response = http.get(`${BASE_URL}/test-circuit-breaker?success=true`);
    } else {
      // Các trường hợp còn lại gửi request failure
      response = http.get(`${BASE_URL}/test-circuit-breaker?success=false`);
    }
    
    // console.log(`[${new Date().toISOString()}]`, `- Response: ${response.json().code}`);
    check(response, {
      'Response code is valid': (r) => {
        const body = r.json();
        // Kiểm tra cả HTTP status và response code
        if (r.status === 500) {
          // console.log('Service failed with 500');
          return body.code === '500';     // INTERNAL_SERVER_ERROR
        } else if (r.status === 503) {
          // console.log('Circuit Breaker OPEN');
          return body.code === '503';     // CIRCUIT_BREAKER_OPEN
        }
        return false;
      }
    });
    
    monitorCircuitBreakerState();
    sleep(0.1);  // Giảm sleep time để tạo nhiều lỗi nhanh hơn
  },

  // Test Bulkhead Pattern
  bulkhead: function() {
    const response = http.get(`${BASE_URL}/test-circuit-breaker?success=true`);
    
    check(response, {
      'Response is valid': (r) => {
        const body = r.json();
        return body.code === '200' ||   // SUCCESS
               body.code === '503';     // BULKHEAD_FULL
      }
    });
    
    if (response.json().code === '503') {
      console.warn(`[${new Date().toISOString()}] Bulkhead full`);
    }
    
    sleep(0.1);
  },

  // Test Rate Limiter Pattern
  rateLimiter: function() {
    const response = http.get(`${BASE_URL}/test-circuit-breaker?success=true`);
    
    check(response, {
      'Response is valid': (r) => {
        const body = r.json();
        return body.code === '200' ||   // SUCCESS
               body.code === '429';     // RATE_LIMIT_EXCEEDED
      }
    });
    
    if (response.json().code === '429') {
      console.warn(`[${new Date().toISOString()}] Rate limit exceeded`);
    }
  },

  // Test Timeout Pattern
  timeout: function() {
    const response = http.get(`${BASE_URL}/test-circuit-breaker/timeout`);
    
    check(response, {
      'Response handled correctly': (r) => {
        const body = r.json();
        return body.code === '200' ||   // SUCCESS
               body.code === '504';     // TIMEOUT_ERROR
      }
    });
    
    console.log(`Response time: ${response.timings.duration}ms`);
  }
};

// Lấy trạng thái Circuit Breaker từ endpoint status
function getCurrentCircuitBreakerState() {
  const response = http.get(`${BASE_URL}/test-circuit-breaker/status`);
  return response.json().data;
}

// Ghi log trạng thái Circuit Breaker
function monitorCircuitBreakerState() {
  const state = getCurrentCircuitBreakerState();
  console.log(`[${new Date().toISOString()}]`, `- Data: ${state}`);
}

// Hàm chính để chạy test
export default function() {
  patternTests[pattern]();
} 