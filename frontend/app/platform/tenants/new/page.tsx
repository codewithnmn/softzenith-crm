"use client";

import dynamic from "next/dynamic";
import { Rocket } from "lucide-react";
import { PageHeader } from "@/components/common";

// Browser only: the form starts from the draft saved in this browser.
const OnboardingWizard = dynamic(() => import("@/components/platform/OnboardingWizard").then((m) => m.OnboardingWizard), {
  ssr: false,
  loading: () => <p className="text-sm text-muted-foreground">Loading…</p>,
});

export default function OnboardBusinessPage() {
  return (
    <>
      <PageHeader
        icon={<Rocket />}
        title="Onboard a business"
        description="Fill in the steps, check the setup, then go live. Nothing is created until you press Go live."
      />
      <OnboardingWizard />
    </>
  );
}
