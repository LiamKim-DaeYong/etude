# Etude frontend

Follow the root `AGENTS.md` and `CONTRIBUTING.md`. Codex owns `web/`; shared repository files belong in a separate `area:shared` PR. Merge only after owner approval.

## Stack and boundaries

- npm workspaces: `apps/admin` uses React, TypeScript, Vite and React Router. `packages/ui` contains official shadcn `base-nova` components on Base UI. Tailwind v4, Lucide and self-hosted Pretendard provide styling, icons and typography.
- Apps depend on shared packages. Shared UI must not import an app, router or business rules. Future storefront and CS apps choose their own framework and deploy independently.
- Use official CLI components; do not recreate their appearance or behavior. Run `npm run ui:add -- <component>` from `web/` and review diffs before overwriting generated files. Keep local component adaptations documented in `packages/ui/README.md`.
- Keep product text in Korean. Do not present example data as live metrics or enable navigation to unfinished features.

## Commands and verification

Run from `web/`: `npm ci`, `npm run dev`, `npm run check`, `npm run security:check`. Browser tests require `npx playwright install chromium` and own port 4173. Node.js 24 is the CI target; state the actual local runtime when reporting checks.

Verify desktop/mobile layouts, light/dark themes, keyboard interactions, Korean font loading and production CSP. Development HMR is not proof that the production policy works. Do not claim remote CI or deployment verification from local results.

## Security

- Do not place secrets, private documents, raw internal logs or credentials in client bundles, public assets or search indexes.
- Keep inline scripts and eval blocked. Theme initialization uses an external script. New floating/overlay components must work under the policy in `packages/config/security.js`.
- Do not silence security reports with forced dependency changes or broad CSP exceptions. Document remaining findings and their scope.
- Shared source components and fonts retain their license notices. Do not add remote scripts/fonts without a concrete requirement and review.
