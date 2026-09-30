import type { Metadata } from "next";
import PlatformShell from "@/components/platform/PlatformShell";

// Not linked from the staff app and kept out of search engines. Access is protected by the backend, not by obscurity.
export const metadata: Metadata = {
  title: "SoftZenith Platform",
  robots: { index: false, follow: false },
};

export default function PlatformLayout({ children }: { children: React.ReactNode }) {
  return <PlatformShell>{children}</PlatformShell>;
}
