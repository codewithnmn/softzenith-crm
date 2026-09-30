import type { Metadata } from "next";
import WithCallback from "@/components/WithCallback";
import { CityDirectory, PteContent } from "@/components/CityPages";
import { CtaBand, Gallery, PageHero } from "@/components/blocks";
import { media } from "@/lib/content";

export const metadata: Metadata = { title: "PTE Classes | PTE Coaching | PTE Preparation" };

export default function PteClassesPage() {
  return (
    <>
      <PageHero title="PTE Classes" crumbs={[{ label: "PTE Classes" }]} image="/images/slide3.jpg"
                subtitle="Excel in PTE with Western World Visa Services: your pathway to study abroad and high bands." />
      <WithCallback source="Request call back (PTE classes)" course="PTE classes – free 3-day trial">
        <PteContent />
      </WithCallback>
      <Gallery images={media.galleries.pteClasses} title="PTE Students" />
      <CityDirectory kind="pte" />
      <CtaBand course="PTE classes" />
    </>
  );
}
