import { ExampleCard } from "@/components/example-card";
import { resolveApiBaseUrl } from "@/lib/api-base";
import {
  FILE_OPTION_EXAMPLE,
  OPTION_EXAMPLES,
} from "@/options/option-definitions";
import { HttpService } from "@/services/http.service";

const httpService = new HttpService(resolveApiBaseUrl());

export function App(): React.JSX.Element {
  return (
    <div className="min-h-screen bg-slate-50">
      <div className="mx-auto flex max-w-5xl flex-col gap-8 px-4 py-10">
        <header className="space-y-2">
          <h1 className="text-3xl font-bold tracking-tight">Webpb sample</h1>
          <p className="text-slate-600">
            Interactive examples for webpb proto options. Expand an entry to
            edit fields, send requests, and view proto, request, and response
            side by side. Start the backend with{" "}
            <code className="text-sm">./gradlew sample:backend:bootRun</code> or{" "}
            <code className="text-sm">npm run start:all</code> before sending
            requests.
          </p>
        </header>

        <section className="space-y-4">
          <h2 className="text-xl font-semibold">Proto options</h2>
          <div className="grid gap-4">
            <ExampleCard
              example={FILE_OPTION_EXAMPLE}
              httpService={httpService}
            />
            {OPTION_EXAMPLES.map((example) => (
              <ExampleCard
                example={example}
                httpService={httpService}
                key={example.id}
              />
            ))}
          </div>
        </section>
      </div>
    </div>
  );
}
