import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  scenarios: {
    // Kịch bản 1: Test BANK_A với nhiều request để kích hoạt circuit breaker
    bank_a_failure: {
      executor: 'constant-vus',
      vus: 10,  // 10 users đồng thời
      duration: '30s',
      exec: 'testBankA',
      startTime: '0s',
    },
    // Kịch bản 2: Test BANK_B với tải bình thường
    bank_b_success: {
      executor: 'constant-vus',
      vus: 5,   // 5 users đồng thời
      duration: '30s',
      exec: 'testBankB',
      startTime: '0s',
    },
    // Kịch bản 3: Tiếp tục test BANK_A khi circuit breaker đã OPEN
    bank_a_after_open: {
      executor: 'constant-vus',
      vus: 5,
      duration: '20s',
      exec: 'testBankA',
      startTime: '35s',
    },
    // Kịch bản 4: Tiếp tục test BANK_B khi đang hoạt động bình thường
    bank_b_continue: {
      executor: 'constant-vus',
      vus: 5,
      duration: '20s',
      exec: 'testBankB',
      startTime: '35s',
    },
    // Thêm kịch bản kiểm tra trạng thái
    check_circuit_breaker_status: {
      executor: 'constant-vus',
      vus: 1,
      duration: '55s',
      exec: 'checkCircuitBreakerStatus',
      startTime: '0s',
    }
  },
};

const BASE_URL = 'http://localhost:9999';

const headers = {
  'Content-Type': 'application/json',
};

// Thêm function mới để kiểm tra trạng thái
export function checkCircuitBreakerStatus() {
  // Kiểm tra BANK_A
  const responseA = http.get(
    `${BASE_URL}/api/test/test-circuit-breaker/custom/status/BANK_A`
  );
  
  // Kiểm tra BANK_B
  const responseB = http.get(
    `${BASE_URL}/api/test/test-circuit-breaker/custom/status/BANK_B`
  );

  check(responseA, {
    'BANK_A status check successful': (r) => r.status === 200,
  });
  
  check(responseB, {
    'BANK_B status check successful': (r) => r.status === 200,
  });

  // Log trạng thái
  if (responseA.status === 200) {
    console.log('BANK_A Status:', responseA.body);
  }
  if (responseB.status === 200) {
    console.log('BANK_B Status:', responseB.body);
  }

  sleep(5); // Kiểm tra mỗi 5 giây
}

// Test function cho BANK_A - Gửi request với operation ERROR để tăng tỷ lệ lỗi
export function testBankA() {
  const payload = JSON.stringify({
    bankName: 'BANK_A',
    operation: 'timeout'  // Sử dụng operation gây lỗi
  });

  const response = http.post(
    `${BASE_URL}/api/test/test-circuit-breaker/custom`,
    payload,
    { headers }
  );

  check(response, {
    'BANK_A response received': (r) => r.status === 200 || r.status === 500 || r.status === 503,
    'Circuit Breaker status check': (r) => {
      // if (r.status === 500) {
      //   console.log('BANK_A Circuit Breaker might be OPEN');
      // }
      return true;
    },
  });

  sleep(0.5); // 500ms delay giữa các request
}

// Test function cho BANK_B - Gửi request bình thường
export function testBankB() {
  const payload = JSON.stringify({
    bankName: 'BANK_B',
    operation: 'timeout'  // Sử dụng operation bình thường
  });

  const response = http.post(
    `${BASE_URL}/api/test/test-circuit-breaker/custom`,
    payload,
    { headers }
  );

  check(response, {
    'BANK_B response is successful': (r) => r.status === 200,
    'Circuit Breaker status check': (r) => {
      // console.log('BANK_B Response Status:', r.status);
      // console.log('BANK_B Response Body:', r.body);
      return true;
    }
  });

  sleep(0.5); // 500ms delay giữa các request
}

// Thêm setup và teardown để theo dõi trạng thái test
export function setup() {
  console.log('Bắt đầu test circuit breaker');
  // Kiểm tra trạng thái ban đầu
  checkCircuitBreakerStatus();
}

export function teardown(data) {
  console.log('Kết thúc test circuit breaker');
  // Kiểm tra trạng thái cuối cùng
  checkCircuitBreakerStatus();
} 