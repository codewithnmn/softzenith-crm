"use client";

import { useRouter } from "next/navigation";
import type { FormEvent } from "react";
import { Mail } from "lucide-react";

/**
 * "Stay connected with us". A lead needs a mobile number, so subscribing opens the enquiry form with the email filled
 * in (owner decision, 26 Sep 2026).
 */
export default function NewsletterBox() {
  const router = useRouter();
  const submit = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const email = String(new FormData(e.currentTarget).get("email") ?? "");
    router.push(`/contact-us?${new URLSearchParams({ email, course: "Newsletter / stay connected" })}#enquiry`);
  };
  return (
    <div className="rounded-3xl bg-brand-dark p-6 text-white shadow-xl">
      <Mail className="size-8 text-sky" />
      <h3 className="mt-3 text-xl font-bold text-white">Stay connected with us</h3>
      <p className="mt-1 text-sm text-white/70">Get updates on intakes, IELTS batches and visa news.</p>
      <form onSubmit={submit} className="mt-4 flex gap-2">
        <input name="email" type="email" required placeholder="Enter Your Email Address"
               className="min-w-0 flex-1 rounded-xl border border-white/15 bg-white/10 px-4 py-3 text-sm text-white outline-none placeholder:text-white/50 focus:border-white/40" />
        <button className="rounded-xl bg-accent px-4 py-3 text-sm font-bold text-white hover:bg-accent-dark">Subscribe</button>
      </form>
    </div>
  );
}
