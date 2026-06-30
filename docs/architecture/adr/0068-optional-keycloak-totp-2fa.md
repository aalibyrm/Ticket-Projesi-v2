# ADR-0068: Optional Keycloak TOTP 2FA

## Status

Accepted

## Context

The project already uses Keycloak as the authentication authority and keeps user
password handling outside the React app and backend services. The realm has a
TOTP policy, but users should not be forced to enroll during local demo login.

## Decision

Two-factor authentication is optional and implemented through Keycloak TOTP.
Users can enable authenticator-app based 2FA from Keycloak account management.
Users who do not enable 2FA continue to sign in with username and password only.

Users manage 2FA in Keycloak account management. The React app does not
collect, validate, store, or log OTP codes or TOTP secrets.

## Consequences

- Demo and local development login flows remain low-friction.
- The application can show 2FA support for delivery requirements.
- Higher-risk accounts are not forcibly protected unless the user enables 2FA.
- If mandatory MFA is required later, the Keycloak required action can be
  enabled without moving OTP logic into application services.
