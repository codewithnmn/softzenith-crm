import type { Metadata } from "next";
import WithCallback from "@/components/WithCallback";
import { CtaBand, Gallery, PageHero, Packages, Section, SectionTitle, TestDetails } from "@/components/blocks";
import { IELTS_GENERAL_PACKAGES, IELTS_GENERAL_TEST } from "@/content/copy";
import { media } from "@/lib/content";

export const metadata: Metadata = {
  title: "IELTS General | IELTS Coaching | IELTS Preparation | IELTS For Canada",
  description: "The General version of IELTS is easier than the academic version. All candidates do the same Listening and Speaking sections",
};

export default function IeltsGeneralPage() {
  return (
    <>
      <PageHero title="IELTS General" crumbs={[{ label: "Services" }, { label: "IELTS General" }]} image="/images/slide1.jpg"
                subtitle="Online and classroom coaching for the IELTS General Training test." />
      <Section className="bg-slate-50">
        <SectionTitle eyebrow="Packages" title="IELTS General Packages" center />
        <Packages packages={IELTS_GENERAL_PACKAGES} source="IELTS General packages" />
      </Section>
      <WithCallback source="Request call back (IELTS General)" course="IELTS General">
        <SectionTitle eyebrow="IELTS test" title="About the IELTS General test" />
        <TestDetails test={IELTS_GENERAL_TEST} />
      </WithCallback>
      <Gallery images={media.galleries.ieltsGeneral} />
      <CtaBand course="IELTS General" />
    </>
  );
}
