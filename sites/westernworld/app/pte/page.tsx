import type { Metadata } from "next";
import Link from "next/link";
import { ArrowRight, BookOpen, Headphones, Mic, PenLine } from "lucide-react";
import WithCallback from "@/components/WithCallback";
import { CtaBand, Gallery, PageHero } from "@/components/blocks";
import { media } from "@/lib/content";

export const metadata: Metadata = { title: "PTE - Pearson English Language Tests" };

export default function PtePage() {
  return (
    <>
      <PageHero title="PTE – Pearson English Language Tests" crumbs={[{ label: "Services" }, { label: "PTE" }]} image="/images/slide3.jpg" />
      <WithCallback source="Request call back (PTE)" course="PTE">
        <h2 className="text-2xl font-bold">Pearson English Language Tests</h2>
        <div className="prose-copy mt-4 text-slate-600">
          <p>PTE Academic is a computer-based academic English language test aimed at non-native English speakers wanting to study abroad. It tests Reading, Listening and Speaking &amp; Writing.</p>
          <p>Questions often test 2 skills together, such as listening and reading or reading and speaking. The whole test is done in a single session, lasting 3 hours and is taken sitting at a computer in a secure test environment. The speaking part of the exam is done at the computer. Your voice is recorded and sent for marking.</p>
        </div>
        <div className="mt-6 flex flex-wrap gap-3">
          {[["Speaking", Mic], ["Writing", PenLine], ["Reading", BookOpen], ["Listening", Headphones]].map(([label, Icon]) => {
            const I = Icon as typeof Mic;
            return (
              <span key={label as string} className="inline-flex items-center gap-2 rounded-2xl bg-brand-light px-4 py-3 font-bold text-brand">
                <I className="size-5" /> {label as string}
              </span>
            );
          })}
        </div>
        <Link href="/pte-classes-india" className="mt-8 inline-flex items-center gap-2 font-bold text-accent">See our PTE classes <ArrowRight className="size-4" /></Link>
      </WithCallback>
      <Gallery images={media.galleries.ieltsGeneral} title="Our students" />
      <CtaBand course="PTE" />
    </>
  );
}
