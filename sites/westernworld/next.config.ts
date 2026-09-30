import type { NextConfig } from "next";
import cities from "./content/cities.json";

/** Where the CRM backend runs; enquiries are proxied to it so the browser never needs CORS. */
const CRM_API_URL = process.env.CRM_API_URL ?? "http://localhost:8081";
const dev = process.env.NODE_ENV !== "production";

/** A public marketing site: no framing, scripts only from here (and Cloudflare's captcha), Google Maps embed. */
const csp = [
  "default-src 'self'",
  `script-src 'self' 'unsafe-inline'${dev ? " 'unsafe-eval'" : ""} https://challenges.cloudflare.com`,
  "style-src 'self' 'unsafe-inline'",
  "img-src 'self' data: blob:",
  "font-src 'self' data:",
  `connect-src 'self'${dev ? " ws:" : ""}`,
  "frame-src https://challenges.cloudflare.com https://www.google.com",
  "frame-ancestors 'none'",
  "base-uri 'self'",
  "form-action 'self'",
  "object-src 'none'",
].join("; ");

const securityHeaders = [
  { key: "Content-Security-Policy", value: csp },
  { key: "X-Frame-Options", value: "DENY" },
  { key: "X-Content-Type-Options", value: "nosniff" },
  { key: "Referrer-Policy", value: "strict-origin-when-cross-origin" },
  { key: "Permissions-Policy", value: "camera=(), microphone=(), geolocation=()" },
  ...(dev ? [] : [{ key: "Strict-Transport-Security", value: "max-age=31536000; includeSubDomains" }]),
];

const COUNTRY_PAGES: Record<string, string> = {
  "study-in-canada": "canada",
  "study-in-newzealand": "new-zealand",
  "study-in-usa": "usa",
  "study-in-uk": "uk",
  "study-in-ireland": "ireland",
  "study-in-france": "france",
  "study-in-germany": "germany",
  "study-in-singapore": "singapore",
};

const PAGES = [
  "about-us", "contact-us", "blog", "ielts-general", "ielts-academy", "pte", "toefl", "ukvi-ielts",
  "online-courses", "classroom-courses", "ielts-classes-india", "pte-classes-india", "visa-assistance",
];

const nextConfig: NextConfig = {
  /**
   * Only the CRM's public enquiry endpoints are reachable through this site; the staff API, dev login and everything
   * else stay off the public website's domain.
   */
  async rewrites() {
    return ["enquiry-form", "enquiries"].map((endpoint) => ({
      source: `/api/v1/public/tenants/:slug/${endpoint}`,
      destination: `${CRM_API_URL}/api/v1/public/tenants/:slug/${endpoint}`,
    }));
  },
  async headers() {
    return [{ source: "/:path*", headers: securityHeaders }];
  },
  /**
   * Every URL of the old PHP site keeps working (bookmarks, Google results, ads).
   * universities-detail.php?uid=N is handled by app/universities-detail.php/route.ts.
   */
  async redirects() {
    return [
      { source: "/index.php", destination: "/", permanent: true },
      ...PAGES.map((p) => ({ source: `/${p}.php`, destination: `/${p}`, permanent: true })),
      ...Object.entries(COUNTRY_PAGES).map(([p, slug]) => ({ source: `/${p}.php`, destination: `/study-in/${slug}`, permanent: true })),
      { source: "/german-language-training.php", destination: "/online-courses", permanent: true },
      ...cities.ielts.map((c) => ({ source: `/delhi/${c.slug}`, destination: `/ielts-classes/${c.slug}`, permanent: true })),
      ...cities.pte.map((c) => ({ source: `/pte/${c.slug}`, destination: `/pte-classes/${c.slug}`, permanent: true })),
    ];
  },
};

export default nextConfig;
