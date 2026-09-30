import type { Metadata } from "next";
import WithCallback from "@/components/WithCallback";
import { CtaBand, Gallery, PageHero, Packages, Section, SectionTitle, TestDetails } from "@/components/blocks";
import { IELTS_ACADEMIC_TEST, IELTS_ACADEMY_PACKAGES } from "@/content/copy";
import { media } from "@/lib/content";

export const metadata: Metadata = { title: "IELTS Academy | IELTS Coaching | IELTS Preparation | IELTS For Canada" };

/** The menu's CELPIP, OET, DUOLINGO, GRE and GMAT entries also land here, as on the old site. */
const ALSO = ["CELPIP", "OET", "DUOLINGO", "GRE", "GMAT"];

export default function IeltsAcademyPage() {
  return (
    <>
      <PageHero title="IELTS Academy" crumbs={[{ label: "Services" }, { label: "IELTS Academy" }]} image="/images/slide2.jpg"
                subtitle="IELTS Academic coaching, plus preparation for CELPIP, OET, Duolingo, GRE and GMAT." />
      <Section className="bg-slate-50">
        <SectionTitle eyebrow="Packages" title="IELTS Academy Packages" center />
        <Packages packages={IELTS_ACADEMY_PACKAGES} source="IELTS Academy packages" />
        <div className="mt-10 flex flex-wrap justify-center gap-3">
          <span className="text-sm font-semibold text-slate-500">We also coach for:</span>
          {ALSO.map((x) => <span key={x} className="rounded-full bg-white px-4 py-1.5 text-sm font-bold text-brand shadow-sm ring-1 ring-slate-100">{x}</span>)}
        </div>
      </Section>
      <WithCallback source="Request call back (IELTS Academy)" course="IELTS Academic">
        <SectionTitle eyebrow="IELTS course" title="About the IELTS Academic test" />
        <TestDetails test={IELTS_ACADEMIC_TEST} />
      </WithCallback>
      <Gallery images={media.galleries.ieltsAcademy} />
      <CtaBand course="IELTS Academic" />
    </>
  );
}
