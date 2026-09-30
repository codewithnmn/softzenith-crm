import type { Metadata } from "next";
import WithCallback from "@/components/WithCallback";
import { CtaBand, Gallery, PageHero } from "@/components/blocks";
import { media } from "@/lib/content";

export const metadata: Metadata = { title: "TOEFL Preparation Specialization" };

export default function ToeflPage() {
  return (
    <>
      <PageHero title="TOEFL Preparation Specialization" crumbs={[{ label: "Services" }, { label: "TOEFL" }]} image="/images/slide2.jpg" />
      <WithCallback source="Request call back (TOEFL)" course="TOEFL">
        <div className="prose-copy text-slate-600">
          <p>The Test of English as a Foreign Language (TOEFL) is a premier English-language proficiency test that is conducted by the Educational Testing Service (ETS). The TOEFL exam is conducted in computer-based mode, known as TOEFL iBT. The alternative paper-delivered mode is only available for locations where testing via the internet is not available. TOEFL iBT test measures all 4 communication skills — reading, listening, speaking, and writing, and is preferred by educational institutions. Over 11,000 universities and institutions spread across 150 countries accept TOEFL scores for admission, jobs, and immigration purposes. The computer-based test is administered more than 60 times a year and can be taken at designated test centres. Availability of test dates and exam centres can be checked at the time of TOEFL registration.</p>
        </div>
        <div className="mt-6 rounded-3xl bg-brand-light p-6">
          <h2 className="text-xl font-bold text-brand">TOEFL Eligibility Criteria</h2>
          <p className="mt-2 text-slate-700">There are no specific eligibility criteria for the TOEFL iBT exam as it is an English language proficiency test and any person who wants to study, work or immigrate abroad can take the TOEFL exam. That being said, the applicant must meet the institute-specific TOEFL eligibility criteria or minimum section-wise or overall marks in TOEFL as mandated by the institute applied for admission.</p>
        </div>
      </WithCallback>
      <Gallery images={media.galleries.toefl} title="Student Gallery" />
      <CtaBand course="TOEFL" />
    </>
  );
}
