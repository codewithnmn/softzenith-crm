import type { ReactNode } from "react";
import { cn } from "@/lib/utils";

/** Title block at the top of every staff page. */
export function PageHeader({ eyebrow, title, description, icon, actions }: {
  eyebrow?: ReactNode;
  title: ReactNode;
  description?: ReactNode;
  icon?: ReactNode;
  actions?: ReactNode;
}) {
  return (
    <div className="mb-6 flex flex-wrap items-start justify-between gap-4">
      <div className="flex items-start gap-4">
        {icon && (
          <div className="flex size-12 shrink-0 items-center justify-center rounded-xl bg-sidebar text-sidebar-foreground [&_svg]:size-6">
            {icon}
          </div>
        )}
        <div>
          {eyebrow && <p className="text-xs font-semibold tracking-wider text-muted-foreground uppercase">{eyebrow}</p>}
          <h1 className="text-2xl font-semibold tracking-tight">{title}</h1>
          {description && <p className="mt-1 text-sm text-muted-foreground">{description}</p>}
        </div>
      </div>
      {actions && <div className="flex flex-wrap items-center gap-2">{actions}</div>}
    </div>
  );
}

const STATUS_STYLES: Record<string, string> = {
  NEW: "bg-blue-50 text-blue-700 ring-blue-200",
  CONTACTED: "bg-violet-50 text-violet-700 ring-violet-200",
  ASSIGNED: "bg-emerald-50 text-emerald-700 ring-emerald-200",
  CLOSED: "bg-zinc-100 text-zinc-600 ring-zinc-200",
  SENT: "bg-emerald-50 text-emerald-700 ring-emerald-200",
  FAILED: "bg-red-50 text-red-700 ring-red-200",
  DEMO: "bg-amber-50 text-amber-700 ring-amber-200",
  INVITED: "bg-blue-50 text-blue-700 ring-blue-200",
  ACTIVE: "bg-emerald-50 text-emerald-700 ring-emerald-200",
  DISABLED: "bg-zinc-100 text-zinc-600 ring-zinc-200",
};

/** Coloured pill for a status code (lead, notification, staff). */
export function StatusBadge({ status, children }: { status?: string; children?: ReactNode }) {
  if (!status) return null;
  return (
    <span className={cn(
      "inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium whitespace-nowrap ring-1 ring-inset",
      STATUS_STYLES[status] ?? "bg-muted text-muted-foreground ring-border",
    )}>
      {children ?? humanize(status)}
    </span>
  );
}

/** Label + control, stacked. The label wraps the control, so screen readers (and tests) find the field by its label. */
export function Field({ label, hint, className, children }: {
  label: ReactNode;
  hint?: ReactNode;
  className?: string;
  children: ReactNode;
}) {
  return (
    <label className={cn("grid gap-1.5", className)}>
      <span className="flex items-center gap-2 text-xs leading-none font-medium select-none">
        {label}
        {hint && <span className="font-normal text-muted-foreground">{hint}</span>}
      </span>
      {children}
    </label>
  );
}

export function ErrorText({ children }: { children?: ReactNode }) {
  if (!children) return null;
  return <p className="text-sm text-destructive">{children}</p>;
}

export function Initials({ name, className }: { name?: string; className?: string }) {
  return (
    <span className={cn(
      "inline-flex size-9 shrink-0 items-center justify-center rounded-lg bg-sky-100 text-xs font-semibold text-sky-800",
      className,
    )}>
      {initials(name)}
    </span>
  );
}

export const initials = (name?: string) =>
  (name ?? "?").split(/\s+/).filter(Boolean).slice(0, 2).map((w) => w[0]!.toUpperCase()).join("");

/** WEBSITE_FORM → Website form */
export const humanize = (code?: string | null) =>
  code ? code.charAt(0) + code.slice(1).toLowerCase().replaceAll("_", " ") : "";
