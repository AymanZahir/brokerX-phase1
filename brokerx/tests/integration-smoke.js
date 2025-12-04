import http from 'k6/http';
import { check } from 'k6';

export const options = {
  vus: 1,
  duration: '10s',
};

const BASE = __ENV.BASE_URL || 'http://localhost:8081';
const EMAIL = __ENV.SMOKE_EMAIL || 'demo@brokerx.dev';
const PASSWORD = __ENV.SMOKE_PASSWORD || 'P@ssw0rd!';

export default function () {
  const login = http.post(`${BASE}/api/v1/auth/login`, { email: EMAIL, password: PASSWORD });
  check(login, { 'login 200': (r) => r.status === 200 });
  const token = login.json('token');

  const orderPayload = {
    side: 'BUY',
    type: 'MARKET',
    symbol: 'AAPL',
    qty: 1,
    clientOrderId: `k6-smoke-${Date.now()}`,
  };

  const order = http.post(`${BASE}/api/v1/orders`, JSON.stringify(orderPayload), {
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
  });
  check(order, { 'order 200': (r) => r.status === 200 });
}
