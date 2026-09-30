"use client";

import Image from "next/image";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { useState } from "react";
import { ChevronDown, Mail, Menu, Phone, X } from "lucide-react";
import SocialIcons from "@/components/SocialIcons";
import { NAV, SITE, enquire, tel, type NavItem } from "@/lib/site";

export default function Header() {
  const path = usePathname();
  const [open, setOpen] = useState(false);
  const [expanded, setExpanded] = useState<string | null>(null);
  const active = (item: NavItem) =>
    item.href === "/" ? path === "/" : path.startsWith(item.href) || !!item.children?.some((c) => path.startsWith(c.href));

  return (
    <header className="sticky top-0 z-40">
      <div className="bg-brand-dark text-xs text-white/80">
        <div className="mx-auto flex max-w-7xl items-center justify-between gap-4 px-4 py-2">
          <p className="hidden font-medium tracking-wide sm:block">WELCOME TO {SITE.name.toUpperCase()}</p>
          <div className="flex items-center gap-5">
            <a href={tel(SITE.phones[0])} className="flex items-center gap-1.5 hover:text-white"><Phone className="size-3.5" />{SITE.phones[0]}</a>
            <a href={`mailto:${SITE.email}`} className="hidden items-center gap-1.5 hover:text-white md:flex"><Mail className="size-3.5" />{SITE.email}</a>
            <SocialIcons className="[&_a]:size-6 [&_svg]:size-3" />
          </div>
        </div>
      </div>

      <div className="border-b border-slate-100 bg-white/95 backdrop-blur">
        <div className="mx-auto flex max-w-7xl items-center justify-between gap-4 px-4 py-3">
          <Link href="/" onClick={() => setOpen(false)} className="shrink-0">
            <Image src="/images/logo.png" alt={SITE.legalName} width={200} height={40} priority className="h-10 w-auto" />
          </Link>

          <nav className="hidden items-center gap-0.5 lg:flex">
            {NAV.map((item) => (
              <div key={item.label} className="group relative">
                <Link href={item.href}
                      className={`flex items-center gap-1 rounded-lg px-2.5 py-2 text-sm font-semibold transition hover:text-accent ${active(item) ? "text-accent" : "text-slate-700"}`}>
                  {item.label}
                  {item.children && <ChevronDown className="size-3.5 transition group-hover:rotate-180" />}
                </Link>
                {item.children && (
                  <div className="invisible absolute top-full left-0 z-50 pt-2 opacity-0 transition group-focus-within:visible group-focus-within:opacity-100 group-hover:visible group-hover:opacity-100">
                    <ul className="w-60 rounded-2xl border border-slate-100 bg-white p-2 shadow-xl">
                      {item.children.map((c) => (
                        <li key={c.label}>
                          <Link href={c.href} className="block rounded-lg px-3 py-2 text-sm text-slate-700 hover:bg-brand-light hover:text-brand">{c.label}</Link>
                        </li>
                      ))}
                    </ul>
                  </div>
                )}
              </div>
            ))}
          </nav>

          <div className="flex items-center gap-2">
            <Link href={enquire()} className="hidden rounded-xl bg-accent px-4 py-2.5 text-sm font-bold text-white shadow-md shadow-accent/20 transition hover:bg-accent-dark sm:inline-block">
              Free counselling
            </Link>
            <button className="rounded-lg p-2 text-slate-700 lg:hidden" onClick={() => setOpen(!open)} aria-label="Menu" aria-expanded={open}>
              {open ? <X /> : <Menu />}
            </button>
          </div>
        </div>

        {open && (
          <nav className="max-h-[75vh] overflow-y-auto border-t border-slate-100 px-4 pb-4 lg:hidden">
            {NAV.map((item) => (
              <div key={item.label} className="border-b border-slate-100 last:border-0">
                <div className="flex items-center justify-between">
                  <Link href={item.href} onClick={() => setOpen(false)} className="py-3 text-sm font-semibold text-slate-800">{item.label}</Link>
                  {item.children && (
                    <button className="p-2" aria-label={`Show ${item.label}`} onClick={() => setExpanded(expanded === item.label ? null : item.label)}>
                      <ChevronDown className={`size-4 transition ${expanded === item.label ? "rotate-180" : ""}`} />
                    </button>
                  )}
                </div>
                {item.children && expanded === item.label && (
                  <ul className="mb-2 grid grid-cols-2 gap-1">
                    {item.children.map((c) => (
                      <li key={c.label}>
                        <Link href={c.href} onClick={() => setOpen(false)} className="block rounded-lg bg-slate-50 px-3 py-2 text-xs text-slate-700">{c.label}</Link>
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            ))}
          </nav>
        )}
      </div>
    </header>
  );
}
