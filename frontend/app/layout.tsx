import type { Metadata } from "next";
import { Geist } from "next/font/google";
import { Toaster } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { cn } from "@/lib/utils";
import "./globals.css";

const geist = Geist({ subsets: ["latin"], variable: "--font-sans" });

export const metadata: Metadata = {
  title: "CRM",
  description: "SoftZenith CRM",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html lang="en" className={cn("font-sans antialiased", geist.variable)}>
      <body>
        <TooltipProvider>{children}</TooltipProvider>
        <Toaster theme="light" position="bottom-center" />
      </body>
    </html>
  );
}
