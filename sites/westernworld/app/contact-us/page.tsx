import type { Metadata } from "next";
import { Mail, MapPin, Phone } from "lucide-react";
import EnquiryForm from "@/components/EnquiryForm";
import SocialIcons from "@/components/SocialIcons";
import { PageHero, Section } from "@/components/blocks";
import { SITE, tel } from "@/lib/site";

export const metadata: Metadata = { title: "Contact Us" };

const first = (v: string | string[] | undefined) => (Array.isArray(v) ? v[0] : v);

export default async function ContactPage({ searchParams }: PageProps<"/contact-us">) {
  const q = await searchParams;
  const course = first(q.course);
  const country = first(q.country);

  return (
    <>
      <PageHero title="Contact Us" crumbs={[{ label: "Contact Us" }]}
                subtitle="Call, WhatsApp, visit our office, or ask for a call back. Someone from our team will get in touch with you." />
      <Section>
        <div className="grid gap-10 lg:grid-cols-[1fr_1.2fr]">
          <div className="space-y-5">
            <div className="rounded-3xl border border-slate-100 p-6 shadow-sm">
              <MapPin className="size-7 text-accent" />
              <h2 className="mt-3 text-lg font-bold">Office Address</h2>
              <p className="mt-1 text-slate-600">{SITE.address}</p>
            </div>
            <div className="rounded-3xl border border-slate-100 p-6 shadow-sm">
              <Phone className="size-7 text-accent" />
              <h2 className="mt-3 text-lg font-bold">Call Now</h2>
              {SITE.phones.map((p) => <a key={p} href={tel(p)} className="block text-slate-600 hover:text-brand">{p}</a>)}
            </div>
            <div className="rounded-3xl border border-slate-100 p-6 shadow-sm">
              <Mail className="size-7 text-accent" />
              <h2 className="mt-3 text-lg font-bold">Email</h2>
              <a href={`mailto:${SITE.email}`} className="text-slate-600 hover:text-brand">{SITE.email}</a>
              <div className="mt-4 text-brand [&_a]:bg-brand-light"><SocialIcons /></div>
            </div>
            <iframe title="Map to Western World Visa Services, Rohtak" loading="lazy" className="h-64 w-full rounded-3xl border-0"
                    src={`https://www.google.com/maps?q=${encodeURIComponent(SITE.address)}&output=embed`} />
          </div>
          <div id="enquiry" className="scroll-mt-32 rounded-3xl bg-white p-6 shadow-xl ring-1 ring-slate-100 sm:p-8">
            <EnquiryForm
              key={`${course}-${first(q.email)}`}
              title="Call Back Now"
              subtitle="Fill in your details and we will call you back."
              source={course ? `Call back (${course})` : "Call back (contact page)"}
              fields={["name", "email", "mobile", "course"]}
              defaults={{ course, email: first(q.email) }}
              country={country}
              submitLabel="Request a call back"
            />
          </div>
        </div>
      </Section>
    </>
  );
}
