import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { IeltsCityPage, titleCase } from "@/components/CityPages";
import { cities } from "@/lib/content";

const find = (slug: string) => cities.ielts.find((c) => c.slug === slug);

export function generateStaticParams() {
  return cities.ielts.map((c) => ({ slug: c.slug }));
}

export async function generateMetadata({ params }: PageProps<"/ielts-classes/[slug]">): Promise<Metadata> {
  const c = find((await params).slug);
  if (!c) return {};
  const name = titleCase(c.city);
  return {
    title: `IELTS Coaching Classes ${name} & Study Abroad ${name}`,
    description: `Western World Visa Services is one of the leading consultant for study abroad and IELTS in ${name}, India`,
  };
}

export default async function Page({ params }: PageProps<"/ielts-classes/[slug]">) {
  const c = find((await params).slug);
  if (!c) notFound();
  return <IeltsCityPage city={c} />;
}
