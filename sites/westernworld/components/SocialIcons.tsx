import { SITE } from "@/lib/site";

// Brand marks are not in lucide; simple inline SVGs.
const paths = {
  Facebook: "M14 9h3V5.5h-3c-2.2 0-4 1.8-4 4V11H8v3.5h2V22h3.5v-7.5H16l.5-3.5h-3v-1.5c0-.3.2-.5.5-.5Z",
  Instagram:
    "M12 7.3A4.7 4.7 0 1 0 12 16.7 4.7 4.7 0 0 0 12 7.3Zm0 7.7a3 3 0 1 1 0-6 3 3 0 0 1 0 6Zm6-7.9a1.1 1.1 0 1 1-2.2 0 1.1 1.1 0 0 1 2.2 0ZM16.5 2h-9A5.5 5.5 0 0 0 2 7.5v9A5.5 5.5 0 0 0 7.5 22h9a5.5 5.5 0 0 0 5.5-5.5v-9A5.5 5.5 0 0 0 16.5 2Zm3.7 14.5a3.7 3.7 0 0 1-3.7 3.7h-9a3.7 3.7 0 0 1-3.7-3.7v-9a3.7 3.7 0 0 1 3.7-3.7h9a3.7 3.7 0 0 1 3.7 3.7v9Z",
  LinkedIn:
    "M6.9 21H3.2V9h3.7v12ZM5 7.4a2.1 2.1 0 1 1 0-4.3 2.1 2.1 0 0 1 0 4.3ZM21 21h-3.7v-5.8c0-1.4 0-3.2-2-3.2s-2.2 1.5-2.2 3.1V21H9.4V9h3.5v1.6h.1c.5-.9 1.7-2 3.5-2 3.7 0 4.4 2.5 4.4 5.6V21Z",
};

export default function SocialIcons({ className = "" }: { className?: string }) {
  const links = [
    ["Facebook", SITE.social.facebook], ["Instagram", SITE.social.instagram], ["LinkedIn", SITE.social.linkedin],
  ] as const;
  return (
    <div className={`flex items-center gap-2 ${className}`}>
      {links.map(([name, href]) => (
        <a key={name} href={href} target="_blank" rel="noopener noreferrer" aria-label={name}
           className="flex size-8 items-center justify-center rounded-full bg-white/10 transition hover:bg-white/25">
          <svg viewBox="0 0 24 24" className="size-4 fill-current"><path d={paths[name]} /></svg>
        </a>
      ))}
    </div>
  );
}
