import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: __ENV.VUS ? parseInt(__ENV.VUS, 10) : 5,
  duration: __ENV.DURATION || '30s'
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const ACCOUNT_ID = __ENV.ACCOUNT_ID || '11111111-1111-1111-1111-111111111111';

export default function () {
  const loginRes = http.post(`${BASE_URL}/api/v1/auth/login`, JSON.stringify({
    email: 'seed@brokerx.dev',
    password: 'password123',
    otp: '123456'
  }), {
    headers: { 'Content-Type': 'application/json' }
  });
  check(loginRes, { 'login ok': (r) => r.status === 200 });
  const token = loginRes.status === 200 ? loginRes.json('token') : null;

  const clientOrderId = `load-${Date.now()}-${Math.random().toString(16).slice(2, 8)}`;
  const orderRes = http.post(`${BASE_URL}/api/v1/orders`, JSON.stringify({
    accountId: ACCOUNT_ID,
    side: 'BUY',
    type: 'LIMIT',
    symbol: 'AAPL',
    qty: 1,
    limitPrice: 100,
    clientOrderId
  }), {
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) }
  });
  check(orderRes, { 'order accepted': (r) => r.status === 200 });

  sleep(1);
}
