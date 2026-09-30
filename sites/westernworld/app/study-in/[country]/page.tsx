import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { ArrowRight, MapPin } from "lucide-react";
import EnquiryForm from "@/components/EnquiryForm";
import UniImage from "@/components/UniImage";
import { CtaBand, PageHero, Section } from "@/components/blocks";
import { countries, country as findCountry, universitiesOf } from "@/lib/content";
import { enquire } from "@/lib/site";

export function generateStaticParams() {
  return countries.map((c) => ({ country: c.slug }));
}

export async function generateMetadata({ params }: PageProps<"/study-in/[country]">): Promise<Metadata> {
  const c = findCountry((await params).country);
  return { title: c ? `Study in ${c.name}` : "Study abroad" };
}

export default async function CountryPage({ params }: PageProps<"/study-in/[country]">) {
  const c = findCountry((await params).country);
  if (!c) notFound();
  const unis = universitiesOf(c);

  return (
    <>
      <PageHero title={`Study in ${c.name}`} crumbs={[{ label: "Global Education" }, { label: `Study in ${c.name}` }]}
                subtitle={unis.length > 0
                  ? `${unis.length} partner universit${unis.length === 1 ? "y" : "ies"} and colleges. Our counsellors help you choose, apply and get your visa.`
                  : `Planning to study in ${c.name}? Our counsellors will guide you on universities, admissions and the visa.`} />

      {unis.length > 0 ? (
        <Section>
          <div className="grid gap-6 md:grid-cols-2">
            {unis.map((u) => (
              <article key={u.uid} className="group flex flex-col overflow-hidden rounded-3xl border border-slate-100 bg-white shadow-sm transition hover:shadow-xl sm:flex-row">
                <UniImage src={u.cardImage ?? u.image} alt={u.name} className="h-48 shrink-0 sm:h-auto sm:w-56" sizes="(min-width: 640px) 224px, 100vw" />
                <div className="flex flex-1 flex-col p-6">
                  <h2 className="text-lg font-bold"><Link href={`/universities/${u.uid}`} className="hover:text-brand">{u.cardTitle ?? u.name}</Link></h2>
                  <p className="mt-1 flex items-center gap-1 text-xs font-semibold text-accent"><MapPin className="size-3.5" />{u.location || `University in ${c.name}`}</p>
                  {u.blurb && <p className="mt-3 line-clamp-3 flex-1 text-sm text-slate-500">{u.blurb}</p>}
                  <div className="mt-5 flex flex-wrap gap-2">
                    <Link href={`/universities/${u.uid}`} className="inline-flex items-center gap-1 rounded-xl bg-brand-light px-4 py-2 text-sm font-bold text-brand hover:bg-brand hover:text-white">
                      Read More <ArrowRight className="size-4" />
                    </Link>
                    <Link href={enquire(u.cardTitle ?? u.name, c.name)} className="rounded-xl bg-accent px-4 py-2 text-sm font-bold text-white hover:bg-accent-dark">Enroll Now</Link>
                  </div>
                </div>
              </article>
            ))}
          </div>
        </Section>
      ) : (
        <Section>
          <div className="mx-auto max-w-2xl rounded-3xl bg-white p-8 shadow-xl ring-1 ring-slate-100">
            <EnquiryForm title={`Talk to us about studying in ${c.name}`} source={`Study in ${c.name}`} country={c.name}
                         fields={["name", "email", "mobile", "message"]} defaults={{ message: `I would like to study in ${c.name}.` }} />
          </div>
        </Section>
      )}
      <CtaBand title={`Ready to study in ${c.name}?`} course={`Study in ${c.name}`} />
    </>
  );
}
