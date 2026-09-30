import { NextResponse, type NextRequest } from "next/server";
import { university } from "@/lib/content";

/** Old URL: /universities-detail.php?uid=42(&country=...) → /universities/42 */
export function GET(request: NextRequest) {
  const uid = Number(request.nextUrl.searchParams.get("uid"));
  const target = university(uid) ? `/universities/${uid}` : "/study-in/canada";
  return NextResponse.redirect(new URL(target, request.url), 308);
}
