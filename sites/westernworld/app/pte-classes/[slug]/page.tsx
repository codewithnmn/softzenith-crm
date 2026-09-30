import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { PteCityPage, titleCase } from "@/components/CityPages";
import { cities } from "@/lib/content";

const find = (slug: string) => cities.pte.find((c) => c.slug === slug);

export function generateStaticParams() {
  return cities.pte.map((c) => ({ slug: c.slug }));
}

export async function generateMetadata({ params }: PageProps<"/pte-classes/[slug]">): Promise<Metadata> {
  const c = find((await params).slug);
  if (!c) return {};
  const name = titleCase(c.city);
  return {
    title: `PTE Coaching Classes ${name} & Study Abroad ${name}`,
    description: `Western World Visa Services is one of the leading consultant for study abroad and PTE in ${name}, India`,
  };
}

export default async function Page({ params }: PageProps<"/pte-classes/[slug]">) {
  const c = find((await params).slug);
  if (!c) notFound();
  return <PteCityPage city={c} />;
}
