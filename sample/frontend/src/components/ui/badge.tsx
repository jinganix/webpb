import * as React from "react";

import { cn } from "@/lib/utils";

export const Badge = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div
    className={cn(
      "inline-flex items-center rounded-md border border-slate-200 px-2.5 py-0.5 text-xs font-semibold text-slate-700",
      className,
    )}
    ref={ref}
    {...props}
  />
));
Badge.displayName = "Badge";
