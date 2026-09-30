import Link from "next/link";
import { enquire } from "@/lib/site";

export default function NotFound() {
  return (
    <section className="mx-auto max-w-xl px-4 py-24 text-center">
      <p className="text-6xl font-extrabold text-brand/20">404</p>
      <h1 className="mt-4 text-3xl font-extrabold">This page has moved</h1>
      <p className="mt-3 text-slate-600">We have a new website. Find what you need from the menu, or ask us and someone from our team will get in touch with you.</p>
      <div className="mt-8 flex justify-center gap-3">
        <Link href="/" className="rounded-xl bg-brand px-6 py-3 text-sm font-bold text-white">Home</Link>
        <Link href={enquire()} className="rounded-xl bg-accent px-6 py-3 text-sm font-bold text-white">Contact us</Link>
      </div>
    </section>
  );
}
