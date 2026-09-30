import type { Metadata } from "next";
import WithCallback from "@/components/WithCallback";
import { CtaBand, Gallery, PageHero } from "@/components/blocks";
import { media } from "@/lib/content";

export const metadata: Metadata = { title: "IELTS tests for UK Visas and Immigration" };

export default function UkviIeltsPage() {
  return (
    <>
      <PageHero title="IELTS tests for UK Visas and Immigration" crumbs={[{ label: "Services" }, { label: "UKVI-IELTS" }]} image="/images/slide1.jpg" />
      <WithCallback source="Request call back (UKVI-IELTS)" course="UKVI-IELTS" country="UK">
        <div className="prose-copy text-slate-600">
          <p>IELTS Academic, IELTS General Training and IELTS Life Skills are accepted by the UK Visas and Immigration (UKVI) as proof of English proficiency for those wishing to live, work and study in the UK. IELTS tests for UK Visas and Immigration are managed by the IELTS Partners, which comprises The British Council, IDP: IELTS Australia and Cambridge Assessment English.</p>
        </div>
        <div className="mt-6 rounded-3xl bg-brand-light p-6">
          <h2 className="text-xl font-bold text-brand">IELTS for a Tier 4 Student Visa (without a presessional)</h2>
          <p className="mt-2 text-slate-700">If you are applying for a Tier 4 Student Visa to enter directly onto a bachelor or postgraduate degree at a university that is a Tier 4 Sponsor, you must meet the English language level set by the institution. All British universities and colleges accept IELTS results. Tier 4 Student Visa applicants can apply to the UK universities listed here with an IELTS result from more than 1,600 IELTS test locations worldwide. Please note that some institutions may have additional requirements.</p>
        </div>
      </WithCallback>
      <Gallery images={media.galleries.ieltsGeneral} />
      <CtaBand course="UKVI-IELTS" />
    </>
  );
}
