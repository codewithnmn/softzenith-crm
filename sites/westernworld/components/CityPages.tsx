import Link from "next/link";
import { CheckCircle2, MapPin, Phone } from "lucide-react";
import WithCallback from "@/components/WithCallback";
import { CtaBand, Gallery, PageHero, Section, SectionTitle } from "@/components/blocks";
import { ieltsCityCopy, pteCopy } from "@/content/copy";
import { cities, media, type City } from "@/lib/content";
import { SITE, tel } from "@/lib/site";

/** Directory of the "IELTS / PTE classes in <city>" pages (all Haryana towns the old site listed). */
export function CityDirectory({ kind }: { kind: "ielts" | "pte" }) {
  const label = kind === "ielts" ? "IELTS" : "PTE";
  return (
    <Section className="bg-slate-50">
      <SectionTitle eyebrow="Near you" title={`${label} classes across Haryana`} center
                    intro="Students join us from all these towns, in our Rohtak classroom or online." />
      <div className="grid grid-cols-2 gap-2 sm:grid-cols-3 lg:grid-cols-5">
        {cities[kind].map((c) => (
          <Link key={c.slug} href={`/${kind}-classes/${c.slug}`}
                className="flex items-center gap-2 rounded-xl bg-white px-3 py-2.5 text-sm font-medium text-slate-700 shadow-sm ring-1 ring-slate-100 hover:text-brand hover:ring-brand">
            <MapPin className="size-3.5 shrink-0 text-accent" /> {label} classes in {titleCase(c.city)}
          </Link>
        ))}
      </div>
    </Section>
  );
}

export const titleCase = (s: string) => s.toLowerCase().replace(/(^|[\s-])\S/g, (m) => m.toUpperCase());

function CallUs() {
  return (
    <p className="mt-4 flex flex-wrap items-center gap-3 font-semibold text-slate-800">
      <Phone className="size-4 text-accent" />
      {SITE.phones.map((p) => <a key={p} href={tel(p)} className="text-brand hover:underline">{p}</a>)}
    </p>
  );
}

export function IeltsCityPage({ city }: { city: City }) {
  const name = titleCase(city.city);
  const copy = ieltsCityCopy(name);
  return (
    <>
      <PageHero title={`IELTS Coaching Classes in ${name} & Study Abroad`} image="/images/slide1.jpg"
                crumbs={[{ label: "IELTS Classes", href: "/ielts-classes-india" }, { label: name }]} />
      <WithCallback source={`IELTS classes in ${name}`} course={`IELTS coaching (${name})`}>
        <div className="prose-copy text-slate-600">{copy.intro.map((p) => <p key={p.slice(0, 40)}>{p}</p>)}</div>
        <div className="mt-6 grid gap-4 sm:grid-cols-2">
          {copy.features.map(([t, d]) => (
            <div key={t} className="rounded-2xl border border-slate-100 bg-white p-5 shadow-sm">
              <h3 className="flex items-center gap-2 font-bold"><CheckCircle2 className="size-5 text-accent" />{t}</h3>
              <p className="mt-2 text-sm leading-relaxed text-slate-500">{d}</p>
            </div>
          ))}
        </div>
        <div className="mt-8 rounded-3xl bg-brand-light p-6">
          <p className="text-slate-700">{copy.closing}</p>
          <CallUs />
        </div>
      </WithCallback>
      <Gallery images={media.galleries.ieltsClasses} />
      <CityDirectory kind="ielts" />
      <CtaBand course={`IELTS coaching (${name})`} />
    </>
  );
}

export function PteContent({ city }: { city?: string }) {
  const copy = pteCopy(city);
  return (
    <>
      <div className="prose-copy text-slate-600"><p>{copy.intro}</p></div>
      <h2 className="mt-8 text-2xl font-bold">{copy.whyTitle}</h2>
      <p className="mt-2 text-slate-600">{copy.whyIntro}</p>
      <div className="mt-6 grid gap-4 sm:grid-cols-2">
        {copy.reasons.map(([t, d]) => (
          <div key={t} className="rounded-2xl border border-slate-100 bg-white p-5 shadow-sm">
            <h3 className="flex items-center gap-2 font-bold"><CheckCircle2 className="size-5 text-accent" />{t}</h3>
            <p className="mt-2 text-sm leading-relaxed text-slate-500">{d}</p>
          </div>
        ))}
      </div>
      <h2 className="mt-10 text-2xl font-bold">Comprehensive PTE Coaching</h2>
      <p className="mt-2 text-slate-600">{copy.modulesIntro}</p>
      <div className="mt-4 grid gap-4 sm:grid-cols-2">
        {copy.modules.map(([t, d]) => (
          <div key={t} className="rounded-2xl bg-slate-50 p-5">
            <h3 className="font-bold text-brand">{t}</h3>
            <p className="mt-1 text-sm text-slate-600">{d}</p>
          </div>
        ))}
      </div>
      <h2 className="mt-10 text-2xl font-bold">{copy.gatewayTitle}</h2>
      <p className="mt-2 text-slate-600">{copy.gateway}</p>
      <h2 className="mt-10 text-2xl font-bold">{copy.benefitsTitle}</h2>
      <ul className="mt-4 space-y-2">
        {copy.benefits.map((b) => <li key={b} className="flex gap-2 text-slate-600"><CheckCircle2 className="mt-0.5 size-5 shrink-0 text-accent" />{b}</li>)}
      </ul>
      {copy.enrol && (
        <div className="mt-8 rounded-3xl bg-brand-light p-6">
          <h3 className="text-lg font-bold text-brand">{copy.enrol.title}</h3>
          <p className="mt-1 text-slate-700">{copy.enrol.text}</p>
          <CallUs />
        </div>
      )}
      <p className="mt-8 text-lg font-semibold text-slate-800">{copy.closing}</p>
    </>
  );
}

export function PteCityPage({ city }: { city: City }) {
  const name = titleCase(city.city);
  return (
    <>
      <PageHero title={`PTE Coaching Classes in ${name} & Study Abroad`} image="/images/slide3.jpg"
                crumbs={[{ label: "PTE Classes", href: "/pte-classes-india" }, { label: name }]} />
      <WithCallback source={`PTE classes in ${name}`} course={`PTE coaching (${name}) – free 3-day trial`}>
        <PteContent city={name} />
      </WithCallback>
      <Gallery images={media.galleries.ieltsClasses} title="Our students" />
      <CityDirectory kind="pte" />
      <CtaBand course={`PTE coaching (${name})`} />
    </>
  );
}
