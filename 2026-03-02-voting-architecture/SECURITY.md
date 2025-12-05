# Security

## Bot detection & prevention

- `MFA` for voters
- Device binding - Link a user to a specific device on registration, avoiding access from multiple devices
- CAPTCHA
  - Behavioral CAPTCHAs - analyses user interactions as mouse move, scrolling, etc.
  - Invisible CAPTCHAs - works on the background, analyzing user activity and presenting challenges on suspicious activity
  - Proof-of-work challenges - requires computational processing to complete, making it expensive for bot massive attacks
- JS Challenges
  - Execute javascript on the client. Browsers can execute JS, while most bots do not have a JS stack.
- Rate limiting
- Honeypots
  - Traps to lure and detect bots, usually with hidden form fields, fake links, invisible to human, but attractive to scripts
