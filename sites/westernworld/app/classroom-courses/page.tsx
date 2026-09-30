import type { Metadata } from "next";
import WithCallback from "@/components/WithCallback";
import { CtaBand, Gallery, PageHero, TestDetails } from "@/components/blocks";
import { IELTS_GENERAL_TEST } from "@/content/copy";
import { media } from "@/lib/content";

export const metadata: Metadata = {
  title: "Classroom Courses",
  description: "The General version of IELTS is easier than the academic version. All candidates do the same Listening and Speaking sections",
};

export default function ClassroomCoursesPage() {
  return (
    <>
      <PageHero title="Classroom Courses" crumbs={[{ label: "IELTS Academic" }, { label: "Classroom Courses" }]} image="/images/banner6.jpg"
                subtitle="Learn in person at our Rohtak centre with small batches and daily practice." />
      <WithCallback source="Request call back (classroom courses)" course="IELTS classroom course">
        <TestDetails test={IELTS_GENERAL_TEST} />
      </WithCallback>
      <Gallery images={media.galleries.ieltsGeneral} />
      <CtaBand course="IELTS classroom course" />
    </>
  );
}
