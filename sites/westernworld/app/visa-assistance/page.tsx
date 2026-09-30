import type { Metadata } from "next";
import Link from "next/link";
import { FileCheck2, Globe2, GraduationCap } from "lucide-react";
import WithCallback from "@/components/WithCallback";
import { Feature, PageHero } from "@/components/blocks";
import { countries } from "@/lib/content";

export const metadata: Metadata = { title: "Visa Assistance" };

/**
 * The old page was titled "UKVI-IELTS" with only a call-back form (a copy-paste slip on the old site). Kept as the
 * Visa Assistance page with that form, plus the visa-support wording from the old home page.
 */
export default function VisaAssistancePage() {
  return (
    <>
      <PageHero title="Visa Assistance" crumbs={[{ label: "Immigration Services" }, { label: "Visa Assistance" }]}
                subtitle="WWVS could help clients with visa applications, requirements and processing." />
      <WithCallback source="Request call back (visa assistance)" course="Visa Assistance">
        <div className="prose-copy text-slate-600">
          <p>The requirement for a visa will depend on the country the clients wants to study in. Equipped with the latest information about student migration, migration agents will be able to help clients achieve their student visas.</p>
        </div>
        <div className="mt-6 grid gap-5 sm:grid-cols-2">
          <Feature icon={<FileCheck2 />} title="Visa Support & Filing">Western World Visa Services help in providing visa support and filling process from renowned universities</Feature>
          <Feature icon={<GraduationCap />} title="Career Counselling">Western World Visa Services offer Career Counselling all over the world most influential universities</Feature>
        </div>
        <h2 className="mt-10 mb-4 text-xl font-bold">Student visa destinations</h2>
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
          {countries.map((c) => (
            <Link key={c.slug} href={`/study-in/${c.slug}`}
                  className="flex items-center gap-2 rounded-2xl border border-slate-100 p-3 text-sm font-semibold hover:border-brand hover:text-brand">
              <Globe2 className="size-4 text-accent" /> {c.name}
            </Link>
          ))}
        </div>
      </WithCallback>
    </>
  );
}
