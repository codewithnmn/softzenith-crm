import type { Metadata } from "next";
import Image from "next/image";
import Link from "next/link";
import { ArrowRight } from "lucide-react";
import { CtaBand, PageHero, Section } from "@/components/blocks";
import { enquire } from "@/lib/site";

export const metadata: Metadata = { title: "Our Blog" };

// The old blog had one post; its detail page returned "404 Not Found", so the card leads to enrolment instead.
const POSTS = [{ title: "Enrol now and get your Required Band!", tag: "IELTS", image: "/images/latest/blog-1.jpg" }];

export default function BlogPage() {
  return (
    <>
      <PageHero title="Our Blog" crumbs={[{ label: "Our Blog" }]} subtitle="News, tips and offers from Western World Visa Services." />
      <Section>
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {POSTS.map((p) => (
            <Link key={p.title} href={enquire(p.tag)} className="group overflow-hidden rounded-3xl border border-slate-100 bg-white shadow-sm transition hover:shadow-xl">
              <div className="relative h-56 overflow-hidden">
                <Image src={p.image} alt={p.title} fill className="object-cover transition duration-500 group-hover:scale-105" sizes="(min-width: 1024px) 33vw, 100vw" />
                <span className="absolute top-4 left-4 rounded-full bg-accent px-3 py-1 text-xs font-bold text-white">{p.tag}</span>
              </div>
              <div className="p-6">
                <h2 className="text-lg font-bold group-hover:text-brand">{p.title}</h2>
                <span className="mt-4 inline-flex items-center gap-1 text-sm font-bold text-accent">Read more <ArrowRight className="size-4" /></span>
              </div>
            </Link>
          ))}
        </div>
      </Section>
      <CtaBand course="IELTS" />
    </>
  );
}
