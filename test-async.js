import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  vus: 100,             // Number of virtual users
  duration: '60s',     // Total duration of test
};

const BASE_URL = 'http://localhost:9999';

export default function () {
  let res = http.get(`${BASE_URL}/api/test/call-async-flow-to-spring-app-2/9`);

  check(res, {
    '✅ status is 200': (r) => r.status === 200,
    '🚀 response time < 500ms': (r) => r.timings.duration < 500,
  });
}