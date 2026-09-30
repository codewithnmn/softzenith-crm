"use client";

import { useState, type FormEvent } from "react";
import useSWR from "swr";
import { Building2, Plus } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { ErrorText, PageHeader, StatusBadge } from "@/components/common";
import { api, errorMessage, unwrap, type Schemas } from "@/lib/api";

type Branch = Schemas["BranchResponse"];

export default function BranchesPage() {
  const { data: branches = [], mutate: load } = useSWR("branches", () => unwrap(api.GET("/api/v1/branches")));
  const [error, setError] = useState("");

  const add = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const form = e.currentTarget;
    const f = Object.fromEntries(new FormData(form)) as Record<string, string>;
    const { error } = await api.POST("/api/v1/branches", { body: { name: f.name, city: f.city || undefined } });
    if (error) setError(errorMessage(error));
    else { setError(""); form.reset(); load(); }
  };

  const toggle = async (b: Branch) => {
    const { error } = await api.PUT("/api/v1/branches/{id}", {
      params: { path: { id: b.id! } },
      body: { name: b.name!, city: b.city, active: !b.active },
    });
    setError(error ? errorMessage(error) : "");
    load();
  };

  return (
    <>
      <PageHeader title="Branches" description="Offices your team works from. Leads and staff belong to a branch." />

      <Card className="mb-6">
        <CardContent>
          <form onSubmit={add} className="flex flex-wrap gap-3">
            <Input name="name" placeholder="Branch name, e.g. Delhi · Nehru Place" required className="min-w-60 flex-1" />
            <Input name="city" placeholder="City" className="w-48" />
            <Button type="submit"><Plus /> Add branch</Button>
          </form>
          <ErrorText>{error}</ErrorText>
        </CardContent>
      </Card>

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
        {branches.map((b) => (
          <Card key={b.id} className="flex-row items-center gap-4 px-5">
            <span className="flex size-10 items-center justify-center rounded-lg bg-sky-100 text-sky-800"><Building2 className="size-5" /></span>
            <div className="min-w-0 flex-1">
              <p className="truncate font-medium">{b.name}</p>
              <p className="text-xs text-muted-foreground">{b.city ?? "—"}</p>
            </div>
            <StatusBadge status={b.active ? "ACTIVE" : "DISABLED"}>{b.active ? "Active" : "Inactive"}</StatusBadge>
            <Button variant="ghost" size="sm" onClick={() => toggle(b)}>{b.active ? "Deactivate" : "Activate"}</Button>
          </Card>
        ))}
      </div>
    </>
  );
}
