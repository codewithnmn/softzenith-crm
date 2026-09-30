import type { Metadata } from "next";
import Link from "next/link";
import { CheckCircle2, Mail, MessageCircle, Phone } from "lucide-react";
import { SITE, tel } from "@/lib/site";

export const metadata: Metadata = { title: "Thank you", robots: { index: false } };

const first = (v: string | string[] | undefined) => (Array.isArray(v) ? v[0] : v);

/** Where every enquiry form lands after the CRM has accepted it. */
export default async function ThankYouPage({ searchParams }: PageProps<"/thank-you">) {
  const q = await searchParams;
  const name = first(q.name);
  return (
    <section className="bg-gradient-to-b from-brand-light to-white">
      <div className="mx-auto max-w-2xl px-4 py-20 text-center">
        <div className="mx-auto flex size-20 items-center justify-center rounded-full bg-emerald-100">
          <CheckCircle2 className="size-11 text-emerald-600" />
        </div>
        <h1 className="mt-6 text-3xl font-extrabold sm:text-4xl">Thank you{name ? `, ${name}` : ""}!</h1>
        <p className="mt-4 text-lg text-slate-600">
          We have received your enquiry. Someone from our team will get in touch with you shortly.
        </p>
        <div className="mt-10 grid gap-4 text-left sm:grid-cols-3">
          <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
            <Mail className="size-6 text-brand" />
            <p className="mt-2 text-sm font-semibold">Check your inbox</p>
            <p className="text-xs text-slate-500">We have emailed you a confirmation.</p>
          </div>
          <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
            <MessageCircle className="size-6 text-emerald-600" />
            <p className="mt-2 text-sm font-semibold">WhatsApp</p>
            <p className="text-xs text-slate-500">A confirmation is on its way on WhatsApp.</p>
          </div>
          <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
            <Phone className="size-6 text-accent" />
            <p className="mt-2 text-sm font-semibold">In a hurry?</p>
            <a href={tel(SITE.phones[0])} className="text-xs text-slate-500 hover:text-brand">Call {SITE.phones[0]}</a>
          </div>
        </div>
        <div className="mt-10 flex flex-wrap justify-center gap-3">
          <Link href="/" className="rounded-xl bg-brand px-6 py-3 text-sm font-bold text-white hover:bg-brand-dark">Back to home</Link>
          <Link href="/study-in/canada" className="rounded-xl border border-slate-200 px-6 py-3 text-sm font-bold text-slate-700 hover:border-brand">Explore destinations</Link>
        </div>
      </div>
    </section>
  );
}
