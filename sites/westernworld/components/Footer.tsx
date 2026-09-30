import Image from "next/image";
import Link from "next/link";
import { Mail, MapPin, Phone } from "lucide-react";
import SocialIcons from "@/components/SocialIcons";
import { FOOTER_LINKS, NAV, SITE, tel } from "@/lib/site";

export default function Footer() {
  const destinations = NAV.find((n) => n.label === "Global Education")?.children ?? [];
  return (
    <footer className="bg-brand-dark text-white/75">
      <div className="mx-auto grid max-w-7xl gap-10 px-4 py-16 md:grid-cols-2 lg:grid-cols-4">
        <div className="space-y-4">
          <div className="inline-block rounded-xl bg-white p-3">
            <Image src="/images/logo.png" alt={SITE.legalName} width={200} height={40} className="h-9 w-auto" />
          </div>
          <p className="text-sm leading-relaxed">{SITE.footerAbout}</p>
          <SocialIcons />
        </div>

        <div>
          <h4 className="mb-4 text-sm font-bold tracking-wider text-white uppercase">Other links</h4>
          <ul className="space-y-2 text-sm">
            {FOOTER_LINKS.map((l) => <li key={l.label}><Link href={l.href} className="hover:text-white">{l.label}</Link></li>)}
          </ul>
          <h4 className="mt-6 mb-3 text-sm font-bold tracking-wider text-white uppercase">Immigration services</h4>
          <ul className="space-y-2 text-sm">
            <li><Link href="/visa-assistance" className="hover:text-white">Visa Assistance</Link></li>
            <li><a href={SITE.webmail} target="_blank" rel="noopener noreferrer" className="hover:text-white">Webmail Login</a></li>
          </ul>
        </div>

        <div>
          <h4 className="mb-4 text-sm font-bold tracking-wider text-white uppercase">Study destinations</h4>
          <ul className="space-y-2 text-sm">
            {destinations.map((l) => <li key={l.label}><Link href={l.href} className="hover:text-white">{l.label}</Link></li>)}
          </ul>
        </div>

        <div>
          <h4 className="mb-4 text-sm font-bold tracking-wider text-white uppercase">Address</h4>
          <ul className="space-y-3 text-sm">
            <li className="flex gap-3"><MapPin className="mt-0.5 size-4 shrink-0 text-accent" />{SITE.address}</li>
            {SITE.phones.map((p) => (
              <li key={p}><a href={tel(p)} className="flex gap-3 hover:text-white"><Phone className="size-4 shrink-0 text-accent" />{p}</a></li>
            ))}
            <li><a href={`mailto:${SITE.email}`} className="flex gap-3 break-all hover:text-white"><Mail className="size-4 shrink-0 text-accent" />{SITE.email}</a></li>
          </ul>
        </div>
      </div>
      <div className="border-t border-white/10">
        <p className="mx-auto max-w-7xl px-4 py-5 text-xs text-white/50">
          © {new Date().getFullYear()} {SITE.legalName}. All Rights Reserved.
        </p>
      </div>
    </footer>
  );
}
