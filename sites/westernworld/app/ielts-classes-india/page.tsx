import type { Metadata } from "next";
import WithCallback from "@/components/WithCallback";
import { CityDirectory } from "@/components/CityPages";
import { CtaBand, Gallery, PageHero, TestDetails } from "@/components/blocks";
import { IELTS_GENERAL_TEST } from "@/content/copy";
import { media } from "@/lib/content";

export const metadata: Metadata = {
  title: "IELTS Classes | IELTS Coaching | IELTS Preparation | IELTS For Canada",
  description: "The General version of IELTS is easier than the academic version. All candidates do the same Listening and Speaking sections",
};

export default function IeltsClassesPage() {
  return (
    <>
      <PageHero title="IELTS Classes" crumbs={[{ label: "IELTS Classes" }]} image="/images/slide1.jpg"
                subtitle="IELTS coaching in Rohtak and online, for students across Haryana." />
      <WithCallback source="Request call back (IELTS classes)" course="IELTS classes">
        <TestDetails test={{ ...IELTS_GENERAL_TEST, title: "IELTS Classes" }} />
      </WithCallback>
      <Gallery images={media.galleries.ieltsClasses} />
      <CityDirectory kind="ielts" />
      <CtaBand course="IELTS classes" />
    </>
  );
}
