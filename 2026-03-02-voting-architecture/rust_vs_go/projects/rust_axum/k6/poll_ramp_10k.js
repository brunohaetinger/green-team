import http from 'k6/http';
import { check, sleep } from 'k6';
import { htmlReport } from 'https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const USE_SETUP = (__ENV.USE_SETUP || '') !== '' && __ENV.USE_SETUP !== '0';
const SLEEP_MS = (__ENV.SLEEP_MS ? parseInt(__ENV.SLEEP_MS, 10) : 0);
const TARGET_VUS = (__ENV.TARGET_VUS ? parseInt(__ENV.TARGET_VUS, 10) : 10000);

let POLL_ID = __ENV.POLL_ID ? parseInt(__ENV.POLL_ID, 10) : 1;
let OPTION_ID = __ENV.OPTION_ID ? parseInt(__ENV.OPTION_ID, 10) : 1;

export const options = {
  discardResponseBodies: true,
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<2000'],
  },
  scenarios: {
    ramp_10k: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 1000 },
        { duration: '30s', target: 2000 },
        { duration: '60s', target: TARGET_VUS },
        { duration: '60s', target: 0 },
      ],
      gracefulRampDown: '30s',
    },
  },
};

export function setup() {
  if (!USE_SETUP) {
    return { pollID: POLL_ID, optionID: OPTION_ID };
  }

  // create a poll and grab the numeric IDs from the response
  let res = http.post(
    `${BASE_URL}/polls`,
    JSON.stringify({ question: 'Load test poll', options: ['Option LT'] }),
    { headers: { 'Content-Type': 'application/json' }, tags: { endpoint: 'polls_create' } }
  );
  check(res, { 'created poll (201)': (r) => r.status === 201 });
  const body = res.json();
  const pollID = body.id;
  const optionID = body.options[0].id;

  return { pollID, optionID };
}

export default function (data) {
  const pollID = data.pollID;
  const optionID = data.optionID;
  const url = `${BASE_URL}/vote`;

  const voterID = `vu${__VU}-it${__ITER}-${Date.now()}`;
  const payload = JSON.stringify({
    poll_id: pollID,
    option_id: optionID,
    voter_id: voterID,
  });
  const params = { headers: { 'Content-Type': 'application/json' }, tags: { endpoint: 'vote' } };
  const res = http.post(url, payload, params);

  check(res, { 'vote accepted (202)': (r) => r.status === 202 });

  if (SLEEP_MS > 0) {
    sleep(SLEEP_MS / 1000);
  }
}

export function handleSummary(data) {
  return {
    'k6-report.html': htmlReport(data),
    'k6-summary.json': JSON.stringify(data, null, 2),
  };
}
