import Image from "next/image";
import Link from "next/link";
import type { ReactNode } from "react";
import { ArrowRight, CalendarDays, CheckCircle2, ChevronRight, Clock, Phone } from "lucide-react";
import EnquiryForm from "@/components/EnquiryForm";
import type { Package, TestSection } from "@/content/copy";
import { SITE, enquire, tel } from "@/lib/site";

/** Page title band with breadcrumb, used on every inner page. */
export function PageHero({ title, crumbs = [], subtitle, image = "/images/banner8.jpg" }: {
  title: string;
  crumbs?: { label: string; href?: string }[];
  subtitle?: ReactNode;
  image?: string;
}) {
  return (
    <section className="relative overflow-hidden bg-brand-dark">
      <Image src={image} alt="" fill priority className="object-cover opacity-25" sizes="100vw" />
      <div className="absolute inset-0 bg-gradient-to-r from-brand-dark via-brand-dark/90 to-brand/60" />
      <div className="relative mx-auto max-w-7xl px-4 py-16 lg:py-20">
        <nav className="mb-3 flex flex-wrap items-center gap-1 text-sm text-white/60">
          <Link href="/" className="hover:text-white">Home</Link>
          {crumbs.map((c) => (
            <span key={c.label} className="flex items-center gap-1">
              <ChevronRight className="size-3.5" />
              {c.href ? <Link href={c.href} className="hover:text-white">{c.label}</Link> : <span className="text-white/90">{c.label}</span>}
            </span>
          ))}
        </nav>
        <h1 className="max-w-3xl text-3xl font-extrabold tracking-tight text-white sm:text-5xl">{title}</h1>
        {subtitle && <p className="mt-4 max-w-2xl text-lg text-white/75">{subtitle}</p>}
      </div>
    </section>
  );
}

export function SectionTitle({ eyebrow, title, intro, center = false, light = false }: {
  eyebrow?: string; title: ReactNode; intro?: ReactNode; center?: boolean; light?: boolean;
}) {
  return (
    <div className={`mb-10 ${center ? "mx-auto max-w-2xl text-center" : "max-w-3xl"}`}>
      {eyebrow && <p className="mb-2 text-xs font-bold tracking-[0.2em] text-accent uppercase">{eyebrow}</p>}
      <h2 className={`text-3xl font-extrabold tracking-tight sm:text-4xl ${light ? "text-white" : ""}`}>{title}</h2>
      {intro && <p className={`mt-4 text-base leading-relaxed ${light ? "text-white/75" : "text-slate-500"}`}>{intro}</p>}
    </div>
  );
}

export function Section({ children, className = "", id }: { children: ReactNode; className?: string; id?: string }) {
  return (
    <section id={id} className={`py-16 lg:py-24 ${className}`}>
      <div className="mx-auto max-w-7xl px-4">{children}</div>
    </section>
  );
}

/** Course packages with "Book Now" going to the enquiry form, pre-filled with the package. */
export function Packages({ packages, source }: { packages: Package[]; source: string }) {
  return (
    <div className="grid gap-6 md:grid-cols-3">
      {packages.map((p, i) => (
        <article key={p.name}
                 className={`group relative flex flex-col overflow-hidden rounded-3xl border bg-white shadow-sm transition hover:-translate-y-1 hover:shadow-xl ${i === 1 ? "border-brand ring-2 ring-brand/10" : "border-slate-100"}`}>
          {p.image && (
            <div className="relative h-44 overflow-hidden">
              <Image src={p.image} alt={p.name} fill className="object-cover transition duration-500 group-hover:scale-105" sizes="(min-width: 768px) 33vw, 100vw" />
            </div>
          )}
          <div className="flex flex-1 flex-col p-6">
            <span className="mb-3 w-fit rounded-full bg-brand-light px-3 py-1 text-xs font-bold text-brand">{p.weeksLabel}</span>
            <h3 className="text-lg font-bold">{p.name}</h3>
            {p.price && <p className="mt-2 text-3xl font-extrabold text-accent">{p.price}</p>}
            <p className="mt-2 flex-1 text-sm text-slate-500">{p.description}</p>
            <dl className="mt-5 grid grid-cols-2 gap-3 rounded-2xl bg-slate-50 p-4 text-sm">
              <div><dt className="flex items-center gap-1 text-xs text-slate-400"><CalendarDays className="size-3.5" />Course Duration</dt><dd className="font-bold text-slate-800">{p.duration}</dd></div>
              <div><dt className="flex items-center gap-1 text-xs text-slate-400"><Clock className="size-3.5" />Personal Training</dt><dd className="font-bold text-slate-800">{p.training}</dd></div>
            </dl>
            <Link href={enquire(p.name)} data-source={source}
                  className="mt-5 inline-flex items-center justify-center gap-2 rounded-xl bg-brand px-5 py-3 text-sm font-bold text-white transition group-hover:bg-accent">
              Book Now <ArrowRight className="size-4" />
            </Link>
          </div>
        </article>
      ))}
    </div>
  );
}

/** "What is the IELTS … test like?" — four sections and scoring. */
export function TestDetails({ test }: {
  test: { title: string; question: string; intro: string; sections: TestSection[]; scores: string };
}) {
  return (
    <div>
      <h3 className="text-2xl font-bold">{test.title}</h3>
      <p className="mt-2 font-semibold text-brand">{test.question}</p>
      <p className="mt-2 text-slate-600">{test.intro}</p>
      <div className="mt-6 grid gap-4 sm:grid-cols-2">
        {test.sections.map((s) => (
          <div key={s.name} className="rounded-2xl border border-slate-100 bg-white p-5 shadow-sm">
            <h4 className="text-lg font-bold">{s.name}</h4>
            {s.summary && <p className="mt-1 text-sm font-semibold text-accent">{s.summary}</p>}
            <ul className="mt-3 space-y-2 text-sm text-slate-600">
              {s.points.map((p) => (
                <li key={p} className="flex gap-2"><CheckCircle2 className="mt-0.5 size-4 shrink-0 text-brand" />{p}</li>
              ))}
            </ul>
          </div>
        ))}
      </div>
      <div className="mt-4 rounded-2xl bg-brand-light p-5">
        <h4 className="font-bold text-brand">Level and scores</h4>
        <p className="mt-1 text-sm text-slate-700">{test.scores}</p>
      </div>
    </div>
  );
}

/** Student photo wall. */
export function Gallery({ images, title = "IELTS Students", intro }: { images: string[]; title?: string; intro?: string }) {
  if (images.length === 0) return null;
  return (
    <Section className="bg-slate-50">
      <SectionTitle eyebrow="Our results" title={title} intro={intro} center />
      <div className="columns-2 gap-4 sm:columns-3 lg:columns-4">
        {images.map((src) => (
          <div key={src} className="mb-4 break-inside-avoid overflow-hidden rounded-2xl bg-white shadow-sm">
            <Image src={src} alt="Western World Visa Services student" width={400} height={400} className="h-auto w-full" sizes="(min-width: 1024px) 25vw, 50vw" />
          </div>
        ))}
      </div>
    </Section>
  );
}

/** Sticky side form used on content pages ("Request call back"). */
export function CallbackCard({ source, course, country, title = "Request call back" }: {
  source: string; course?: string; country?: string; title?: string;
}) {
  return (
    <aside className="rounded-3xl bg-brand p-6 shadow-xl lg:sticky lg:top-32">
      <EnquiryForm dark title={title} subtitle="Leave your details and our counsellor will call you." source={source}
                   country={country} defaults={{ message: course ? `I am interested in ${course}.` : undefined }} />
      <div className="mt-5 border-t border-white/10 pt-4 text-sm text-white/80">
        <p className="mb-2 font-semibold text-white">Prefer to call?</p>
        {SITE.phones.map((p) => (
          <a key={p} href={tel(p)} className="flex items-center gap-2 py-0.5 hover:text-white"><Phone className="size-4" />{p}</a>
        ))}
      </div>
    </aside>
  );
}

export function CtaBand({ title = "Ready to start your journey abroad?", text = "Talk to our counsellors today. Someone from our team will get in touch with you.", course }: {
  title?: string; text?: string; course?: string;
}) {
  return (
    <section className="bg-gradient-to-r from-accent to-accent-dark">
      <div className="mx-auto flex max-w-7xl flex-col items-start justify-between gap-6 px-4 py-12 md:flex-row md:items-center">
        <div>
          <h2 className="text-2xl font-extrabold text-white sm:text-3xl">{title}</h2>
          <p className="mt-2 text-white/85">{text}</p>
        </div>
        <div className="flex flex-wrap gap-3">
          <Link href={enquire(course)} className="rounded-xl bg-white px-6 py-3.5 text-sm font-bold text-accent shadow-lg transition hover:bg-slate-100">Enquire now</Link>
          <a href={tel(SITE.phones[0])} className="rounded-xl border-2 border-white/60 px-6 py-3 text-sm font-bold text-white transition hover:bg-white/10">Call {SITE.phones[0]}</a>
        </div>
      </div>
    </section>
  );
}

export function Feature({ icon, title, children }: { icon: ReactNode; title: string; children: ReactNode }) {
  return (
    <div className="rounded-3xl border border-slate-100 bg-white p-6 shadow-sm transition hover:shadow-lg">
      <div className="mb-4 flex size-12 items-center justify-center rounded-2xl bg-brand-light text-brand [&_svg]:size-6">{icon}</div>
      <h3 className="text-lg font-bold">{title}</h3>
      <div className="mt-2 text-sm leading-relaxed text-slate-500">{children}</div>
    </div>
  );
}
