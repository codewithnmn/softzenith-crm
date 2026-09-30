/** Sends website enquiries to the CRM's public intake (the tenant is configured, never hard-coded). */
export const CRM_TENANT = process.env.NEXT_PUBLIC_CRM_TENANT ?? "westernworld";

export type Enquiry = {
  fullName: string;
  phone: string;
  email?: string;
  serviceInterest?: string;
  preferredCountry?: string;
  message?: string;
  /** Which form on which page, so staff know where the lead came from. */
  sourceDetail: string;
  website?: string; // honeypot
  captchaToken?: string; // Cloudflare Turnstile, when the site has a key
};

export async function submitEnquiry(e: Enquiry): Promise<{ message: string }> {
  const utm = new URLSearchParams(window.location.search);
  const res = await fetch(`/api/v1/public/tenants/${CRM_TENANT}/enquiries`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      ...e,
      utmSource: utm.get("utm_source") ?? undefined,
      utmMedium: utm.get("utm_medium") ?? undefined,
      utmCampaign: utm.get("utm_campaign") ?? undefined,
    }),
  });
  const body = await res.json().catch(() => ({}));
  if (!res.ok) {
    throw new Error(body?.detail ?? "Sorry, we could not send your enquiry. Please call us or try again.");
  }
  return body;
}
