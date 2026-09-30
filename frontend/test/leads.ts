import type { Schemas } from "@/lib/api";

type Lead = Schemas["LeadResponse"];

/** A lead as the backend returns it; override what the test cares about. */
export function lead(overrides: Partial<Lead> = {}): Lead {
  return {
    id: "lead-1", leadNumber: "LD-000001", fullName: "Riya Sharma", phone: "+919876543210", email: "riya@mail.test",
    serviceInterest: "Study Abroad", preferredCountry: "Canada", sourceType: "WEBSITE_FORM", status: "NEW",
    enquiryCount: 1, createdAt: "2026-09-29T10:00:00Z", lastEnquiryAt: "2026-09-29T10:00:00Z", customFields: {},
    ...overrides,
  };
}

export function page(items: Lead[], overrides: Partial<Schemas["PageResponseLeadResponse"]> = {}) {
  return { items, page: 0, size: 25, totalElements: items.length, totalPages: 1, ...overrides };
}
