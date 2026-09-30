"use client";

import Link from "next/link";
import useSWR from "swr";
import { Building, ExternalLink, Plus } from "lucide-react";
import { buttonVariants } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { ErrorText, PageHeader, StatusBadge } from "@/components/common";
import { formatDate, unwrap } from "@/lib/api";
import { platformApi } from "@/lib/platform";

/** Every business on the platform, and the way in to onboard a new one. */
export default function PlatformHome() {
  const { data: tenants, error } = useSWR("platform-tenants", () => unwrap(platformApi.GET("/api/platform/tenants")));

  return (
    <>
      <PageHeader
        icon={<Building />}
        title="Businesses"
        description="Each business is a separate tenant: its own staff, branches, leads and settings."
        actions={<Link href="/platform/tenants/new" className={buttonVariants()}><Plus /> Onboard a business</Link>}
      />
      <ErrorText>{error?.message}</ErrorText>
      <Card className="p-0">
        <Table>
          <TableHeader>
            <TableRow className="bg-muted/40 text-xs uppercase">
              <TableHead className="pl-4">Business</TableHead>
              <TableHead>Web address</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="pr-4">Onboarded</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {tenants?.map((t) => (
              <TableRow key={t.id}>
                <TableCell className="pl-4 font-medium">{t.name}</TableCell>
                <TableCell>
                  <a href={`/enquiry/${t.slug}`} target="_blank" rel="noreferrer" className="inline-flex items-center gap-1 hover:text-primary">
                    {t.slug} <ExternalLink className="size-3.5" />
                  </a>
                </TableCell>
                <TableCell><StatusBadge status={t.status} /></TableCell>
                <TableCell className="pr-4 text-muted-foreground">{formatDate(t.createdAt)}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        {tenants?.length === 0 && <p className="p-8 text-center text-sm text-muted-foreground">No businesses yet.</p>}
      </Card>
    </>
  );
}
