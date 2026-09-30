import type { ReactNode } from "react";
import { CallbackCard, Section } from "@/components/blocks";

/** Page body with the "Request call back" form alongside. */
export default function WithCallback({ children, source, course, country }: {
  children: ReactNode; source: string; course?: string; country?: string;
}) {
  return (
    <Section>
      <div className="grid gap-10 lg:grid-cols-[1fr_380px]">
        <div className="min-w-0">{children}</div>
        <CallbackCard source={source} course={course} country={country} />
      </div>
    </Section>
  );
}
