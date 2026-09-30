import { MessageCircle } from "lucide-react";
import { SITE } from "@/lib/site";

/** WhatsApp chat button. The old site's tawk.to chat was removed: its messages never reached the CRM (owner, 26 Sep 2026). */
export default function FloatingContact() {
  return (
    <a href={SITE.whatsapp} target="_blank" rel="noopener noreferrer" aria-label="Chat on WhatsApp"
       className="fixed right-5 bottom-5 z-40 flex items-center gap-2 rounded-full bg-emerald-500 px-4 py-3 text-sm font-bold text-white shadow-xl shadow-emerald-500/30 transition hover:bg-emerald-600">
      <MessageCircle className="size-5" /> <span className="hidden sm:inline">WhatsApp us</span>
    </a>
  );
}
