import { redirect } from "next/navigation";

/** The dashboard sends users without a dashboard on to their lead list. */
export default function Home() {
  redirect("/dashboard");
}
