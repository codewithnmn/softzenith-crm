import type { Metadata } from "next";
import Link from "next/link";
import { ArrowRight } from "lucide-react";
import { CtaBand, Gallery, PageHero, Packages, Section, SectionTitle } from "@/components/blocks";
import { IELTS_ACADEMIC_BLURB, IELTS_GENERAL_BLURB, ONLINE_PACKAGES } from "@/content/copy";
import { media } from "@/lib/content";

export const metadata: Metadata = { title: "Online Courses" };

export default function OnlineCoursesPage() {
  return (
    <>
      <PageHero title="Online Courses" crumbs={[{ label: "IELTS Academic" }, { label: "Online Courses" }]} image="/images/slide3.jpg"
                subtitle="Live online IELTS preparation with personal training hours." />
      <Section className="bg-slate-50">
        <SectionTitle eyebrow="Online" title="Online IELTS programs" center />
        <Packages packages={ONLINE_PACKAGES} source="Online course packages" />
      </Section>
      <Section>
        <SectionTitle eyebrow="IELTS course" title="Which IELTS do you need?" center />
        <div className="grid gap-6 md:grid-cols-2">
          {/* The old "Read More" links pointed to a missing German-language page; they go to the right course now. */}
          {[["IELTS General", IELTS_GENERAL_BLURB, "/ielts-general"], ["IELTS Academic", IELTS_ACADEMIC_BLURB, "/ielts-academy"]].map(([t, d, href]) => (
            <div key={t} className="rounded-3xl border border-slate-100 p-8 shadow-sm">
              <h3 className="text-xl font-bold">{t}</h3>
              <p className="mt-2 text-slate-600">{d}</p>
              <Link href={href} className="mt-4 inline-flex items-center gap-1 font-bold text-accent">Read More <ArrowRight className="size-4" /></Link>
            </div>
          ))}
        </div>
      </Section>
      <Gallery images={media.galleries.ieltsGeneral} />
      <CtaBand course="IELTS online course" />
    </>
  );
}
