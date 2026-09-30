import type { Metadata } from "next";
import { Plus_Jakarta_Sans } from "next/font/google";
import Header from "@/components/Header";
import Footer from "@/components/Footer";
import FloatingContact from "@/components/FloatingContact";
import { SITE } from "@/lib/site";
import "./globals.css";

const jakarta = Plus_Jakarta_Sans({ subsets: ["latin"], variable: "--font-jakarta" });

export const metadata: Metadata = {
  metadataBase: new URL(SITE.url),
  title: { default: "Western World Visa Services | Study Abroad Consultant", template: "%s | Western World Visa Services" },
  description:
    "Tips for Study ABROAD takes pleasure to introduce ourselves as we are one of the leading overseas consultant for studying abroad services.",
  keywords: [
    "Study Abroad", "Study in Canada", "Study in U.K", "Study in U.S.A", "Study in Australia", "Career in Abroad",
    "Study Abroad Programs", "Overseas Education in UK", "Overseas Education in Canada", "Overseas Education in USA",
    "Study Abroad Consultant", "Overseas Education Consultant", "MBA From Abroad", "Overseas Consultant",
    "Study Abroad after 12th", "List of Courses in Abroad",
  ],
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html lang="en" className={jakarta.variable}>
      <body className="flex min-h-screen flex-col">
        <Header />
        <main className="flex-1">{children}</main>
        <Footer />
        <FloatingContact />
      </body>
    </html>
  );
}
