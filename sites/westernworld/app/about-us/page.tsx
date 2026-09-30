import type { Metadata } from "next";
import Image from "next/image";
import { BadgeCheck } from "lucide-react";
import WithCallback from "@/components/WithCallback";
import { CtaBand, PageHero } from "@/components/blocks";

export const metadata: Metadata = { title: "About Us" };

const SERVICES = [
  "Visa Assistance", "Institutes For IELTS", "Visa Assistance For Canada", "Student Visa Assistance",
  "Visa Assistance For USA", "Visa Assistance For Australia",
];

export default function AboutPage() {
  return (
    <>
      <PageHero title="About Us" crumbs={[{ label: "About Us" }]} image="/images/banner7.jpg"
                subtitle="Western World Visa Services, Rohtak: IELTS institute and study-abroad visa assistance since 2020." />
      <WithCallback source="Request call back (about us)">
        <Image src="/images/banner7.jpg" alt="Western World Visa Services office" width={1200} height={440}
               className="mb-8 h-64 w-full rounded-3xl object-cover shadow-lg" />
        <div className="prose-copy text-slate-600">
          <h2 className="mb-4 text-2xl font-bold">Western World Visa Services</h2>
          <p>Western World Visa Services in Rohtak is one of the leading businesses in the Institutes For IELTS. Also known for Visa Assistance, Institutes For IELTS, Visa Assistance For Canada, Student Visa Assistance, Visa Assistance For USA, Visa Assistance For Australia, Online Websites For BITSAT Exam, Online Websites For Central University and much more. Find Address, Contact Number, Reviews &amp; Ratings, Photos, Maps of Western World Visa Services, Rohtak.</p>
          <p>Established in the year 2020, Western World Visa Services in Rohtak HO, Rohtak is a top player in the category Institutes For IELTS in the Rohtak. This well-known establishment acts as a one-stop destination servicing customers both local and from other parts of Rohtak. Over the course of its journey, this business has established a firm foothold in its industry. The belief that customer satisfaction is as important as their products and services, have helped this establishment garner a vast base of customers, which continues to grow by the day. This business employs individuals that are dedicated towards their respective roles and put in a lot of effort to achieve the common vision and larger goals of the company.</p>
          <p>In the near future, this business aims to expand its line of products and services and cater to a larger client base. In Rohtak, this establishment occupies a prominent location in Rohtak HO. It is an effortless task in commuting to this establishment as there are various modes of transport readily available. It is at Delhi Road Rohtak, Opposite MDU Gate No.2, which makes it easy for first-time visitors in locating this establishment. It is known to provide top service in the following categories: Visa Assistance, Institutes For IELTS, Visa Assistance For Canada, Student Visa Assistance, Visa Assistance For USA, Visa Assistance For Australia, Online Websites For BITSAT Exam, Online Websites For Central University.</p>
        </div>
        <div className="mt-8 grid gap-3 sm:grid-cols-2">
          {SERVICES.map((s) => (
            <div key={s} className="flex items-center gap-3 rounded-2xl border border-slate-100 bg-white p-4 font-semibold text-slate-800 shadow-sm">
              <BadgeCheck className="size-5 text-accent" /> {s}
            </div>
          ))}
        </div>
      </WithCallback>
      <CtaBand />
    </>
  );
}
