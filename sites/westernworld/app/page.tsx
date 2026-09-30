import Image from "next/image";
import Link from "next/link";
import {
  ArrowRight, BookOpen, ClipboardCheck, FileCheck2, GraduationCap, MessagesSquare, PlaneTakeoff, Target, UserRound,
  Users,
} from "lucide-react";
import EnquiryForm from "@/components/EnquiryForm";
import NewsletterBox from "@/components/NewsletterBox";
import Slider from "@/components/Slider";
import { CtaBand, Feature, Gallery, Packages, Section, SectionTitle } from "@/components/blocks";
import { HOME_PACKAGES, IELTS_ACADEMIC_BLURB, IELTS_GENERAL_BLURB } from "@/content/copy";
import { countries, media } from "@/lib/content";
import { enquire } from "@/lib/site";

const SLIDE_ALTS = [
  "Study Abroad in Delhi", "Study Abroad in Rohini", "Western World Visa Services", "Western World Visa Services in delhi",
  "Western World Visa Services in Rohtak", "Western World Visa Services in Haryana", "Western World Visa Services in India",
];

const FAQ = [
  ["What type of learning experience should I expect?",
    "The content will be a mix of interactive video lectures, reading material, practice quizzes and mock tests along with daily private sessions with your trainer."],
  ["Can I get a trial class to understand your teaching style?",
    "Yes, if you want a taste of what our online class experience is and how intense can it be. You can request for a free session with us."],
  ["How is this course different from other courses?", [
    "This course starts with a thorough diagnosis of your current English Language skills and IELTS Exam readiness assessment.",
    "This assessment is conducted/analysed by a senior trainer who defines a roadmap for you to get your desired band in 1 attempt.",
    "Based on the analysis of your strengths and weaknesses done by the senior trainer you are allotted multiple trainers who will work with on your areas for improvement.",
    "Your Senior Trainer evaluates your performance every week. Based on your progress, your Senior trainer, along with the other trainers, will customize your study plan for sustained improvement.",
    "After sufficient language improvement, you are directed to IELTS specific training",
    "In the end you are put on a rigorous mock test routine",
    "Once you start scoring your desired bands in our internal IELTS Mock Tests you are ready to pick an exam date",
  ]],
  ["How long are the personal training sessions?", "Each personal training session is of a total duration of 1 hour."],
] as const;

export default function Home() {
  return (
    <>
      {/* Hero: slider + Keep in touch */}
      <section className="relative isolate overflow-hidden bg-brand-dark">
        <Slider images={media.sliders.home} alts={SLIDE_ALTS} />
        <div className="absolute inset-0 bg-gradient-to-r from-brand-dark via-brand-dark/85 to-brand-dark/30" />
        <div className="relative mx-auto grid max-w-7xl items-center gap-10 px-4 py-16 lg:grid-cols-[1fr_440px] lg:py-24">
          <div>
            <p className="mb-4 inline-flex items-center gap-2 rounded-full bg-white/10 px-4 py-1.5 text-xs font-bold tracking-widest text-white uppercase backdrop-blur">
              <PlaneTakeoff className="size-4 text-sky" /> Study abroad · Visa · IELTS / PTE
            </p>
            <h1 className="text-4xl leading-tight font-extrabold text-white sm:text-6xl">Welcome to Western World Visa Services</h1>
            <p className="mt-5 max-w-xl text-lg text-white/80">
              WWVS could help clients with visa applications, requirements and processing. We provide educational avenues in
              international countries like: UK, Canada, USA, Germany, Europe, Australia, Singapore and also Coaching for IELTS
              General and Academic courses.
            </p>
            <div className="mt-8 flex flex-wrap gap-3">
              <Link href="/study-in/canada" className="inline-flex items-center gap-2 rounded-xl bg-accent px-6 py-3.5 text-sm font-bold text-white shadow-lg shadow-accent/30 hover:bg-accent-dark">
                Explore destinations <ArrowRight className="size-4" />
              </Link>
              <Link href="/ielts-general" className="rounded-xl border-2 border-white/40 px-6 py-3 text-sm font-bold text-white hover:bg-white/10">IELTS & PTE coaching</Link>
            </div>
          </div>
          <div id="keep-in-touch" className="rounded-3xl bg-white p-6 shadow-2xl sm:p-8">
            <EnquiryForm title="Keep in touch" subtitle="Tell us what you need and someone from our team will get in touch with you."
                         source="Keep in touch (home)" />
          </div>
        </div>
      </section>

      {/* Services */}
      <Section className="-mt-8 pt-0 lg:pt-0">
        <div className="relative grid gap-6 md:grid-cols-3">
          <Feature icon={<GraduationCap />} title="Career Counselling">
            Western World Visa Services offer Career Counselling all over the world most influential universities
          </Feature>
          <Feature icon={<FileCheck2 />} title="Visa Support & Filing">
            Western World Visa Services help in providing visa support and filling process from renowned universities
          </Feature>
          <Feature icon={<BookOpen />} title="IELTS | PTE Training">
            Western World Visa Services started its journey with a clear vision to be a pioneer in the field of Immigration and IELTS/PTE/CELPIP
          </Feature>
        </div>
      </Section>

      {/* Welcome / about */}
      <Section className="pt-0 lg:pt-0">
        <div className="grid items-center gap-12 lg:grid-cols-2">
          <div className="relative">
            <Image src="/images/w4.jpg" alt="study and examine every case" width={640} height={520} className="rounded-3xl object-cover shadow-xl" />
            <div className="absolute -right-4 -bottom-6 hidden rounded-2xl bg-accent px-6 py-5 text-white shadow-xl sm:block">
              <p className="text-3xl font-extrabold">2000+</p>
              <p className="text-xs font-semibold tracking-wide uppercase">Enrolled students</p>
            </div>
          </div>
          <div className="prose-copy">
            <SectionTitle eyebrow="Welcome to Western World Visa Services" title="Your study abroad and IELTS partner" />
            <p>WWVS could help clients with visa applications, requirements and processing.</p>
            <p>The requirement for a visa will depend on the country the clients wants to study in. Equipped with the latest information about student migration, migration agents will be able to help clients achieve their student visas.</p>
            <p>We provides IELTS training also. Our main motive is to bridge the gap for all queries related to IELTS and Overseas Education.</p>
            <p>We provide educational avenues in international countries like: UK, Canada, USA, Germany, Europe, Australia, Singapore and also Coaching for IELTS General and Academic courses.</p>
            <p>We provide best and better coaching to students from others and help them in IELTS exam also in all four section. Students can Enrolment in both Online and Offline IELTS Course Preparation at TIPS Abroad Study. We are a team of talented faculties, best teachers. We are committed to offering the quality training for IELTS focusing the student’s dream of clearing the exam. Our outstanding facilities make our students experience effective and exciting learning which contribute to their success.</p>
          </div>
        </div>
      </Section>

      {/* Why us */}
      <Section className="bg-slate-50">
        <div className="grid items-center gap-12 lg:grid-cols-2">
          <div className="prose-copy">
            <SectionTitle eyebrow="Why us?" title="Hard work and transparency, case by case" intro="With extreme hard work and transparency Western World Visa Services has succeeded in developing its position as one of the leading education consultancies" />
            <p>We, at WWVS, have a team of passionate and experienced members who are dedicatedly working to realise the dreams of the aspiring candidates.</p>
            <p>At WWVS, we consider it our professional concern to study and examine every case before actually representing the client, further taking care of individuality and client preference. Being experienced experts, our team is always up-to-date with recent developments and updates. Our organization takes immense pride to work closely and meet every need of the clients. In addition to this, we always keep our clients informed with the latest status of their cases. Providing the right study abroad solutions to the students over the years, we have set highest benchmarks in tune with the global competencies</p>
          </div>
          <Image src="/images/w5.jpg" alt="expert professionals at a personal level" width={640} height={520} className="rounded-3xl object-cover shadow-xl" />
        </div>
      </Section>

      {/* How we are different */}
      <Section>
        <SectionTitle eyebrow="How we are different" title="Coaching built around you" center />
        <div className="grid gap-6 md:grid-cols-3">
          <Feature icon={<UserRound />} title="One-to-One session">
            Get trained from expert professionals at a personal level. We provide a safe and interactive environment for students to learn and grow. These highly personalized sessions can be flexibly planned and can be taken anywhere.
          </Feature>
          <Feature icon={<Target />} title="Customized Study Plan For Every Student">
            Students are trained based on their strengths and weaknesses through a tailor-made study plan
          </Feature>
          <Feature icon={<ClipboardCheck />} title="Comprehensive Mock Test Series">
            Comprehensive mock tests after completion of every module.
          </Feature>
        </div>
      </Section>

      {/* Packages */}
      <Section className="bg-slate-50">
        <SectionTitle eyebrow="Courses" title="IELTS General packages" center
                      intro="IELTS developed the General test if you wish to study at university or college as an undergraduate or postgraduate student, or, if you want to join or gain entry into a professional institution." />
        <Packages packages={HOME_PACKAGES} source="Home packages" />
      </Section>

      {/* How it works */}
      <Section>
        <SectionTitle eyebrow="How it works" title="From assessment to exam day" center />
        <ol className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
          {[
            ["Pre-training assessment", "This course starts with a thorough diagnosis of your current English Language skills and IELTS Exam readiness assessment."],
            ["Customized study plan", "Based on the assessment, the Senior Trainer creates a tailored study plan, specifically for you. From there on, Multiple trainers will regularly guide, evaluate and monitor your progress."],
            ["Mock Tests & Exam conditioning", "Throughout the program, you will give over 30 mock tests, and discuss and improve upon each one of them with your trainer so you are 100% exam ready on the day of the test."],
            ["3:1 faculty to student ratio", "You will always have multiple trainers collectively working on you to achieve your desired band. Throughout the course, you will have daily private classes where you will learn key concepts and get all your doubts answered."],
          ].map(([title, text], n) => (
            <li key={title} className="relative rounded-3xl border border-slate-100 bg-white p-6 shadow-sm">
              <span className="text-5xl font-extrabold text-brand/10">0{n + 1}</span>
              <h3 className="mt-2 text-lg font-bold">{title}</h3>
              <p className="mt-2 text-sm leading-relaxed text-slate-500">{text}</p>
            </li>
          ))}
        </ol>
      </Section>

      {/* Achievements */}
      <section className="bg-brand">
        <div className="mx-auto grid max-w-7xl grid-cols-2 gap-8 px-4 py-14 text-center lg:grid-cols-4">
          {[["2000", "Enrolled students"], ["40", "Courses"], ["600", "Reviews"], ["375", "Publications"]].map(([n, label]) => (
            <div key={label}>
              <p className="text-4xl font-extrabold text-white sm:text-5xl">{n}+</p>
              <p className="mt-1 text-sm font-semibold tracking-wider text-white/70 uppercase">{label}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Our courses */}
      <Section>
        <SectionTitle eyebrow="Our courses" title="IELTS General and IELTS Academic" center />
        <div className="grid gap-6 md:grid-cols-2">
          {[
            ["IELTS General", IELTS_GENERAL_BLURB, "/images/banner-bg-1.jpg", "/ielts-general"],
            ["IELTS Academic", IELTS_ACADEMIC_BLURB, "/images/banner-bg-2.jpg", "/ielts-academy"],
          ].map(([title, text, img, href]) => (
            <Link key={title} href={href} className="group relative flex min-h-80 overflow-hidden rounded-3xl">
              <Image src={img} alt={title} fill className="object-cover transition duration-700 group-hover:scale-105" sizes="(min-width: 768px) 50vw, 100vw" />
              <div className="absolute inset-0 bg-gradient-to-t from-brand-dark via-brand-dark/70 to-transparent" />
              <div className="relative mt-auto p-8">
                <h3 className="text-2xl font-extrabold text-white">{title}</h3>
                <p className="mt-2 text-sm text-white/80">{text}</p>
                <span className="mt-4 inline-flex items-center gap-2 text-sm font-bold text-white">View Course <ArrowRight className="size-4 transition group-hover:translate-x-1" /></span>
              </div>
            </Link>
          ))}
        </div>
      </Section>

      {/* Destinations (new: quick links to the country pages that were only in the menu) */}
      <Section className="bg-slate-50">
        <SectionTitle eyebrow="Global education" title="Where would you like to study?" center />
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
          {countries.map((c) => (
            <Link key={c.slug} href={`/study-in/${c.slug}`}
                  className="group flex items-center justify-between rounded-2xl border border-slate-100 bg-white px-5 py-4 font-bold text-slate-800 shadow-sm transition hover:border-brand hover:text-brand">
              Study in {c.name} <ArrowRight className="size-4 text-slate-300 transition group-hover:text-brand" />
            </Link>
          ))}
        </div>
      </Section>

      <Gallery images={media.galleries.home} title="IELTS Students" />

      {/* FAQ + newsletter */}
      <Section>
        <div className="grid gap-12 lg:grid-cols-[1fr_420px]">
          <div>
            <SectionTitle eyebrow="FAQ" title="Questions students ask us" />
            <div className="space-y-3">
              {FAQ.map(([q, a]) => (
                <details key={q} className="group rounded-2xl border border-slate-100 bg-white p-5 shadow-sm open:shadow-md">
                  <summary className="flex cursor-pointer list-none items-center justify-between gap-4 font-bold text-slate-900">
                    {q}
                    <MessagesSquare className="size-5 shrink-0 text-brand transition group-open:text-accent" />
                  </summary>
                  {typeof a === "string" ? <p className="mt-3 text-sm leading-relaxed text-slate-600">{a}</p> : (
                    <ul className="mt-3 list-disc space-y-1.5 pl-5 text-sm text-slate-600">{a.map((x) => <li key={x}>{x}</li>)}</ul>
                  )}
                  {q.startsWith("Can I get a trial") && (
                    <Link href={enquire("Trial class")} className="mt-3 inline-flex items-center gap-1 text-sm font-bold text-accent">
                      Click here to schedule a trial class <ArrowRight className="size-4" />
                    </Link>
                  )}
                </details>
              ))}
            </div>
          </div>
          <div className="space-y-6">
            <NewsletterBox />
            <div className="rounded-3xl border border-slate-100 p-6">
              <Users className="size-8 text-brand" />
              <p className="mt-3 font-bold text-slate-900">Visit our Rohtak office</p>
              <p className="mt-1 text-sm text-slate-500">1st Floor, POWER HOUSE CHOWK, above CANARA Bank, Model Town, Rohtak, Haryana 124001</p>
            </div>
          </div>
        </div>
      </Section>

      <CtaBand />
    </>
  );
}
