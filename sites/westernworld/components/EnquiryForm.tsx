"use client";

import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";
import { Send } from "lucide-react";
import { submitEnquiry } from "@/lib/enquiry";
import { Turnstile } from "@/components/Turnstile";

type Field = "name" | "email" | "mobile" | "message" | "course";

/**
 * Every form on the site (Keep in touch, Call back, Request call back, Book now, Enroll now) is this one form with
 * different fields. It creates a lead in the CRM, which acknowledges the enquirer by email and WhatsApp, then shows
 * the thank-you page.
 */
export default function EnquiryForm({
  title, subtitle, source, fields = ["name", "email", "mobile", "message"], courseLabel = "Course",
  defaults = {}, country, submitLabel = "Submit", dark = false,
}: {
  title?: string;
  subtitle?: string;
  source: string;
  fields?: Field[];
  courseLabel?: string;
  defaults?: Partial<Record<Field, string>>;
  country?: string;
  submitLabel?: string;
  dark?: boolean;
}) {
  const router = useRouter();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [attempt, setAttempt] = useState(0);

  const submit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const f = Object.fromEntries(new FormData(e.currentTarget)) as Record<string, string>;
    setBusy(true);
    setError("");
    try {
      await submitEnquiry({
        fullName: f.name,
        phone: f.mobile,
        email: f.email || undefined,
        serviceInterest: f.course || undefined,
        preferredCountry: country,
        message: f.message || undefined,
        sourceDetail: `westernworldvisaservices.com: ${source}`,
        website: f.website || undefined,
        captchaToken: f["cf-turnstile-response"] || undefined,
      });
      router.push(`/thank-you?${new URLSearchParams({ name: f.name.split(" ")[0] })}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
      setAttempt((n) => n + 1); // captcha tokens are single-use
      setBusy(false);
    }
  };

  const input = `w-full rounded-xl border px-4 py-3 text-sm outline-none transition focus:ring-4 ${
    dark
      ? "border-white/15 bg-white/10 text-white placeholder:text-white/50 focus:border-white/40 focus:ring-white/10"
      : "border-slate-200 bg-white text-slate-900 placeholder:text-slate-400 focus:border-brand focus:ring-brand/10"
  }`;
  const label = `mb-1.5 block text-xs font-semibold ${dark ? "text-white/80" : "text-slate-600"}`;
  const has = (x: Field) => fields.includes(x);

  return (
    <form onSubmit={submit} className="grid gap-4 sm:grid-cols-2">
      {title && (
        <div className="sm:col-span-2">
          <h3 className={`text-xl font-bold ${dark ? "text-white" : "text-slate-900"}`}>{title}</h3>
          {subtitle && <p className={`mt-1 text-sm ${dark ? "text-white/70" : "text-slate-500"}`}>{subtitle}</p>}
        </div>
      )}
      {has("name") && (
        <label className="sm:col-span-2"><span className={label}>Name *</span>
          <input name="name" required maxLength={200} autoComplete="name" className={input} defaultValue={defaults.name} />
        </label>
      )}
      {has("email") && (
        <label><span className={label}>E-Mail *</span>
          <input name="email" type="email" required maxLength={200} autoComplete="email" className={input} defaultValue={defaults.email} />
        </label>
      )}
      {has("mobile") && (
        <label><span className={label}>Mobile *</span>
          <input name="mobile" type="tel" required maxLength={20} autoComplete="tel" placeholder="98765 43210" className={input} />
        </label>
      )}
      {has("course") && (
        <label className="sm:col-span-2"><span className={label}>{courseLabel} *</span>
          <input name="course" required maxLength={200} className={input} defaultValue={defaults.course}
                 placeholder="e.g. IELTS Academic, Study in Canada" />
        </label>
      )}
      {has("message") && (
        <label className="sm:col-span-2"><span className={label}>Message *</span>
          <textarea name="message" required rows={3} maxLength={2000} className={input} defaultValue={defaults.message} />
        </label>
      )}
      {/* Honeypot: hidden from people, bots fill it in. */}
      <input name="website" tabIndex={-1} autoComplete="off" aria-hidden="true" className="absolute -left-[10000px] h-0 w-0" />
      <Turnstile key={attempt} className="sm:col-span-2" />
      {error && <p className="text-sm font-medium text-red-500 sm:col-span-2" role="alert">{error}</p>}
      <button type="submit" disabled={busy}
              className="inline-flex items-center justify-center gap-2 rounded-xl bg-accent px-6 py-3.5 text-sm font-bold text-white shadow-lg shadow-accent/25 transition hover:bg-accent-dark disabled:opacity-60 sm:col-span-2">
        <Send className="size-4" /> {busy ? "Sending…" : submitLabel}
      </button>
      <p className={`text-xs sm:col-span-2 ${dark ? "text-white/60" : "text-slate-400"}`}>
        We confirm by email and WhatsApp, and someone from our team will get in touch with you.
      </p>
    </form>
  );
}
