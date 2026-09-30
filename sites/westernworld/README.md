# Western World Visa Services website

Public marketing site for westernworldvisaservices.com, rebuilt in Next.js 16 + Tailwind v4. Every enquiry form posts
to the CRM's public intake, so each submission becomes a lead (or is added to the person's open lead) and the student
gets the email + WhatsApp acknowledgement.

```bash
npm install
npm run dev -- -p 3001      # needs the CRM backend on :8081 for the forms
npm run build && npx next start -p 3001
```

| Setting | Default | Meaning |
|---|---|---|
| `CRM_API_URL` | `http://localhost:8081` | CRM backend; `/api/*` is proxied to it (no CORS needed) |
| `NEXT_PUBLIC_CRM_TENANT` | `westernworld` | CRM tenant slug that receives the enquiries (owner, 30 Sep 2026: Western World is the first tenant; onboard it before forms work) |

## Content

- `content/*.json` were generated from the old site on 26 Sep 2026 (visible content only, HTML comments ignored):
  8 countries, 50 universities, 79 IELTS + 79 PTE city pages, student galleries and sliders. Images are in `public/`
  under their old paths (`download (1).jpg` became `download-1.jpg`).
- `content/copy.ts` holds text that several pages share (packages, IELTS test details, city-page copy).
- Page-specific text lives in each `app/**/page.tsx`.

## Old URLs

All old `.php` URLs, `/universities-detail.php?uid=N`, `/delhi/ielts-classes-*` and `/pte/pte-classes-*` redirect (308)
to the new pages (`next.config.ts`, `app/universities-detail.php/route.ts`).

## Known gaps inherited from the old site

- Study in Ireland (HTTP 500), the German-language page (404) and the only blog post (404) had no content. Ireland
  and USA show an enquiry form instead of a university list; the blog card leads to enrolment.
- 7 images were missing on the old server; those universities show a placeholder tile.
- Some university entries look like data-entry mistakes (uid 102 "Study in Canada", 107 "ST Michelle College11",
  empty descriptions). Kept as they were, for the client to clean up.
- Lorem-ipsum section subtitles ("Fusce sem dolor…") were not carried over.
- The old site's tawk.to live chat was removed (owner, 26 Sep 2026): chats went to an outside account and never became
  leads. Contact is by the enquiry forms (→ CRM leads) and the WhatsApp button. A chat can come back later as a CRM
  lead source (tawk.to webhook → intake).
