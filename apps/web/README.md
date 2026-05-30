# Ticket Management Web

React 18 web istemcisi #50 kapsaminda ADR-0037 kararina gore scaffold
edilmistir.

## Stack

- React 18 + TypeScript.
- Material UI v7 ve MUI X Data Grid Community.
- React Router Framework SPA mode.
- Redux Toolkit + React Redux.
- TanStack Query.
- OpenAPI Generator `typescript-axios`.
- Keycloak JS.
- React Hook Form + Zod.
- Vitest + Testing Library + Playwright + MSW.

## Commands

```powershell
npm install
npm run dev
npm run lint
npm test -- --watch=false
npm run build
```

## Environment

`apps/web/.env.example` public frontend configuration keysini gosterir.
Secret degerler frontend bundle'a eklenmez.
